# Infomaniak Android Core library

Infomaniak Core is a modular Android library used across multiple projects.
It can be consumed locally using **Gradle Composite Builds**, enabling fast development
without publishing artifacts, while still supporting the historical **Legacy** module.


## Overview

This repository contains:

* The **new modular Core**, designed to be consumed via **composite builds**
* The **Legacy** module, kept for backward compatibility
* Shared resources, models, and networking utilities

The composite build integration is powered by the Gradle **Settings plugin**:

```
com.infomaniak.core.composite.CoreCompositePlugin
```

📘 For advanced configuration and exact dependency mapping rules,
**refer to the KDoc of `CoreCompositePlugin`.**


## Installation (Composite Build – recommended)

Import the project as a git sub-module in the root folder of your project

To use the **new Core with composite build**, you must update the **host project’s**
`settings.gradle.kts`.

### 1️⃣ Include Core build logic

```kotlin
pluginManagement {
    includeBuild("Core/build-logic")
}
```

> [!IMPORTANT]
> The Core composite plugin is provided by Core’s build logic.


### 2️⃣ Apply the Core composite plugin

```kotlin
plugins {
    id("com.infomaniak.core.composite")
}
```

Once applied:

* Core is included as a **composite build**
* All `com.infomaniak.core:*` dependencies are automatically substituted
* Core modules are resolved as **local Gradle projects**


## Using Core modules

Core modules are declared using **Maven coordinates**, but resolved locally.

```kotlin
dependencies {
    implementation("com.infomaniak.core:Core")
    implementation("com.infomaniak.core:TwoFactorAuth.Front")
}
```

📘 The full mapping logic (module name → project path) is documented in the
**KDoc of `com.infomaniak.core.composite.CoreCompositePlugin`**.


## Legacy module support (build-time)

The composite plugin also supports the **Legacy** module located inside Core.

### Enable Legacy

In `settings.gradle.kts` of the host project:

```kotlin
include(":Core:Legacy")
```

### Use Legacy

```kotlin
dependencies {
    implementation(project(":Core:Legacy"))
}
```

📘 All Legacy-related rules and behavior are documented in the plugin KDoc.


## Legacy runtime initialization

Init the `InfomaniakCore` object in `MainApplication` and configure it like this :

```kt
        InfomaniakCore.init(
    this,
    BuildConfig.VERSION_NAME,
    BuildConfig.API_APP_TOKEN,
    BuildConfig.VERSION_CODE,
    BuildConfig.DEBUG
)
```

You'll be able to call the method `getHeaders()` everywhere in your app, as long as you've initiated the InfomaniakCore library in
`MainApplication`. Like this example :

```kt
val request = Request.Builder()
    .url(url)
    .headers(getHeaders())
    .post(requestBody)
    .build()
```


## What does Core contain?

* Infomaniak base colors
* Shared and reusable strings
* Data models
* HTTP-related helpers and utilities
* Modular features exposed via Core submodules


## Documentation

* **Core composite integration**:
  `com.infomaniak.core.composite.CoreCompositePlugin` (KDoc)

This class documents:

* Dependency substitution rules
* Module name mapping
* Custom Core root paths
* Legacy integration behavior


## License

    Copyright 2021-2025 Infomaniak Network SA

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

