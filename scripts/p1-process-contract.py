#!/usr/bin/env python3
"""Real Quarkus JVM/dev process, startup failures and snapshot lifetime (stdlib only)."""
import argparse
import json
import os
from pathlib import Path
import signal
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
REPORTS = ROOT / 'build/reports/p1'

def free_port():
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        return sock.getsockname()[1]

def response(url):
    with urllib.request.urlopen(url, timeout=2) as reply:
        return reply.status, json.load(reply)

def stop(process):
    try:
        os.killpg(process.pid, signal.SIGTERM)
    except ProcessLookupError:
        pass
    try:
        process.wait(timeout=15)
    except subprocess.TimeoutExpired:
        os.killpg(process.pid, signal.SIGKILL)
        process.wait(timeout=10)

def launch(location, name, dev=False, expect_failure=False):
    port = free_port()
    env = dict(os.environ, CATALOG_XTF_LOCATION=location)
    props = [f'-Dquarkus.http.port={port}', '-Dquarkus.http.host=127.0.0.1']
    if dev:
        props += ['-Dquarkus.http.cors.enabled=true',
                  '-Dquarkus.http.cors.origins=http://localhost:8081',
                  '-Dquarkus.http.cors.methods=GET,HEAD,OPTIONS',
                  '-Dquarkus.http.cors.headers=Accept,Content-Type',
                  '-Dquarkus.http.cors.access-control-allow-credentials=false']
    command = (['./gradlew', '--console=plain', *props, '-Dquarkus.console.enabled=false', '-Dquarkus.analytics.disabled=true', 'quarkusDev']
               if dev else ['java', *props, '-jar', 'build/quarkus-app/quarkus-run.jar'])
    logpath = REPORTS / f'{name}.log'
    with logpath.open('w') as log:
        process = subprocess.Popen(command, cwd=ROOT, env=env, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
    url = f'http://127.0.0.1:{port}'
    started = time.monotonic()
    try:
        deadline = time.monotonic() + (120 if dev else 30)
        while time.monotonic() < deadline:
            if process.poll() is not None:
                assert expect_failure and process.returncode != 0, (name, process.returncode, logpath.read_text()[-2000:])
                assert 'Cannot load catalog XTF' in logpath.read_text(), (name, logpath.read_text()[-2000:])
                print(f'{name}: rejected before ready; exit={process.returncode}')
                return {'name': name, 'exit': process.returncode, 'ready': False}
            try:
                status, health = response(url + '/q/health/ready')
                assert not expect_failure, f'{name}: invalid XTF became ready'
                assert status == 200 and health['status'] == 'UP'
                break
            except (urllib.error.URLError, TimeoutError):
                time.sleep(0.2)
        else:
            raise AssertionError(f'{name}: process did not terminate or become ready; {logpath}')
        elapsed = round(time.monotonic() - started, 3)
        assert not expect_failure
        with (REPORTS / f'{name}-http.log').open('w') as smoke_log:
            run = subprocess.run(['python3', 'scripts/p0-http-contract.py', url], cwd=ROOT, stdout=smoke_log, stderr=subprocess.STDOUT, timeout=60)
        assert run.returncode == 0, (name, (REPORTS / f'{name}-http.log').read_text())
        assert response(url + '/q/health/live')[1]['status'] == 'UP'
        if dev:
            verify_dev_cors(url)
        # A completed snapshot never reopens/parses the source per HTTP request.
        if not dev:
            Path(location.removeprefix('file:')).write_text('<invalid/>')
            assert len(response(url + '/api/v1/ebenen')[1]) == 886
            assert response(url + '/q/health/ready')[1]['status'] == 'UP'
        print(f'{name}: ready in {elapsed}s; HTTP contract and health succeeded')
        return {'name': name, 'ready': True, 'readySeconds': elapsed, 'httpExit': run.returncode}
    finally:
        stop(process)

def verify_dev_cors(base):
    """Documented QWC2 dev origin, real preflight/GET and rejected foreign origin."""
    for origin, method, expected in [('http://localhost:8081', 'OPTIONS', 200),
                                     ('http://localhost:8081', 'GET', 200),
                                     ('http://localhost:9999', 'OPTIONS', 403),
                                     ('http://localhost:9999', 'GET', 403)]:
        headers = {'Origin': origin}
        if method == 'OPTIONS':
            headers.update({'Access-Control-Request-Method': 'GET',
                            'Access-Control-Request-Headers': 'Content-Type'})
        request = urllib.request.Request(base + '/api/v1/themen', headers=headers, method=method)
        try:
            result = urllib.request.urlopen(request, timeout=5)
        except urllib.error.HTTPError as error:
            result = error
        with result:
            assert result.status == expected, (origin, method, result.status)
            allowed = result.headers.get('Access-Control-Allow-Origin')
            assert allowed == (origin if expected == 200 else None), (origin, method, allowed)
            assert result.headers.get('Access-Control-Allow-Credentials') != 'true'
        print('CORS', method, origin, expected, allowed)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dev', action='store_true')
    args = parser.parse_args()
    REPORTS.mkdir(parents=True, exist_ok=True)
    results = []
    with tempfile.TemporaryDirectory(prefix='kartenkatalog-p1-') as temp:
        directory = Path(temp)
        source = directory / 'catalog.xml'
        source.write_bytes((ROOT / 'data/kartenkatalog-harvest-ohne-qml.xtf').read_bytes())
        results.append(launch('file:' + str(source), 'dev' if args.dev else 'jvm', dev=args.dev))
        if not args.dev:
            small = (ROOT / 'src/test/resources/contract/with-qml.xml').read_text()
            layer = small[small.index('<k:Ebene'):small.index('</k:Ebene>') + len('</k:Ebene>')]
            cases = {
                'malformed': '<invalid/>',
                'wrong-model': small.replace('SO_AGI_Kartenkatalog_Publikation_20251210', 'ForeignModel'),
                'dangling-reference': small.replace('ili:ref="test-theme"', 'ili:ref="absent"'),
                'duplicate-layer': small.replace('</k:Kartenkatalog>', layer.replace('test-layer', 'other-tid').replace('85ee5e38-804c-4014-bb9b-490a2a3ea768', '95ee5e38-804c-4014-bb9b-490a2a3ea768') + '</k:Kartenkatalog>'),
                'xxe': small.replace('<?xml version="1.0" encoding="UTF-8"?>', '<!DOCTYPE transfer [<!ENTITY attack SYSTEM "file:///etc/passwd">]>').replace('QML Test', '&attack;'),
            }
            for name, xml in cases.items():
                path = directory / (name + '.xml'); path.write_text(xml)
                results.append(launch('file:' + str(path), name, expect_failure=True))
            for name, location in [('missing-file', 'file:' + str(directory / 'missing.xml')), ('missing-classpath', 'classpath:missing.xml'), ('http-source', 'https://example.invalid/catalog.xtf')]:
                results.append(launch(location, name, expect_failure=True))
    (REPORTS / ('dev-summary.json' if args.dev else 'process-summary.json')).write_text(json.dumps(results, indent=2) + '\n')
    print('All P1 process assertions succeeded.')

if __name__ == '__main__':
    main()
