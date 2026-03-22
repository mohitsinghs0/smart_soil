plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

group = "com.smartsoil"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.mysql.connector.j)
    runtimeOnly(libs.h2)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
