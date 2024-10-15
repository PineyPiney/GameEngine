package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.RelativeTransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.extension_functions.firstNotNullOfOrNull
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.sign

class ObjectBrowser(parent: GameObject, val screen: EditorScreen): DefaultInteractorComponent(parent, "OBB"), UpdatingAspectRatioComponent {

	val objectList = MenuItem("Object List")
	var selected: ObjectNode? = null

	val colour = ColourRendererComponent(parent, Vec3(.7f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape)

	init {
		parent.components.add(RelativeTransformComponent(parent, Vec2(-1f, -.6f), Vec2(.3f, 1.6f), Vec2(0f)))
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

	fun addRootObject(obj: GameObject){
		screen.add(obj)
		val node = addNode(objectList, obj)
		node.parent.scale = Vec3(.8f, .045f, 1f)
	}

	fun addNode(nodeParent: GameObject, obj: GameObject): ObjectNode{
		val item = MenuItem("${obj.name} node")
		val node = ObjectNode(item, obj)
		item.components.add(node)
		item.components.add(ColourRendererComponent(item, Vec3(.6f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
		nodeParent.addChild(item)
		item.init()

		val textChild = Text.makeMenuText(obj.name)
		textChild.position = Vec3(.1f, .5f, 0f)
		textChild.scale = Vec3(.8f)
		item.addChild(textChild)
		textChild.init()

		positionNodes()
		return node
	}

	fun positionNodes(){
		var y = .95f
		for(c in objectList.children.mapNotNull { it.getComponent<ObjectNode>() }){
			y -= (c.position(.05f, y) * .05f)
		}
	}

	override fun updateAspectRatio(renderer: RendererI) {
		positionNodes()
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if(action == 1) addRootObject(GameObject())
		return super.onSecondary(window, action, mods, cursorPos)
	}

	override fun onCursorEnter(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		colour.colour = Vec4(cursorPos.y,  .5f + (.5f * cursorDelta.y.sign), 0f, 1f)
	}

	override fun onCursorExit(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		colour.colour = Vec4(.7f,  .7f, .7f, 1f)
	}
}