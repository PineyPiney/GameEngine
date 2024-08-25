package com.pineypiney.game_engine.vr.util

import com.pineypiney.game_engine.vr.VRGameEngine
import mu.KLogger
import org.lwjgl.openvr.VR
import org.lwjgl.openvr.VR.*

fun Int.logInitError(severity: KLogger.(String) -> Unit = KLogger::info) =
	logError(VR::VR_GetVRInitErrorAsEnglishDescription, severity)

fun Int.logCompositorError(severity: KLogger.(String) -> Unit = KLogger::info) =
	logError(VRCompositorErrorMessages::get, severity)

fun Int.logInputError(severity: KLogger.(String) -> Unit = KLogger::info) =
	logError(VRInputErrorMessages::get, severity)

fun Int.logError(getMessage: (Int) -> String?, severity: KLogger.(String) -> Unit = KLogger::info): Boolean {
	if (this != 0) {
		VRGameEngine.logger.severity(getMessage(this) ?: return true)
		return true
	}
	return false
}

//fun <R: Exception> Int.throwError(exc: (String) -> R = ::RuntimeException){
//    throw exc(VR.VR_GetVRInitErrorAsEnglishDescription(this) ?: return)
//}

val VRCompositorErrorMessages = mapOf(
	EVRCompositorError_VRCompositorError_RequestFailed to "Request Failed",
	EVRCompositorError_VRCompositorError_IncompatibleVersion to "Incompatible Version",
	EVRCompositorError_VRCompositorError_DoNotHaveFocus to "Do Not Have Focus",
	EVRCompositorError_VRCompositorError_InvalidTexture to "Invalid Texture",
	EVRCompositorError_VRCompositorError_IsNotSceneApplication to "Is Not Scene Application",
	EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice to "Texture Is On Wrong Device",
	EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat to "Texture Uses Unsupported Format",
	EVRCompositorError_VRCompositorError_SharedTexturesNotSupported to "Shared Textures Not Supported",
	EVRCompositorError_VRCompositorError_IndexOutOfRange to "Index Out Of Range",
	EVRCompositorError_VRCompositorError_AlreadySubmitted to "Already Submitted",
	EVRCompositorError_VRCompositorError_InvalidBounds to "Invalid Bounds",
	EVRCompositorError_VRCompositorError_AlreadySet to "Already Set",
)

val VRInputErrorMessages = mapOf(
	EVRInputError_VRInputError_NameNotFound to "Name Not Found",
	EVRInputError_VRInputError_WrongType to "Wrong Type",
	EVRInputError_VRInputError_InvalidHandle to "Invalid Handle",
	EVRInputError_VRInputError_InvalidParam to "Invalid Param",
	EVRInputError_VRInputError_NoSteam to "No Steam",
	EVRInputError_VRInputError_MaxCapacityReached to "Max Capacity Reached",
	EVRInputError_VRInputError_IPCError to "IPC Error",
	EVRInputError_VRInputError_NoActiveActionSet to "No Active Action Set",
	EVRInputError_VRInputError_InvalidDevice to "Invalid Device",
	EVRInputError_VRInputError_InvalidSkeleton to "Invalid Skeleton",
	EVRInputError_VRInputError_InvalidBoneCount to "Invalid Bone Count",
	EVRInputError_VRInputError_InvalidCompressedData to "Invalid Compressed Data",
	EVRInputError_VRInputError_NoData to "No Data",
	EVRInputError_VRInputError_BufferTooSmall to "Buffer Too Small",
	EVRInputError_VRInputError_MismatchedActionManifest to "Mismatched Action Manifest",
	EVRInputError_VRInputError_MissingSkeletonData to "Missing Skeleton Data",
	EVRInputError_VRInputError_InvalidBoneIndex to "Invalid Bone Index",
	EVRInputError_VRInputError_InvalidPriority to "Invalid Priority",
	EVRInputError_VRInputError_PermissionDenied to "Permission Denied",
	EVRInputError_VRInputError_InvalidRenderModel to "Invalid Render Model",
)