package com.pineypiney.game_engine.level_editor

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.level_editor.objects.GameObjects
import com.pineypiney.game_engine.level_editor.objects.PixelObjectCollection
import com.pineypiney.game_engine.level_editor.objects.hud.EditorSidebar
import com.pineypiney.game_engine.level_editor.objects.hud.Inspector
import com.pineypiney.game_engine.level_editor.objects.hud.InventorySidebar
import com.pineypiney.game_engine.level_editor.objects.util.ShadedGameItem
import com.pineypiney.game_engine.level_editor.resources.levels.LevelWriter
import com.pineypiney.game_engine.level_editor.screens.PauseMenuScreen
import com.pineypiney.game_engine.level_editor.util.ClickState
import com.pineypiney.game_engine.level_editor.util.KeyBinds
import com.pineypiney.game_engine.level_editor.util.edits.Edit
import com.pineypiney.game_engine.level_editor.util.edits.PlaceEdit
import com.pineypiney.game_engine.level_editor.util.edits.RemoveEdit
import com.pineypiney.game_engine.level_editor.util.edits.TransformEdit
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Monitor
import com.pineypiney.game_engine.window.WindowI
import glm_.f
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kool.ByteBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL46C.*
import org.lwjgl.stb.STBImageWrite
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.math.PI

class LevelMakerScreen(override val gameEngine: PixelEngine, var worldName: String, var width: Float = 50f, val creationDate: LocalDateTime): PixelScene() {

    val height = 10
    private val camDist = 5f

    override var gameObjects: ObjectCollection = PixelObjectCollection()

    private var cursorGamePosition: Vec2 = Vec2()

    var colour = Vec3(1)
    val unitGrid: ShadedGameItem = ShadedGameItem("UnitGrid",
        unitGridShader
    )

    // The lastClick is the last mouse button action of the user
    private var lastClick: ClickState? = null
    // Edits and UndoneEdits are used for undo and redo functionality
    private val edits = mutableSetOf<Edit>()
    private val undoneEdits = mutableSetOf<Edit>()

    // The current action of the user, can be none, placing a new item or moving a current item
    var state = NONE

    private var oldTransform: Transform2D? = null
    var grabPoint: Vec2 = Vec2()

    var selectedItem: RenderedGameObject2D? = null
        set(value) {
            if(value != field){
                // Remove the old inspector
                inspector?.delete()
                inspector = null

                if(value != null){
                    inspector = Inspector(this, value)
                    inspector?.init()
                    add(inspector)
                }

                field?.shader = RenderedGameObject2D.defaultShader
                value?.shader = selectedItemShader

                field = value
            }
        }
    var holding: Boolean = false
    val heldItem: RenderedGameObject2D? get() = if(holding) selectedItem else null

    private var copiedItem: RenderedGameObject2D? = null

    private val inventory = InventorySidebar(this, window)
    val editorSidebar = EditorSidebar(this)
    private var inspector: Inspector? = null

    private var shouldScreenshot = false

    override fun init() {
        super.init()

        width = width.coerceAtLeast(10 * window.aspectRatio)

        val v = (width - (camera.getSpan().x)) * 0.5f
        camera.cameraMinPos = Vec3(-v, 0, camDist)
        camera.cameraMaxPos = Vec3(v, 0, camDist)
        camera.range = Vec2(4, 100)

        // unitGrid is not added so that it is not included in the list of layers
        unitGrid.init()
        unitGrid.depth = 99
        unitGrid.scale(Vec2(width, 10))
        unitGrid.visible = false

        // Remember not to add the inventory immediately, it is added by pressing E
        // This means it must be initiated manually
        inventory.init()
        editorSidebar.init()

        add(editorSidebar)
    }

    override fun open() {
        super.open()
        glClearColor(colour.r, colour.g, colour.b, 1f)
    }

    override fun addObjects() {

    }

    override fun render(tickDelta: Double) {
        renderer.render(this, tickDelta)
        if(shouldScreenshot){
            screenshot(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss dd-MM-yyyy")))
            shouldScreenshot = false
        }
    }

    override fun update(interval: Float, input: Inputs) {
        super.update(interval, input)
        updateMovement(interval, input)
    }

    private fun updateMovement(interval: Float, input: Inputs){

        var direction = 0
        // This will read the keys continually
        if(KeyBinds.isActive(ResourceKey("key/left"))){
            direction += -1
        }
        if(KeyBinds.isActive(ResourceKey("key/right"))){
            direction += 1
        }

        if(direction != 0){
            // Sprinting
            val sprinting = KeyBinds.isActive(ResourceKey("key/sprint"))
            val speed = direction *
                    if(sprinting) 20f
                    else 10f

            camera.setPos(camera.cameraPos + Vec3(speed * interval, 0, 0))

            cursorGamePosition = camera.screenToWorld(input.mouse.lastPos)
            heldItem?.position = cursorGamePosition - grabPoint
        }
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)

        val v = (width - camera.getSpan().x) * 0.5f
        camera.cameraMinPos = Vec3(-v, 0, camDist)
        camera.cameraMaxPos = Vec3(v, 0, camDist)
    }

    override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
        super.onCursorMove(cursorPos, cursorDelta)

        cursorGamePosition = camera.screenToWorld(cursorPos)
        heldItem?.position = cursorGamePosition - grabPoint
    }

    override fun onScroll(scrollDelta: Vec2): Int {
        super.onScroll(scrollDelta)

        val inputs = gameEngine.input
        val speed = if(inputs.getMod(GLFW.GLFW_MOD_SHIFT)) 5f else 1f
        if(inputs.getMod(GLFW.GLFW_MOD_CONTROL)){
            heldItem?.scale(Vec2(1f + (scrollDelta.y * 0.02f * speed)))
        }
        else{
            heldItem?.rotate(scrollDelta.y * -0.02f * PI.f * speed)
        }

        return 0
    }

    override fun onInput(state: InputState, action: Int): Int {
        KeyBinds.keyBinds.values.firstOrNull { it.key == state.key }?.state = action
        val s = super.onInput(state, action)
        if(s == Interactable.INTERRUPT) return Interactable.INTERRUPT

        if(state.controlType == ControlType.MOUSE && action != 2) {
            lastClick = ClickState(state.i, action, Timer.frameTime, input.mouse.lastPos)
        }

        if(action == GLFW.GLFW_RELEASE && !inventory.textField.forceUpdate){
            when(state.i){
                GLFW.GLFW_KEY_ESCAPE -> {
                    if(!inventory.active) openPauseMenu()
                    else inventory.toggle(this)
                    return Interactable.INTERRUPT
                }
                GLFW.GLFW_KEY_DELETE -> {
                    selectedItem?.let { removeGameObject(it) }
                    selectedItem = null
                    return Interactable.INTERRUPT
                }
                GLFW.GLFW_KEY_E -> {
                    inventory.toggle(this)
                    return Interactable.INTERRUPT
                }
                GLFW.GLFW_KEY_S -> {
                    if(state.control){
                        save()
                        return Interactable.INTERRUPT
                    }
                }
                GLFW.GLFW_KEY_C -> {
                    if(state.control){
                        copiedItem = selectedItem
                    }
                }
                GLFW.GLFW_KEY_V -> {
                    if(state.control){
                        selectedItem = copiedItem?.let{
                            val copied = copy(it)

                            copied?.apply{
                                position = cursorGamePosition
                                rotation = it.rotation
                                scale = it.scale
                                depth = it.depth
                            }
                        }
                        add(selectedItem)
                    }
                }
                GLFW.GLFW_KEY_Z -> {
                    if(state.control){
                        if(state.shift){
                            undoneEdits.lastOrNull()?.let { edit ->
                                edit.redo()
                                undoneEdits.remove(edit)
                                edits.add(edit)
                            }
                        }
                        else{
                            edits.lastOrNull()?.let{ edit ->
                                edit.undo()
                                edits.remove(edit)
                                undoneEdits.add(edit)
                            }
                        }
                    }
                }
                GLFW.GLFW_KEY_F2 -> {
                    shouldScreenshot = true
                    return Interactable.INTERRUPT
                }
            }
        }

        return action
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte) {
        super.onPrimary(window, action, mods)

        if(inspector?.hover == true) return

        when(action){
            GLFW.GLFW_PRESS -> {

                if(state != PLACING_NEW){
                    // If the selected item is not covered by the mouse then try to select a new item
                    if(selectedItem?.isCovered(cursorGamePosition) != true){
                        selectedItem = getClicked()
                    }

                    // Then try to pick up the selected item
                    pickUpSelected()
                }
            }
            GLFW.GLFW_RELEASE -> {
                if(!inventory.active){

                    // The grabPoint is only moved when a pre-placed item is being moved,
                    // so this code is executed when an object has been clicked
                    // after being initially placed
                    if (state == MOVING_OLD) {

                        // Place the item that was being moved
                        heldItem?.let {
                            place(it)
                        }

                        // Reset variables
                        holding = false
                        state = NONE

                        // If the mouse was quickly clicked in place then cycle the selected item
                        lastClick?.let {
                            if(it.pos == input.mouse.lastPos && Timer.frameTime - it.time < 0.5){
                                selectedItem = getClicked()
                            }
                        }
                    }
                    else{
                        // Place a copy of the item that the user is placing from the inventory
                        heldItem?.let {
                            val newItem = copy(it)?.apply {
                                position = it.position
                                rotation = it.rotation
                                scale = it.scale
                                shader = RenderedGameObject2D.defaultShader
                            } ?: return
                            addGameObject(newItem)
                        }
                    }
                }
            }
        }
    }

    override fun onSecondary(window: WindowI, action: Int, mods: Byte) {
        super.onSecondary(window, action, mods)

        selectedItem?.let{
            when(state){
                NONE -> {

                }
                MOVING_OLD -> {
                    place(it)
                }
                PLACING_NEW -> {
                    it.delete()
                }
            }
            selectedItem = null
        }

        state = NONE
    }

    private fun getClicked(): RenderedGameObject2D? = gameObjects.gameItems.filterIsInstance<RenderedGameObject2D>().firstOrNull{ it.visible && it.isCovered(cursorGamePosition) }

    private fun place(item: GameObject2D){
        add(item)
    if(oldTransform != null && oldTransform != item.transform){
            edits.add(TransformEdit(this, item, oldTransform!!, item.transform))
        }
    }

    private fun pickUpSelected(){

        selectedItem?.let { item ->
            remove(item)
            holding = true

            oldTransform = item.transform.copy()
            grabPoint = cursorGamePosition - item.position
        }

        state = if(holding) MOVING_OLD else NONE
    }

    override fun setFullscreen(monitor: Monitor?) {
        if(!inventory.textField.forceUpdate){
            super.setFullscreen(monitor)
        }
    }

    fun addGameObject(o: GameObject2D){
        o.init()
        edits.add(PlaceEdit(this, o))
        add(o)
    }

    fun removeGameObject(o: GameObject2D){
        edits.add(RemoveEdit(this, o))
        o.delete()
        remove(o)
    }

    override fun add(o: Storable?) {
        super.add(o)

        if(o is GameObject2D) editorSidebar.updateLayers(o.depth)
    }

    override fun remove(o: Storable?) {
        super.remove(o)

        if(o is GameObject2D) editorSidebar.updateLayers(o.depth)
    }

    inline fun <reified E: GameObject> copy(o: E): E?{
        val n = GameObjects.getObject(o.name)
        if(n is E) return n
        return null
    }

    private fun openPauseMenu(){
        if(!inventory.textField.forceUpdate) {
            setMenu(PauseMenuScreen(gameEngine))
            openMenu()
        }
    }

    override fun cleanUp() {
        unitGrid.delete()
        save()
        super.cleanUp()
    }

    private fun save(){
        PixelEngine.logger.info("Saving World $worldName")
        val writer = LevelWriter(this)
        val file = File("src/main/resources/levels/$worldName.pgl")

        thread {
            remove(unitGrid)
            writer.writeToFile(file)
            this.gameObjects.addObject(unitGrid)
        }

        // This cannot be done in a separate thread as only one thread can have the window be the current context at a time
        // and LevelMakerScreen#screenshot uses OpenGL functions
        val iconName = worldName
        screenshot("levelIcons/$iconName")
    }

    fun screenshot(name: String) = screenshot(File("src\\main\\resources\\screenshots\\$name.png"))

    fun screenshot(file: File): Boolean{
        // Clear previous errors
        glGetError()

        // Read texture data of frame buffer texture
        glBindTexture(GL_TEXTURE_2D, renderer.gameBuffer.TCB)
        val w = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH)
        val h = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)
        val format = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT)

        val error = glGetError()
        if(error != 0){
            PixelEngine.logger.warn("Error reading frame buffer data")
            return false
        }

        val layers = TextureLoader.formatToChannels(format)

        val buffer = ByteBuffer(w * h * layers)
        glGetTexImage(GL_TEXTURE_2D, 0, format, GL_BYTE, buffer)

        file.parentFile.mkdirs()
        return STBImageWrite.stbi_write_png(file.path, w, h, layers, buffer, layers * w)
    }

    companion object{
        val unitGridShader = ShaderLoader.getShader(ResourceKey("vertex/background"), ResourceKey("fragment/unit_grid"))
        val selectedItemShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\bordered_texture"))

        const val NONE: Byte = 0
        const val PLACING_NEW: Byte = 1
        const val MOVING_OLD: Byte = 2
    }
}