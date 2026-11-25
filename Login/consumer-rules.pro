# Gson uses these fields when deserializing tokens.
-keepclassmembers class com.infomaniak.core.login.ApiToken {
    !static !transient <fields>;
}
