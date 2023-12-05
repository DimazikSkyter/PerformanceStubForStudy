import org.gradle.jvm.tasks.Jar

tasks.test {
    useJUnitPlatform()
}

plugins {
    id("java")
}
group = "stub"

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {

    implementation(project(":StubCommon"))

    // https://mvnrepository.com/artifact/io.projectreactor.netty/reactor-netty-http
    implementation("io.projectreactor.netty:reactor-netty-http:1.1.10")


    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    implementation("org.apache.kafka:kafka-clients:3.6.0")

    implementation("org.apache.jmeter:ApacheJMeter_core:5.5") {
        exclude("org.apache.jmeter", "bom")
    }
    implementation("org.apache.jmeter:ApacheJMeter_java:5.5"){
        exclude("org.apache.jmeter", "bom")
    }

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

val fatJar = tasks.register<Jar>("uberJar") {
    archiveClassifier = "uber"
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(sourceSets.main.get().output)


    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}


tasks {
    "build" {
        dependsOn(fatJar)
    }
}