plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.bmuschko.docker-remote-api") version "7.2.0"
}

group = "ru.nspk.kafka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.kafka:kafka-clients:3.2.3")
    implementation("org.apache.kafka:connect-api:3.2.3")
    implementation("org.apache.kafka:kafka-streams:3.2.3")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    // Add ClickHouse Kafka Connect sink connector dependency if available
    // For this example, we assume you manually install the ClickHouse connector
    // implementation("com.clickhouse:clickhouse-kafka-connect:0.4")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.20")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.20")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "ru.nspk.kafkaconnect.MainKt"
        }
    }

    val shadowJar by getting(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveClassifier.set("all")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "ru.nspk.kafkaconnect.MainKt"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    // Docker tasks
    register<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("buildDockerImage") {
        dependsOn(shadowJar)
        dependsOn("jar")
        dependsOn("inspectClassesForKotlinIC")
        inputDir.set(projectDir)
        dockerFile.set(file("Dockerfile"))
        images.add("ticket-replicator_kafka-connect:latest")
    }

    register<com.bmuschko.gradle.docker.tasks.image.DockerPushImage>("pushDockerImage") {
        dependsOn("buildDockerImage")
        images.add("your-docker-image-name:latest")
    }
}