package com.pineypiney.game_engine.resources.models

import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants

class DataSource(val id: String, val count: Int, val stride: Int, val arrays: Map<String, Array<String>>) {

    operator fun get(index: String): Array<String>{
        return arrays.getOrDefault(index, arrays.getOrDefault(0, arrayOf()))
    }

    operator fun get(index: String, indey: Int): String{
        val array = get(index)
        return array.getOrElse(indey){ array.getOrElse(0) {""} }
    }

    fun createIntArray(s: String = "X"): IntArray{
        val x = arrays[s]
        return if(x == null) intArrayOf()
        else IntArray(x.size){ x[it].i }
    }

    fun createFloatArray(s: String = "X"): FloatArray{
        val x = arrays[s]
        return if(x == null) floatArrayOf()
        else FloatArray(x.size){ x[it].f }
    }

    fun createVec2Array(s: String = "X-Y"): Array<Vec2>{
        val (x, y) = s.split("-").map { arrays[it] }
        return if(x == null || y == null) arrayOf()
        else Array(x.size){ Vec2(x[it].f, y[it].f) }
    }

    fun createVec3Array(s: String = "X-Y-Z"): Array<Vec3>{
        val (x, y, z) = s.split("-").map { arrays[it] }
        return if(x == null || y == null || z == null) arrayOf()
        else Array(x.size){ Vec3(x[it].f, y[it].f, z[it].f) }
    }

    fun createVec4Array(s: String = "X-Y-Z-W"): Array<Vec4>{
        val (x, y, z, w) = s.split("-").map { arrays[it] }
        return if(x == null || y == null || z == null || w == null) arrayOf()
        else Array(x.size){ Vec4(x[it].f, y[it].f, z[it].f, w[it].f) }
    }

    companion object{

        val EMPTY = DataSource("", 0, 0, mapOf())

        @Throws(TypeCastException::class)
        fun readDataFromXML(id: String, root: String, doc: Document, path: XPath = ModelLoader.xPath): DataSource {

            val arrayRoot = "$root[contains(@id, '$id')]/*[@id = '$id-array']"

            val accessorsRoot = "$root[contains(@id, '$id')]/technique_common/accessor[contains(@source, '#$id-array')]"
            // Get information about how to read the data
            val atts = getFirstAttributes(doc, accessorsRoot, path)
            val count = getAttribute(atts, "count", 0)
            val stride = getAttribute(atts, "stride", 0)

            val accessors = path.evaluate("$accessorsRoot/param", doc, XPathConstants.NODESET) as NodeList
            val params = getAttributes(accessors, "name")

            val stringArray = (path.evaluate(arrayRoot, doc, XPathConstants.STRING) as String).split(" ")
            if(stringArray.joinToString { it }.isEmpty() || stringArray.size < count * stride) return EMPTY

            val map: MutableMap<String, Array<String>> = mutableMapOf()
            for(i in IntRange(0, stride - 1)){
                val subList = stringArray.slice(IntRange(0, stringArray.size - 1).filter { it.mod(stride) == i })
                map[params[i]] = subList.toTypedArray()
            }

            return DataSource(id, count, stride, map.toMap())
        }

        @Throws(TypeCastException::class)
        fun readAllDataFromXML(root: String, tag: String, doc: Document, path: XPath = ModelLoader.xPath): List<DataSource> {
            val tagRoot = "$root/$tag"
            val nodes = path.evaluate(tagRoot, doc, XPathConstants.NODESET) as NodeList
            val allNodes = getAttributes(nodes)
            return allNodes.map { readDataFromXML(it, tagRoot, doc, path) }
        }
    }
}