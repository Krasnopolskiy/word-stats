plugins {
    java
}

group = "io.github.krasnopolskiy"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all,-serial", "-Werror"))
}

tasks.jar {
    archiveFileName = "word-stats.jar"
    manifest {
        attributes("Main-Class" to "io.github.krasnopolskiy.wordstats.Main")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register("downloadDependencies") {
    description = "Downloads the libraries that compiling and testing need."
    val classpaths = files(configurations.compileClasspath, configurations.testRuntimeClasspath)
    doLast {
        classpaths.files
    }
}
