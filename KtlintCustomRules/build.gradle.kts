
plugins {
    id("kotlin")
}

dependencies {
    compileOnly(core.ktlint.cliRuleset)
    compileOnly(core.ktlint.ruleEngineCore)
}
