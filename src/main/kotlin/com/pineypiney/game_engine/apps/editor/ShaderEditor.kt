package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.parameters.*
import com.pineypiney.game_engine.util.ApiEnum
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.knm.CursorPosition
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.text.Text
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.ceil
import kotlin.random.Random
import kotlin.reflect.full.starProjectedType

class ShaderEditor(parent: GameObject, val screen: EditorScreen) : DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val componentContainer = GameObject("Component Container", 1)

	var height = 1f
	var scroll: Float = 0f

	var shader: RenderShader = ShaderLoader[ResourceKey("editor"), ResourceKey("vertex/2D"), ResourceKey("fragment/colour"), RenderShaderParameters(), null]
	var blended: Boolean = shader.parameters.blending != null
	var blending: Blending = shader.parameters.blending?.copy() ?: Blending()

	val fields = mutableListOf<ComponentField<*>>()

	init {
		parent.components.add(
			PixelTransformComponent(
				parent,
				Vec2i(-screen.settings.componentBrowserWidth, screen.settings.fileBrowserHeight),
				Vec2i(screen.settings.componentBrowserWidth, 324),
				Vec2(1f, -1f)
			)
		)
		parent.components.add(ChildContainingRenderer(parent, Mesh.cornerSquareShape, Vec3(.7f)))
	}

	override fun init() {
		super.init()
		parent.addChild(componentContainer)
		addFields()
	}

	fun setRenderShader(shader: RenderShader) {
		this.shader.delete()
		this.shader = shader
	}

	fun updateFields() {
		for (container in componentContainer.children) {
			for (field in container.children) {
				val fieldEditor = field.getComponent<FieldEditor<*, *>>() ?: continue
				try {
					fieldEditor.update(screen.settings.textScale)
				} catch (e: NullPointerException) {
					GameEngineI.logger.warn("Caught NullPointerException while updating field editor for field ${fieldEditor.id} in component ${fieldEditor.field.container}")
				}
			}
		}
	}

	fun updateShader(stage: ShaderStage, id: String) {
		val newShader = when (stage) {
			ShaderStage.VERTEX -> ShaderLoader[ResourceKey("editor"), ResourceKey(id), ResourceKey(shader.fragment.getName()), shader.stages.map { ResourceKey(it.getName()) }, shader.parameters.copy(), null]
			ShaderStage.FRAGMENT -> ShaderLoader[ResourceKey("editor"), ResourceKey(shader.vertex.getName()), ResourceKey(id), shader.stages.map { ResourceKey(it.getName()) }, shader.parameters.copy(), null]
			else -> {
				val optionalStages = shader.stages.toMutableList()
				optionalStages.removeIf { it.getStage() == stage }
				val stages = optionalStages.map { ResourceKey(it.getName()) } + ResourceKey(id)
				ShaderLoader[ResourceKey("editor"), ResourceKey(shader.vertex.getName()), ResourceKey(shader.fragment.getName()), stages, shader.parameters.copy(), null]
			}
		}

		if (newShader != RenderShader.missing) setRenderShader(newShader)
	}

	fun updateShaderParameters(params: RenderShaderParameters.() -> RenderShaderParameters) {
		val newShader = shader.withParameters(ResourceKey("editor"), shader.parameters.params(), null)
		if (newShader != RenderShader.missing) setRenderShader(newShader)
	}

	fun <E : ApiEnum> updateShaderBlending(createSeparate: (Blending.() -> Blending.Separate<E>)?, params: Blending.(Blending.ColourAndAlpha<E>) -> Blending = { this }) {
		if (createSeparate != null) {
			val value = blending.createSeparate()
			blending = blending.params(if (value.a == value.rgb) Blending.Same(value.rgb) else value)
		}
		val newShader = shader.withParameters(ResourceKey("editor"), shader.parameters.copy(blending = if (blended) blending else null), null)
		if (newShader != RenderShader.missing) setRenderShader(newShader)
	}

	fun addFields(): GameObject {
		val compCont = GameObject("Container", 1)
		val textHeight = screen.settings.textScale
		val spacing = ceil(textHeight * .1f).toInt()

		// Displays the name of the component
		val compText = Text.makeMenuText("Shader", Vec4(0f, 0f, 0f, 1f), textHeight, Text.ALIGN_CENTER_LEFT)
		compText.pixel(Vec2i(0, -textHeight * 2), Vec2i(screen.settings.componentBrowserWidth, textHeight * 2), Vec2(0f, 1f))
		compCont.addChild(compText)

		// Tallies the pixel height of the whole component section
		var pixelY = textHeight * 2

		// Add all component fields
		for (stage in ShaderStage.entries.filter(ShaderStage::isGraphics)) {
			fields.add(
				StringField(stage.name, shader, {
					shader.getModule(stage)?.getName() ?: ""
				}) { id ->
					updateShader(stage, id)
				}
			)
		}


		fields.addAll(
			EnumField("Topology", null, Codec.enum(InputTopology::valueOf), { shader.parameters.topology }) { updateShaderParameters { copy(topology = it) } },
			EnumField("Fill Mode", null, Codec.enum(PolygonMode::valueOf), { shader.parameters.fillMode }) { updateShaderParameters { copy(fillMode = it) } },
			EnumField("Cull Mode", null, Codec.enum(CullMode::valueOf), { shader.parameters.cullMode }) { updateShaderParameters { copy(cullMode = it) } },
			NullableEnumField("Depth Test", null, Codec.nullableEnum(CompareOp::valueOf), CompareOp::class.starProjectedType, { shader.parameters.depthTestOp }) {
				updateShaderParameters {
					copy(
						depthTestOp = it
					)
				}
			},
			IntField("Tesselation", null, { shader.parameters.tesselation }) { updateShaderParameters { copy(tesselation = it) } },
			IntField("Multisampling", null, { shader.parameters.multisampling }) { updateShaderParameters { copy(multisampling = it) } },
		)

		fields.add(BoolField("Blend", null, ::blended) {
			blended = it
			updateShaderBlending<BlendOp>(null)
		})
		fields.addAll(
			EnumField("Src Colour", null, Codec.enum(BlendFactor::valueOf), { blending.src.colour() }) { updateShaderBlending({ Blending.Separate(it, src.alpha()) }) { blend -> copy(src = blend) } },
			EnumField("Src Alpha", null, Codec.enum(BlendFactor::valueOf), { blending.src.alpha() }) { updateShaderBlending({ Blending.Separate(src.colour(), it) }) { blend -> copy(src = blend) } },
			EnumField("Dst Colour", null, Codec.enum(BlendFactor::valueOf), { blending.dst.colour() }) { updateShaderBlending({ Blending.Separate(it, dst.alpha()) }) { blend -> copy(dst = blend) } },
			EnumField("Dst Alpha", null, Codec.enum(BlendFactor::valueOf), { blending.dst.alpha() }) { updateShaderBlending({ Blending.Separate(dst.colour(), it) }) { blend -> copy(dst = blend) } },
			EnumField("Colour Op", null, Codec.enum(BlendOp::valueOf), { blending.op.colour() }) { updateShaderBlending({ Blending.Separate(it, op.alpha()) }) { blend -> copy(op = blend) } },
			EnumField("Alpha Op", null, Codec.enum(BlendOp::valueOf), { blending.op.alpha() }) { updateShaderBlending({ Blending.Separate(op.colour(), it) }) { blend -> copy(op = blend) } },
		)

		for (f in fields) {
			pixelY += (addField(f, compCont, pixelY, textHeight) + spacing)
		}

		pixelY += textHeight / 2
		compCont.pixel(0, -pixelY, screen.settings.componentBrowserWidth, pixelY, 0f, 1f)
//		compCont.components.add(Container(compCont, this, component))

		// Colour the background for each component based on it's name
		val r = Random(0)
		val col = Vec3(r.nextFloat(), r.nextFloat(), r.nextFloat())
		compCont.components.add(ColourRendererComponent(compCont, col, ColourRendererComponent.menuShader, Mesh.cornerSquareShape))

		componentContainer.addChild(compCont)
		return compCont
	}

	fun <T, F : ComponentField<T>> addField(f: F, compCont: GameObject, pixelY: Int, pixelHeight: Int): Int {
		val editor = createEditor(
			GameObject("Field Editor ${f.id}", 1),
			f,
			Vec2i(0, -pixelY),
			Vec2i(screen.settings.componentBrowserWidth, pixelHeight)
		) { ov, v ->
//			screen.setFieldValue(component, f, ov, v)
		}.applied().parent

		compCont.addChild(editor)
		return editor.getComponent<PixelTransformComponent>()!!.pixelScale.y
	}

	fun positionComponents() {
		var y = 0
		for (c in componentContainer.children) {
			val pixel = c.getComponent<PixelTransformComponent>() ?: continue

			y -= 10
			y -= pixel.pixelScale.y
			pixel.pixelPos = Vec2i(pixel.pixelPos.x, y)
		}

		height = -y.toFloat() / parent.getComponent<PixelTransformComponent>()!!.pixelScale.y
		scroll = if (height <= 1) 0f
		else scroll.coerceIn(0f, height - 1f)

		componentContainer.position = Vec3(0f, scroll, 0f)
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		if (action == 1) {
//			screen.setContextMenu(ComponentBrowserContext(this), componentBrowserContextMenu, cursorPos.position)
			return INTERRUPT
		}
		return super.onSecondary(window, action, mods, cursorPos)
	}

	override fun onScroll(window: WindowI, scrollDelta: Vec2): Int {
		super.onScroll(window, scrollDelta)

		scroll = if (height <= 1) 0f
		else (scroll - scrollDelta.y * .1f).coerceIn(0f, height - 1f)
		positionComponents()

		return INTERRUPT
	}

	override fun updateAspectRatio(view: Viewport) {
		val trans = parent.getComponent<PixelTransformComponent>()
		val newScale = Vec2i(screen.settings.componentBrowserWidth, view.size.y - screen.settings.fileBrowserHeight)
		trans?.pixelScale = newScale

		positionComponents()
	}

	override fun delete() {
		shader.delete()
		super.delete()
	}
}