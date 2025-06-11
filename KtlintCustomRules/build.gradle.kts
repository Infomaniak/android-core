
plugins {
    id("kotlin")
}

dependencies {
    compileOnly("com.pinterest.ktlint:ktlint-cli-ruleset-core:1.6.0")
    compileOnly("com.pinterest.ktlint:ktlint-rule-engine-core:1.6.0")

    testImplementation("junit:junit:4.13.2")
}
