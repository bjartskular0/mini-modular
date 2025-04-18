package buildsrc.convention

plugins {
    id("org.gradlex.extra-java-module-info")
}

extraJavaModuleInfo {
    deriveAutomaticModuleNamesFromFileNames = true
//    failOnMissingModuleInfo = false
    skipLocalJars = true
    module("io.ktor:ktor-server-core", "ktor.server.core") {
        exportAllPackages()
        requiresTransitive("jdk.unsupported")
        requireAllDefinedDependencies()

    }
//    automaticModule("com.akuleshov7:ktoml-core", "ktoml.core")
//    automaticModule("com.akuleshov7:ktoml-file", "ktoml.file")

//    automaticModule("com.squareup.okio:okio", "okio")
//    module("com.akuleshov7:ktoml-core-jvm", "ktoml.core") {
//        exportAllPackages()
//        requireAllDefinedDependencies()
//    }
//    module("com.akuleshov7:ktoml-file-jvm", "ktoml.file") {
//        exportAllPackages()
//        requireAllDefinedDependencies()
//    }
//
}
