plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit Jupiter
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")

    // Application dependencies
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.slf4j:slf4j-simple:2.0.12")

        // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.remoteswe.crawler.App")
    
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
