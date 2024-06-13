import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    val kotlinVersion = "1.9.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    `maven-publish`
}

val ver: String = "1.0-SNAPSHOT"
group = "com.pineypiney.game_engine"
version = ver

val lwjglVersion = "3.3.3"

// Use https://www.lwjgl.org/customize to set natives
val lwjglNatives = run {
    val name = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val a64 = arch.startsWith("aarch64")
    when {
        name.startsWith("Windows") -> {
            if(arch.contains("64")) "natives-windows${if(a64) "-arm64" else ""}"
            else "natives-windows-x86"
        }
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } -> {
            if(a64) "natives-macos-arm64" else "natives-macos"
        }
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else if (arch.startsWith("ppc"))
                "natives-linux-ppc64le"
            else if (arch.startsWith("riscv"))
                "natives-linux-riscv64"
            else
                "natives-linux"

        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

val javacv = "1.5.7"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    // Mary is used by GLM because they say that Jitpack is cringe
    maven("https://raw.githubusercontent.com/kotlin-graphics/mary/master")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.24")

    // Logback
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // GLM
    implementation("kotlin.graphics:kool:0.9.75")
    implementation("kotlin.graphics:unsigned:3.3.32")
    implementation("kotlin.graphics:glm:0.9.9.1-11")

    // LWJGL
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-jemalloc")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-openvr")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openvr", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    // JavaCV for video processing
    implementation("org.bytedeco:javacv:$javacv")
    implementation("org.bytedeco:javacpp:$javacv")
    implementation("org.bytedeco:opencv-platform-gpu:4.9.0-1.5.10")
    implementation("org.bytedeco:ffmpeg-platform-gpl:6.1.1-1.5.10")

    // Gson for JSON parsing
    implementation("org.json:json:20231013")
	implementation(kotlin("reflect"))
}

publishing{
    publications{
        register<MavenPublication>("gpr"){
            group = "com.github.PineyPiney"
            artifactId = "GameEngine"
            version = ver


            from(components["java"])
        }
    }
}

/*
// Package Resources into ZIP file
tasks.register<Zip>("packageResources"){
    from(layout.projectDirectory.dir("\\src\\main\\resources"))

    archiveFileName.set("resources.zip")
    destinationDirectory.set(layout.projectDirectory.dir("\\"))
}

// Create full game file
tasks.register<Zip>("packageGame"){
    from(layout.projectDirectory.dir("\\out\\artifacts\\GameEngine_main_jar\\"))
    include("\\GameEngine.main.jar")
    from(layout.projectDirectory.dir("\\"))
    include("\\resources.zip")

    archiveFileName.set("GameEngine.zip")
    destinationDirectory.set(layout.projectDirectory.dir("\\out\\artifacts\\GameEngine_main_jar"))
}
 */

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "20"
}

java{
    sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20
}