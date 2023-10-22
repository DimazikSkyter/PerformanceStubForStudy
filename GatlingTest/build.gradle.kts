import io.gatling.gradle.LogHttp
import org.gradle.api.internal.artifacts.configurations.MutationValidator
import java.net.URI

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.gatling.gradle") version "3.9.5.5"
}


sourceSets {
    main {
        scala.setSrcDirs(listOf("gatling"))
    }
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // Replace with the GitHub repository URL
    jcenter()
}

group = "ru.performance.gatlingtest"
version = "1.0-SNAPSHOT"


gatling {
    // WARNING: options below only work when logback config file isn't provided
    logLevel = "WARN" // logback root level
    logHttp = LogHttp.NONE // set to 'ALL' for all HTTP traffic in TRACE, 'FAILURES' for failed HTTP traffic in DEBUG

    enterprise.closureOf<Any> {
        // Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-cloud
        // Enterprise Self-Hosted configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-self-hosted
    }
}

//val fatJar = task("${project.name}-fat", type = Jar::class) {
//    manifest {
//        attributes["Implementation-Title"] = "Gradle Jar File Example"
//        attributes["Implementation-Version"] = version
//        attributes["Main-Class"] = "com.mkyong.DateUtils"
//    }
//    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
//    with(tasks.jar.get() as CopySpec)
//}


tasks.test {
    useJUnitPlatform()
}
dependencies {

    // https://mvnrepository.com/artifact/io.gatling.highcharts/gatling-charts-highcharts
    implementation("io.gatling.highcharts:gatling-charts-highcharts:3.9.5")
    implementation("ru.tinkoff:gatling-jdbc-plugin_2.13:0.10.3")
    implementation("io.gatling:gatling-test-framework:3.9.5")
    gatling("org.reflections:reflections:0.9.12")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "ru.performance.gatling.Main"
    }
}
val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Main-Class"] = "ru.performance.gatling.Main"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(files(sourceSets.gatling.get().output.classesDirs))
    from(files(sourceSets.gatling.get().output))
    from(configurations.runtimeClasspath.get().map {  if (it.isDirectory()) it else zipTree(it)  })
    with(tasks["jar"] as CopySpec)
}
