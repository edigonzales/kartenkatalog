package ch.so.agi.meta.config;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.lucene.analysis.tokenattributes.KeywordAttributeImpl;

/** Lucene 10.6 AttributeFactory resolves this GermanAnalyzer attribute by name.
 * Required by the observed native startup ClassNotFoundException; no FS index fallback.
 */
@RegisterForReflection(targets=KeywordAttributeImpl.class, methods=false, fields=false)
public final class LuceneNativeConfiguration {
    private LuceneNativeConfiguration() {}
}
