// Declara os plugins usados pelos módulos, sem aplicá-los aqui.
// As versões são centralizadas em gradle/libs.versions.toml (version catalog).
//
// Obs.: o plugin `kotlin-android` NÃO é usado — desde o AGP 9.0 o Kotlin é
// suportado nativamente (built-in Kotlin). Para usar uma versão do Kotlin
// Gradle plugin maior que a padrão do AGP, ela é declarada via buildscript.
// Ver: https://developer.android.com/build/migrate-to-built-in-kotlin

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
