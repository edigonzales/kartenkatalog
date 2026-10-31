#!/usr/bin/env python3
"""Framework independent smoke against a running harvested-XTF process (stdlib only)."""
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

BASE = sys.argv[1] if len(sys.argv) > 1 else 'http://127.0.0.1:8083'
CONTRACT = json.loads((Path(__file__).resolve().parents[1] / 'src/test/resources/contract/baseline.json').read_text())

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

OPENER = urllib.request.build_opener(NoRedirect)

def request(path, status=200, mime='application/json'):
    try:
        response = OPENER.open(BASE + path, timeout=20)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        body = response.read().decode('utf-8')
        assert response.code == status, (path, response.code, status, body[:250])
        if mime:
            assert response.headers.get_content_type() == mime, (path, response.headers)
        print(f'{response.code} {path} [{response.headers.get_content_type()}]')
        return body, response.headers

def api(path):
    return json.loads(request(path)[0])

def main():
    layer = api('/api/v1/ebenen/' + CONTRACT['layer']['id'])
    assert layer == CONTRACT['layer'], layer
    themes, layers, catalog = [api(p) for p in ['/api/v1/themen', '/api/v1/ebenen', '/api/v1/catalog']]
    assert len(themes) == 169 and len(layers) == 886 and len(catalog['themes']) == 169
    assert next(t for t in themes if t['identifier'] == CONTRACT['theme']['identifier']) == CONTRACT['theme']
    by_id = {e['id']: e for e in layers}
    assert len(by_id) == 886
    for e in layers:
        assert set(e) == set(CONTRACT['layerFields'])
        assert e['qwcLayerName'] == e['id'] and isinstance(e['keywords'], list) and isinstance(e['synonyms'], list)
    flattened = []
    for entry, theme in zip(catalog['themes'], themes):
        assert set(entry) == {'theme', 'layers'} and entry['theme'] == theme
        assert set(theme) == set(CONTRACT['themeFields'])
        assert theme['layerCount'] == len(entry['layers'])
        assert all(e['themeIdentifier'] == theme['identifier'] for e in entry['layers'])
        flattened.extend(entry['layers'])
    assert len(flattened) == 886 and {e['id']: e for e in flattened} == by_id
    for expected in CONTRACT['products']:
        actual = api('/api/v1/ebenen/' + expected['id'])
        assert all(actual[k] == v for k, v in expected.items())
    assert api('/api/v1/themen/harvest-hoheitsgrenzen/ebenen') == next(e['layers'] for e in catalog['themes'] if e['theme']['identifier'] == 'harvest-hoheitsgrenzen')
    assert layer in api('/api/v1/ebenen?q=%20GEMEINDEGRENZEN%20&thema=harvest-hoheitsgrenzen')
    assert api('/api/v1/ebenen?q=wasser')
    assert api('/api/v1/ebenen?q=not-a-real-layer-548139') == []
    assert api('/api/v1/ebenen?q=' + 'x' * 200) == []
    for expected in CONTRACT['errors']:
        actual = json.loads(request(expected['path'], expected['status'], 'application/problem+json')[0])
        assert all(actual[k] == expected[k] for k in ['status', 'title', 'detail']), actual
        assert actual['instance'] == expected['path'].split('?')[0]
        assert 'trace' not in actual and 'exception' not in actual
    body, headers = request('/', 302, None)
    assert urllib.parse.urlparse(headers['Location']).path == '/thema'
    for path, title in [('/thema', 'Hoheitsgrenzen'), ('/themen/harvest-hoheitsgrenzen/ebenen', 'Gemeindegrenzen'), ('/ebene/ch.so.agi.gemeindegrenzen', 'Gemeindegrenzen')]:
        assert title in request(path, mime='text/html')[0]
    for path in ['/ebene/unknown', '/themen/unknown/ebenen', '/ebene/unknown/style']:
        request(path, 404, 'application/problem+json')
    assert request('/ping', mime='text/plain')[0] == 'kartenkatalog'
    print('All P0 process contract assertions succeeded.')

if __name__ == '__main__':
    main()
