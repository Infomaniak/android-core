# InAppReview Module Documentation

The `InAppReview` module manages the in-app review flow with a configurable countdown mechanism.  
It supports two distribution flavors:

- **`standard`** — Google Play Review API (`ReviewManagerFactory`)
- **`fdroid`** — No-op manager (no Play dependency). If you still want to prompt users, you can show `ReviewAlertDialog` yourself.

The review prompt is only shown after a configurable number of app launches ("countdown"), and is never shown again once it has been displayed to the user.

## 1. Integration (Gradle)

Add the following dependency to your `build.gradle.kts`:

```kotlin
implementation(project(":Core:InAppReview"))
```

or with a composite build:

```kotlin
implementation(core.infomaniak.core.inappreview)
```

Make sure your module declares the `distribution` flavor dimension and includes both `standard` and `fdroid` product flavors to match the ones defined in this module.

---

## 2. Setup

### 2.1 Initialize the manager

Create an `InAppReviewManager` instance in your `ComponentActivity` and call `init()`.

```kotlin
class MainActivity : ComponentActivity() {

    private val inAppReviewManager by lazy { InAppReviewManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inAppReviewManager.init(
            // LifecycleBased → countdown decremented automatically on each onResume
            // Manual         → you call decrementAppReviewCountdown() yourself
            countdownBehavior = BaseInAppReviewManager.Behavior.LifecycleBased,
            appReviewThreshold = 50,        // Optional: initial countdown value (default 50)
            onUserWantToReview = { },       // Optional: called when user taps "Review"
            onUserWantToGiveFeedback = { }, // Optional: called when user taps "Give feedback"
        )
    }
}
```

> **Default:** `appReviewThreshold = 50`.
> Once the prompt has been displayed, call `onReviewDialogShown()` so it is never shown again.

---

## 3. Showing the review dialog

### 3.1 Observe `shouldDisplayReviewDialog`

`shouldDisplayReviewDialog` is a `Flow<Boolean>` that emits `true` when:
- the user has **not** already been asked for a review, **and**
- the countdown has reached **0 or below**.

Once the prompt is shown, call `onReviewDialogShown()` to mark it as asked; the flow will never emit `true` again.

Collect it in your Compose UI and show a dialog or bottom sheet accordingly:

```kotlin
@Composable
fun ReviewPromptObserver(reviewManager: InAppReviewManager) {
    val shouldDisplay by reviewManager.shouldDisplayReviewDialog.collectAsState(initial = false)

    if (shouldDisplay) {
        reviewManager.onReviewDialogShown()
        ReviewDialog(
            onReview = { reviewManager.onUserWantsToReview() },
            onFeedback = { reviewManager.onUserWantsToGiveFeedback("https://yourapp.example.com/feedback") },
            onDismiss = { reviewManager.onUserWantsToDismiss() },
        )
    }
}
```

### 3.2 Example dialog composable

```kotlin
@Composable
fun ReviewDialog(
    onReview: () -> Unit,
    onFeedback: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.reviewTitle)) },
        text = { Text(text = stringResource(R.string.reviewDescription)) },
        confirmButton = {
            TextButton(onClick = onReview) {
                Text(text = stringResource(R.string.reviewPositive))
            }
        },
        dismissButton = {
            TextButton(onClick = onFeedback) {
                Text(text = stringResource(R.string.reviewNegative))
            }
        },
    )
}
```

---

## 4. Manual countdown behavior

If you prefer to control when the countdown decrements (e.g., only after a meaningful user action), use `Behavior.Manual` and call `decrementAppReviewCountdown()` yourself:

```kotlin
inAppReviewManager.init(
    countdownBehavior = BaseInAppReviewManager.Behavior.Manual,
)

// Later, when a meaningful action occurs:
inAppReviewManager.decrementAppReviewCountdown()
```
