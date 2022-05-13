import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.6.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    `maven-publish`
}

group = "com.pineypiney.game_engine"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.1"
val lwjglNatives = "natives-windows"

val javacv = "1.5.7"
val kx = "com.github.kotlin-graphics"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

    //implementation(project("PixelGameImporter"))

    // https://kotlinlang.org/docs/releases.html#release-details
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")


    // GLM
    implementation("$kx.glm:glm:375708cf1c0942b0df9d624acddb1c9993f6d92d")


    // LWJGL
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-assimp:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-bgfx:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-cuda:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-egl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-jawt:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-jemalloc:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-libdivide:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-llvm:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-lmdb:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-lz4:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-meow:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-nfd:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-nuklear:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-odbc:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opencl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengles:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-openvr:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opus:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-ovr:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-par:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-remotery:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-rpmalloc:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-shaderc:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-sse:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-tinyexr:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-tinyfd:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-tootle:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-vma:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-vulkan:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-xxhash:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-yoga:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-zstd:$lwjglVersion")

    runtimeOnly("org.lwjgl:lwjgl:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-assimp:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-bgfx:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-jemalloc:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-libdivide:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-llvm:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-lmdb:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-lz4:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-meow:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nanovg:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nfd:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nuklear:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengles:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openvr:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opus:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-ovr:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-par:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-remotery:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-rpmalloc:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-shaderc:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-sse:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-tinyexr:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-tinyfd:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-tootle:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-vma:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-xxhash:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-yoga:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-zstd:$lwjglNatives")

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")

    // Reflection is needed by Assimp for model loading
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

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