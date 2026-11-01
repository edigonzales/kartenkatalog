#!/usr/bin/env python3
"""Read all eight public Docker Hub manifests and require matching variant digests."""
import argparse
import json
from pathlib import Path
import re
import urllib.parse
import urllib.request

REPOSITORY = 'sogis/kartenkatalog'
ACCEPT = ', '.join(['application/vnd.oci.image.manifest.v1+json',
                    'application/vnd.docker.distribution.manifest.v2+json',
                    'application/vnd.oci.image.index.v1+json',
                    'application/vnd.docker.distribution.manifest.list.v2+json'])


def verify(version):
    if not re.fullmatch(r'0\.2\.[1-9][0-9]*', version):
        raise ValueError('Expected release version 0.2.<positive GitHub run number>')
    query = urllib.parse.urlencode({'service': 'registry.docker.io', 'scope': 'repository:' + REPOSITORY + ':pull'})
    with urllib.request.urlopen('https://auth.docker.io/token?' + query, timeout=30) as response:
        token = json.load(response)['token']
    digests = {}
    for suffix in ['', '-jvm']:
        tags = [version + suffix, 'latest' + suffix, '0' + suffix, '0.2' + suffix]
        for tag in tags:
            request = urllib.request.Request('https://registry-1.docker.io/v2/' + REPOSITORY + '/manifests/' + tag,
                                             headers={'Authorization': 'Bearer ' + token, 'Accept': ACCEPT})
            with urllib.request.urlopen(request, timeout=30) as response:
                digest = response.headers['Docker-Content-Digest']
                manifest = json.load(response)
            assert digest and re.fullmatch(r'sha256:[0-9a-f]{64}', digest), (tag, digest)
            assert manifest['schemaVersion'] == 2, (tag, manifest)
            if 'manifests' in manifest:
                # BuildKit may wrap one runtime manifest plus provenance in an OCI index.
                platforms = [(m.get('platform', {}).get('os'), m.get('platform', {}).get('architecture'))
                             for m in manifest['manifests']
                             if m.get('platform', {}).get('os') != 'unknown']
                assert platforms == [('linux', 'amd64')], (tag, platforms)
            else:
                assert 'config' in manifest, (tag, manifest)
            digests[tag] = digest
            print(REPOSITORY + ':' + tag, digest, flush=True)
        assert len({digests[tag] for tag in tags}) == 1, ('Variant aliases differ', tags, digests)
    assert digests[version] != digests[version + '-jvm'], 'Native and JVM tags must identify distinct images'
    return {'repository': REPOSITORY, 'version': version, 'tags': digests}


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--version', required=True)
    parser.add_argument('--report', type=Path, default=Path('build/reports/release/registry-tags.json'))
    args = parser.parse_args()
    result = verify(args.version)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(result, indent=2) + '\n')
    print('PASS: all eight registry tags match their variant digest')
