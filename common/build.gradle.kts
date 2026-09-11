plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation(libs.spring.web)
    implementation(libs.jakarta.persistence)
}