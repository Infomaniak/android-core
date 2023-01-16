# Infomaniak Android Core library

Infomaniak Core library for Android (many purposes)

## Installation

Import the project as a git modsub-module in the root folder of your project

## Use

Init the `InfomaniakCore` object in `ApplicationMain` and configure it like this :

``` 
        InfomaniakCore.init(
            this,
            BuildConfig.VERSION_NAME,
            BuildConfig.API_APP_TOKEN,
            BuildConfig.VERSION_CODE,
            BuildConfig.DEBUG
        )
```

You'll be able to call the method `getHeaders()` everywhere in your app, as long as you've initiated the InfomaniakCore library in  App main.
Like this example :

``` 
val request = Request.Builder()
    .url(url)
    .headers(getHeaders())
    .post(requestBody)
    .build()
```

## What does it contain

* Infomaniak basic colors
* Basic and reusable strings (for Infomaniak)
* Models
* Some actions relatives to HTTP

## License

    Copyright 2023 Infomaniak Network SA

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
