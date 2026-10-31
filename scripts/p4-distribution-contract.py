#!/usr/bin/env python3
"""Real JVM/native parity, fresh heap index, read-only sources, startup failures.

No mocks and no automatic native fallback. Docker mode tests Linux containers with
an arbitrary OpenShift UID, read-only rootfs and data; local mode runs a real binary.
"""
import argparse
import contextlib
import hashlib
import json
import os
from pathlib import Path
import re
import signal
import socket
import statistics
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

ROOT = Path(__file__).resolve().parents[1]


def http(base, path, hx=False, timeout=5):
    request = urllib.request.Request(base + path, headers={'HX-Request': 'true'} if hx else {})
    try:
        response = urllib.request.urlopen(request, timeout=timeout)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        body = response.read().decode('utf-8')
        return response.status, response.headers.get('Content-Type', ''), body


def get_json(base, path):
    status, mime, body = http(base, path)
    assert status == 200 and 'json' in mime, (path, status, mime, body[:300])
    return json.loads(body)


class Runner:
    def __init__(self, kind, reports, executable=None, image=None):
        self.kind, self.reports, self.executable, self.image = kind, reports, executable, image

    @contextlib.contextmanager
    def start(self, source, cwd, label, failure=None):
        with socket.socket() as sock:
            sock.bind(('127.0.0.1', 0))
            port = sock.getsockname()[1]
        name = 'kartenkatalog-p4-' + uuid.uuid4().hex[:12]
        logpath = self.reports / (self.kind + '-' + label + '.log')
        props = ['-Dquarkus.http.host=127.0.0.1', f'-Dquarkus.http.port={port}']
        env = dict(os.environ, CATALOG_XTF_LOCATION='file:' + str(source))
        if self.image:
            command = ['docker', 'run', '--rm', '--name', name, '--read-only', '--cap-drop=ALL',
                       '--security-opt=no-new-privileges', '--user', '1000730000:0', '--memory=512m',
                       '--tmpfs', '/tmp:rw,noexec,nosuid,size=64m,mode=1777',
                       '-p', f'127.0.0.1:{port}:8080',
                       '--mount', f'type=bind,source={source.parent},target=/catalog,readonly',
                       '-e', 'CATALOG_XTF_LOCATION=file:/catalog/' + source.name,
                       '-e', 'QUARKUS_HTTP_HOST=0.0.0.0', '-e', 'QUARKUS_HTTP_PORT=8080',
                       '-e', 'JAVA_TOOL_OPTIONS=-Xmx256m -XX:-UsePerfData', self.image]
        elif self.executable:
            command = [str(self.executable), *props]
        else:
            command = ['java', *props, '-jar', str(ROOT / 'build/quarkus-app/quarkus-run.jar')]
        before = sorted(p.name for p in cwd.iterdir())
        with logpath.open('w') as log:
            process = subprocess.Popen(command, cwd=cwd, env=env, stdout=log,
                                       stderr=subprocess.STDOUT, start_new_session=True)
        base = f'http://127.0.0.1:{port}'
        started = time.monotonic()
        try:
            deadline = started + 45
            while time.monotonic() < deadline:
                if process.poll() is not None:
                    assert failure and process.returncode != 0, (command, process.returncode, logpath.read_text())
                    assert failure in logpath.read_text(), logpath.read_text()
                    result = {'failureExit': process.returncode, 'expectedError': failure}
                    print(self.kind, label, result, flush=True)
                    yield None, result
                    return
                try:
                    status, _, body = http(base, '/q/health/ready', timeout=1)
                    if status == 200:
                        assert not failure, 'invalid catalog became ready'
                        assert json.loads(body)['status'] == 'UP'
                        break
                except (urllib.error.URLError, TimeoutError, socket.timeout, ConnectionError):
                    pass
                time.sleep(.1)
            else:
                raise AssertionError(f'{self.kind}/{label}: not ready or terminated; {logpath}')
            text = logpath.read_text()
            assert 'Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer' in text, text
            assert 'Quarkus 3.40.1.1' in text, text
            expected_runtime = ' native (powered by' if self.kind == 'native' else ' on JVM (powered by'
            assert expected_runtime in text, 'Wrong runtime identity: expected ' + self.kind
            result = {'readySeconds': round(time.monotonic() - started, 3),
                      'indexMillis': int(re.search(r'index (\d+) ms', text).group(1))}
            if self.image:
                inspection = json.loads(subprocess.check_output(['docker', 'inspect', name], text=True))[0]
                assert inspection['HostConfig']['ReadonlyRootfs']
                assert inspection['Config']['User'] == '1000730000:0'
                assert inspection['Mounts'] and all(not mount['RW'] for mount in inspection['Mounts'] if mount['Type'] != 'tmpfs')
                assert set(inspection['HostConfig']['Tmpfs']) == {'/tmp'}
                assert 'size=64m' in inspection['HostConfig']['Tmpfs']['/tmp']
                result['containerInspection'] = {key: inspection[key] for key in ['Platform', 'Mounts']}
                result['image'] = subprocess.check_output(['docker', 'image', 'inspect', self.image,
                    '--format', '{{.Os}}/{{.Architecture}} {{.Id}}'], text=True).strip()
                result['rssKiB'] = int(subprocess.check_output(['docker', 'top', name, '-eo', 'pid,rss'], text=True).splitlines()[1].split()[1])
            else:
                result['rssKiB'] = int(subprocess.check_output(['ps', '-o', 'rss=', '-p', str(process.pid)], text=True).strip())
            yield base, result
            assert sorted(p.name for p in cwd.iterdir()) == before, 'runtime persisted files in read-only cwd'
            if self.image:
                scratch = self.reports / (self.kind + '-' + label + '-tmp')
                scratch.mkdir(exist_ok=True)
                subprocess.run(['docker', 'cp', name + ':/tmp/.', str(scratch)], check=True, stdout=subprocess.DEVNULL)
                files = sorted(str(p.relative_to(scratch)) for p in scratch.rglob('*') if p.is_file())
                assert not any(re.match(r'(segments_|pending_segments_|write\.lock|_[a-z0-9]+.*\.(si|cfs|cfe|tim|tip|tmd|fnm|fdx|fdt|dvd|dvm)$)', Path(f).name) for f in files), files
                (self.reports / (self.kind + '-' + label + '-tmp-files.json')).write_text(json.dumps(files, indent=2))
                diff = subprocess.check_output(['docker', 'diff', name], text=True).strip()
                assert not diff, ('container persisted files', diff)
        finally:
            if self.image:
                subprocess.run(['docker', 'stop', '--time', '10', name], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=20)
                subprocess.run(['docker', 'rm', '-f', name], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=20)
            if self.image:
                # docker stop terminates the container; the client exits asynchronously.
                # Wait for that client rather than signalling its possibly vanished process group.
                try:
                    process.wait(timeout=15)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait(timeout=10)
            elif process.poll() is None:
                try:
                    os.killpg(process.pid, signal.SIGTERM)
                except ProcessLookupError:
                    pass
                try:
                    process.wait(timeout=15)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL)
                    process.wait(timeout=10)


def verify(base, label):
    layers = get_json(base, '/api/v1/ebenen')
    themes = get_json(base, '/api/v1/themen')
    catalog = get_json(base, '/api/v1/catalog')
    expected = 886 if label == 'harvest' else (1 if label == 'qml' else 12)
    assert len(layers) == expected and len({e['id'] for e in layers}) == expected
    first = layers[0]
    theme = first['themeIdentifier']
    detail = get_json(base, '/api/v1/ebenen/' + urllib.parse.quote(first['id'], safe=''))
    assert detail['qwcLayerName'] == first['id']
    assert isinstance(detail['keywords'], list) and isinstance(detail['synonyms'], list)
    themed = get_json(base, '/api/v1/themen/' + urllib.parse.quote(theme, safe='') + '/ebenen')
    assert first['id'] in [e['id'] for e in themed]
    root = http(base, '/')
    assert root[0] == 200 and '<!doctype html>' in root[2] and 'catalog-state' in root[2]
    fragment = http(base, '/', hx=True)
    assert fragment[0] == 200 and '<html' not in fragment[2] and 'catalog-state' in fragment[2]
    full_theme = http(base, '/themen/' + urllib.parse.quote(theme, safe='') + '/ebenen')
    theme_fragment = http(base, '/themen/' + urllib.parse.quote(theme, safe='') + '/ebenen', hx=True)
    assert full_theme[0] == theme_fragment[0] == 200 and '<html' not in theme_fragment[2]
    assert http(base, '/ebene/' + urllib.parse.quote(first['id'], safe=''))[0] == 200
    assert http(base, '/catalog.js')[0] == 200
    assert get_json(base, '/q/health/live')['status'] == 'UP'
    for path, code in [('/api/v1/ebenen/unknown', 404), ('/themen/unknown/ebenen', 404),
                       ('/api/v1/ebenen?typ=bogus', 400), ('/api/v1/ebenen?q=' + 'x' * 201, 400)]:
        status, mime, body = http(base, path)
        assert status == code and 'application/problem+json' in mime and json.loads(body)['status'] == code
    queries = ['q=wasser&sort=relevance'] if label == 'harvest' else (['q=QML'] if label == 'qml' else [
        'q=atlas&sort=relevance', 'q=WASSERKARTE%20B%C3%A4che', 'q=AV', 'q=Strasse%20Bodenprobe',
        'q=atlas%20impossible', 'q=Muster', 'thema=hydro&thema=plan&typ=extwms&typ=layergroup&organisation=Amt%20A&organisation=Amt%20B',
        'q=atlas&thema=hydro&typ=datasetview', 'sort=title-desc'])
    payloads = {q: get_json(base, '/api/v1/ebenen?' + q) for q in queries}
    if label in ('search', 'restart'):
        assert [e['id'] for e in payloads[queries[0]]][:3] == ['atlas', 'ch.test.exact', 'ch.test.prefix']
        assert [e['id'] for e in payloads[queries[1]]] == ['ch.test.cross']
        assert {e['id'] for e in payloads[queries[2]]} == {'ch.test.av', 'ch.test.cross'}
        assert [e['id'] for e in payloads[queries[3]]] == ['ch.test.cross']
        assert payloads[queries[4]] == []
        assert {e['id'] for e in payloads[queries[6]]} == {'ch.test.prefix', 'ch.test.keyword', 'ch.test.av'}
    if label == 'qml':
        status, mime, xml = http(base, '/ebene/ch.test.qml/style')
        assert status == 200 and 'application/xml' in mime and '<qgis' in xml
        assert http(base, '/ebene/ch.test.qml')[2].find('onerror=') == -1
    else:
        assert http(base, '/ebene/' + urllib.parse.quote(first['id'], safe='') + '/style')[0] == 404
    facets = {q: get_json(base, '/api/v1/facets?' + q) for q in queries}
    timings = []
    for _ in range(10):
        started = time.monotonic()
        get_json(base, '/api/v1/ebenen?' + queries[0])
        timings.append((time.monotonic() - started) * 1000)
    return {'layers': layers, 'themes': themes, 'catalog': catalog, 'detail': detail, 'themed': themed,
            'queries': payloads, 'facets': facets}, round(statistics.median(timings), 3)


def main():
    parser = argparse.ArgumentParser()
    native = parser.add_mutually_exclusive_group(required=True)
    native.add_argument('--native-executable', type=Path)
    native.add_argument('--native-image')
    parser.add_argument('--jvm-image')
    parser.add_argument('--report-dir', type=Path, default=ROOT / 'build/reports/p4/distribution')
    args = parser.parse_args()
    if args.native_executable:
        args.native_executable = args.native_executable.resolve(strict=True)
        assert args.native_executable.is_file() and os.access(args.native_executable, os.X_OK)
    for image in [args.native_image, args.jvm_image]:
        if image is not None and not image.strip():
            parser.error('Container image must not be blank; no JVM fallback permitted')
    reports = args.report_dir.resolve()
    reports.mkdir(parents=True, exist_ok=True)
    runners = [Runner('jvm', reports, image=args.jvm_image),
               Runner('native', reports, executable=args.native_executable, image=args.native_image)]
    summary, parity = {}, {}
    with tempfile.TemporaryDirectory(prefix='catalog-p4-') as temp:
        directory = Path(temp)
        directory.chmod(0o755)
        fixtures = {name: (ROOT / path).read_text() for name, path in [
            ('harvest', 'data/kartenkatalog-harvest-ohne-qml.xtf'), ('search', 'src/test/resources/search/catalog.xml'),
            ('qml', 'src/test/resources/contract/with-qml.xml')]}
        for runner in runners:
            summary[runner.kind] = {}
            for label in ['harvest', 'search', 'restart', 'qml']:
                source = directory / (label + '.xml')
                source.write_text(fixtures['search' if label == 'restart' else label])
                source.chmod(0o444)
                directory.chmod(0o555)
                try:
                    with runner.start(source, directory, label) as (base, metrics):
                        payload, median = verify(base, label)
                        if label == 'restart':
                            assert payload == parity[(runner.kind, 'search')], 'restart changed the index results'
                        if runner.kind == 'native':
                            if payload != parity[('jvm', label)]:
                                for runtime, data in [('jvm', parity[('jvm', label)]), ('native', payload)]:
                                    (reports / (runtime + '-' + label + '-mismatch.json')).write_text(json.dumps(data, ensure_ascii=False, indent=2))
                            assert payload == parity[('jvm', label)], f'JVM/native payload/ranking/facets mismatch: {label}'
                        parity[(runner.kind, label)] = payload
                        metrics.update(medianSearchMs=median, documents=len(payload['layers']),
                                       payloadSha256=hashlib.sha256(json.dumps(payload, sort_keys=True).encode()).hexdigest())
                        summary[runner.kind][label] = metrics
                        print(runner.kind, label, json.dumps(metrics), flush=True)
                        if label == 'harvest':
                            with (reports / (runner.kind + '-p0-http.log')).open('w') as log:
                                smoke = subprocess.run([sys.executable, str(ROOT / 'scripts/p0-http-contract.py'), base], stdout=log, stderr=subprocess.STDOUT, timeout=60)
                            assert smoke.returncode == 0, 'P0 HTTP contract failed'
                finally:
                    directory.chmod(0o755)
                    source.chmod(0o644)
            # Same path, different XTF on next process: the index cannot be a stale file/snapshot.
            source = directory / 'search.xml'
            source.write_text(fixtures['search'].replace('<k:title>Atlas</k:title>', '<k:title>Neuer Titel</k:title>'))
            source.chmod(0o444)
            with runner.start(source, directory, 'changed') as (base, metrics):
                ids = [e['id'] for e in get_json(base, '/api/v1/ebenen?q=atlas&sort=relevance')]
                assert 'ch.test.exact' not in ids and ids[0] == 'atlas'
                summary[runner.kind]['changed'] = metrics
            source.chmod(0o644)
            cases = {'malformed': ('<invalid/>', 'Cannot load catalog XTF'),
                     'xxe': (fixtures['qml'].replace('<?xml version="1.0" encoding="UTF-8"?>', '<!DOCTYPE transfer [<!ENTITY attack SYSTEM "file:///etc/passwd">]>'), 'Cannot load catalog XTF'),
                     'reference': (fixtures['search'].replace('ili:ref="hydro"', 'ili:ref="missing"'), 'Cannot load catalog XTF'),
                     'index': (fixtures['search'].replace('<k:title>Atlas</k:title>', '<k:title>' + 'x' * 40000 + '</k:title>'), 'Cannot build catalog RAM index')}
            for label, (content, error) in cases.items():
                source.write_text(content)
                source.chmod(0o444)
                with runner.start(source, directory, label, failure=error) as (_, metrics):
                    summary[runner.kind][label] = metrics
                source.chmod(0o644)
    (reports / 'summary.json').write_text(json.dumps(summary, indent=2) + '\n')
    print('PASS: JVM/native payloads, ranking, facets, HTTP/HTMX/QML, fresh RAM index and startup failures; no persisted index.', flush=True)


if __name__ == '__main__':
    main()
