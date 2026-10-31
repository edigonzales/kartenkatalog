# Spring/XTF baseline (P0)

`baseline.json` was captured from real HTTP responses on base commit
`eabbb24ab73aad21ed108c13ed8893e6cb8848a5`, using the versioned incomplete
169-theme / 886-layer harvest. The SHA-256 pins this fixture only, not production.
One complete Layer and Theme payload protects field names, scalar types, explicit
nulls, empty arrays, plain/HTML descriptions and source ID vs published QWC ID.
Three additional focused products cover every other harvested product type.
Error cases protect status, title, detail, instance path and problem MIME type;
dynamic headers and request ports are deliberately excluded.

`BaselineContractTests` now sends real HTTP to a random-port Quarkus server (ported in P1). It
also checks catalog membership/counts, de-CH ordering, all five API forms, search
boundaries, errors and canonical HTML links. `BaselineQmlTests` loads the separate
synthetic `with-qml.xml` through the real XTF parser and Quarkus REST/Qute stack; it does
not add metadata to the harvest. It checks exact XML bytes, MIME, attachment
filename and sanitized descriptions.

Reproduce:

```sh
./gradlew test
./gradlew quarkusDev -Dquarkus.http.host=127.0.0.1 -Dquarkus.http.port=8083
# In a second terminal, Python 3 standard library; assertions must remain enabled:
python3 scripts/p0-http-contract.py http://127.0.0.1:8083
```

The standalone smoke checks the same fixture against a separately started process
without Spring dependencies, so it remains usable after the migration. Keep the
JSON contract stable; the JUnit infrastructure was ported in P1 without changing the JSON fixture. The observed root
redirect may become a full page as explicitly allowed by the plan; fragment-only
theme HTML, broken relative detail links and missing no-JS navigation were legacy
defects corrected by the minimal P1 Qute pages (see implementation/P0.md).

Mutation check actually executed in P0: renamed the public DTO record component
`qwcLayerName` to `publishedLayerName`; the focused HTTP contract test failed
(Gradle exit 1, one assertion failure). The source was restored before the final
complete test run. See `build/reports/p0/canary.*`; repeat this probe only outside
an immutable review checkout, e.g. in a disposable copy.
