import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    `maven-publish`
}

group = "com.pineypiney.game_engine"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.1"
val lwjglNatives = "natives-windows"

val javacv = "1.5.7"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("junit:junit:4.13.1")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation(kotlin("test"))

    // Logback
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    // GLM
    implementation("com.github.kotlin-graphics.glm:glm:375708cf1c0942b0df9d624acddb1c9993f6d92d")


    // LWJGL
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-jemalloc")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    // JavaCV for video processing
    implementation("org.bytedeco:javacv:$javacv")
    implementation("org.bytedeco:javacpp:$javacv")
    implementation("org.bytedeco:opencv-platform-gpu:4.5.5-$javacv")
    implementation("org.bytedeco:ffmpeg-platform-gpl:5.0-$javacv")
}

publishing{
    publications{
        register<MavenPublication>("gpr"){
            group = "com.github.PineyPiney"
            artifactId = "GameEngine"
            version = version

            from(components["java"])
        }
    }
}

// Package Resources into ZIP file
tasks.register<Zip>("packageResources"){
    from(layout.projectDirectory.dir("\\src\\main\\resources"))

    archiveFileName.set("resources.zip")
    destinationDirectory.set(layout.projectDirectory.dir("\\"))
}

// Create full game file
tasks.register<Zip>("packageGame"){
    from(layout.projectDirectory.dir("\\out\\artifacts\\PixelGame_main_jar\\"))
    include("\\PixelGame.main.jar")
    from(layout.projectDirectory.dir("\\"))
    include("\\resources.zip")

    archiveFileName.set("PixelGame.zip")
    destinationDirectory.set(layout.projectDirectory.dir("\\out\\artifacts\\PixelGame_main_jar"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}