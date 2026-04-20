# Keep FLAVOR field in app's BuildConfig
-keep class **.BuildConfig {
    public static final java.lang.String FLAVOR;
}

#noinspection ShrinkerUnresolvedReference
# Keep GoogleApiAvailability for play services detection (comes from app dependency)
-keep class com.google.android.gms.common.GoogleApiAvailability {
    #noinspection ShrinkerUnresolvedReference
    public static com.google.android.gms.common.GoogleApiAvailability getInstance();
    public int isGooglePlayServicesAvailable(android.content.Context);
}

