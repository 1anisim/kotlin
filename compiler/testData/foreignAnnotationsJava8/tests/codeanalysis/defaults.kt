// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER
// FILE: A.java

import codeanalysis.experimental.annotations.*;

@DefaultNotNull
public class A {
    public String defaultField = "";
    @Nullable public String field = null;

    public String everythingNotNullable(String x) { return ""; }

    @DefaultNullable
    public String everythingNullable(String x) { return ""; }

    @DefaultNullnessUnknown
    public String everythingUnknown(String x) { return ""; }

    @DefaultNullable
    public String mixed(@NotNull String x) { return ""; }

    public String explicitlyNullnessUnknown(@NullnessUnknown String x) { return ""; }
}

// FILE: main.kt

fun main(a: A) {
    a.everythingNotNullable(<!NULL_FOR_NONNULL_TYPE!>null<!>)<!UNNECESSARY_SAFE_CALL!>?.<!>length
    a.everythingNotNullable(<!NULL_FOR_NONNULL_TYPE!>null<!>).length
    a.everythingNotNullable("").length

    a.everythingNullable(null)<!UNSAFE_CALL!>.<!>length
    a.everythingNullable(null)?.length

    a.everythingUnknown(null).length
    a.everythingUnknown(null)?.length

    a.mixed(<!NULL_FOR_NONNULL_TYPE!>null<!>)<!UNSAFE_CALL!>.<!>length
    a.mixed(<!NULL_FOR_NONNULL_TYPE!>null<!>)?.length
    a.mixed("")?.length

    a.explicitlyNullnessUnknown("").length
    a.explicitlyNullnessUnknown("")<!UNNECESSARY_SAFE_CALL!>?.<!>length
    a.explicitlyNullnessUnknown(null).length

    a.defaultField<!UNNECESSARY_SAFE_CALL!>?.<!>length
    a.defaultField.length

    a.field?.length
    a.field<!UNSAFE_CALL!>.<!>length
}
