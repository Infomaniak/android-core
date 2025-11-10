# Cross-app login

## How to integrate into a new app

### 1. Declare the certificate

Declare the new app's signature in [infomaniakAppsCertificates.kt](Back/src/main/kotlin/com/infomaniak/core/crossapplogin/back/internal/certificates/infomaniakAppsCertificates.kt).
Make sure to declare the final app's certificate. If using Play App Signing, use the sha256 hash of
the certificate that corresponds to the keystore the Google Play is using to sign the delivered apps.
Don't use the upload certificate.

### 2. Allow apps to see each other

Declare the new app's package name(s) in this module's [AndroidManifest.xml](Back/src/main/AndroidManifest.xml) file,
in the `<queries>` `</queries>` tag.

It should look like this:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <queries>
        <package android:name="com.infomaniak.drive" />
        <package android:name="com.infomaniak.mail" />
        <package android:name="com.infomaniak.whatever" /><!-- <=== Add the new one here. -->
        <!-- ... -->
    </queries>

</manifest>
```

NOTE: The changes until here need to be integrated into all the production apps.

### 3. Add the right dependency

Add this dependency in the host app's Gradle build file:

```kotlin
implementation(project(":Core:CrossAppLogin:Front"))
```

### 4. Ensure cross-app device id will be synced

First, create the `DeviceInfoUpdateWorker` class. It needs to subclass `AbstractDeviceInfoUpdateWorker`.

Here's a copy-paste ready example implementation:

```kotlin
class DeviceInfoUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : AbstractDeviceInfoUpdateWorker(appContext, params) {

    override suspend fun getConnectedHttpClient(userId: Int): OkHttpClient {
        return AccountUtils.getHttpClient(userId = userId)
    }
}
```

Then, in the `Application` class, somewhere inside the `onCreate` method, call the following:

```kotlin
someAppProcessLongCoroutineScope.launch {
    DeviceInfoUpdateManager.sharedInstance.scheduleWorkerOnDeviceInfoUpdate<DeviceInfoUpdateWorker>()
}
```

### 5. Configure the server

Create the `CrossAppLoginService` class. It must subclass `BaseCrossAppLoginService`.

Here's how it's supposed to look like:

```kotlin
class CrossAppLoginService : BaseCrossAppLoginService(
    selectedUserIdFlow = someDataStore.map { it.idOfCurrentlySelectedUser }
)
```

Then, declare it in the `AndroidManifest.xml` file, inside the `<application>` `</application>` tag:

```xml
<!-- The CrossAppLoginService is designed to be bound, and checks the identity of its clients. -->
<service
    android:name=".CrossAppLoginService"
    android:exported="true"
    tools:ignore="ExportedService">
    <intent-filter>
        <!-- WARNING: Once this is into a release (even a beta), it shall never be changed. -->
        <action android:name="com.infomaniak.crossapp.login" />
    </intent-filter>
</service>
```

### 6. Configure the client

Create the `CrossAppLoginViewModel` class. It must subclass `BaseCrossAppLoginViewModel`.

It should look close to this:

```kotlin
class CrossAppLoginViewModel : BaseCrossAppLoginViewModel(BuildConfig.APPLICATION_ID, clientId = BuildConfig.CLIENT_ID)
```

Then, integrate the UI as such:

1. Declare the ViewModel: `private val crossAppLoginViewModel: CrossAppLoginViewModel by viewModels()`
2. Create an `OnboardingScreen` composable function that is similar to the one in kDrive.
3. When the login UI is shown:
   1. Collect `availableAccounts` and `skippedAccountIds` as Compose state to feed them to the `OnboardingScreen` composable.
   2. Launch a coroutine (once, in the scope of this login UI) that will call `activateUpdates(…)` with the host Activity.
4. Implement the login logic for the host app (see the `handleLogin` function in kDrive).

### X. Register for Play Integrity on the backend

On the Google Cloud console, there's a place to enable Play Integrity for a new app,
and there, you can download a file that contains data that needs to be put in the backend.
