# 2 factor authentication

## How to integrate into a new app (Part One, polling-only)

This enables the feature, that tries to retrieve any ongoing login challenge when the app is brought to the foreground.
It does NOT include notifications support, which is an extra (Part Two).

### 1. Add the dependencies

Depend on these 2FA related libraries:

```kotlin
implementation(project(":Core:TwoFactorAuth:Front"))
implementation(project(":Core:TwoFactorAuth:Back:WithUserDb"))
```

### 2. Define the TwoFactorAuthManager singleton

#### A. With the user db dependency

Example:

```kotlin
/**
 * Singleton for incoming 2FA (two factor authentication) challenges.
 *
 * Not a ViewModel because the state needs to be scoped for the entire app.
 */
val twoFactorAuthManager = TwoFactorAuthManager { userId -> AccountUtils.getHttpClient(userId) }
```

#### B. With NO dependency on the user db

If you can't or don't want to depend on the user database dependency, you need to provide several more parameters.
Here's an example.

```kotlin
val twoFactorAuthManager = TwoFactorAuthManager(
    coroutineScope = coroutineScope,
    userIds = someStorage.connectedUsers.map(mutableSetOf()) { users ->
        users.map { it.id }
    }.distinctUntilChanged(),
    getAccountInfo = {
        val info = getUserAccountInfoById(it)
        ConnectionAttemptInfo.TargetAccount(
            avatarUrl = info.avatar,
            fullName = info.displayName,
            initials = info.computeInitials(),
            email = info.email,
            id = it.toLong(),
        )
    },
    getConnectedHttpClient = { userId -> getHttpClientForUser(userId) }
)
```

### 3. Add the overlay where needed

For each Activity in the app (including login screen for multi-account apps):

Add `TwoFactorAuthApprovalAutoManagedBottomSheet(twoFactorAuthManager)` at the root of the content.

Example:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TwoFactorAuthApprovalAutoManagedBottomSheet(twoFactorAuthManager) // References the singleton declare just before.
            Whatever()
        }
    }
}
```

If the Activity is NOT using Compose, or if you don't know (e.g. in a `BaseActivity` class), the `addComposableOverlay`
function was made just for that, just make sure it's called after `setContentView` or `setContent`.

Example:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(whatever)
        addComposableOverlay { TwoFactorAuthApprovalAutoManagedBottomSheet(twoFactorAuthManager) }
    }
}
```

At this point, you can expect the feature to be working (albeit without notifications).

## How to integrate into a new app (Part Two, notifications support)

**⚠️⚠️ NOTE: ⚠️⚠️** 

Unless specified otherwise, the **code below is to be put into Google Play Services dedicated source sets**.

For example, if the host app is published to F-Droid + the Google Play Store and has 2 product flavors named "fdroid" and "standard", where "standard" contains Google Play Services dependent code, by default, the code below should be located into `app-module/src/standard/kotlin`, where `app-module` is the target app or library module.

### 1. Add the right dependency in the right configuration

Add this dependency in the host app's Gradle build file:

```kotlin
"standardImplementation"(project(":Core:Notifications:Registration"))
```

Here, "standard" matches the product flavor that includes Firebase/Google Play Services dependencies.

### 2. Ensure notification token and topics will be synced

First, create the `RegisterUserDeviceWorker` class (if it doesn't exist already). It needs to subclass `AbstractNotificationsRegistrationWorker`.

You can find the Mail app example here: https://github.com/Infomaniak/android-kMail/blob/e272d5b5ff5f4b1eb1f11784537a3d624c063e80/app/src/standard/java/com/infomaniak/mail/firebase/RegisterUserDeviceWorker.kt

Follow other changes from this PR in the Mail, or the kDrive app:
https://github.com/Infomaniak/android-kDrive/pull/1872/changes
https://github.com/Infomaniak/android-kMail/pull/2698/changes

Here's the list of changes you need to add from the example PRs above:
- Ensure `TwoFactorAuthNotifications.channel()` is created/submitted to Android's NotificationManager.
- Ensure `NotificationsRegistrationManager` is added in `userDataCleanableList` at the beginning of the app process for the Play Services app variant.
- Ensure the user addition and removal functions to call `resetForUser` in elements registered `userDataCleanableList` (already done if added Cross-app login first).
- Ensure `NotificationsRegistrationManager.scheduleWorkerOnUpdate` is called in an app process wide coroutine, right from the app process start.
- Ensure the `Application` subclass is declared properly in the manifest for the Play Services dependent product flavor
- In the `FirebaseMessagingService` subclass (create it and declare it in the manifest if needed), make sure the `onNewToken` and `onMessageReceived` functions are implemented properly to forward the new tokens to `NotificationsRegistrationManager`, and matching notifications (the ones with `TwoFactorAuthNotifications.TYPE` in key `"type"`), forwarded to `twoFactorAuthManager.onApprovalChallengePushed(…)`.
- Ensure any other push notification topics are properly integrated for `RegisterUserDeviceWorker` and `NotificationsRegistrationManager.scheduleWorkerOnUpdate`.
