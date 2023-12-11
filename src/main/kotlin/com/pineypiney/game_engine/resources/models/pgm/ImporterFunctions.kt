package com.pineypiney.game_engine.resources.models.pgm

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.d
import glm_.f
import glm_.i
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants


/**
 * A function to get the value of an attribute called [att] cast to [T] from the first instance that fits the expression [ex]
 *
 * @param doc The Document being read
 * @param ex The expression for the location of the attribute
 * @param att The name of the attribute
 * @param default The value to return if no matching attribute can be found
 *
 * @return The value of the attribute cast to [T], or [default] if no attribute is found
 */
inline fun <reified T> getFirstAttribute(doc: Document, ex: String, att: String, default: T, path: XPath = ModelLoader.xPath): T{
    val attributes = getFirstAttributes(doc, ex, path) ?: return default
    return getAttribute(attributes, att, default)
}

fun getFirstAttributes(doc: Document, ex: String, path: XPath = ModelLoader.xPath): NamedNodeMap?{
    val nodes = path.evaluate(ex, doc, XPathConstants.NODESET) as NodeList
    return nodes.item(0)?.attributes
}

inline fun <reified T> getAttribute(attributes: NamedNodeMap?, att: String, default: T): T{

    val a: String = attributes?.getNamedItem(att)?.nodeValue ?: return default

    return convertString(a, default)
}

fun getAttributes(attributes: NodeList, attribute: String = "id"): Array<String>{
    return Array(attributes.length) { getAttribute(attributes.item(it).attributes, attribute, "") }
}

fun getAllAttributes(doc: Document, ex: String, path: XPath = ModelLoader.xPath): Map<String, String>{
    val attributes = getFirstAttributes(doc, ex, path) ?: return mapOf()
    val nodes = List<Node>(attributes.length) {attributes.item(it)}
    return nodes.associate { Pair(it.nodeName, it.nodeValue) }
}

fun getSource(errorMsg: String, source: String, ex: String, doc: Document, path: XPath = ModelLoader.xPath): Pair<String, String>?{
    val input = getAllAttributes(doc, ex, path)
    val sourceId = input["source"] ?: run{
        return null
    }
    val sourcePair = getAttributeIdentifier(sourceId) ?: run{
        GameEngineI.warn("$errorMsg because $source source $sourceId could be parsed")
        return null
    }
    return sourcePair
}

inline fun <reified T> getArray(sources: List<DataSource>, errorMsg: String, source: String, ex: String, doc: Document, path: XPath = ModelLoader.xPath, assembler: (DataSource) -> Array<T>): Array<T>?{

    val verticesSourcePair = getSource(errorMsg, source, ex, doc, path) ?: return null
    val verticesSource: DataSource = sources.firstOrNull { it pointedBy verticesSourcePair } ?: return arrayOf()
    return assembler(verticesSource)
}

fun getAttributeIdentifier(s: String): Pair<String, String>?{
    return when(s[0]){
        '#' -> "id" to s.substring(1)
        '.' -> "class" to s.substring(1)
        '@' -> s.substring(1).substringBefore('=').replaceWhiteSpaces() to s.substringAfter('\'').removeSuffix("\'")
        else -> null
    }
}

inline fun <reified T> convertString(string: String, default: T): T{
    if(default is String) return string as T
    return try{
        T::class.java.cast(when(T::class.java){
            Integer::class.java -> string.i
            java.lang.Float::class.java -> string.f
            java.lang.Double::class.java -> string.d
            else -> default
        })
    }
    catch(e: ClassCastException){
        GameEngineI.warn("Could not cast $string to ${T::class.simpleName}")
        default
    }
}