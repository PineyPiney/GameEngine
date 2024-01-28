package com.pineypiney.game_engine.level_editor.resources.levels

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.level_editor.gameEngine
import com.pineypiney.game_engine.level_editor.objects.GameObjects
import com.pineypiney.game_engine.level_editor.startTime
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.resources.models.pgm.DataSource
import com.pineypiney.game_engine.resources.models.pgm.getAllAttributes
import com.pineypiney.game_engine.resources.models.pgm.getAttribute
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.f
import glm_.i
import glm_.min
import glm_.vec3.Vec3
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class LevelLoader private constructor(){

    private val levelMap: MutableMap<ResourceKey, LevelDetails> = mutableMapOf()

    fun loadLevels(streams: Map<String, InputStream>) {
        streams.filter { it.key.endsWith(".pgl") }.forEach level@ { (fileName, stream) ->

            val details = getLevelInfo(fileName, stream)

            levelMap[ResourceKey(details.worldName)] = details
        }
    }

    private fun getLevelInfo(fileName: String, stream: InputStream): LevelDetails {
        val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = builder.parse(stream)
        val path: XPath = xPath

        val detailsRoot = "PGL/level"
        val name = getStringAt("$detailsRoot/name", doc, path)
        val width =
            try {
                getStringAt("$detailsRoot/width", doc, path).f
            } catch (e: NumberFormatException) {
                PixelEngine.logger.warn("Could not parse width in level $fileName")
                e.printStackTrace()
                17.78f
            }
        val colour: Vec3 =
            try {
                val nums = getStringAt("$detailsRoot/colour", doc, path).split(" ").map { it.f }
                Vec3(nums[0], nums[1], nums[2])
            } catch (e: Exception) {
                PixelEngine.logger.warn("Could not parse colour in level $fileName")
                e.printStackTrace()
                Vec3(1)
            }
        val icon = getStringAt("$detailsRoot/icon", doc, path)

        val creationString = getStringAt("$detailsRoot/creation-date", doc, path)
        val editString = getStringAt("$detailsRoot/edit-date", doc, path)

        val formatter = LevelDetails.formatter

        val creationTime = try {
            LocalDateTime.parse(creationString, formatter)
        } catch (e: DateTimeParseException) {
            LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
        }
        val editTime = try {
            LocalDateTime.parse(editString, formatter)
        } catch (e: DateTimeParseException) {
            LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
        }

        return LevelDetails(
            name,
            fileName,
            width,
            colour,
            TextureLoader.getTexture(ResourceKey(icon)),
            creationTime,
            editTime
        )
    }

    fun loadLevel(key: ResourceKey): MutableSet<GameObject> {
        val details = levelMap[key]
        return loadLevel(details ?: return mutableSetOf()) ?: mutableSetOf()
    }

    fun loadLevel(details: LevelDetails): MutableSet<GameObject>?{

        val stream =
            if(details.edited < startTime){
                gameEngine.resourcesLoader.getStream(details.fileName) ?: return null

            }
            else{
                val file = File(details.fileName)
                file.inputStream()
            }


        return loadLevel(details.worldName, stream)
    }

    fun loadLevel(name: String, stream: InputStream): MutableSet<GameObject> {
        val itemCollection = mutableSetOf<GameObject>()

        val doc = getDocument(stream)
        val path = xPath
        val layersRoot = "PGL/layers/layer"

        val layers = path.evaluate(layersRoot, doc, XPathConstants.NODESET) as NodeList

        val layerIDs: MutableSet<String> = mutableSetOf()
        for (x in 0 until layers.length) {

            val value = getAttribute(layers.item(x).attributes, "id", "")
            layerIDs.add(value)
        }

        layerIDs.forEach layer@ { id ->

            val layer = try{
                id.i
            }
            catch (e: java.lang.NumberFormatException){
                PixelEngine.logger.warn("Could not parse layer $id in level $name")
                e.printStackTrace()
                return@layer
            }

            val itemsRoot = "$layersRoot[@id = $id]/item"

            val items = path.evaluate(itemsRoot, doc, XPathConstants.NODESET) as NodeList

            val itemIDs: MutableSet<String> = mutableSetOf()
            for (x in 0 until items.length) {
                val value = getAttribute(items.item(x).attributes, "name", "")
                itemIDs.add(value)
            }

            itemIDs.forEach item@ { item ->

                val itemRoot = "$itemsRoot[contains(@name, '$item')]"
                val itemType = GameObjects.getAllItems()[item] ?: return@item

                // Read the Sources
                val sources = DataSource.readAllDataFromXML(itemRoot, doc, path)

                // Get the input data as maps of strings associated with strings
                val samplerRoot = "$itemRoot/sampler"
                val translationInput = getAllAttributes(doc, "$samplerRoot/input[@semantic = 'TRANSLATION']", path)
                val rotationInput = getAllAttributes(doc, "$samplerRoot/input[@semantic = 'ROTATION']", path)
                val scaleInput = getAllAttributes(doc, "$samplerRoot/input[@semantic = 'SCALE']", path)

                val translationsSource = sources.firstOrNull { it.attributes[id] == translationInput["source"]?.removePrefix("#") }
                val rotationsSource = sources.firstOrNull { it.attributes[id] == rotationInput["source"]?.removePrefix("#") }
                val scalesSource = sources.firstOrNull { it.attributes[id] == scaleInput["source"]?.removePrefix("#") }

                val translationsList = translationsSource?.createVec2Array() ?: arrayOf()
                val rotationsList = rotationsSource?.createFloatArray("ROTATION") ?: floatArrayOf()
                val scalesList = scalesSource?.createVec2Array() ?: arrayOf()

                val total = translationsList.size.min(rotationsList.size).min(scalesList.size)
                for (i in 0..<total) {
                    val copyItem = itemType()
                    copyItem.position = translationsList[i]
                    copyItem.rotation = rotationsList[i]
                    copyItem.scale = scalesList[i]
                    copyItem.depth = layer

                    itemCollection.add(copyItem)
                }
            }
        }

        return itemCollection
    }

    private fun getDocument(stream: InputStream): Document{
        val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        return builder.parse(stream)
    }

    fun deleteLevel(details: LevelDetails){

        val root = "src/main/resources/"
        val filepath = "$root${details.fileName.removePrefix(root)}"
        val file = File(filepath)
        file.delete()

        levelMap.entries.removeIf { entry ->
            entry.value.worldName == details.worldName
        }
    }

    fun getStringAt(root: String, doc: Document, path: XPath = xPath): String{
        return path.evaluate(root, doc, XPathConstants.STRING) as String
    }

    companion object {
        val INSTANCE: LevelLoader = LevelLoader()
        val xPath: XPath = XPathFactory.newInstance().newXPath()

        fun getAllLevels() = INSTANCE.levelMap.values.toList()
        fun loadLevel(key: ResourceKey) = INSTANCE.loadLevel(key)
    }
}