#!/usr/bin/env python3
"""Fresh heap index per JVM restart, read-only source/cwd, search/filter/failed-index checks."""
import json
import os
import re
from pathlib import Path
import signal
import socket
import statistics
import subprocess
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
REPORTS = ROOT / 'build/reports/p2'

def request(base, path):
    with urllib.request.urlopen(base + path, timeout=3) as reply:
        return json.load(reply)

def run(source, cwd, label, failure=False, harvest=False):
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0)); port = sock.getsockname()[1]
    command=['java', '-Dquarkus.http.host=127.0.0.1', f'-Dquarkus.http.port={port}', '-jar', str(ROOT/'build/quarkus-app/quarkus-run.jar')]
    logpath=REPORTS/(label+'.log')
    with logpath.open('w') as log:
        process=subprocess.Popen(command,cwd=cwd,env=dict(os.environ,CATALOG_XTF_LOCATION='file:'+str(source)),stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
    base=f'http://127.0.0.1:{port}'
    started=time.monotonic()
    before=sorted(p.name for p in cwd.iterdir())
    try:
        deadline=time.monotonic()+30
        while time.monotonic()<deadline:
            if process.poll() is not None:
                assert failure and process.returncode!=0, logpath.read_text()
                assert 'Cannot build catalog RAM index' in logpath.read_text()
                print(label, 'expected index-start failure:', process.returncode)
                return {'indexFailureExit':process.returncode}
            try:
                health=request(base,'/q/health/ready')
                assert not failure, 'invalid index became ready'
                assert health['status']=='UP'
                break
            except (urllib.error.URLError,TimeoutError):time.sleep(.1)
        else:raise AssertionError('startup neither ready nor failed')
        ready=round(time.monotonic()-started,3)
        assert 'Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer' in logpath.read_text()
        layers=request(base,'/api/v1/ebenen')
        expected=886 if harvest else 12
        assert len(layers)==expected and len({e['id'] for e in layers})==expected
        search_path='/api/v1/ebenen?q=wasser' if harvest else '/api/v1/ebenen?q=atlas&sort=relevance'
        hits=request(base,search_path)
        assert hits
        if harvest:
            assert request(base,'/api/v1/ebenen/ch.so.agi.gemeindegrenzen')['qwcLayerName']=='ch.so.agi.gemeindegrenzen'
            assert request(base,'/api/v1/facets')['organisation']==[]
        else:
            assert hits[0]['id']=='atlas'
            assert [e['id'] for e in request(base,'/api/v1/ebenen?q=WASSERKARTE%20B%C3%A4che')]==['ch.test.cross']
            assert [e['id'] for e in request(base,'/api/v1/ebenen?q=AV')]==['ch.test.av','ch.test.cross']
            filtered=request(base,'/api/v1/ebenen?thema=hydro&thema=plan&typ=extwms&typ=layergroup&organisation=Amt%20A&organisation=Amt%20B')
            assert {e['id'] for e in filtered}=={'ch.test.prefix','ch.test.keyword','ch.test.av'}
            facets=request(base,'/api/v1/facets?q=atlas&thema=hydro&typ=datasetview')
            assert next(f['count'] for f in facets['thema'] if f['value']=='plan')==3
        latencies=[]
        for _ in range(10):
            t=time.monotonic();request(base,search_path);latencies.append((time.monotonic()-t)*1000)
        rss=int(subprocess.check_output(['ps','-o','rss=','-p',str(process.pid)],text=True).strip())
        assert sorted(p.name for p in cwd.iterdir())==before, 'process wrote an index or other files into the read-only cwd'
        result={'ids':[e['id'] for e in layers],'ranked':[e['id'] for e in hits], 'readySeconds':ready, 'rssKiB':rss, 'medianSearchMs':round(statistics.median(latencies),3), 'indexMillis':int(re.search(r'index (\d+) ms', logpath.read_text()).group(1))}
        print(label,json.dumps({k:v for k,v in result.items() if k not in ('ids','ranked')}), 'documents=',len(layers))
        return result
    finally:
        if process.poll() is None:
            os.killpg(process.pid,signal.SIGTERM)
            try:process.wait(timeout=15)
            except subprocess.TimeoutExpired:os.killpg(process.pid,signal.SIGKILL);process.wait(timeout=10)

def main():
    REPORTS.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='catalog-p2-') as tmp:
        directory=Path(tmp);source=directory/'catalog.xml'
        original=(ROOT/'src/test/resources/search/catalog.xml').read_text()
        source.write_text(original);source.chmod(0o444)
        harvest_source=directory/'harvest.xml';harvest_source.write_bytes((ROOT/'data/kartenkatalog-harvest-ohne-qml.xtf').read_bytes());harvest_source.chmod(0o444)
        directory.chmod(0o555)
        try:
            harvest=run(harvest_source,directory,'harvest',harvest=True)
            first=run(source,directory,'first')
            second=run(source,directory,'second')
            assert first['ids']==second['ids'] and first['ranked']==second['ranked']
            source.chmod(0o644);source.write_text(original.replace('<k:title>Atlas</k:title>','<k:title>Neuer Titel</k:title>'));source.chmod(0o444)
            changed=run(source,directory,'changed')
            assert 'ch.test.exact' not in changed['ranked'], 'restart reused a stale index'
            source.chmod(0o644);source.write_text(original.replace('<k:title>Atlas</k:title>','<k:title>'+'x'*40000+'</k:title>'));source.chmod(0o444)
            broken=run(source,directory,'broken-index',failure=True)
            (REPORTS/'summary.json').write_text(json.dumps({'harvest':harvest,'first':first,'second':second,'changed':changed,'broken':broken},indent=2)+'\n')
        finally:
            directory.chmod(0o755);source.chmod(0o644);harvest_source.chmod(0o644)
    print('All P2 process assertions succeeded; no persisted index files.')

if __name__=='__main__':main()
