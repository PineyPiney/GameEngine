package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser.Companion.FileContext
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser.Companion.fileContextMenu
import com.pineypiney.game_engine.apps.editor.util.Draggable
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.components.widgets.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.widgets.TextFieldComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.input.CursorPosition
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

open class FileComponent(parent: GameObject, val file: File, val browser: FileBrowser) : DefaultInteractorComponent(parent), Draggable {

	var fileSelect = 0.0

	init {
		parent.components.add(SpriteComponent(parent, getIcon(Vec2(.5f, .25f)), SpriteComponent.menuShader))
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)

		when(action){
			1 -> {
				fileSelect = Timer.time
				browser.screen.setDragging(this, cursorPos)
				return INTERRUPT
			}
			0 -> {
				browser.screen.clearDragging(cursorPos.position)
				if(Timer.time - fileSelect < .5) open()
				return INTERRUPT
			}
		}

		return action
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onSecondary(window, action, mods, cursorPos)
		if(action == 1) {
			browser.screen.setContextMenu(FileContext(browser, this), fileContextMenu, cursorPos.position)
			return INTERRUPT
		}
		return action
	}

	fun rename(){
		parent.removeAndDeleteChild(file.name + " Text Object")

		val textField = ActionTextField<ActionTextFieldComponent<*>>(file.name + " Naming Field", Vec3(-.75f, -.4f, .01f), Vec2(1.5f, .25f), file.nameWithoutExtension, 20){ f, _, _ ->
			file.renameTo(File(file.parentFile, f.text + '.' + file.extension))
			browser.refreshDirectory()
		}
		parent.addChild(textField)
		textField.init()
		textField.getComponent<TextFieldComponent>()?.forceUpdate = true
	}

	open fun open(){
		val saveType = SavableFiles.list.firstOrNull { it.ext == file.extension } ?: return

		browser.screen.sceneObjects.delete()
		saveType.load(file, browser.screen)
		browser.screen.loadedFile(file)
	}

	override fun getElement(): Any = file

	override fun addRenderer(parent: GameObject, cursor: CursorPosition){
		val menuRenderer = GameObject("Menu Renderer", 1)
		val dragSprite = getIcon(Vec2(.5f))
		menuRenderer.components.add(SpriteComponent(menuRenderer, dragSprite, SpriteComponent.menuShader))
		menuRenderer.scale = this.parent.transformComponent.worldScale
		parent.addChild(menuRenderer)
	}

	protected open fun getIcon(center: Vec2, size: Int = browser.screen.settings.fileBrowserIconSize): Sprite{

		val ext = file.extension

		// If this texture is already loaded then don't reload it
		val tex = browser.loadedTextures[ext] ?: let {

			// Get icon
			val image = FileSystemView.getFileSystemView().getSystemIcon(file, size, size) ?: return Sprite(Texture.broke, size.toFloat(), center)

			// Convert icon to BufferedImage
			val imBuf = BufferedImage(image.iconWidth, image.iconHeight, BufferedImage.TYPE_INT_ARGB)
			val gr = imBuf.graphics
			image.paintIcon(null, gr, 0, 0)
			gr.dispose()

			val bytes = ByteArrayOutputStream()
			ImageIO.write(imBuf, "png", bytes)
			val pngBuffer = bytes.toByteArray().toBuffer()
			val rawBuffer = STBImage.stbi_load_from_memory(pngBuffer, IntArray(1), IntArray(1), IntArray(1), 4)
			Texture("$ext Icon", TextureLoader.createTexture(rawBuffer, size, size, GL11C.GL_RGBA))
		}
		val sprite = Sprite(tex, max(size, size).toFloat(), center)
		browser.loadedTextures[ext] = tex
		return sprite
	}

	companion object {
		val fileTypes = mutableMapOf(
			Pair("", ::FolderFile),
			Pair("png", ::ImageFile),
			Pair("pfb", ::PrefabFile),
		)
	}
}