plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation(project(":common"))
    implementation(project(":customer"))
}