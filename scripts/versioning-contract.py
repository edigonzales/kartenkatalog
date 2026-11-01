#!/usr/bin/env python3
"""Exercise the real Gradle version, including legacy environment precedence."""
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
base = {k: v for k, v in os.environ.items() if k not in ('GITHUB_RUN_NUMBER', 'BUILD_NUMBER', 'TRAVIS_BUILD_NUMBER')}
cases = [('local', {}, '0.2.LOCALBUILD'),
         ('github', {'GITHUB_RUN_NUMBER': '73'}, '0.2.73'),
         ('legacy-only', {'BUILD_NUMBER': '900', 'TRAVIS_BUILD_NUMBER': '901'}, '0.2.LOCALBUILD'),
         ('github-wins', {'GITHUB_RUN_NUMBER': '74', 'BUILD_NUMBER': '900', 'TRAVIS_BUILD_NUMBER': '901'}, '0.2.74')]
for name, extra, expected in cases:
    run = subprocess.run(['./gradlew', '-q', '--console=plain', 'printVersion'], cwd=ROOT,
                         env=dict(base, **extra), capture_output=True, text=True, timeout=180)
    assert run.returncode == 0 and run.stdout.strip() == expected, (name, run.returncode, run.stdout, run.stderr)
    print(name, expected, 'PASS', flush=True)
for value in ['', 'abc', '0', '-1', '73/suffix']:
    run = subprocess.run(['./gradlew', '-q', '--console=plain', 'printVersion'], cwd=ROOT,
                         env=dict(base, GITHUB_RUN_NUMBER=value), capture_output=True, text=True, timeout=180)
    assert run.returncode != 0 and 'GITHUB_RUN_NUMBER must be a positive integer' in run.stderr, (value, run.stdout, run.stderr)
    print('invalid run number', repr(value), 'rejected', flush=True)
