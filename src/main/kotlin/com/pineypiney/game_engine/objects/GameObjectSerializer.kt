package com.pineypiney.game_engine.objects

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.Components
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.getAllFieldsExt
import com.pineypiney.game_engine.objects.prefabs.*
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.NodeTree
import com.pineypiney.game_engine.util.extension_functions.string
import com.pineypiney.game_engine.util.serialisation.JsonOps
import com.pineypiney.game_engine.util.serialisation.SerialOps
import glm_.asHexString
import java.io.File

class GameObjectSerializer {

	companion object {

		// SERIALISATION -------------------------------------------------------------------------------

		fun <E> serialise(obj: GameObject, ops: SerialOps<E>): E {

			if (obj is Prefab) return serialisePrefab(obj, ops)

			val head = ops.createMap()
			ops.put(head, "type", ByteData.int2String(0xdefa, 2))
			ops.put(head, "name", obj.name)
			ops.put(head, "active", obj.active)
			ops.put(head, "layer", obj.layer)

			if (obj.components.isNotEmpty()) addListPart("components", obj.components, head, ops, ComponentI::encode)
			if (obj.children.isNotEmpty()) addListPart("children", obj.children, head, ops, ::serialise)

			return head
		}

		private fun <T, E> addListPart(
			name: String,
			list: Collection<T>,
			head: E,
			ops: SerialOps<E>,
			transform: (T, SerialOps<E>) -> E
		) {
			val array = ops.createArray()
			ops.appendMap(head, name, array)
			list.forEach { ops.appendArray(array, transform(it, ops)) }
		}

		private fun <E> serialisePrefab(prefab: Prefab, ops: SerialOps<E>): E {

			val head = ops.createMap()
			ops.put(head, "type", ByteData.int2String(0xefab, 2))
			ops.put(head, "path", prefab.file.path)
			ops.put(head, "name", prefab.name)

			val template = prefab.parse()
			val edits = mutableListOf<Pair<String, PrefabEdit>>()
			getEdits(prefab, template, "", edits)
			val tree = NodeTree.createFrom(edits, Pair<String, *>::first, '$')

			if (tree.nodes.isNotEmpty()) {
				val nodeArray = ops.createArray()
				ops.appendMap(head, "edits", nodeArray)
				for (node in tree.nodes) {
					ops.appendArray(nodeArray, serialisePrefabEdit(node, ops))
				}
			}

			return head
		}

		private fun <E> serialisePrefabEdit(node: NodeTree.Node<Pair<String, PrefabEdit>>, ops: SerialOps<E>): E {

			val head = ops.createMap()
			ops.put(head, "id", node.id)

			when(node){
				is NodeTree.ListNode -> {
					addListPart("node", node.items.map { it.second }, head, ops, PrefabEdit::serialise)
				}

				is NodeTree.ItemNode -> {
					addListPart("node", listOf(node.item.second), head, ops, PrefabEdit::serialise)
				}
			}
			if (node.children.isNotEmpty()) addListPart("children", node.children, head, ops, ::serialisePrefabEdit)

			return head
		}

		private fun getEdits(prefab: GameObject, template: GameObject, chain: String, edits: MutableList<Pair<String, PrefabEdit>>) {
			if (prefab.layer != template.layer) edits.add(chain to PrefabFieldEdit("l", JsonPrimitive(template.layer)))
			if (prefab.active != template.active) edits.add(chain to PrefabFieldEdit("a", JsonPrimitive(template.active)))

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
						val serialised = editedField.encode(JsonOps)
						// If the serialisation of the templates field is different then save it as a change
						if (serialised != tempField.encode(JsonOps)) {
							val field = editedComp.id + '.' + editedField.id
							edits.add(chain to PrefabFieldEdit(field, serialised))
						}
					}
				}
				// If the template did not have this component then it needs to be saved as a new component
				else{
					val json = editedComp.encode(JsonOps)
					edits.add(chain to PrefabComponentAddEdit(json))
				}
			}
			// Any components left in this list are components that were deleted in the prefab instance
			for(i in tempComps){
				edits.add(chain to PrefabComponentRemoveEdit(i))
			}

			// This is a list of all template children that will be removed
			// as they are checked against the prefab's children
			val tempChildren = template.children.map { it.name }.toMutableList()
			for(i in prefab.children){
				val other = template.getChild(i.name)
				// If the template also has the child then the child's changes should also be saved
				if(other != null) {
					val newChain = if(chain.isEmpty()) i.name else chain + '$' + i.name
					getEdits(i, other, newChain, edits)
					tempChildren.remove(i.name)
				}
				// If the template did not have this child then it needs to be saved as a new child
				else{
					val json = serialise(i, JsonOps)
					edits.add(chain to PrefabChildAddEdit(json))
				}
			}
			// Any components left in this list are components that were deleted in the prefab instance
			for(i in tempChildren){
				edits.add(chain to PrefabChildRemoveEdit(i))
			}
		}

		// PARSING -------------------------------------------------------------------------------------

		fun parse(file: File, dst: GameObject? = null): GameObject {
			val json = JsonParser.parseString(file.readText(Charsets.ISO_8859_1))
			return parse(JsonOps, json, dst)
		}

		fun <E> parse(ops: SerialOps<E>, head: E, dst: GameObject? = null): GameObject {
			val lateParse = mutableListOf<Triple<Any, ComponentField<*>, E>>()
			val obj = parse(ops, head, dst, lateParse)
			for ((_, field, data) in lateParse) field.set(ops, data)
			return obj
		}

		fun <E> parse(ops: SerialOps<E>, head: E, dst: GameObject?, lateParse: LateParse<E>): GameObject {
			try {
				val o = parseChild(ops, head, lateParse, dst)
				return o
			} catch (e: Exception) {
				GameEngineI.logger.error("Failed to parse GameObject:")
				e.printStackTrace()
				return GameObject("Womp Womp")
			}
		}

		fun <E> parseChild(ops: SerialOps<E>, head: E, lateParse: LateParse<E>, dest: GameObject? = null): GameObject {
			return when (val type = ByteData.string2Int(ops.getString(head, "type"))) {
				0xdefa -> parseDefaultObject(ops, head, lateParse, dest)
				0xefab -> parsePrefab(ops, head, lateParse, dest)
				else -> {
					GameEngineI.logger.error("Couldn't parse game object type ${type.asHexString}, should be 0xdefa for normal game object or 0xefab for a prefab")
					dest ?: GameObject()
				}
			}
		}

		fun <E> parseDefaultObject(ops: SerialOps<E>, head: E, lateParse: LateParse<E>, dest: GameObject? = null): GameObject {
			val name = ops.getString(head, "name")
			val o: GameObject
			if(dest == null) o = GameObject(name)
			else {
				dest.name = name
				o = dest
			}

			o.active = ops.getBool(head, "active")
			o.layer = ops.getInt(head, "layer")

			val components = ops.getChild(head, "components")
			parseComponents(ops, components, lateParse, o)

			val children = ops.getChild(head, "children")
			ops.forEach(children) { child ->
				o.addChild(parseChild(ops, child, lateParse, null))
			}

			return o
		}

		fun <E> parseComponents(ops: SerialOps<E>, head: E, lateParse: LateParse<E>, parent: GameObject) {
			ops.forEach(head) { component ->
				parseComponent(ops, component, lateParse, parent)
			}
		}

		fun parseComponent(data: ByteArray, lateParse: LateParse<ByteArray>, parent: GameObject): ComponentI? {
			val stream = data.inputStream()
			val nameLength = stream.read()
			val componentName = stream.string(nameLength)
			val component = Components.createComponent(componentName, parent) ?: return null

			component.decode(stream, lateParse)
			stream.close()
			parent.components.add(component)
			return component
		}

		fun <E> parseComponent(ops: SerialOps<E>, head: E, lateParse: LateParse<E>, parent: GameObject): ComponentI? {
			val componentName = ops.getString(head, "name")
			val component = Components.createComponent(componentName, parent) ?: return null

			component.decode(ops, head, lateParse)
			parent.components.add(component)
			return component
		}

		fun <E> parsePrefab(ops: SerialOps<E>, head: E, lateParse: LateParse<E>, dest: GameObject? = null): GameObject {
			val path = ops.getString(head, "path")
			val file = File(path)
			val o = dest as? Prefab ?: Prefab(file)

			parse(ops, ops.parse(file.reader(Charsets.ISO_8859_1)), o, lateParse)

			o.name = ops.getString(head, "name")

			val edits = ops.getChild(head, "edits")
			ops.forEach(edits) { edit ->
				o.edits.addAll(parsePrefabEdit("", ops, edit, lateParse))
			}

			@Suppress("UNCHECKED_CAST")
			for ((loc, edit) in o.edits) edit.execute(o, loc, lateParse as LateParse<JsonElement>)

			return o
		}

		fun <E> parsePrefabEdit(parent: String, ops: SerialOps<E>, head: E, lateParse: LateParse<E>): List<Pair<String, PrefabEdit>> {

			val nodeName = ops.getString(head, "id")
			val parentLoc = if(parent.isEmpty()) nodeName else "$parent$$nodeName"

			val list = mutableListOf<Pair<String, PrefabEdit>>()

			val node = ops.getChild(head, "node")
			for (edit in ops.iterator(node)) {
				list.add(parentLoc to (PrefabEdit.CODEC.decode(ops, edit) ?: continue))
			}

			val children = ops.getChild(head, "children")
			ops.forEach(children) { child ->
				list.addAll(parsePrefabEdit(parentLoc, ops, child, lateParse))
			}
			return list
		}
	}
}

typealias LateParse<D> = MutableList<Triple<Any, ComponentField<*>, D>>