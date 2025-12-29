package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.Components
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.getAllFieldsExt
import com.pineypiney.game_engine.objects.prefabs.*
import com.pineypiney.game_engine.resources.readString
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.NodeTree
import glm_.asHexString
import glm_.getInt
import glm_.int
import glm_.short
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class GameObjectSerializer {

	companion object {

		fun lengthAndString(string: String, lengthBytes: Int = 4) = ByteData.int2String(string.length, lengthBytes) + string

		// SERIALISATION -------------------------------------------------------------------------------

		fun serialise(obj: GameObject): String {
			val h = StringBuilder()
			val d = StringBuilder()
			serialiseParts(obj, h, d)
			return ByteData.int2String(h.length) + h.toString() + d.toString()
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
			head.append("lay${ByteData.int2String(obj.layer)}")

			val parts = mutableListOf<Pair<StringBuilder, StringBuilder>>()
			if (obj.components.isNotEmpty()) parts.add(addListPart("COMP", obj.components, ComponentI::serialise))
			if (obj.children.isNotEmpty()) parts.add(addListPart("CHLD", obj.children, ::serialiseParts))

			head.append(ByteData.int2String(parts.size))
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
			val head = StringBuilder(name + ByteData.int2String(list.size))
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
			if(prefab.layer != template.layer) edits.add(PrefabFieldEdit(chain, "l", prefab.layer.toString(), null))
			if(prefab.active != template.active) edits.add(PrefabFieldEdit(chain, "a", if(prefab.active) "\u0001" else "\u0000", null))

			// This is a list of all template components that will be removed
			// as they are checked against the prefab's components
			val tempComps = template.components.map { it.id }.toMutableList()

			for(editedComp in prefab.components){
				val tempComp = template.getComponent(editedComp.id)

				// If the template also has the component then only any fields that have been modified need to be serialised
				if(tempComp != null){
					tempComps.remove(tempComp.id)
					val fields = editedComp.getAllFieldsExt()
					val tempFields = tempComp.getAllFieldsExt()
					for(editedField in fields){
						val tempField = tempFields.first { it.id == editedField.id }
						val serialised = editedField.serialiseValue(editedComp)
						// If the serialisation of the templates field is different then save it as a change
						if(serialised != tempField.serialiseValue(tempComp)){
							val field = editedComp.id + '.' + editedField.id
							edits.add(PrefabFieldEdit(chain, field, serialised, null))
						}
					}
				}
				// If the template did not have this component then it needs to be saved as a new component
				else{
					val head = StringBuilder()
					val data = StringBuilder()
					editedComp.serialise(head, data)
					edits.add(PrefabComponentAddEdit(chain, head.toString(), data.toString(), null))
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

		fun parse(stream: InputStream, dest: GameObject? = null, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>> = mutableListOf(), parseLate: Boolean = true): GameObject {
			try {
				val headLen = stream.int()
				val head = stream.readNBytes(headLen).inputStream()
				val o = parseChild(head, stream, lateParse, dest, parseLate)
				if(parseLate) {
					for ((comp, field, str) in lateParse) field.set(str, comp)
				}
				return o
			} catch (e: Exception) {
				GameEngineI.logger.error("Failed to parse GameObject:")
				e.printStackTrace()
				return GameObject("Womp Womp")
			}
		}

		fun parseChild(head: InputStream, data: InputStream, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>>, dest: GameObject? = null, parseLate: Boolean): GameObject {
			val type = head.short()
			return when(type){
				0xdefa -> parseDefaultObject(head, data, lateParse, dest, parseLate)
				0xefab -> parsePrefab(head, data, lateParse, dest, parseLate)
				else -> {
					GameEngineI.logger.error("Couldn't parse game object type ${type.asHexString}, should be 0xdefa for normal game object or 0xefab for a prefab")
					dest ?: GameObject()
				}
			}
		}

		fun parseDefaultObject(head: InputStream, data: InputStream, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>>, dest: GameObject? = null, parseLate: Boolean): GameObject{
			val nameLen =
				head.readNBytes(2).reversed().withIndex().sumOf { (index, byte) -> (byte.toInt() shl (index * 8)) }
			val name = head.readString(nameLen)
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

			repeat(partCount) {
				val type = head.readString(4)
				when (type) {
					"COMP" -> parseComponents(head, data, lateParse, o)
					"CHLD" -> {
						var childrenSize = head.int()
						while (childrenSize-- > 0) {
							o.addChild(parseChild(head, data, lateParse, null, parseLate))
						}
					}
				}
			}


			return o
		}

		fun parseComponents(head: InputStream, data: InputStream, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>>, parent: GameObject) {
			val componentCount = head.int()
			repeat(componentCount) { parseComponent(head, data, lateParse, parent) }
		}

		fun parseComponent(head: InputStream, data: InputStream, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>>, parent: GameObject){
			val nameSize = head.read()
			val componentName = head.readString(nameSize)
			val component = Components.createComponent(componentName, parent)
			val numFields = head.read()

			if (component == null) {
				repeat(numFields) {
					head.skipNBytes(head.read().toLong())
					data.skipNBytes(head.int().toLong())
				}
				return
			}

			val fields = component.getAllFieldsExt()
			repeat(numFields) { parseField(head, data, component, fields, lateParse) }
			parent.components.add(component)
		}

		fun parseField(head: InputStream, data: InputStream, component: ComponentI, fields: Set<ComponentField<*>>, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>>){
			val fieldNameSize = head.read()
			val fieldName = head.readString(fieldNameSize)
			val fieldSize = head.int()
			val value = data.readString(fieldSize)
			val field = fields.firstOrNull { it.id == fieldName } ?: return
			if(field.isLateParse()) lateParse.add(Triple(component, field, value))
			else field.set(value, component)
		}

		fun parsePrefab(head: InputStream, data: InputStream, lateParse: MutableList<Triple<ComponentI, ComponentField<*>, String>>, dest: GameObject? = null, parseLate: Boolean): GameObject{
			val fileLength = head.short()
			val file = File(head.readString(fileLength))
			val o = dest as? Prefab ?: Prefab(file)
			parse(file.inputStream(), o, lateParse, false)

			val nameLength = head.read()
			val name = head.readString(nameLength)
			o.name = name

			if(head.available() > 0){
				o.edits.addAll(parseNode("", head, data, if(parseLate) lateParse else null))
			}

			for(edit in o.edits) edit.execute(o)

			return o
		}

		fun parseNode(parent: String, head: InputStream, data: InputStream, lateParse: LateParse?): List<PrefabEdit>{

			val nodeNameLength = head.read()
			val nodeName = if(nodeNameLength == 0) "" else head.readNBytes(nodeNameLength).toString(Charsets.ISO_8859_1)
			val parentLoc = if(parent.isEmpty()) nodeName else "$parent$$nodeName"

			val list = mutableListOf<PrefabEdit>()
			val parts = head.read()
			repeat(parts){
				val type = head.readString(4)
				when(type){
					"NODE" -> {
						val numNodes = head.int()
						repeat(numNodes){
							val editType = head.readString(4)
							when(editType){
								"FLED" -> list.add(PrefabFieldEdit(parentLoc, head.readNBytes(head.short()).toString(Charsets.ISO_8859_1), data.readNBytes(head.int()).toString(Charsets.ISO_8859_1), lateParse))
								"CPAD" -> list.add(PrefabComponentAddEdit(parentLoc, head.readNBytes(head.int()).toString(Charsets.ISO_8859_1), data.readNBytes(head.int()).toString(Charsets.ISO_8859_1), lateParse))
								"CPRM" -> list.add(PrefabComponentRemoveEdit(parentLoc, data.readNBytes(head.read()).toString(Charsets.ISO_8859_1)))
								"CHAD" -> list.add(PrefabChildAddEdit(parentLoc, data.readNBytes(head.int()).toString(Charsets.ISO_8859_1)))
								"CHRM" -> list.add(PrefabChildRemoveEdit(parentLoc, data.readNBytes(head.read()).toString(Charsets.ISO_8859_1)))
							}
						}
					}
					"CHLD" -> repeat(head.int()) { list.addAll(parseNode(parentLoc, head, data, lateParse)) }
				}
			}
			return list
		}

		fun parseScene(stream: InputStream, list: LateParse? = null): List<GameObject>{
			val numObjects = stream.int()
			return List(numObjects) {
				try {
					val objSize = stream.int()
					val objData = stream.readNBytes(objSize)
					if(list == null) parse(ByteArrayInputStream(objData))
					else parse(ByteArrayInputStream(objData), null, list, false)
				} catch (_: Exception) {
					null
				}
			}.filterNotNull()
		}
	}
}

typealias LateParse = MutableList<Triple<ComponentI, ComponentField<*>, String>>