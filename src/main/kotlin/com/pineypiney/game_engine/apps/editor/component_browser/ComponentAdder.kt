package com.pineypiney.game_engine.apps.editor.component_browser

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.SpriteButton
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.ShadUn
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Shape
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11C

class ComponentAdder(parent: GameObject, browser: ComponentBrowser): DefaultInteractorComponent(parent), RenderedComponentI {

	override var visible: Boolean = true
	override val renderSize: Vec2 = Vec2(1f)
	val mesh = Mesh.cornerSquareShape
	override val shape: Shape<*> = mesh.shape

	val shadUn = ShadUn(ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/colour")], ::setUniforms)
	val grab = Vec2(-1f)

	val closeButton = SpriteButton("Close Button", Sprite(TextureLoader[ResourceKey("menu_items/icons")], 40f, Vec2(0f), Vec2(0f), Vec2(.25f, 1f)), Vec3(.875f, .9f, .01f), Vec2(.125f, .1f), ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/background_texture")]){ _, _ ->
		browser.adderPos = this.parent.position
		this.parent.delete()
	}

	val searchBar = ActionTextField<ActionTextFieldComponent<*>>("Search Bar", Vec2(0f), Vec2(1f), "", 1f, ActionTextFieldComponent.UPDATE_EVERY_CHAR){ f, c, i ->
		componentList.search = f.text.lowercase()
	}

	val componentList = NewComponentList(MenuItem("ComponentList"), browser).applied()

	override fun init() {
		super.init()
		// This uses stencils so will render its children on its own
		parent.throwKidsAtRenderer = false
		setUniforms()

		closeButton.getComponent<ColouredSpriteComponent>()?.uniforms?.setVec4Uniform("backgroundColour"){ if(closeButton.getComponent<ButtonComponent>()?.hover == true) Vec4(1f, 0f, 0f, 1f) else Vec4(0f)}

		searchBar.position = Vec3(0f, .8f, .1f)
		searchBar.scale = Vec3(2f, .1f, 1f)
		componentList.parent.apply { position = Vec3(0f, 0f, .01f); scale = Vec3(1f, .8f, 1f) }
		parent.addChild(closeButton, searchBar, componentList.parent)
	}

	override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
		if(super.onInput(window, input, action, cursorPos) == INTERRUPT) return INTERRUPT

		if(action == GLFW.GLFW_PRESS && input.i == GLFW.GLFW_KEY_ESCAPE){
			parent.delete()
			return INTERRUPT
		}

		return action
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		val rel = (cursorPos - Vec2(parent.position))
		if(action == 1 && rel.y > parent.scale.y * .9f) {
			grab.put(rel)
			return INTERRUPT
		}
		else if(action != 2) grab.put(-1f, -1f)
		return action
	}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)

		if(grab.x != -1f){
			parent.position = Vec3(cursorPos - grab, 0f)
			componentList.updateLimits()
		}
	}

	fun setUniforms(){
		shadUn.shader.setRendererDefaults(shadUn.uniforms)
		shadUn.uniforms.setMat4Uniform("model", parent::worldModel)
		shadUn.uniforms.setVec4Uniform("colour"){ Vec4.fromHex(0xCFA2B0, 1f) }
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		GL11C.glEnable(GL11C.GL_STENCIL_TEST)

		GL11C.glDisable(GL11C.GL_SCISSOR_TEST)
		GLFunc.stencilWriteMask = 255
		GL11C.glClearStencil(0)
		GL11C.glClear(GL11C.GL_STENCIL_BUFFER_BIT)

		GLFunc.stencilOp = Vec3i(GL11C.GL_KEEP, GL11C.GL_REPLACE, GL11C.GL_REPLACE)
		GLFunc.stencilFRM = Vec3i(GL11C.GL_ALWAYS, 1, 255)

		shadUn.shader.setUp(shadUn.uniforms, renderer)
		mesh.bindAndDraw()

		GLFunc.stencilFRM = Vec3i(GL11C.GL_EQUAL, 1, 255)
		GLFunc.stencilWriteMask = 0

		val descendants = mutableSetOf<GameObject>()
		parent.children.forEach { it.allActiveDescendants(descendants) }
		for(o in descendants){
			val renderers = o.components.filterIsInstance<RenderedComponentI>()
			val preRenderers = o.components.filterIsInstance<PreRenderComponent>()
			if(renderers.isEmpty()){
				preRenderers.forEach { it.preRender(renderer, tickDelta) }
			}
			else{
				val vis = renderers.any { it.visible }
				preRenderers.forEach{ if(vis || !it.whenVisible) it.preRender(renderer, tickDelta)}
				renderers.forEach{ if (it.visible) it.render(renderer, tickDelta)}
			}
		}

		GL11C.glDisable(GL11C.GL_STENCIL_TEST)
	}
}