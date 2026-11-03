#!/usr/bin/env python3
"""Independent invariants for the reviewed XTF enrichment."""
import copy
import hashlib
import importlib.util
import json
import re
import unittest

spec = importlib.util.spec_from_file_location('enrich', __file__.replace('-tests.py', '.py'))
enrich = importlib.util.module_from_spec(spec)
spec.loader.exec_module(enrich)


class EnrichmentTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.text = enrich.XTF.read_bytes().decode('utf-8')
        cls.manifest = json.loads(enrich.MANIFEST.read_text())

    def test_original_transfer_bytes_are_preserved_except_the_three_metadata_fields(self):
        original = re.sub(r'\n        <k:(?:data_owner|org_name|contact)>[^<]*</k:(?:data_owner|org_name|contact)>', '', self.text)
        self.assertEqual('de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86',
                         hashlib.sha256(original.encode()).hexdigest())
        updated, report = enrich.enrich(original, self.manifest)
        self.assertEqual(self.text, updated)
        self.assertEqual(report, json.loads(enrich.REPORT.read_text()))

    def test_repeated_enrichment_is_identical_and_every_layer_is_covered(self):
        updated, report = enrich.enrich(self.text, self.manifest)
        self.assertEqual(self.text, updated)
        self.assertEqual({'legacy-exact': 465, 'researched': 421}, report['layerMethods'])
        self.assertEqual(166, report['themesWithOwnerAndContact'])
        self.assertEqual(3, len(report['mixedThemes']))

    def test_missing_layer_and_invalid_contact_stop_generation(self):
        broken = copy.deepcopy(self.manifest)
        del broken['layers']['ch.so.agi.gemeindegrenzen']
        with self.assertRaisesRegex(AssertionError, 'Manifest'):
            enrich.enrich(self.text, broken)
        broken = copy.deepcopy(self.manifest)
        broken['organizations']['Amt für Geoinformation']['contact'] = 'mailto:a@so.ch?cc=b@so.ch'
        with self.assertRaisesRegex(AssertionError, 'contact'):
            enrich.enrich(self.text, broken)


if __name__ == '__main__':
    unittest.main()
