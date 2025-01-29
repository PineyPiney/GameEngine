package com.pineypiney.game_engine.apps.editor.file_browser

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.file_browser.files.*
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenuEntry
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.SpriteButton
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import java.io.File
import kotlin.math.ceil

class FileBrowser(parent: GameObject, val screen: EditorScreen, root: File = File("src/main/resources")): DefaultInteractorComponent(
	parent
), UpdatingAspectRatioComponent {

	var currentDirectory = root
	val loadedTextures = mutableMapOf<String, Sprite>()

	private val filesContainer = MenuItem("Files")
	private val parentFileButton =
		SpriteButton("Parent File Button", TextureLoader.Companion[ResourceKey("menu_items/up_arrow")], 64f) { _, _ ->
			if (currentDirectory == root) return@SpriteButton
			currentDirectory = currentDirectory.parentFile ?: return@SpriteButton
			refreshDirectory()
		}
	val height get() = screen.settings.fileBrowserHeight
	var maxScroll = 0
	var scroll = 0

	init {
		parent.components.add(PixelTransformComponent(parent, Vec2i(0), Vec2i(960, height)))
		parent.components.add(ChildContainingRenderer(parent, Mesh.Companion.cornerSquareShape, Vec3(.8f)))
	}

	override fun init() {
		super.init()
		parent.addChild(filesContainer, parentFileButton)
		refreshDirectory()
	}

	fun refreshDirectory(){
		filesContainer.deleteAllChildren()
		val cols = parent.getComponent<PixelTransformComponent>()?.let { it.pixelScale.x / screen.settings.fileBrowserIconSpace } ?: 10
		val files = currentDirectory.listFiles()?.sortedBy { it.isFile } ?: return

		for((i, subFile) in files.withIndex()){
			if(subFile == null) continue
			val child = MenuItem("File ${subFile.name}")
			filesContainer.addChild(child)
			child.pixel(Vec2i(60, 166), Vec2i(screen.settings.fileBrowserIconSize), screenRelative = true)

			val button = when(subFile.extension){
				"" -> ::FolderFile
				"png" -> ::ImageFile
				"pfb" -> ::PrefabFile
				"scn" -> ::SceneFile
				else -> ::FileComponent
			}
			child.components.add(button(child, subFile, this))

			placeChild(child, i, cols)

			val textChild = Text.Companion.makeMenuText(subFile.name, maxWidth = 1.5f, maxHeight = .6f, fontSize = .22f, alignment = Text.Companion.ALIGN_TOP_CENTER)
			textChild.position = Vec3(0f, -.25f, .01f)
			child.addChild(textChild)

			child.init()
		}
		maxScroll = maxOf(0, (ceil(files.size.toFloat() / cols).toInt() * screen.settings.fileBrowserIconSpace) - screen.settings.fileBrowserHeight)
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onSecondary(window, action, mods, cursorPos)
		if(action == 1) {
			screen.setContextMenu(FileBrowserContext(this), fileBrowserContextMenu, cursorPos)
			return INTERRUPT
		}
		return action
	}

	override fun onScroll(window: WindowI, scrollDelta: Vec2): Int {
		super.onScroll(window, scrollDelta)

		scroll = (scroll - scrollDelta.y * 30).toInt().coerceIn(0, maxScroll)
		val cols = parent.getComponent<PixelTransformComponent>()?.let { it.pixelScale.x / screen.settings.fileBrowserIconSpace } ?: 10
		for((i, c) in filesContainer.children.withIndex()){
			placeChild(c, i, cols)
		}

		return INTERRUPT
	}

	private fun placeChild(child: GameObject, i: Int, cols: Int){
		val trans = child.getComponent<PixelTransformComponent>() ?: return
		trans.pixelPos = Vec2i((screen.settings.fileBrowserIconSpace * ((i % cols) + .5f)).toInt(), height + scroll - (screen.settings.fileBrowserIconSpace * ((i / cols) + .5f)).toInt())
	}

	override fun updateAspectRatio(renderer: RendererI) {
		parent.getComponent<PixelTransformComponent>()?.pixelScale = Vec2i(renderer.viewportSize.x, height)

		val invAsp = 1f / renderer.aspectRatio
		val cols = parent.getComponent<PixelTransformComponent>()?.let { it.pixelScale.x / screen.settings.fileBrowserIconSpace } ?: 10

		val files = filesContainer.children
		maxScroll = maxOf(0, ((files.size / cols) * screen.settings.fileBrowserIconSpace) - screen.settings.fileBrowserHeight)

		for((i, c) in files.withIndex()){
			placeChild(c, i, cols)
		}

		parentFileButton.position = Vec3(.1f, .9f, .01f)
		parentFileButton.scale = Vec3(.08f * invAsp, .4f, 1f)
	}

	override fun delete() {
		super.delete()
		for((_, s) in loadedTextures) s.texture.delete()
	}


	companion object {
		data class FileBrowserContext(val browser: FileBrowser)
		data class FileContext(val browser: FileBrowser, val file: FileComponent)

		val fileBrowserContextMenu = ContextMenu<FileBrowserContext>(
			arrayOf(
				ContextMenuEntry(
				"New", arrayOf(
					ContextMenuEntry("Prefab File") {
						val directory = browser.currentDirectory.path
						val fileName = "$directory/prefab_"
						var i = 0
						while (File("$fileName$i.pfb").exists()) i++

						val newFile = File("$fileName$i.pfb")
						newFile.createNewFile()
						newFile.writeBytes(ByteArray(4))

						browser.refreshDirectory()
					},
					ContextMenuEntry("Scene File") {
						val directory = browser.currentDirectory.path
						val fileName = "$directory/scene_"
						var i = 0
						while (File("$fileName$i.scn").exists()) i++

						val newFile = File("$fileName$i.scn")
						newFile.createNewFile()
						newFile.writeBytes(ByteArray(4))

						browser.refreshDirectory()
					}
			))
		))

		val fileContextMenu = ContextMenu<FileContext>(
			arrayOf(
				ContextMenuEntry("Rename") {
					file.rename()
				},
				ContextMenuEntry("Delete") {
					if (file.file.delete()) browser.refreshDirectory()
				}
		))
	}
}