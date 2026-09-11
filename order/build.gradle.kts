plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation(project(":common"))
    implementation(project(":customer"))
    implementation(project(":embassy"))

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.webmvc)
}