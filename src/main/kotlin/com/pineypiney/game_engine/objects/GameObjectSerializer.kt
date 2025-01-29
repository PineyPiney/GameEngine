package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.Components
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.getAllFieldsExt
import com.pineypiney.game_engine.objects.prefabs.*
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.NodeTree
import com.pineypiney.game_engine.util.extension_functions.toByteString
import glm_.getInt
import glm_.int
import glm_.short
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

class GameObjectSerializer {

	companion object {

		fun lengthAndString(string: String, lengthBytes: Int = 4) = ByteData.int2String(string.length, lengthBytes) + string

		// SERIALISATION -------------------------------------------------------------------------------

		fun serialise(obj: GameObject): String {
			val h = StringBuilder()
			val d = StringBuilder()
			serialiseParts(obj, h, d)
			return h.length.toByteString() + h.toString() + d.toString()
		}

		private fun serialiseParts(
			obj: GameObject,
			head: StringBuilder = StringBuilder(),
			data: StringBuilder = StringBuilder()
		) {
			if(obj is Prefab) return serialisePrefab(obj, head, data)
			head.append(ByteData.int2String(0xdefa, 2))
			head.append(lengthAndString(obj.name, 2))
			head.append("act" + if (obj.active) 1.toChar() else 0.toChar())
			head.append("lay${obj.layer.toByteString()}")

			val parts = mutableListOf<Pair<StringBuilder, StringBuilder>>()
			if (obj.components.isNotEmpty()) parts.add(addListPart("COMP", obj.components, ComponentI::serialise))
			if (obj.children.isNotEmpty()) parts.add(addListPart("CHLD", obj.children, ::serialiseParts))

			head.append(parts.size.toByteString())
			for ((h, d) in parts) {
				head.append(h)
				data.append(d)
			}
		}

		private fun <T> addListPart(
			name: String,
			list: Collection<T>,
			transform: (T, StringBuilder, StringBuilder) -> Unit
		): Pair<StringBuilder, StringBuilder> {
			val head = StringBuilder(name + list.size.toByteString())
			val data = StringBuilder()
			list.forEach { transform(it, head, data) }
			return head to data
		}

		private fun serialisePrefab(prefab: Prefab, head: StringBuilder = StringBuilder(), data: StringBuilder = StringBuilder()){

			head.append(ByteData.int2String(0xefab, 2))
			head.append(lengthAndString(prefab.file.path, 2))
			head.append(lengthAndString(prefab.name, 1))
			val template = prefab.parse()
			val edits = mutableListOf<PrefabEdit>()
			getChanges(prefab, template, "", edits)
			val tree = NodeTree.createFrom(edits, PrefabEdit::parentLoc, '$')
			for(node in tree.nodes){
				serialiseNode(node, head, data)
			}
		}

		private fun serialiseNode(node: NodeTree.Node<PrefabEdit>, head: StringBuilder, data: StringBuilder){

			head.append(lengthAndString(node.id, 1))

			val parts = mutableListOf<Pair<StringBuilder, StringBuilder>>()
			when(node){
				is NodeTree.ListNode -> { parts.add(addListPart("NODE", node.items, PrefabEdit::serialise))}
				is NodeTree.ItemNode -> { parts.add(addListPart("NODE", listOf(node.item), PrefabEdit::serialise))}
			}
			if(node.children.isNotEmpty()) parts.add(addListPart("CHLD", node.children, ::serialiseNode))

			head.append(ByteData.int2String(parts.size, 1))
			for ((h, d) in parts) {
				head.append(h)
				data.append(d)
			}
		}

		private fun getChanges(prefab: GameObject, template: GameObject, chain: String, edits: MutableList<PrefabEdit>){
			// This is a list of all template components that will be removed
			// as they are checked against the prefab's components
			val tempComps = template.components.map { it.id }.toMutableList()
			for(i in prefab.components){
				val tempComp = template.getComponent(i.id)
				val fields = i.getAllFieldsExt()
				// If the template also has the component then only any fields that have been modified need to be serialised
				if(tempComp != null){
					tempComps.remove(tempComp.id)
					val tempFields = tempComp.getAllFieldsExt()
					for(f in fields){
						val tempField = tempFields.first { it.id == f.id }
						val serialised = f.serialiseValue()
						// If the serialisation of the templates field is different then save it as a change
						if(serialised != tempField.serialiseValue()){
							val field = i.id + '.' + f.id
							edits.add(PrefabFieldEdit(chain, field, serialised))
						}
					}
				}
				// If the template did not have this component then it needs to be saved as a new component
				else{
					val head = StringBuilder()
					val data = StringBuilder()
					i.serialise(head, data)
					edits.add(PrefabComponentAddEdit(chain, head.toString(), data.toString()))
				}
			}
			// Any components left in this list are components that were deleted in the prefab instance
			for(i in tempComps){
				edits.add(PrefabComponentRemoveEdit(chain, i))
			}

			// This is a list of all template children that will be removed
			// as they are checked against the prefab's children
			val tempChildren = template.children.map { it.name }.toMutableList()
			for(i in prefab.children){
				val other = template.getChild(i.name)
				// If the template also has the child then the child's changes should also be saved
				if(other != null) {
					val newChain = if(chain.isEmpty()) i.name else chain + '$' + i.name
					getChanges(i, other, newChain, edits)
					tempChildren.remove(i.name)
				}
				// If the template did not have this child then it needs to be saved as a new child
				else{
					edits.add(PrefabChildAddEdit(chain, serialise(i)))
				}
			}
			// Any components left in this list are components that were deleted in the prefab instance
			for(i in tempChildren){
				edits.add(PrefabChildRemoveEdit(chain, i))
			}
		}

		// PARSING -------------------------------------------------------------------------------------

		fun parse(stream: InputStream, dest: GameObject? = null): GameObject {
			try {
				val headLen = stream.int()
				val head = stream.readNBytes(headLen).inputStream()
				return parseChild(head, stream, dest)
			} catch (e: Exception) {
				return GameObject("Womp Womp")
			}
		}

		fun parseChild(head: InputStream, data: InputStream, dest: GameObject? = null): GameObject {
			val type = head.short()
			return when(type){
				0xdefa -> parseDefaultObject(head, data, dest)
				0xefab -> parsePrefab(head, data, dest)
				else -> dest ?: GameObject()
			}
		}

		fun parseDefaultObject(head: InputStream, data: InputStream, dest: GameObject? = null): GameObject{
			val nameLen =
				head.readNBytes(2).reversed().withIndex().sumOf { (index, byte) -> (byte.toInt() shl (index * 8)) }
			val name = head.readNBytes(nameLen).toString(Charset.defaultCharset())
			val o: GameObject
			if(dest == null) o = GameObject(name)
			else {
				dest.name = name
				o = dest
			}

			val active = head.readNBytes(4)
			val layer = head.readNBytes(7)

			o.active = active[3] > 0
			o.layer = layer.getInt(3)

			val partCount = head.int()

			for (i in 1..partCount) {
				val type = head.readNBytes(4).toString(Charset.defaultCharset())
				when (type) {
					"COMP" -> parseComponents(head, data, o)
					"CHLD" -> {
						var childrenSize = head.int()
						while (childrenSize-- > 0) {
							o.addChild(parseChild(head, data))
						}
					}
				}
			}


			return o
		}

		fun parseComponents(head: InputStream, data: InputStream, parent: GameObject) {
			val componentCount = head.int()
			for (cn in 1..componentCount) parseComponent(head, data, parent)
		}

		fun parseComponent(head: InputStream, data: InputStream, parent: GameObject){
			val nameSize = head.read()
			val componentName = head.readNBytes(nameSize).toString(Charset.defaultCharset())
			val component = Components.createComponent(componentName, parent)
			val numFields = head.read()

			if (component == null) {
				head.skipNBytes(numFields * 7L)
				return
			}

			val fields = component.getAllFieldsExt()
			for (fn in 1..numFields) parseField(head, data, fields)
			parent.components.add(component)
		}

		fun parseField(head: InputStream, data: InputStream, fields: Set<ComponentField<*>>){
			val fieldNameSize = head.read()
			val fieldName = head.readNBytes(fieldNameSize).toString(Charset.defaultCharset())
			val fieldSize = head.int()
			val value = data.readNBytes(fieldSize).toString(Charsets.ISO_8859_1)
			fields.firstOrNull { it.id == fieldName }?.set(value)
		}

		fun parsePrefab(head: InputStream, data: InputStream, dest: GameObject? = null): GameObject{
			val fileLength = head.short()
			val file = File(head.readNBytes(fileLength).toString(Charset.defaultCharset()))
			val o = dest as? Prefab ?: Prefab(file)
			parse(file.inputStream(), o)

			val nameLength = head.read()
			val name = head.readNBytes(nameLength).toString(Charsets.ISO_8859_1)
			o.name = name

			while(head.available() > 0){
				o.edits.addAll(parseNode("", head, data))
			}

			for(edit in o.edits) edit.execute(o)
			return o
		}

		fun parseNode(parent: String, head: InputStream, data: InputStream): List<PrefabEdit>{

			val nodeNameLength = head.read()
			val nodeName = if(nodeNameLength == 0) "" else head.readNBytes(nodeNameLength).toString(Charsets.ISO_8859_1)
			val parentLoc = if(parent.isEmpty()) nodeName else "$parent$$nodeName"

			val list = mutableListOf<PrefabEdit>()
			val parts = head.read()
			for(i in 1..parts){
				val type = head.readNBytes(4).toString(Charsets.ISO_8859_1)
				when(type){
					"NODE" -> {
						val numNodes = head.int()
						for(j in 1..numNodes){
							val editType = head.readNBytes(4).toString(Charsets.ISO_8859_1)
							when(editType){
								"FLED" -> list.add(PrefabFieldEdit(parentLoc, head.readNBytes(head.short()).toString(Charsets.ISO_8859_1), data.readNBytes(head.int()).toString(Charsets.ISO_8859_1)))
								"CPAD" -> list.add(PrefabComponentAddEdit(parentLoc, head.readNBytes(head.int()).toString(Charsets.ISO_8859_1), data.readNBytes(head.int()).toString(Charsets.ISO_8859_1)))
								"CPRM" -> list.add(PrefabComponentRemoveEdit(parentLoc, data.readNBytes(head.read()).toString(Charsets.ISO_8859_1)))
								"CHAD" -> list.add(PrefabChildAddEdit(parentLoc, data.readNBytes(head.int()).toString(Charsets.ISO_8859_1)))
								"CHRM" -> list.add(PrefabChildRemoveEdit(parentLoc, data.readNBytes(head.read()).toString(Charsets.ISO_8859_1)))
							}
						}
					}
					"CHLD" -> for(i in 1..head.int()) list.addAll(parseNode(parentLoc, head, data))
				}
			}
			return list
		}
	}
}