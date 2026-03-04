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
    id("com.infomaniak.core.compose.lint")
}
```

And you're done!
