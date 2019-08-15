// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// CODE_ANALYSIS_STATE ignore
// FILE: A.java

import codeanalysis.experimental.annotations.*;

public class A {
    @Nullable public String field = null;

    @Nullable
    public String foo(@NotNull String x, @NullnessUnknown CharSequence y) {
        return "";
    }

    @NotNull
    public String bar() {
        return "";
    }

    @DefaultNotNull
    public String everythingNotNullable(String x) { return ""; }

    @DefaultNullable
    public String everythingNullable(String x) { return ""; }

    @DefaultNullnessUnknown
    public String everythingUnknown(String x) { return ""; }
}

// FILE: main.kt

fun main(a: A) {
    a.foo("", null)?.length
    a.foo("", null).length
    a.foo(null, "").length

    a.bar().length
    a.bar()!!.length

    a.field?.length
    a.field.length

    a.everythingNotNullable(null)?.length
    a.everythingNotNullable(null).length
    a.everythingNotNullable("").length

    a.everythingNullable(null).length
    a.everythingNullable(null)?.length

    a.everythingUnknown(null).length
    a.everythingUnknown(null)?.length
}
