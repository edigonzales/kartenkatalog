#!/usr/bin/env python3
"""Registry verification must fail on split aliases, missing tags or invalid versions."""
import contextlib
import importlib.util
import io
import json
from pathlib import Path
import unittest
from unittest.mock import patch
import urllib.error

spec = importlib.util.spec_from_file_location('published', Path(__file__).with_name('verify-published-images.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class Reply(io.BytesIO):
    def __init__(self, data, digest=None):
        super().__init__(json.dumps(data).encode())
        self.headers = {'Docker-Content-Digest': digest}


class RegistryContract(unittest.TestCase):
    def check_registry(self, bad_tag=None, missing=False, platforms=None):
        seen = []
        def fetch(request, timeout):
            if isinstance(request, str):
                return Reply({'token': 'public-pull-token'})
            tag = request.full_url.rsplit('/', 1)[-1]
            self.assertEqual('Bearer public-pull-token', request.get_header('Authorization'))
            seen.append(tag)
            if tag == bad_tag and missing:
                raise urllib.error.HTTPError(request.full_url, 404, 'Not Found', {}, None)
            digest = 'sha256:' + ('c' if tag == bad_tag else ('b' if tag.endswith('-jvm') else 'a')) * 64
            manifest = {'schemaVersion': 2, 'config': {}} if platforms is None else {
                'schemaVersion': 2, 'manifests': [{'platform': {'os': os, 'architecture': arch}} for os, arch in platforms]}
            return Reply(manifest, digest)
        with patch.object(module.urllib.request, 'urlopen', side_effect=fetch), contextlib.redirect_stdout(io.StringIO()):
            result = module.verify('0.2.73')
        return seen, result

    def test_all_eight_tags_are_checked_and_distinct_variants_match(self):
        seen, result = self.check_registry()
        self.assertEqual(['0.2.73', 'latest', '0', '0.2', '0.2.73-jvm', 'latest-jvm', '0-jvm', '0.2-jvm'], seen)
        self.assertEqual(8, len(result['tags']))

    def test_mixed_variant_alias_is_rejected(self):
        with self.assertRaisesRegex(AssertionError, 'Variant aliases differ'):
            self.check_registry('latest')

    def test_missing_tag_is_not_a_success(self):
        with self.assertRaises(urllib.error.HTTPError):
            self.check_registry('0.2-jvm', missing=True)

    def test_single_amd64_index_with_provenance_is_accepted(self):
        seen, _ = self.check_registry(platforms=[('linux', 'amd64'), ('unknown', 'unknown')])
        self.assertEqual(8, len(seen))

    def test_unrequested_platforms_are_rejected(self):
        for platforms in [[('linux', 'arm64')], [('linux', 'amd64'), ('linux', 'arm64')], []]:
            with self.assertRaises(AssertionError): self.check_registry(platforms=platforms)

    def test_non_release_version_never_requests_a_token(self):
        with patch.object(module.urllib.request, 'urlopen') as fetch:
            for version in ['0.2.LOCALBUILD', '0.2.0', '0.0.73', '0.2.73-jvm', '']:
                with self.assertRaises(ValueError): module.verify(version)
            fetch.assert_not_called()


if __name__ == '__main__':
    unittest.main(verbosity=2)
