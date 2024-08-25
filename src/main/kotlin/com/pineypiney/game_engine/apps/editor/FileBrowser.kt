package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.RelativeTransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.SpriteButton
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kool.toBuffer
import org.lwjgl.opengl.GL11C
import org.lwjgl.stb.STBImage
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.filechooser.FileSystemView
import kotlin.math.max

class FileBrowser(parent: GameObject, val screen: EditorScreen, private val root: File = File("src/main/resources")): DefaultInteractorComponent(parent, "FBS"), UpdatingAspectRatioComponent {

	private var currentDirectory = root
	private val loadedTextures = mutableMapOf<String, Sprite>()

	private val filesContainer = MenuItem("Files")
	private val parentFileButton = SpriteButton("Parent File Button", TextureLoader[ResourceKey("menu_items/up_arrow")], 64f){ _, _ ->
		openFile(currentDirectory.parentFile ?: return@SpriteButton)
	}

	var fileSelect = 0.0

	init {
		parent.components.add(RelativeTransformComponent(parent, Vec2(-1f), Vec2(2f, .4f), Vec2(0f)))
		parent.components.add(ColourRendererComponent(parent, Vec3(.8f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
	}

	override fun init() {
		super.init()
		parent.addChild(filesContainer, parentFileButton)
		openFile(root)
	}

	private fun openFile(file: File){
		currentDirectory = file
		filesContainer.deleteAllChildren()
		val invAsp = 2f / parent.scale.x
		val cols = (parent.scale.x * 5f).toInt()
		for((i, subFile) in file.listFiles()?.withIndex() ?: return){
			if(subFile == null) continue
			val sprite = getIcon(subFile, Vec2(.5f))
			val child = MenuItem("File ${subFile.name}")
			child.components.add(SpriteComponent(child, sprite, SpriteComponent.menuShader))
			val dragSprite = getIcon(subFile, Vec2(.5f))
			if(subFile.isDirectory){
				child.components.add(ButtonComponent(child,
					{ _, _ ->
						fileSelect = Timer.time
						screen.setDragging(subFile) {
							it.components.add(SpriteComponent(it, dragSprite, SpriteComponent.menuShader))
						}.let { it.scale = child.transformComponent.worldScale }
					},
					{ _, _ -> screen.clearDragging(); if(Timer.time - fileSelect < .5) openFile(subFile) }
				))
			}
			else {
				child.components.add(ButtonComponent(child,
					{ _, _ ->
						fileSelect = Timer.time
						screen.setDragging(subFile) {
							it.components.add(SpriteComponent(it, dragSprite, SpriteComponent.menuShader))
						}.let { it.scale = child.transformComponent.worldScale }
					},
					{ _, _ -> screen.clearDragging() }
				))
			}
			placeChild(child, i, invAsp, cols)
			filesContainer.addChild(child)
			child.init()

			val textChild = Text.makeMenuText(subFile.name, alignment = Text.ALIGN_CENTER)
			textChild.position = Vec3(-.1f, -.6f, .01f)
			textChild.scale = Vec3(.2f)
			child.addChild(textChild)
			textChild.init()
		}
	}

	private fun getIcon(file: File, center: Vec2, width: Int = 64, height: Int = 64): Sprite{

		val ext = file.extension

		when(ext){
			"" -> {
				val numChildren = file.listFiles()?.size ?: 0
				val start: Float = if(numChildren == 0) 0.6666667f
				else if(numChildren <= 3) 0.33333334f
				else 0f

				return Sprite(TextureLoader[ResourceKey("menu_items/folders")], 138f, center, Vec2(start, 0f), Vec2(0.33333334f, 1f))
			}
			"png" -> {
				val id = file.path.substring(28, file.path.length - 4)
				val tex = TextureLoader[ResourceKey(id)]
				return Sprite(tex, max(tex.width, tex.height).toFloat(), center)
			}
		}

		// If this texture is already loaded then don't reload it
		loadedTextures[ext]?.let { return it }

		// Get icon
		val image = FileSystemView.getFileSystemView().getSystemIcon(file, width, height) ?: return Sprite(Texture.broke, 64f)

		// Convert icon to BufferedImage
		val imBuf = BufferedImage(image.iconWidth, image.iconHeight, BufferedImage.TYPE_INT_ARGB)
		val gr = imBuf.graphics
		image.paintIcon(null, gr, 0, 0)
		gr.dispose()

		val bytes = ByteArrayOutputStream()
		ImageIO.write(imBuf, "png", bytes)
		val pngBuffer = bytes.toByteArray().toBuffer()
		val rawBuffer = STBImage.stbi_load_from_memory(pngBuffer, IntArray(1), IntArray(1), IntArray(1), 4)
		val tex = Texture("$ext Icon", TextureLoader.createTexture(rawBuffer, width, height, GL11C.GL_RGBA))
		val sprite = Sprite(tex, max(width, height).toFloat(), center)
		loadedTextures[ext] = sprite
		return sprite
	}

	override fun updateAspectRatio(renderer: RendererI) {
		val invAsp = 1f / renderer.aspectRatio
		val cols = (renderer.aspectRatio * 10f).toInt()
		for((i, c) in filesContainer.children.withIndex()){
			placeChild(c, i, invAsp, cols)
		}

		parentFileButton.position = Vec3(.1f, .9f, .01f)
		parentFileButton.scale = Vec3(.08f * invAsp, .4f, 1f)
	}

	private fun placeChild(child: GameObject, i: Int, invAsp: Float, cols: Int){
		child.position = Vec3(((i % cols) + .5f) * invAsp * .1f, .6f - (.5f * (i / cols)), .01f)
		child.scale = Vec3(invAsp * .06f, .3f, 1f)
	}

	override fun delete() {
		super.delete()
		for((_, s) in loadedTextures) s.texture.delete()
	}
}