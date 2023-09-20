package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.vr.util.logInitError
import glm_.mat4x4.Mat4
import kool.IntBuffer
import org.lwjgl.openvr.*

class VRUtil {


    companion object{
        // https://github.com/kotlin-graphics/openvr/blob/master/src/main/kotlin/openvr/lib/openvr.kt Line 482
        fun initVR(): Int{
            val p = IntBuffer(1)
            val token = VR.VR_InitInternal(p, VR.EVRApplicationType_VRApplication_Scene)

            VR.VR_GetInitToken()

            if(p[0].logInitError()) {
                throw RuntimeException("VRInit failed")
            }

            OpenVR.create(token)
            if (!VR.VR_IsInterfaceVersionValid("FnTable:" + "IVRSystem_020")) {
                VR.VR_ShutdownInternal()
                return VR.EVRInitError_VRInitError_Init_InterfaceNotFound
            }

            return p[0]
        }

        fun getProjectionMatrix(eye: Int, near: Float, far: Float, res: Mat4 = Mat4()): Mat4{
            val m = HmdMatrix44.create()
            VRSystem.VRSystem_GetProjectionMatrix(eye, near, far, m)
            return mat4(m, res)
        }

        fun getEyeToHeadMatrix(eye: Int, res: Mat4 = Mat4()): Mat4{
            val m = HmdMatrix34.create()
            VRSystem.VRSystem_GetEyeToHeadTransform(eye, m)
            return mat4(m, res)
        }

        fun hmdIsPresent(): Boolean{
            return VR.VR_IsHmdPresent()
        }

        fun runtimeInstalled(): Boolean{
            return VR.VR_IsRuntimeInstalled()
        }

        fun mat4(hmdm: HmdMatrix44, res: Mat4 = Mat4()): Mat4{
            val m = hmdm.m()
            return res(
                m[0], m[4], m[8], m[12],
                m[1], m[5], m[9], m[13],
                m[2], m[6], m[10], m[14],
                m[3], m[7], m[11], m[15]
            )
        }
        fun mat4(hmdm: HmdMatrix34, res: Mat4 = Mat4()): Mat4{
            val m = hmdm.m()
            return res(
                m[0], m[4], m[8], 0f,
                m[1], m[5], m[9], 0f,
                m[2], m[6], m[10], 0f,
                m[3], m[7], m[11], 1f
            )
        }
    }
}