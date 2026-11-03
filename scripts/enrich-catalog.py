#!/usr/bin/env python3
"""Apply the reviewed organisation manifest; preserve every unrelated XTF byte.

The default is a read-only verification. Use --write to regenerate metadata.
The historical source is optional: --legacy verifies its hash and exact matches.
"""
import argparse
from collections import Counter, defaultdict
import hashlib
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parents[1]
XTF = ROOT / 'data/kartenkatalog-harvest-ohne-qml.xtf'
MANIFEST = ROOT / 'data/metadata/organizations.json'
REPORT = ROOT / 'data/metadata/enrichment-report.json'
NS = 'http://www.interlis.ch/xtf/2.4/SO_AGI_Kartenkatalog_Publikation_20251210'
ILI = 'http://www.interlis.ch/xtf/2.4/INTERLIS'
MAILTO = re.compile(r"mailto:[A-Za-z0-9.!#$%&'*+/=^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+\Z")


def enrich(text, manifest):
    root = ET.fromstring(text)
    layers = list(root.iter(f'{{{NS}}}Ebene'))
    themes = list(root.iter(f'{{{NS}}}Thema'))
    reviewed = manifest['layers']
    assert len(themes) == 169 and len(layers) == 886
    identifiers = [e.findtext(f'{{{NS}}}ident_part') for e in layers]
    assert len(set(identifiers)) == len(identifiers) and set(identifiers) == set(reviewed), 'Manifest does not cover exactly the transfer'
    owners = defaultdict(set)
    for layer in layers:
        item = reviewed[layer.findtext(f'{{{NS}}}ident_part')]
        assert item['organization'] and item['source'].startswith('https://')
        owners[layer.find(f'{{{NS}}}Thema_R').attrib[f'{{{ILI}}}ref']].add(item['organization'])
    theme_metadata = {}
    mixed = []
    for theme in themes:
        tid = theme.attrib[f'{{{ILI}}}tid']
        identifier = theme.findtext(f'{{{NS}}}identifier')
        orgs = sorted(owners[tid])
        override = manifest.get('themeOwners', {}).get(identifier)
        owner = override['organization'] if override else orgs[0] if len(orgs) == 1 else None
        contact = None
        if owner:
            contact = manifest['themeContacts'].get(identifier, manifest['organizations'][owner]['contact'])
            assert contact and MAILTO.fullmatch(contact), f'Missing/invalid verified contact for {identifier}: {owner}'
        else:
            mixed.append({'identifier': identifier, 'title': theme.findtext(f'{{{NS}}}title'), 'organizations': orgs})
        theme_metadata[identifier] = (owner, contact)

    def replace_object(match):
        kind, attributes, body = match.groups()
        element = ET.fromstring(f'<k:{kind} xmlns:k="{NS}" xmlns:ili="{ILI}">{body}</k:{kind}>')
        if kind == 'Thema':
            owner, contact = theme_metadata[element.findtext(f'{{{NS}}}identifier')]
            additions = [('data_owner', owner), ('contact', contact)]
        else:
            identifier = element.findtext(f'{{{NS}}}ident_part')
            additions = [('org_name', reviewed[identifier]['organization'])]
        for field, value in additions:
            body = re.sub(r'\n        <k:' + field + r'>[^<]*</k:' + field + r'>', '', body)
            if value:
                body = body.rstrip() + f'\n        <k:{field}>{escape(value)}</k:{field}>\n      '
        return f'<k:{kind}{attributes}>{body}</k:{kind}>'

    pattern = re.compile(r'<k:(Thema|Ebene)([^>]*)>(.*?)</k:\1>', re.S)
    updated = pattern.sub(replace_object, text)
    report = {
        'themes': len(themes), 'layers': len(layers),
        'layerMethods': dict(sorted(Counter(v['method'] for v in reviewed.values()).items())),
        'organizations': len(set(v['organization'] for v in reviewed.values())),
        'themesWithOwnerAndContact': sum(owner is not None for owner, _ in theme_metadata.values()),
        'mixedThemes': mixed, 'sha256': hashlib.sha256(updated.encode()).hexdigest(),
        'legacySha256': manifest['legacySha256']
    }
    return updated, report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write', action='store_true')
    parser.add_argument('--legacy', type=Path)
    args = parser.parse_args()
    manifest = json.loads(MANIFEST.read_text())
    if args.legacy:
        assert hashlib.sha256(args.legacy.read_bytes()).hexdigest() == manifest['legacySha256']
        exact = defaultdict(set)
        for element in ET.parse(args.legacy).getroot().iter():
            fields = {e.tag.split('}')[-1]:(e.text or '').strip() for e in element}
            for prefix in ('p_', 'c_'):
                identifier, owner = fields.get(prefix+'ident_part'), fields.get(prefix+'org_name')
                if identifier and owner and owner != 'org.from.migration':
                    exact[identifier].add(manifest['aliases'].get(owner, owner))
        for identifier, item in manifest['layers'].items():
            if item['method'] == 'legacy-exact':
                assert exact[identifier] == {item['organization']}, identifier
    original = XTF.read_bytes().decode('utf-8')
    updated, report = enrich(original, manifest)
    rendered = json.dumps(report, ensure_ascii=False, indent=2) + '\n'
    if args.write:
        XTF.write_bytes(updated.encode('utf-8'))
        REPORT.write_text(rendered)
    else:
        assert updated == original, 'XTF metadata is stale; run with --write'
        assert REPORT.read_text() == rendered, 'Enrichment report is stale'
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
