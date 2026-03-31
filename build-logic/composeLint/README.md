# Setup

This plugin automatically adds the lint to all modules defined in the whole project and
use [lint.xml](../../lint.xml) as the single source of truth to configure the lints in every app

To enable these compose lints in a new project, follow these steps:

1. Make sure your top level settings.gradle.kts defines the following if not already done in order to import Core's composite
   build:

```kts
pluginManagement {
    includeBuild("Core/build-logic")
}
```

2. Inside the top-level build.gradle.kts add the following plugin:

```kts
plugins {
    alias(core.plugins.compose.lint)
}
```

3. Generate the baseline file for each module so only new issues will be caught by the CI. (The baseline file definition for each
   module is also specified inside our plugin). And add `ignoreWarnings = true`
   inside [ComposeLintPlugin.kt](src/main/kotlin/com/infomaniak/core/compose/lint/ComposeLintPlugin.kt) to only create a baseline
   with errors and not with warnings which don't need to be inside the baseline.

```
./gradlew updateLintBaseline
```

or

```
./gradlew -p Core updateLintBaseline
```

And you're done!
