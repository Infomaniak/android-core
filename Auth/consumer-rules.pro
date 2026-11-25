# Gson uses these fields when storing and restoring user data.
-keepclassmembers class com.infomaniak.core.auth.models.OrganizationAccount {
    !static !transient <fields>;
}
-keepclassmembers class com.infomaniak.core.auth.models.user.Card {
    !static !transient <fields>;
}
-keepclassmembers class com.infomaniak.core.auth.models.user.CardLink {
    !static !transient <fields>;
}
-keepclassmembers class com.infomaniak.core.auth.models.user.ContactMethod {
    !static !transient <fields>;
}
-keepclassmembers class com.infomaniak.core.auth.models.user.Phone {
    !static !transient <fields>;
}
