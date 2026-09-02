plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.epages.restdocs-api-spec") version "0.20.1"
}

group = "junhyeok"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.flywaydb:flyway-mysql")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-batch-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:testcontainers-mysql")

    testImplementation("org.springframework.boot:spring-boot-restdocs")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.20.1")

    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()

    val snippetsDir = layout.buildDirectory.dir("generated-snippets")
    outputs.dir(snippetsDir)
    doFirst { delete(snippetsDir) }
}

openapi3 {
    title = "Blog API"
    version = "1.0.0"
    format = "json"
    setServer("http://localhost:8080")
    outputDirectory = "build/api-spec"
    outputFileNamePrefix = "openapi3"
}

tasks.register<Copy>("copyApiSpec") {
    delete("src/main/resources/static/openapi3.json")
    from("build/api-spec/openapi3.json")
    into("src/main/resources/static")
    dependsOn("openapi3")
}
