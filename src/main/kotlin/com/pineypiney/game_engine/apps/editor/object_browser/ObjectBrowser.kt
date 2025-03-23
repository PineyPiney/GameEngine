package com.pineypiney.game_engine.apps.editor.object_browser

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.util.extension_functions.firstNotNullOfOrNull
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ObjectBrowser(parent: GameObject, val screen: EditorScreen): DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val objectList = MenuItem("Object List")
	var selected: ObjectNode? = null

	val colour = ChildContainingRenderer(parent, Mesh.cornerSquareShape, Vec3(.7f))

	init {
		parent.components.add(PixelTransformComponent(parent, Vec2i(0f, screen.settings.fileBrowserHeight), Vec2i(screen.settings.objectBrowserWidth, 324), Vec2(-1f)))
		parent.components.add(colour)
	}

	override fun init() {
		super.init()
		parent.addChild(objectList)
		objectList.position = Vec3(0f, 0f, .01f)
	}

	fun addObject(obj: GameObject){
		if(obj.parent == null) addRootObject(obj)
		val lineage = obj.getLineage().reversed()
		var p: ObjectNode = parent.children.firstNotNullOfOrNull({ it.getComponent<ObjectNode>()}) { it.obj == lineage[0] } ?: run { addRootObject(obj); return }
		for(o in lineage.listIterator(1)){
			p = p.parent.children.firstNotNullOfOrNull({it.getComponent<ObjectNode>()}) { it.obj == o } ?: run { p.addChild(o); return }
		}
	}

	fun addChildObject(parentNode: ObjectNode, obj: GameObject){
		obj.parent = parentNode.obj
		addNode(parentNode.parent, obj)
	}

	fun addRootObject(obj: GameObject): ObjectNode{
		screen.sceneObjects.addObject(obj)
		val node = addNode(objectList, obj)
		node.parent.scale = Vec3(.7f, .025f, 1f)

		for(c in obj.children) addFamily(node, c)

		return node
	}

	fun addFamily(parentNode: ObjectNode, obj: GameObject){
		val node = addNode(parentNode.parent, obj)
		for(child in obj.children) addFamily(node, child)
	}

	fun addNode(nodeParent: GameObject, obj: GameObject): ObjectNode{
		val item = MenuItem("${obj.name} node")
		item.pixel(Vec2i(5, -22), Vec2i(screen.settings.objectBrowserWidth * .7f, 20), Vec2(0f, 0f))
		val node = ObjectNode(item, obj)
		item.components.add(node)
		item.components.add(ColourRendererComponent(item, Vec3(.6f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
		nodeParent.addChild(item)

		val textChild = Text.makeMenuText(obj.name, Vec4(0f, 0f, 0f, 1f), 16)
		textChild.position = Vec3(.02f, 0f, .01f)
		textChild.scale = Vec3(.96f, 1f, 1f)
		item.addChild(textChild)

		item.init()
		return node
	}

	fun positionNodes(){
		var y = 0
		for(c in objectList.children.mapNotNull { it.getComponent<ObjectNode>() }){
			y += c.position(y)
		}
	}

	fun setSelectedObject(obj: GameObject){
		val line = obj.getAncestry().reversed()
		var nodeObj = parent
		var i = 0
		while(i < line.size){
			val nextCompare = line.getOrNull(i++) ?: return
			nodeObj = nodeObj.children.firstOrNull { it.getComponent<ObjectNode>()?.obj == nextCompare } ?: return
		}

		selected = nodeObj.children.firstNotNullOfOrNull({it.getComponent<ObjectNode>()}) { it.obj == obj } ?: return
	}

	fun reset(){
		objectList.deleteAllChildren()
		for(o in screen.sceneObjects.map.flatMap { it.value }){
			addRootObject(o)
		}
		positionNodes()
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		if(action == 1) {
			addRootObject(GameObject())
			positionNodes()
		}
		return super.onSecondary(window, action, mods, cursorPos)
	}

	override fun updateAspectRatio(view: Viewport) {
		val trans = parent.getComponent<PixelTransformComponent>()
		val newScale = Vec2i(screen.settings.objectBrowserWidth, view.size.y - screen.settings.fileBrowserHeight)
		trans?.pixelScale = newScale

		positionNodes()
	}
}