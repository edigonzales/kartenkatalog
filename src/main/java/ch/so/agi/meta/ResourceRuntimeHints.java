package ch.so.agi.meta;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.stereotype.Component;

import gg.jte.generated.precompiled.JteindexGenerated;
import gg.jte.generated.precompiled.JteproductGenerated;
import gg.jte.generated.precompiled.JteproductdetailGenerated;
import gg.jte.generated.precompiled.JtesearchresultsGenerated;
import gg.jte.generated.precompiled.JtethemelayersGenerated;

@Component
public class ResourceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources()
            .registerPattern("**/*.bin");

        hints.reflection()
            .registerType(JteindexGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JteproductGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JtesearchresultsGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JteproductdetailGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JtethemelayersGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);

    }

}
