package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser.Companion.FileContext
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser.Companion.fileContextMenu
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.objects.components.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.TextFieldComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.rendering.ObjectRenderer
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.window.WindowI
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

open class FileComponent(parent: GameObject, val file: File, val browser: FileBrowser) : DefaultInteractorComponent(parent) {

	var fileSelect = 0.0

	init {
		parent.components.add(SpriteComponent(parent, getIcon(Vec2(.5f, .25f)), SpriteComponent.menuShader))
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)

		when(action){
			1 -> {
				fileSelect = Timer.time
				browser.screen.setDragging(file, this::addRenderer, this::position)
				return INTERRUPT
			}
			0 -> {
				browser.screen.clearDragging(cursorPos)
				if(Timer.time - fileSelect < .5) open()
				return INTERRUPT
			}
		}

		return action
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onSecondary(window, action, mods, cursorPos)
		if(action == 1) {
			browser.screen.setContextMenu(FileContext(browser, this), fileContextMenu, cursorPos)
			return INTERRUPT
		}
		return action
	}

	fun rename(){
		parent.removeAndDeleteChild(file.name + " Text Object")

		val textField = ActionTextField<ActionTextFieldComponent<*>>(file.name + " Naming Field", Vec3(-.75f, -.4f, .01f), Vec2(1.5f, .25f), file.nameWithoutExtension, .9f){ f, _, _ ->
			file.renameTo(File(file.parentFile, f.text + '.' + file.extension))
			browser.refreshDirectory()
		}
		parent.addChild(textField)
		textField.init()
		textField.getComponent<TextFieldComponent>()?.forceUpdate = true
	}

	open fun open(){

	}

	open fun position(obj: GameObject, cursorPos: Vec2){
		obj.position = Vec3(cursorPos, obj.position.z)
	}

	open fun addRenderer(parent: GameObject){
		val dragSprite = getDragIcon()
		parent.components.add(SpriteComponent(parent, dragSprite, SpriteComponent.menuShader))
		parent.scale = this.parent.transformComponent.worldScale
	}

	protected open fun getIcon(center: Vec2, width: Int = 64, height: Int = 64): Sprite{

		val ext = file.extension

		// If this texture is already loaded then don't reload it
		browser.loadedTextures[ext]?.let { return it }

		// Get icon
		val image = FileSystemView.getFileSystemView().getSystemIcon(file, width, height) ?: return Sprite(Texture.broke, 64f, center)

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
		browser.loadedTextures[ext] = sprite
		return sprite
	}

	protected open fun getDragIcon(): Sprite{
		return when(file.extension){
			"pfb" -> {
				val renderer = ObjectRenderer(Vec3(0f, 0f, 5f))
				renderer.render(GameObjectSerializer.parse(file.inputStream()))

				val texture = Texture(file.path, renderer.frameBuffer.TCB)
				Sprite(texture, 16f)
			}
			else -> getIcon(Vec2(.5f))
		}
	}
}