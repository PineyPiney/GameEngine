package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.rendering.GameRendererI
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.xr.OpenXrGameLogicI
import org.lwjgl.openxr.XrView

interface OpenXrRendererI<in E: OpenXrGameLogicI> : GameRendererI<E> {

	fun updateView(view: XrView)
	override fun getRenderingApi(): PresentingApi
}