package com.pineypiney.game_engine.vr.util

import glm_.L
import glm_.i
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.quat.QuatD
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec4.Vec4
import glm_.vec4.Vec4b
import kool.*
import org.lwjgl.openvr.*
import org.lwjgl.system.MemoryUtil.memGetFloat
import org.lwjgl.system.MemoryUtil.memPutFloat
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.asin
import kotlin.math.cos


var HmdMatrix34.m: FloatBuffer
    get() = HmdMatrix34.nm(address())
    set(value) = HmdMatrix34.nm(address(), value)


infix fun HmdMatrix34.to(mat: Mat4): Mat4 {
    return mat(
            m[0], m[4], m[8], 0f,
            m[1], m[5], m[9], 0f,
            m[2], m[6], m[10], 0f,
            m[3], m[7], m[11], 1f)
}

fun mat4FromHmdMatrix34(ptr: Long): Mat4 {
    return Mat4(
            memGetFloat(ptr + Float.BYTES * 0), memGetFloat(ptr + Float.BYTES * 4), memGetFloat(ptr + Float.BYTES * 8), 0f,
            memGetFloat(ptr + Float.BYTES * 1), memGetFloat(ptr + Float.BYTES * 5), memGetFloat(ptr + Float.BYTES * 9), 0f,
            memGetFloat(ptr + Float.BYTES * 2), memGetFloat(ptr + Float.BYTES * 6), memGetFloat(ptr + Float.BYTES * 10), 0f,
            memGetFloat(ptr + Float.BYTES * 3), memGetFloat(ptr + Float.BYTES * 7), memGetFloat(ptr + Float.BYTES * 11), 1f)
}

var HmdMatrix33.m: FloatBuffer
    get() = HmdMatrix33.nm(address())
    set(value) = HmdMatrix33.nm(address(), value)

infix fun HmdMatrix33.to(mat: Mat3): Mat3 {
    return mat(
            m[0], m[3], m[6],
            m[1], m[4], m[7],
            m[2], m[5], m[8])
}

var HmdMatrix44.m: FloatBuffer
    get() = HmdMatrix44.nm(address())
    set(value) = HmdMatrix44.nm(address(), value)

infix fun HmdMatrix44.to(mat: Mat4): Mat4 {
    return mat(
            m[0], m[4], m[8], m[12],
            m[1], m[5], m[9], m[13],
            m[2], m[6], m[10], m[14],
            m[3], m[7], m[11], m[15])
}

var HmdVector3.x: Float
    get() = HmdVector3.nv(address(), 0)
    set(value) = HmdVector3.nv(address(), 0, value)
var HmdVector3.y: Float
    get() = HmdVector3.nv(address(), 1)
    set(value) = HmdVector3.nv(address(), 1, value)
var HmdVector3.z: Float
    get() = HmdVector3.nv(address(), 2)
    set(value) = HmdVector3.nv(address(), 2, value)

fun HmdVector3.toVec3(): Vec3 = to(Vec3())
infix fun HmdVector3.to(vec: Vec3): Vec3 = vec(x, y, z)


var HmdVector4.x: Float
    get() = HmdVector4.nv(address(), 0)
    set(value) = HmdVector4.nv(address(), 0, value)
var HmdVector4.y: Float
    get() = HmdVector4.nv(address(), 1)
    set(value) = HmdVector4.nv(address(), 1, value)
var HmdVector4.z: Float
    get() = HmdVector4.nv(address(), 2)
    set(value) = HmdVector4.nv(address(), 2, value)
var HmdVector4.w: Float
    get() = HmdVector4.nv(address(), 3)
    set(value) = HmdVector4.nv(address(), 3, value)

fun HmdVector4.toVec4(): Vec4 = to(Vec4())
infix fun HmdVector4.to(vec: Vec4): Vec4 = vec(x, y, z, w)

var HmdVector3d.x: Double
    get() = HmdVector3d.nv(address(), 0)
    set(value) = HmdVector3d.nv(address(), 0, value)
var HmdVector3d.y: Double
    get() = HmdVector3d.nv(address(), 1)
    set(value) = HmdVector3d.nv(address(), 1, value)
var HmdVector3d.z: Double
    get() = HmdVector3d.nv(address(), 2)
    set(value) = HmdVector3d.nv(address(), 2, value)

fun HmdVector3d.toVec3d(): Vec3d = to(Vec3d())
infix fun HmdVector3d.to(vec: Vec3d): Vec3d = vec(x, y, z)

var HmdVector2.x: Float
    get() = HmdVector2.nv(address(), 0)
    set(value) = HmdVector2.nv(address(), 0, value)
var HmdVector2.y: Float
    get() = HmdVector2.nv(address(), 1)
    set(value) = HmdVector2.nv(address(), 1, value)

fun HmdVector2.toVec2(): Vec2 = to(Vec2())
infix fun HmdVector2.to(vec: Vec2): Vec2 = vec(x, y)


var HmdQuaternion.w: Double
    get() = HmdQuaternion.nw(address())
    set(value) = HmdQuaternion.nw(address(), value)
var HmdQuaternion.x: Double
    get() = HmdQuaternion.nx(address())
    set(value) = HmdQuaternion.nx(address(), value)
var HmdQuaternion.y: Double
    get() = HmdQuaternion.ny(address())
    set(value) = HmdQuaternion.ny(address(), value)
var HmdQuaternion.z: Double
    get() = HmdQuaternion.nz(address())
    set(value) = HmdQuaternion.nz(address(), value)

fun HmdQuaternion.toQuatD(): QuatD = to(QuatD())
infix fun HmdQuaternion.to(quat: QuatD): QuatD = quat.put(w, x, y, z)

var HmdQuaternionf.w: Float
    get() = HmdQuaternionf.nw(address())
    set(value) = HmdQuaternionf.nw(address(), value)
var HmdQuaternionf.x: Float
    get() = HmdQuaternionf.nx(address())
    set(value) = HmdQuaternionf.nx(address(), value)
var HmdQuaternionf.y: Float
    get() = HmdQuaternionf.ny(address())
    set(value) = HmdQuaternionf.ny(address(), value)
var HmdQuaternionf.z: Float
    get() = HmdQuaternionf.nz(address())
    set(value) = HmdQuaternionf.nz(address(), value)

fun HmdQuaternionf.toQuat(): Quat = to(Quat())
infix fun HmdQuaternionf.to(quat: Quat): Quat = quat.put(w, x, y, z)

var HmdColor.r: Float
    get() = HmdColor.nr(address())
    set(value) = HmdColor.nr(address(), value)
var HmdColor.g: Float
    get() = HmdColor.ng(address())
    set(value) = HmdColor.ng(address(), value)
var HmdColor.b: Float
    get() = HmdColor.nb(address())
    set(value) = HmdColor.nb(address(), value)
var HmdColor.a: Float
    get() = HmdColor.na(address())
    set(value) = HmdColor.na(address(), value)

fun HmdColor.toVec4(): Vec4 = to(Vec4())
infix fun HmdColor.to(vec: Vec4): Vec4 = vec(r, g, b, a)

var HmdQuad.corners: Array<Vec3>
    get() = HmdQuad.nvCorners(address()).map { it.toVec3() }.toTypedArray()
    set(value) {
        var ofs: Long = address() + HmdQuad.VCORNERS
        for (v in value) {
            memPutFloat(ofs, v.x)
            memPutFloat(ofs + Float.BYTES, v.y)
            memPutFloat(ofs + Float.BYTES * 2, v.z)
            ofs += Vec3.size
        }
    }


var HmdRect2.topLeft: Vec2
    get() = Vec2.fromPointer(Ptr(adr + HmdRect2.VTOPLEFT.toULong()))
    set(value) = value to Ptr<Float>(adr + HmdRect2.VTOPLEFT.toULong())
var HmdRect2.bottomRight: Vec2
    get() = Vec2.fromPointer(Ptr(adr + HmdRect2.VBOTTOMRIGHT.toULong()))
    set(value) = value to Ptr<Float>(adr + HmdRect2.VBOTTOMRIGHT.toULong())


val DistortionCoordinates.red: FloatBuffer
    get() = DistortionCoordinates.nrfRed(address())
val DistortionCoordinates.green: FloatBuffer
    get() = DistortionCoordinates.nrfGreen(address())
val DistortionCoordinates.blue: FloatBuffer
    get() = DistortionCoordinates.nrfBlue(address())


var Texture.handle: Int
    get() = Texture.nhandle(address()).i
    set(value) = Texture.nhandle(address(), value.L)
var Texture.type: Int
    get() = Texture.neType(address())
    set(value) = Texture.neType(address(), value)
var Texture.colorSpace: Int
    get() = Texture.neColorSpace(address())
    set(value) = Texture.neColorSpace(address(), value)


var TrackedDevicePose.deviceToAbsoluteTracking: Mat4
    get() = mat4FromHmdMatrix34(address() + TrackedDevicePose.MDEVICETOABSOLUTETRACKING)
    set(value) = value.to(Ptr(address() + TrackedDevicePose.MDEVICETOABSOLUTETRACKING))
val TrackedDevicePose.translation: Vec3
    get() = deviceToAbsoluteTracking.array.run { Vec3(this[12], this[13], this[14]) }
val TrackedDevicePose.rotation: Vec3
    get() {
        val a = deviceToAbsoluteTracking.array

        val y = asin(-a[2])
        val cy = cos(y)
        val x = asin(a[6] / cy)
        val z = asin(a[1] / cy)

        return Vec3(x, y, z)
    }
var TrackedDevicePose.velocity: Vec3
    get() = Vec3.fromPointer(Ptr(adr + TrackedDevicePose.VVELOCITY.toULong()))
    set(value) = value to Ptr<Float>(adr + TrackedDevicePose.VVELOCITY.toULong())
var TrackedDevicePose.angularVelocity: Vec3
    get() = Vec3.fromPointer(Ptr(adr + TrackedDevicePose.VANGULARVELOCITY.toULong()))
    set(value) = value to Ptr<Float>(adr + TrackedDevicePose.VANGULARVELOCITY.toULong())
var TrackedDevicePose.trackingResult: Int
    get() = TrackedDevicePose.neTrackingResult(address())
    set(value) = TrackedDevicePose.neTrackingResult(address(), value)
var TrackedDevicePose.poseIsValid: Boolean
    get() = TrackedDevicePose.nbPoseIsValid(address())
    set(value) = TrackedDevicePose.nbPoseIsValid(address(), value)
var TrackedDevicePose.deviceIsConnected: Boolean
    get() = TrackedDevicePose.nbDeviceIsConnected(address())
    set(value) = TrackedDevicePose.nbDeviceIsConnected(address(), value)


var VRTextureBounds.uMin: Float
    get() = VRTextureBounds.nuMin(address())
    set(value) = VRTextureBounds.nuMin(address(), value)
var VRTextureBounds.vMin: Float
    get() = VRTextureBounds.nvMin(address())
    set(value) = VRTextureBounds.nvMin(address(), value)
var VRTextureBounds.uMax: Float
    get() = VRTextureBounds.nuMax(address())
    set(value) = VRTextureBounds.nuMax(address(), value)
var VRTextureBounds.vMax: Float
    get() = VRTextureBounds.nvMax(address())
    set(value) = VRTextureBounds.nvMax(address(), value)


var VRTextureWithPose.deviceToAbsoluteTracking: Mat4
    get() = mat4FromHmdMatrix34(address() + VRTextureWithPose.MDEVICETOABSOLUTETRACKING)
    set(value) = value.to(Ptr(address() + VRTextureWithPose.MDEVICETOABSOLUTETRACKING))


var VRTextureDepthInfo.handle: Long
    get() = VRTextureDepthInfo.nhandle(address())
    set(value) = VRTextureDepthInfo.nhandle(address(), value)
var VRTextureDepthInfo.projection: Mat4
    get() = Mat4.fromPointer(Ptr(address() + VRTextureDepthInfo.MPROJECTION), true)
    set(value) = value.to(Ptr(address() + VRTextureDepthInfo.MPROJECTION), true)
var VRTextureDepthInfo.range: Vec2
    get() = Vec2.fromPointer(Ptr(address() + VRTextureDepthInfo.VRANGE))
    set(value) = value to Ptr<Float>(address() + VRTextureDepthInfo.VRANGE)


var VRTextureWithDepth.depth: VRTextureDepthInfo
    get() = VRTextureWithDepth.ndepth(address())
    set(value) = VRTextureWithDepth.ndepth(address(), value)


var VRTextureWithPoseAndDepth.depth: VRTextureDepthInfo
    get() = VRTextureWithPoseAndDepth.ndepth(address())
    set(value) = VRTextureWithPoseAndDepth.ndepth(address(), value)


//var VRVulkanTextureData.image: VkImage
//    get() = VRVulkanTextureData.nm_nImage(address())
//    set(value) = VRVulkanTextureData.nm_nImage(address(), value)

/**
fun VRVulkanTextureData.device(physicalDevice: VkPhysicalDevice, ci: VkDeviceCreateInfo): VkDevice {
    return VkDevice(VRVulkanTextureData.nm_pDevice(address()), physicalDevice, ci)
}

fun VRVulkanTextureData.setDevice(device: VkDevice) = VRVulkanTextureData.nm_pDevice(address(), device.address())

fun VRVulkanTextureData.physicalDevice(instance: VkInstance): VkPhysicalDevice {
    return VkPhysicalDevice(VRVulkanTextureData.nm_pPhysicalDevice(address()), instance)
}

fun VRVulkanTextureData.setPhysicalDevice(physicalDevice: VkPhysicalDevice) = VRVulkanTextureData.nm_pPhysicalDevice(address())

fun VRVulkanTextureData.instance(ci: VkInstanceCreateInfo): VkInstance {
    return VkInstance(VRVulkanTextureData.nm_pInstance(address()), ci)
}

fun VRVulkanTextureData.setInstance(instance: VkInstance) = VRVulkanTextureData.nm_pInstance(address())

fun VRVulkanTextureData.queue(device: VkDevice): VkQueue {
    return VkQueue(VRVulkanTextureData.nm_pQueue(address()), device)
}

fun VRVulkanTextureData.setQueue(queue: VkQueue) = VRVulkanTextureData.nm_pQueue(address())

var VRVulkanTextureData.queueFamilyIndex: Int
    get() = VRVulkanTextureData.nm_nQueueFamilyIndex(address())
    set(value) = VRVulkanTextureData.nm_nQueueFamilyIndex(address(), value)
/** JVM custom */
var VRVulkanTextureData.size: Vec2i
    get() = Vec2i(VRVulkanTextureData.nm_nWidth(address()), VRVulkanTextureData.nm_nHeight(address()))
    set(value) {
        VRVulkanTextureData.nm_nWidth(address(), value.x)
        VRVulkanTextureData.nm_nHeight(address(), value.y)
    }
//var VRVulkanTextureData.format: VkFormat
//    get() = VkFormat of VRVulkanTextureData.nm_nFormat(address())
//    set(value) = VRVulkanTextureData.nm_nFormat(address(), value.i)
//var VRVulkanTextureData.sampleCount: VkSampleCount
//    get() = VkSampleCount of VRVulkanTextureData.nm_nSampleCount(address())
//    set(value) = VRVulkanTextureData.nm_nSampleCount(address(), value.i)

**/

//var D3D12TextureData.sampleCount
//    get() = VRVulkanTextureData.nm_nSampleCount(address())
//    set(value) = VRVulkanTextureData.nm_nSampleCount(address(), value)


val VREventController.button: Int
    get() = VREventController.nbutton(address())

/** JVM custom */
val VREventMouse.pos: Vec2
    get() = Vec2(VREventMouse.nx(address()), VREventMouse.ny(address()))
val VREventMouse.button: Int
    get() = VREventMouse.nbutton(address())


val VREventScroll.delta: Vec2
    get() = Vec2(VREventScroll.nxdelta(address()), VREventScroll.nydelta(address()))
val VREventScroll.unused: Int
    get() = VREventScroll.nunused(address())
/** For scrolling on an overlay with laser mouse, this is the overlay's vertical size relative to the overlay height. Range: [0,1] */
val VREventScroll.viewportScale: Float
    get() = VREventScroll.nviewportscale(address())


val VREventTouchPadMove.fingerDown: Boolean
    get() = VREventTouchPadMove.nbFingerDown(address())
val VREventTouchPadMove.secondsFingerDown: Float
    get() = VREventTouchPadMove.nflSecondsFingerDown(address())
val VREventTouchPadMove.valueFirst: Vec2
    get() = Vec2(VREventTouchPadMove.nfValueXFirst(address()), VREventTouchPadMove.nfValueYFirst(address()))
val VREventTouchPadMove.valueRaw: Vec2
    get() = Vec2(VREventTouchPadMove.nfValueXRaw(address()), VREventTouchPadMove.nfValueYRaw(address()))


val VREventNotification.userValue: Long
    get() = VREventNotification.nulUserValue(address())
val VREventNotification.notificationId: Int
    get() = VREventNotification.nnotificationId(address())


val VREventProcess.pid: Int
    get() = VREventProcess.npid(address())
val VREventProcess.oldPid: Int
    get() = VREventProcess.noldPid(address())
val VREventProcess.forced: Boolean
    get() = VREventProcess.nbForced(address())
/** If the associated event was triggered by a connection loss */
val VREventProcess.connectionLost: Boolean
    get() = VREventProcess.nbConnectionLost(address())


val VREventOverlay.overlayHandle: Long
    get() = VREventOverlay.noverlayHandle(address())
val VREventOverlay.devicePath: Long
    get() = VREventOverlay.ndevicePath(address())


val VREventStatus.statusState: Int
    get() = VREventStatus.nstatusState(address())


val VREventKeyboard.newInput: String
    get() {
        val ni = VREventKeyboard.ncNewInput(address())
        val chars = ByteArray(ni.cap) { ni[it] }
        return String(chars)
    }
val VREventKeyboard.userValue: Long
    get() = VREventKeyboard.nuUserValue(address())


val VREventIpd.ipdMeters: Float
    get() = VREventIpd.nipdMeters(address())


val VREventChaperone.previousUniverse: Long
    get() = VREventChaperone.nm_nPreviousUniverse(address())
val VREventChaperone.currentUniverse: Long
    get() = VREventChaperone.nm_nCurrentUniverse(address())


val VREventReserved.reserved0: Long
    get() = VREventReserved.nreserved0(address())
val VREventReserved.reserved1: Long
    get() = VREventReserved.nreserved1(address())
val VREventReserved.reserved2: Long
    get() = VREventReserved.nreserved2(address())
val VREventReserved.reserved3: Long
    get() = VREventReserved.nreserved3(address())
val VREventReserved.reserved4: Long
    get() = VREventReserved.nreserved4(address())
val VREventReserved.reserved5: Long
    get() = VREventReserved.nreserved5(address())


val VREventPerformanceTest.fidelityLevel: Int
    get() = VREventPerformanceTest.nm_nFidelityLevel(address())


val VREventSeatedZeroPoseReset.resetBySystemMenu: Boolean
    get() = VREventSeatedZeroPoseReset.nbResetBySystemMenu(address())


val VREventScreenshot.handle: Int
    get() = VREventScreenshot.nhandle(address())
val VREventScreenshot.type: Int
    get() = VREventScreenshot.ntype(address())


val VREventScreenshotProgress.progress: Float
    get() = VREventScreenshotProgress.nprogress(address())


val VREventApplicationLaunch.pid: Int
    get() = VREventApplicationLaunch.npid(address())
val VREventApplicationLaunch.argsHandle: Int
    get() = VREventApplicationLaunch.nunArgsHandle(address())


val VREventEditingCameraSurface.overlayHandle: Long
    get() = VREventEditingCameraSurface.noverlayHandle(address())
val VREventEditingCameraSurface.visualMode: Int
    get() = VREventEditingCameraSurface.nnVisualMode(address())


val VREventMessageOverlay.response: Int
    get() = VREventMessageOverlay.nunVRMessageOverlayResponse(address())


val VREventProperty.container: Long
    get() = VREventProperty.ncontainer(address())
val VREventProperty.prop: Int
    get() = VREventProperty.nprop(address())


/**
/** coordinates are -1..1 analog values */
val VREventDualAnalog.x: Float
    get() = VREventDualAnalog.nx(address())
/** coordinates are -1..1 analog values */
val VREventDualAnalog.y: Float
    get() = VREventDualAnalog.ny(address())
/** transformed by the center and radius numbers provided by the overlay */
val VREventDualAnalog.transformedX: Float
    get() = VREventDualAnalog.ntransformedX(address())
/** transformed by the center and radius numbers provided by the overlay */
val VREventDualAnalog.transformedY: Float
    get() = VREventDualAnalog.ntransformedY(address())
val VREventDualAnalog.which: DualAnalogWhich
    get() = VREventDualAnalog.nwhich(address())
**/


val VREventHapticVibration.containerHandle: Long
    get() = VREventHapticVibration.ncontainerHandle(address())
val VREventHapticVibration.componentHandle: Long
    get() = VREventHapticVibration.ncomponentHandle(address())
val VREventHapticVibration.durationSeconds: Float
    get() = VREventHapticVibration.nfDurationSeconds(address())
val VREventHapticVibration.frequency: Float
    get() = VREventHapticVibration.nfFrequency(address())
val VREventHapticVibration.amplitude: Float
    get() = VREventHapticVibration.nfAmplitude(address())


val VREventWebConsole.handle: Long
    get() = VREventWebConsole.nwebConsoleHandle(address())


val VREventInputBindingLoad.appContainer: Long
    get() = VREventInputBindingLoad.nulAppContainer(address())
val VREventInputBindingLoad.pathMessage: Long
    get() = VREventInputBindingLoad.npathMessage(address())
val VREventInputBindingLoad.pathUrl: Long
    get() = VREventInputBindingLoad.npathUrl(address())


val VREventShowUI.type: Int
    get() = VREventShowUI.neType(address())


val VREventShowDevTools.browserIdentifier: Int
    get() = VREventShowDevTools.nnBrowserIdentifier(address())


val VREventHDCPError.code: Int
    get() = VREventHDCPError.neCode(address())


val VREventInputActionManifestLoad.pathAppKey: Long
    get() = VREventInputActionManifestLoad.npathAppKey(address())
val VREventInputActionManifestLoad.pathMessage: Long
    get() = VREventInputActionManifestLoad.npathMessage(address())
val VREventInputActionManifestLoad.pathMessageParam: Long
    get() = VREventInputActionManifestLoad.npathMessageParam(address())
val VREventInputActionManifestLoad.pathManifestParam: Long
    get() = VREventInputActionManifestLoad.npathManifestPath(address())

val VREventSpatialAnchor.handle: Int
    get() = VREventSpatialAnchor.nunHandle(address())


val VREventProgressUpdate.applicationPropertyContainer: Long
    get() = VREventProgressUpdate.nulApplicationPropertyContainer(address())
val VREventProgressUpdate.pathDevice: Long
    get() = VREventProgressUpdate.npathDevice(address())
val VREventProgressUpdate.pathInputSource: Long
    get() = VREventProgressUpdate.npathInputSource(address())
val VREventProgressUpdate.pathProgressAction: Long
    get() = VREventProgressUpdate.npathProgressAction(address())
val VREventProgressUpdate.pathIcon: Long
    get() = VREventProgressUpdate.npathIcon(address())
val VREventProgressUpdate.progress: Float
    get() = VREventProgressUpdate.nfProgress(address())


val VREvent.eventType: Int
    get() = VREvent.neventType(address())
val VREvent.trackedDeviceIndex: Int
    get() = VREvent.ntrackedDeviceIndex(address())
val VREvent.eventAgeSeconds: Float
    get() = VREvent.neventAgeSeconds(address())
/** event data must be the end of the struct as its size is variable */
val VREvent.data: VREventData
    get() = VREvent.ndata(address())


val HiddenAreaMesh.data: Array<Vec2>
    get() = Array(triangleCount) {
        Vec2.fromPointer(Ptr(address() + HiddenAreaMesh.PVERTEXDATA + Vec2.size * it))
    }
val HiddenAreaMesh.triangleCount: Int
    get() = HiddenAreaMesh.nunTriangleCount(address())


inline var VRControllerAxis.x: Float
    get() = memGetFloat(address() + VRControllerAxis.X)
    set(value) = memPutFloat(address() + VRControllerAxis.X, value)
inline var VRControllerAxis.y: Float
    get() = memGetFloat(address() + VRControllerAxis.Y)
    set(value) = memPutFloat(address() + VRControllerAxis.Y, value)
var VRControllerAxis.pos: Vec2
    get() = Vec2(VRControllerAxis.nx(address()), VRControllerAxis.ny(address()))
    set(value) {
        VRControllerAxis.nx(address(), value.x)
        VRControllerAxis.ny(address(), value.y)
    }


var VRControllerState.packetNum: Int
    get() = VRControllerState.nunPacketNum(address())
    set(value) = VRControllerState.nunPacketNum(address(), value)
var VRControllerState.buttonPressed: Long
    get() = VRControllerState.nulButtonPressed(address())
    set(value) = VRControllerState.nulButtonPressed(address(), value)
var VRControllerState.buttonTouched: Long
    get() = VRControllerState.nulButtonTouched(address())
    set(value) = VRControllerState.nulButtonTouched(address(), value)
var VRControllerState.axis: VRControllerAxis.Buffer
    get() = VRControllerState.nrAxis(address())
    set(value) = VRControllerState.nrAxis(address(), value)


/**
var CompositorOverlaySettings.size: Int
    get() = CompositorOverlaySettings.nsize(address())
    set(value) = CompositorOverlaySettings.nsize(address(), value)
var CompositorOverlaySettings.curved: Boolean
    get() = CompositorOverlaySettings.ncurved(address())
    set(value) = CompositorOverlaySettings.ncurved(address(), value)
var CompositorOverlaySettings.antialias: Boolean
    get() = CompositorOverlaySettings.nantialias(address())
    set(value) = CompositorOverlaySettings.nantialias(address(), value)
var CompositorOverlaySettings.scale: Float
    get() = CompositorOverlaySettings.nuScale(address())
    set(value) = CompositorOverlaySettings.nuScale(address(), value)
var CompositorOverlaySettings.distance: Float
    get() = CompositorOverlaySettings.ndistance(address())
    set(value) = CompositorOverlaySettings.ndistance(address(), value)
var CompositorOverlaySettings.alpha: Float
    get() = CompositorOverlaySettings.nalpha(address())
    set(value) = CompositorOverlaySettings.nalpha(address(), value)
var CompositorOverlaySettings.uvOffset: Vec2
    get() = Vec2(CompositorOverlaySettings.nuOffset(address()), CompositorOverlaySettings.nvOffset(address()))
    set(value) {
        CompositorOverlaySettings.nuOffset(address(), value.x)
        CompositorOverlaySettings.nvOffset(address(), value.y)
    }
var CompositorOverlaySettings.uvScale: Vec2
    get() = Vec2(CompositorOverlaySettings.nuScale(address()), CompositorOverlaySettings.nvScale(address()))
    set(value) {
        CompositorOverlaySettings.nuScale(address(), value.x)
        CompositorOverlaySettings.nvScale(address(), value.y)
    }
var CompositorOverlaySettings.gridDivs: Float
    get() = CompositorOverlaySettings.ngridDivs(address())
    set(value) = CompositorOverlaySettings.ngridDivs(address(), value)
var CompositorOverlaySettings.gridWidth: Float
    get() = CompositorOverlaySettings.ngridWidth(address())
    set(value) = CompositorOverlaySettings.ngridWidth(address(), value)
var CompositorOverlaySettings.gridScale: Float
    get() = CompositorOverlaySettings.ngridScale(address())
    set(value) = CompositorOverlaySettings.ngridScale(address(), value)
var CompositorOverlaySettings.transform: Mat4
    get() = Mat4.fromPointer(address() + CompositorOverlaySettings.TRANSFORM, true)
    set(value) = value.to(address() + CompositorOverlaySettings.TRANSFORM, true)
**/


val VRBoneTransform.position: Vec4
    get() = Vec4.fromPointer(Ptr(address() + VRBoneTransform.POSITION))
val VRBoneTransform.orientation: Quat
    get() = Quat.fromPointer(Ptr(address() + VRBoneTransform.ORIENTATION))


val CameraVideoStreamFrameHeader.frameType: Int
    get() = CameraVideoStreamFrameHeader.neFrameType(address())
val CameraVideoStreamFrameHeader.resolution: Vec2i
    get() = Vec2i(CameraVideoStreamFrameHeader.nnWidth(address()), CameraVideoStreamFrameHeader.nnHeight(address()))
val CameraVideoStreamFrameHeader.bytePerPixel: Int
    get() = CameraVideoStreamFrameHeader.nnBytesPerPixel(address())
val CameraVideoStreamFrameHeader.frameSequence: Int
    get() = CameraVideoStreamFrameHeader.nnFrameSequence(address())
val CameraVideoStreamFrameHeader.standingTrackedDevicePos: TrackedDevicePose
    get() = CameraVideoStreamFrameHeader.ntrackedDevicePose(address())
/** mid-point of the exposure of the image in host system ticks */
val CameraVideoStreamFrameHeader.frameExposureTime: Long
    get() = CameraVideoStreamFrameHeader.nulFrameExposureTime(address())


val CompositorFrameTiming.size: Int
    get() = CompositorFrameTiming.nm_nSize(address())
val CompositorFrameTiming.frameIndex: Int
    get() = CompositorFrameTiming.nm_nFrameIndex(address())
val CompositorFrameTiming.numFramePresents: Int
    get() = CompositorFrameTiming.nm_nNumFramePresents(address())
val CompositorFrameTiming.numMisPresented: Int
    get() = CompositorFrameTiming.nm_nNumMisPresented(address())
val CompositorFrameTiming.numDroppedFrames: Int
    get() = CompositorFrameTiming.nm_nNumDroppedFrames(address())
val CompositorFrameTiming.reprojectionFlags: Int
    get() = CompositorFrameTiming.nm_nReprojectionFlags(address())
val CompositorFrameTiming.systemTimeInSeconds: Double
    get() = CompositorFrameTiming.nm_flSystemTimeInSeconds(address())
val CompositorFrameTiming.preSubmitGpuMs: Float
    get() = CompositorFrameTiming.nm_flPreSubmitGpuMs(address())
val CompositorFrameTiming.postSubmitGpuMs: Float
    get() = CompositorFrameTiming.nm_flPostSubmitGpuMs(address())
val CompositorFrameTiming.totalRenderGpuMs: Float
    get() = CompositorFrameTiming.nm_flTotalRenderGpuMs(address())
val CompositorFrameTiming.compositorRenderGpuMs: Float
    get() = CompositorFrameTiming.nm_flCompositorRenderGpuMs(address())
val CompositorFrameTiming.compositorRenderCpuMs: Float
    get() = CompositorFrameTiming.nm_flCompositorRenderCpuMs(address())
val CompositorFrameTiming.compositorIdleCpuMs: Float
    get() = CompositorFrameTiming.nm_flCompositorIdleCpuMs(address())
val CompositorFrameTiming.clientFrameIntervalMs: Float
    get() = CompositorFrameTiming.nm_flClientFrameIntervalMs(address())
val CompositorFrameTiming.presentCallCpuMs: Float
    get() = CompositorFrameTiming.nm_flPresentCallCpuMs(address())
val CompositorFrameTiming.waitForPresentCpuMs: Float
    get() = CompositorFrameTiming.nm_flWaitForPresentCpuMs(address())
val CompositorFrameTiming.submitFrameMs: Float
    get() = CompositorFrameTiming.nm_flSubmitFrameMs(address())
val CompositorFrameTiming.waitGetPosesCalledMs: Float
    get() = CompositorFrameTiming.nm_flWaitGetPosesCalledMs(address())
val CompositorFrameTiming.newPosesReadyMs: Float
    get() = CompositorFrameTiming.nm_flNewPosesReadyMs(address())
val CompositorFrameTiming.newFrameReadyMs: Float
    get() = CompositorFrameTiming.nm_flNewFrameReadyMs(address())
val CompositorFrameTiming.compositorUpdateStartMs: Float
    get() = CompositorFrameTiming.nm_flCompositorUpdateStartMs(address())
val CompositorFrameTiming.compositorUpdateEndMs: Float
    get() = CompositorFrameTiming.nm_flCompositorUpdateEndMs(address())
val CompositorFrameTiming.compositorRenderStartMs: Float
    get() = CompositorFrameTiming.nm_flCompositorRenderStartMs(address())
val CompositorFrameTiming.hmdPose: TrackedDevicePose
    get() = CompositorFrameTiming.nm_HmdPose(address())


val DriverDirectModeFrameTiming.size: Int
    get() = DriverDirectModeFrameTiming.nm_nSize(address())
val DriverDirectModeFrameTiming.numFramePresents: Int
    get() = DriverDirectModeFrameTiming.nm_nNumFramePresents(address())
val DriverDirectModeFrameTiming.numMisPresented: Int
    get() = DriverDirectModeFrameTiming.nm_nNumMisPresented(address())
val DriverDirectModeFrameTiming.numDroppedFrames: Int
    get() = DriverDirectModeFrameTiming.nm_nNumDroppedFrames(address())
val DriverDirectModeFrameTiming.reprojectionFlags: Int
    get() = DriverDirectModeFrameTiming.nm_nReprojectionFlags(address())


val ImuSample.sampleTime: Double
    get() = ImuSample.nfSampleTime(address())
val ImuSample.accel: Vec3
    get() = Vec3.fromPointer(Ptr(address() + ImuSample.VACCEL))
val ImuSample.gyro: Vec3
    get() = Vec3.fromPointer(Ptr(address() + ImuSample.VGYRO))
val ImuSample.offScaleFlags: Int
    get() = ImuSample.nunOffScaleFlags(address())

// ivrsystem.h


// ivrapplications.h

var AppOverrideKeys.key: String
    get() = AppOverrideKeys.npchKeyString(address())
    set(value) = AppOverrideKeys.npchKey(address(), value.toByteArray().toBuffer())
//stak {
//        val encoded = it.bufferOfAscii(value)
//        AppOverrideKeys.npchKey(address(), encoded)
//    }
var AppOverrideKeys.value: String
    get() = String(AppOverrideKeys.npchValue(address(), 1024).toByteArray())
    set(value) = AppOverrideKeys.npchValue(address(), value.toByteArray().toBuffer())

// ivrsettings.h

// ivrchaperone.h

// ivrchaperonesetup.h

// ivrcompositor.h ================================================================================================================================================


val CompositorCumulativeStats.pid: Int
    get() = CompositorCumulativeStats.nm_nPid(address())
val CompositorCumulativeStats.numFramePresents: Int
    get() = CompositorCumulativeStats.nm_nNumFramePresents(address())
val CompositorCumulativeStats.numDroppedFrames: Int
    get() = CompositorCumulativeStats.nm_nNumDroppedFrames(address())
val CompositorCumulativeStats.numReprojectedFrames: Int
    get() = CompositorCumulativeStats.nm_nNumReprojectedFrames(address())
val CompositorCumulativeStats.numFramePresentsOnStartup: Int
    get() = CompositorCumulativeStats.nm_nNumFramePresentsOnStartup(address())
val CompositorCumulativeStats.numDroppedFramesOnStartup: Int
    get() = CompositorCumulativeStats.nm_nNumDroppedFramesOnStartup(address())
val CompositorCumulativeStats.numReprojectedFramesOnStartup: Int
    get() = CompositorCumulativeStats.nm_nNumReprojectedFramesOnStartup(address())
val CompositorCumulativeStats.numLoading: Int
    get() = CompositorCumulativeStats.nm_nNumLoading(address())
val CompositorCumulativeStats.numFramePresentsLoading: Int
    get() = CompositorCumulativeStats.nm_nNumFramePresentsLoading(address())
val CompositorCumulativeStats.numDroppedFramesLoading: Int
    get() = CompositorCumulativeStats.nm_nNumDroppedFramesLoading(address())
val CompositorCumulativeStats.numReprojectedFramesLoading: Int
    get() = CompositorCumulativeStats.nm_nNumReprojectedFramesLoading(address())
val CompositorCumulativeStats.numTimedOut: Int
    get() = CompositorCumulativeStats.nm_nNumTimedOut(address())
val CompositorCumulativeStats.numFramePresentsTimedOut: Int
    get() = CompositorCumulativeStats.nm_nNumFramePresentsTimedOut(address())
val CompositorCumulativeStats.numDroppedFramesTimedOut: Int
    get() = CompositorCumulativeStats.nm_nNumDroppedFramesTimedOut(address())
val CompositorCumulativeStats.numReprojectedFramesTimedOut: Int
    get() = CompositorCumulativeStats.nm_nNumReprojectedFramesTimedOut(address())


// ivrnotifications.h

var NotificationBitmap.imageData: ByteBuffer
    get() = NotificationBitmap.nm_pImageData(address(), width * height * bytesPerPixel)
    set(value) = NotificationBitmap.nm_pImageData(address(), value)
/** JVM custom */
var NotificationBitmap.size: Vec2i
    get() = Vec2i(NotificationBitmap.nm_nWidth(address()), NotificationBitmap.nm_nHeight(address()))
    set(value) {
        NotificationBitmap.nm_nWidth(address(), value.x)
        NotificationBitmap.nm_nHeight(address(), value.y)
    }
var NotificationBitmap.width: Int
    get() = NotificationBitmap.nm_nWidth(address())
    set(value) = NotificationBitmap.nm_nWidth(address(), value)
var NotificationBitmap.height: Int
    get() = NotificationBitmap.nm_nHeight(address())
    set(value) = NotificationBitmap.nm_nWidth(address(), value)
var NotificationBitmap.bytesPerPixel: Int
    get() = NotificationBitmap.nm_nBytesPerPixel(address())
    set(value) = NotificationBitmap.nm_nBytesPerPixel(address(), value)


// ivroverlay.h

var VROverlayIntersectionParams.source: Vec3
    get() = Vec3.fromPointer(Ptr(address() + VROverlayIntersectionParams.VSOURCE))
    set(value) = value.to(Ptr<Float>(address() + VROverlayIntersectionParams.VSOURCE))
var VROverlayIntersectionParams.direction: Vec3
    get() = Vec3.fromPointer(Ptr(address() + VROverlayIntersectionParams.VDIRECTION))
    set(value) = value.to(Ptr<Float>(address() + VROverlayIntersectionParams.VDIRECTION))
var VROverlayIntersectionParams.origin: Int
    get() = VROverlayIntersectionParams.neOrigin(address())
    set(value) = VROverlayIntersectionParams.neOrigin(address(), value.i)


val VROverlayIntersectionResults.point: Vec3
    get() = Vec3.fromPointer(Ptr(address() + VROverlayIntersectionResults.VPOINT))
val VROverlayIntersectionResults.normal: Vec3
    get() = Vec3.fromPointer(Ptr(address() + VROverlayIntersectionResults.VNORMAL))
val VROverlayIntersectionResults.uv: Vec2
    get() = Vec2.fromPointer(Ptr(address() + VROverlayIntersectionResults.VUVS))
val VROverlayIntersectionResults.distance: Float
    get() = VROverlayIntersectionResults.nfDistance(address())


var IntersectionMaskRectangle.topLeftX: Float
    get() = IntersectionMaskRectangle.nm_flTopLeftX(address())
    set(value) = IntersectionMaskRectangle.nm_flTopLeftX(address(), value)
var IntersectionMaskRectangle.topLeftY: Float
    get() = IntersectionMaskRectangle.nm_flTopLeftY(address())
    set(value) = IntersectionMaskRectangle.nm_flTopLeftY(address(), value)
/** JVM custom */
var IntersectionMaskRectangle.topLeft: Vec2
    get() = Vec2(IntersectionMaskRectangle.nm_flTopLeftX(address()), IntersectionMaskRectangle.nm_flTopLeftY(address()))
    set(value) {
        IntersectionMaskRectangle.nm_flTopLeftX(address(), value.x)
        IntersectionMaskRectangle.nm_flTopLeftY(address(), value.y)
    }
var IntersectionMaskRectangle.width: Float
    get() = IntersectionMaskRectangle.nm_flWidth(address())
    set(value) = IntersectionMaskRectangle.nm_flWidth(address(), value)
var IntersectionMaskRectangle.height: Float
    get() = IntersectionMaskRectangle.nm_flHeight(address())
    set(value) = IntersectionMaskRectangle.nm_flHeight(address(), value)
/** JVM custom */
var IntersectionMaskRectangle.size: Vec2
    get() = Vec2(IntersectionMaskRectangle.nm_flWidth(address()), IntersectionMaskRectangle.nm_flHeight(address()))
    set(value) {
        IntersectionMaskRectangle.nm_flWidth(address(), value.x)
        IntersectionMaskRectangle.nm_flHeight(address(), value.y)
    }


var IntersectionMaskCircle.centerX: Float
    get() = IntersectionMaskCircle.nm_flCenterX(address())
    set(value) = IntersectionMaskCircle.nm_flCenterX(address(), value)
var IntersectionMaskCircle.centerY: Float
    get() = IntersectionMaskCircle.nm_flCenterY(address())
    set(value) = IntersectionMaskCircle.nm_flCenterY(address(), value)
/** JVM custom */
var IntersectionMaskCircle.center: Vec2
    get() = Vec2(IntersectionMaskCircle.nm_flCenterX(address()), IntersectionMaskCircle.nm_flCenterY(address()))
    set(value) {
        IntersectionMaskCircle.nm_flCenterX(address(), value.x)
        IntersectionMaskCircle.nm_flCenterY(address(), value.y)
    }
var IntersectionMaskCircle.radius: Float
    get() = IntersectionMaskCircle.nm_flRadius(address())
    set(value) = IntersectionMaskCircle.nm_flRadius(address(), value)


var VROverlayIntersectionMaskPrimitiveData.rectangle: IntersectionMaskRectangle
    get() = VROverlayIntersectionMaskPrimitiveData.nm_Rectangle(address())
    set(value) = VROverlayIntersectionMaskPrimitiveData.nm_Rectangle(address(), value)
var VROverlayIntersectionMaskPrimitiveData.circle: IntersectionMaskCircle
    get() = VROverlayIntersectionMaskPrimitiveData.nm_Circle(address())
    set(value) = VROverlayIntersectionMaskPrimitiveData.nm_Circle(address(), value)


var VROverlayIntersectionMaskPrimitive.primitiveType: Int
    get() = VROverlayIntersectionMaskPrimitive.nm_nPrimitiveType(address())
    set(value) = VROverlayIntersectionMaskPrimitive.nm_nPrimitiveType(address(), value.i)
var VROverlayIntersectionMaskPrimitive.primitive: VROverlayIntersectionMaskPrimitiveData
    get() = VROverlayIntersectionMaskPrimitive.nm_Primitive(address())
    set(value) = VROverlayIntersectionMaskPrimitive.nm_Primitive(address(), value)


// ivrrendermodels.h


var RenderModelComponentState.trackingToComponentRenderModel: Mat4
    get() = mat4FromHmdMatrix34(address() + RenderModelComponentState.MTRACKINGTOCOMPONENTRENDERMODEL)
    set(value) = value.to(Ptr(address() + RenderModelComponentState.MTRACKINGTOCOMPONENTRENDERMODEL))
var RenderModelComponentState.mTrackingToComponentLocal: Mat4
    get() = mat4FromHmdMatrix34(address() + RenderModelComponentState.MTRACKINGTOCOMPONENTLOCAL)
    set(value) = value.to(Ptr(address() + RenderModelComponentState.MTRACKINGTOCOMPONENTLOCAL))
val RenderModelComponentState.properties: Int
    get() = RenderModelComponentState.nuProperties(address())


/** position in meters in device space */
var RenderModelVertex.position: Vec3
    get() = Vec3.fromPointer(Ptr(address() + RenderModelVertex.VPOSITION))
    set(value) = value to Ptr<Float>(address() + RenderModelVertex.VPOSITION)
var RenderModelVertex.normal: Vec3
    get() = Vec3.fromPointer(Ptr(address() + RenderModelVertex.VNORMAL))
    set(value) = value to Ptr<Float>(address() + RenderModelVertex.VNORMAL)
var RenderModelVertex.textureCoord: Vec2
    get() = Vec2.fromPointer(Ptr(address() + RenderModelVertex.RFTEXTURECOORD))
    set(value) = value to Ptr<Float>(address() + RenderModelVertex.RFTEXTURECOORD)

/** texture map size in pixels */
val RenderModelTextureMap.width: Int
    get() = RenderModelTextureMap.nunWidth(address()).i
val RenderModelTextureMap.height: Int
    get() = RenderModelTextureMap.nunHeight(address()).i
val RenderModelTextureMap.size: Vec2i
    get() = Vec2i(width, height)
/** Map texture data. All textures are RGBA with 8 bits per channel per pixel. Data size is width * height * 4ub */
val RenderModelTextureMap.textureMapData: ByteBuffer
    get() = RenderModelTextureMap.nrubTextureMapData(address(), size.x * size.y * Vec4b.size)


/** Vertex data for the mesh */
val RenderModel.vertices: FloatArray
    get() {
        val vertices = RenderModel.nrVertexData(address())
        val res = FloatArray(vertexCount * RenderModelVertex.SIZEOF)
        var i = 0
        for (v in vertices) {
            res[i++] = v.position.x
            res[i++] = v.position.y
            res[i++] = v.position.z
            res[i++] = v.normal.x
            res[i++] = v.normal.y
            res[i++] = v.normal.z
            res[i++] = v.textureCoord.x
            res[i++] = v.textureCoord.y
        }
        return res
    }

/** Number of vertices in the vertex data */
val RenderModel.vertexCount: Int
    get() = RenderModel.nunVertexCount(address())

/** Indices into the vertex data for each triangle */
val RenderModel.indices: ShortBuffer
    get() = RenderModel.nIndexData(address())

/** Number of triangles in the mesh. Index count is 3 * TriangleCount */
val RenderModel.triangleCount: Int
    get() = RenderModel.nunTriangleCount(address())

/** Session unique texture identifier. Rendermodels which share the same texture will have the same id. <0 == texture not present */
val RenderModel.diffuseTextureId: Int
    get() = RenderModel.ndiffuseTextureId(address())


/** is this controller currently set to be in a scroll wheel mode */
var RenderModelControllerModeState.scrollWheelVisible: Boolean
    get() = RenderModelControllerModeState.nbScrollWheelVisible(address())
    set(value) = RenderModelControllerModeState.nbScrollWheelVisible(address(), value)


// ivrextendeddisplay.h

// ivrtrackedcamera.h

// ivrscreenshots.h

// ivrdrivermanager.h

// ivrinput.h


/** Whether or not this action is currently available to be bound in the active action set */
val InputAnalogActionData.active: Boolean
    get() = InputAnalogActionData.nbActive(address())

/** The origin that caused this action's current state */
val InputAnalogActionData.activeOrigin: Long
    get() = InputAnalogActionData.nactiveOrigin(address())

/** The current state of this action; will be delta updates for mouse actions */
val InputAnalogActionData.x: Float
    get() = InputAnalogActionData.nx(address())

/** The current state of this action; will be delta updates for mouse actions */
val InputAnalogActionData.y: Float
    get() = InputAnalogActionData.ny(address())

/** The current state of this action; will be delta updates for mouse actions */
val InputAnalogActionData.z: Float
    get() = InputAnalogActionData.nz(address())

/** The current state of this action; will be delta updates for mouse actions */
val InputAnalogActionData.v: Vec3
    get() = Vec3(x, y, z)

/** Deltas since the previous call to UpdateActionState() */
val InputAnalogActionData.deltaX: Float
    get() = InputAnalogActionData.ndeltaX(address())

/** Deltas since the previous call to UpdateActionState() */
val InputAnalogActionData.deltaY: Float
    get() = InputAnalogActionData.ndeltaY(address())

/** Deltas since the previous call to UpdateActionState() */
val InputAnalogActionData.deltaZ: Float
    get() = InputAnalogActionData.ndeltaZ(address())

/** Time relative to now when this event happened. Will be negative to indicate a past time. */
val InputAnalogActionData.updateTime: Float
    get() = InputAnalogActionData.nfUpdateTime(address())


/** Whether or not this action is currently available to be bound in the active action set */
val InputDigitalActionData.active: Boolean
    get() = InputDigitalActionData.nbActive(address())

/** The origin that caused this action's current state */
val InputDigitalActionData.activeOrigin: Long
    get() = InputDigitalActionData.nactiveOrigin(address())

/** The current state of this action; will be true if currently pressed */
val InputDigitalActionData.state: Boolean
    get() = InputDigitalActionData.nbState(address())

/** This is true if the state has changed since the last frame */
val InputDigitalActionData.changed: Boolean
    get() = InputDigitalActionData.nbChanged(address())

/** Time relative to now when this event happened. Will be negative to indicate a past time. */
val InputDigitalActionData.updateTime: Float
    get() = InputDigitalActionData.nfUpdateTime(address())


/** Whether or not this action is currently available to be bound in the active action set */
val InputPoseActionData.active: Boolean
    get() = InputPoseActionData.nbActive(address())

/** The origin that caused this action's current state */
val InputPoseActionData.activeOrigin: Long
    get() = InputPoseActionData.nactiveOrigin(address())

/** The current state of this action */
val InputPoseActionData.pose: TrackedDevicePose
    get() = InputPoseActionData.npose(address())


/** Whether or not this action is currently available to be bound in the active action set */
val InputSkeletalActionData.active: Boolean
    get() = InputSkeletalActionData.nbActive(address())

/** Whether or not this action is currently available to be bound in the active action set */
val InputSkeletalActionData.activeOrigin: Long
    get() = InputSkeletalActionData.nactiveOrigin(address())


val InputOriginInfo.devicePath: Long
    get() = InputOriginInfo.ndevicePath(address())

val InputOriginInfo.trackedDeviceIndex: Int
    get() = InputOriginInfo.ntrackedDeviceIndex(address())

val InputOriginInfo.renderModelComponentName: String
    get() = InputOriginInfo.nrchRenderModelComponentNameString(address())


val InputBindingInfo.devicePathName: String
    get() = InputBindingInfo.nrchDevicePathNameString(address())
val InputBindingInfo.inputPathName: String
    get() = InputBindingInfo.nrchInputPathNameString(address())
val InputBindingInfo.modelName: String
    get() = InputBindingInfo.nrchModeNameString(address())
val InputBindingInfo.slotName: String
    get() = InputBindingInfo.nrchSlotNameString(address())


/** This is the handle of the action set to activate for this frame. */
var VRActiveActionSet.actionSet: Long
    get() = VRActiveActionSet.nulActionSet(address())
    set(value) = VRActiveActionSet.nulActionSet(address(), value)

/** This is the handle of a device path that this action set should be active for.
 *  To activate for all devices, set this to ::invalidInputValueHandle. */
var VRActiveActionSet.restrictedToDevice: Long
    get() = VRActiveActionSet.nulRestrictedToDevice(address())
    set(value) = VRActiveActionSet.nulRestrictedToDevice(address(), value)

/** The action set to activate for all devices other than ulRestrictedDevice.
 * If restrictedToDevice is set to ::invalidInputValueHandle, this parameter is ignored. */
var VRActiveActionSet.secondaryActionSet: Long
    get() = VRActiveActionSet.nulSecondaryActionSet(address())
    set(value) = VRActiveActionSet.nulSecondaryActionSet(address(), value)

/** The priority of this action set relative to other action sets. Any inputs bound to a source (e.g. trackpad, joystick, trigger)
 *  will disable bindings in other active action sets with a smaller priority. */
var VRActiveActionSet.priority: Int
    get() = VRActiveActionSet.nnPriority(address())
    set(value) = VRActiveActionSet.nnPriority(address(), value)


// Contains summary information about the current skeletal pose

/** The amount that each finger is 'curled' inwards towards the palm.  In the case of the thumb,
 * this represents how much the thumb is wrapped around the fist.
 * 0 means straight, 1 means fully curled */
val VRSkeletalSummaryData.fingerCurl: FloatBuffer
    get() = VRSkeletalSummaryData.nflFingerCurl(address())
/** The amount that each pair of adjacent fingers are separated.
 * 0 means the digits are touching, 1 means they are fully separated. */
val VRSkeletalSummaryData.fingerSplay: FloatBuffer
    get() = VRSkeletalSummaryData.nflFingerSplay(address())


// ivriobuffer.h