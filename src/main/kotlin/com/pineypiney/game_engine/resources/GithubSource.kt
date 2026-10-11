package com.pineypiney.game_engine.resources

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pineypiney.game_engine.GameEngineI
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

open class GithubSource(account: String, repo: String, val accessToken: String, val apiVersion: String, val directory: String) : Source {

	constructor(account: String, repo: String, accessToken: File, apiVersion: String, directory: String) : this(
		account, repo, accessToken.readText(), apiVersion, directory
	)

	val repoUrl = "https://api.github.com/repos/$account/$repo/contents"
	val head = "https://raw.githubusercontent.com/$account/$repo/main/$directory"
	val client = OkHttpClient()

	override val streamList: List<String> = createStreamList()

	fun createStreamList(): List<String> {
		val files = mutableListOf<String>()
		getAllFiles(files, "$repoUrl/$directory")
		return files
	}

	fun getAllFiles(files: MutableList<String>, url: String) {
		// Don't include engine test files
		if (url.contains("test")) return

		val json = getJson(url) ?: return
		for (fileJson in json) {
			if (fileJson !is JsonObject) continue
			val downloadURL = fileJson["download_url"]
			if (downloadURL == JsonNull.INSTANCE) {
				getAllFiles(files, fileJson["url"].asString)
			} else files.add(lowercaseExtension(downloadURL.asString.removePrefix("$head/")))
		}
	}

	fun <E> useBody(url: String, func: (ResponseBody) -> E): E? {
		val req = Request.Builder()
			.url(url)
			.header("Authorization", "Bearer $accessToken")
			.header("X-GitHub-Api-Version", "2026-03-10")
			.build()
		client.newCall(req).execute().use { response ->
			return if (response.isSuccessful) func(response.body)
			else null
		}
	}

	fun getJson(url: String): JsonArray? {
		return useBody(url) { body ->
			val reader = body.charStream()
			JsonParser.parseReader(reader) as? JsonArray
		}
	}

	override fun getStream(name: String): InputStream? {
		val path = "$head/$name"
		return try {
			useBody(path) { body ->
				ByteArrayInputStream(body.bytes())
			}
		} catch (e: FileNotFoundException) {
			GameEngineI.warn("Could not find resources file at $path")
			null
		}
	}

	companion object {
		// I understand that putting a raw access token in the repository might be a big problem but
		val ENGINE = GithubSource("PineyPiney", "GameEngine", File("../../GameEngineAccessToken.txt"), "2026-03-10", "src/main/resources")
	}
}