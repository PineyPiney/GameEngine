package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.vr.util.logInitError
import glm_.vec2.Vec2i
import kool.IntBuffer
import mu.KotlinLogging
import org.lwjgl.openvr.VR
import org.lwjgl.openvr.VRCompositor
import org.lwjgl.openvr.VREvent
import org.lwjgl.openvr.VRSystem

// https://gist.github.com/VirtuosoChris/272f803966e62796b83dce2a597adcc7
abstract class VRGameEngine<E: GameLogicI>(resourcesLoader: ResourcesLoader): GameEngine<E>(resourcesLoader) {

    val rtWidth: Int
    val rtHeight: Int

    abstract val inputVR: InputVR

    override var TARGET_FPS: Int = 1000
    override val TARGET_UPS: Int = 20

    abstract val hmd: HMD

    init {
        if(!VRUtil.hmdIsPresent()) throw RuntimeException("No HMD system was detected")
        if(!VRUtil.runtimeInstalled()) throw RuntimeException("No runtime installed on system")


        val eb = IntBuffer(1)
        VRUtil.initVR().logInitError()

        val brand = VRSystem.VRSystem_GetStringTrackedDeviceProperty(VR.k_unTrackedDeviceIndex_Hmd, VR.ETrackedDeviceProperty_Prop_ManufacturerName_String, eb)
        val model = VRSystem.VRSystem_GetStringTrackedDeviceProperty(VR.k_unTrackedDeviceIndex_Hmd, VR.ETrackedDeviceProperty_Prop_ModelNumber_String, eb)

        val s = getRenderDimensions()
        rtWidth = s.x
        rtHeight = s.y
    }

    override fun init() {
        super.init()
        inputVR.init()
    }

    override fun render(tickDelta: Double) {
        hmd.updateHMDMatrix()
        activeScreen.render(tickDelta)
        VRCompositor.VRCompositor_PostPresentHandoff()
    }

    override fun input() {
        handleVRInput()
    }

    protected open fun handleVRInput(){
        pollEvents()
    }

    private fun pollEvents(){
        val event = VREvent.create()
        while(VRSystem.VRSystem_PollNextEvent(event)){
            val s = when(event.eventType()){
                VR.EVREventType_VREvent_TrackedDeviceActivated -> "Tracked Device Activated"
                VR.EVREventType_VREvent_TrackedDeviceDeactivated -> "Tracked Device Deactivated"
                VR.EVREventType_VREvent_TrackedDeviceUpdated -> "Tracked Device Updated"
                else -> null
            }
            if(s != null) logger.info(s)
        }
    }

    override fun shouldRun(): Boolean {
        return hmd.shouldRun
    }

    override fun cleanUp() {
        super.cleanUp()
        VR.VR_ShutdownInternal()
    }

    companion object{
        fun getRenderDimensions(): Vec2i{
            val (wb, hb) = IntBuffer(1) to IntBuffer(1)
            VRSystem.VRSystem_GetRecommendedRenderTargetSize(wb, hb)

            return Vec2i(wb[0], hb[0])
        }

        val logger = KotlinLogging.logger("Minecad VR")
    }
}