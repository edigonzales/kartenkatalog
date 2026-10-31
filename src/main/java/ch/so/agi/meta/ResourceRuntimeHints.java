package ch.so.agi.meta;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import gg.jte.generated.precompiled.JtethemaGenerated;
import gg.jte.generated.precompiled.JteproductdetailGenerated;
import gg.jte.generated.precompiled.JtethemelayersGenerated;

public class ResourceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources()
            .registerPattern("**/*.bin");

        hints.reflection()
            .registerType(JtethemaGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JteproductdetailGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(JtethemelayersGenerated.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS);

    }

}
