package com.pineypiney.game_engine.level_editor.resources.levels

import com.pineypiney.game_engine.level_editor.LevelMakerScreen
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D
import com.pineypiney.game_engine.util.extension_functions.addToListOr
import com.pineypiney.game_engine.util.extension_functions.getOrSet
import com.pineypiney.game_engine.util.extension_functions.round
import glm_.vec2.Vec2
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.nio.ByteBuffer
import java.time.LocalDateTime
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class LevelWriter(val level: LevelMakerScreen) {

    fun writeToFile(filepath: String) = writeToFile(File(filepath))

    fun writeToFile(file: File){

        if(!file.exists()){
            file.parentFile.mkdirs()
        }

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        writeToDoc(doc)

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        transformer.transform(DOMSource(doc), StreamResult(file))

        LevelLoader.INSTANCE.loadLevels(mapOf(Pair(file.path, file.inputStream())))
    }

    private fun writeToDoc(doc: Document){

        val rootElement = doc.createElement("PGL")
        rootElement.setAttribute("version", "1.0")

        val detailsElement = doc.createElement("level")

        val nameElement = writeStringElement(doc, "name", level.worldName)
        detailsElement.appendChild(nameElement)
        val widthElement = writeStringElement(doc, "width", level.width.toString())
        detailsElement.appendChild(widthElement)

        val colour = this.level.colour
        val colourElement = writeStringElement(doc, "colour", colour.array.joinToString(" ") { f -> f.round(2).toString() })
        detailsElement.appendChild(colourElement)

        val iconElement = writeStringElement(doc, "icon", "broke")
        detailsElement.appendChild(iconElement)


        val formatter = LevelDetails.formatter

        val createdElement = writeStringElement(doc, "creation-date", level.creationDate.format(formatter))
        detailsElement.appendChild(createdElement)
        val editedElement = writeStringElement(doc, "edit-date", LocalDateTime.now().format(formatter))
        detailsElement.appendChild(editedElement)



        rootElement.appendChild(detailsElement)

        val layersElement = doc.createElement("layers")

        val itemsByDepth = mutableMapOf<Int, MutableSet<GameObject2D>>()
        level.gameObjects.gameItems.filterIsInstance<GameObject2D>().forEach {
            itemsByDepth.addToListOr(it.depth, it){ mutableSetOf() }
        }

        itemsByDepth.forEach{ (layer, items) ->
            val layerElement = doc.createElement("layer")
            layerElement.setAttribute("id", layer.toString())

            val transformsMap: MutableMap<String, MutableList<Transform2D>> = mutableMapOf()
            items.forEach {item ->
                val transform = Transform2D(item.position, item.rotation, item.scale)
                transformsMap.getOrSet(item.name){ mutableListOf() }.add(transform)
            }

            transformsMap.forEach{ (name, transforms) ->
                val itemElement = doc.createElement("item")
                itemElement.setAttribute("name", name)

                // Create Source
                kotlin.run {
                    val size = transforms.size

                    val translationsArray = transforms.joinToString(" ") { t -> vec2String(t.position) }
                    val translationsSource = writeSource(doc, "$name-translations", translationsArray, size, 2, "X", "float", "Y", "float")

                    val rotationsArray = transforms.joinToString(" ") { t -> t.rotation.toString() }
                    val rotationSource = writeSource(doc, "$name-rotations", rotationsArray, size, 1, "ROTATION", "float")

                    val scalesArray = transforms.joinToString(" ") { t -> vec2String(t.scale) }
                    val scaleSource = writeSource(doc, "$name-scales", scalesArray, size, 2, "X", "float", "Y", "float")

                    itemElement.appendChild(translationsSource)
                    itemElement.appendChild(rotationSource)
                    itemElement.appendChild(scaleSource)

                    // Sampler

                    val sampler = doc.createElement("sampler")
                    sampler.setAttribute("id", "$name-sampler")

                    addInput(doc, sampler, "TRANSLATION", "#$name-translations")
                    addInput(doc, sampler, "ROTATION", "#$name-rotations")
                    addInput(doc, sampler, "SCALE", "#$name-scales")

                    itemElement.appendChild(sampler)
                }
                layerElement.appendChild(itemElement)
            }
            layersElement.appendChild(layerElement)
        }
        rootElement.appendChild(layersElement)
        doc.appendChild(rootElement)

    }

    fun writeStringElement(doc: Document, name: String, value: String): Node{
        val element = doc.createElement(name)
        element.appendChild(doc.createTextNode(value))
        return element
    }

    fun writeSource(doc: Document, name: String, array: String, count: Int, stride: Int, vararg params: String): Node{
        val sourceElement = doc.createElement("source")
        sourceElement.setAttribute("id", name)

        val arrayElement = doc.createElement("float_array")
        arrayElement.setAttribute("id", "$name-array")
        arrayElement.setAttribute("count", (count * stride).toString())
        arrayElement.appendChild(doc.createTextNode(array))
        sourceElement.appendChild(arrayElement)

        val technique = doc.createElement("technique_common")
        val accessor = doc.createElement("accessor")
        accessor.setAttribute("source", "#$name-array")
        accessor.setAttribute("count", count.toString())
        accessor.setAttribute("stride", stride.toString())

        var i = 0
        while(i < params.size){
            val paramX = doc.createElement("param")
            paramX.setAttribute("name", params[i])
            paramX.setAttribute("type", params[i + 1])
            accessor.appendChild(paramX)

            i += 2
        }

        technique.appendChild(accessor)
        sourceElement.appendChild(technique)

        return sourceElement
    }

    fun addInput(doc: Document, parent: Node, semantic: String, source: String){
        val tranInput = doc.createElement("input")
        tranInput.setAttribute("semantic", semantic)
        tranInput.setAttribute("source", source)
        parent.appendChild(tranInput)
    }

    fun setIcon(texture: Int){

        // PNG_HEADER information can be found here:
        // https://en.wikipedia.org/wiki/Portable_Network_Graphics#File_header
        val PNG_HEADER_SIZE = 8
        // The -0x77 is acting as 0x89 because Kotlin bytes are always signed
        val PNG_HEADER = ByteBuffer.wrap(byteArrayOf(-0x77, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))

    }

    fun vec2String(vec: Vec2) = "${vec.x} ${vec.y}"
}