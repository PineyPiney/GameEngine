package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.serialisation.Codec

data class RenderShaderParameters(
	var topology: InputTopology = InputTopology.TRIANGLES,
	var fillMode: PolygonMode = PolygonMode.FILL,
	var cullMode: CullMode = CullMode.BACK,
	var depthTestOp: CompareOp? = CompareOp.GEQUAL,
	var blending: Blending? = Blending.DEFAULT,
	var tesselation: Int = -1,
	var multisampling: Int = 1
) {

	fun topology(topology: InputTopology) = apply { this.topology = topology }
	fun fillMode(mode: PolygonMode) = apply { this.fillMode = mode }
	fun cullMode(mode: CullMode) = apply { this.cullMode = mode }
	fun depthTestOp(depthTestOp: CompareOp?) = apply { this.depthTestOp = depthTestOp }
	fun blending(blending: Blending?) = apply { this.blending = blending }
	fun tesselation(tesselation: Int) = apply { this.tesselation = tesselation }
	fun multisampling(multisampling: Int) = apply { this.multisampling = multisampling }

	companion object {
		val CODEC = Codec.map(
			Codec.enum(InputTopology::valueOf).optional(InputTopology.TRIANGLES).field(RenderShaderParameters::topology, "topology"),
			Codec.enum(PolygonMode::valueOf).optional(PolygonMode.FILL).field(RenderShaderParameters::fillMode, "fillMode"),
			Codec.enum(CullMode::valueOf).optional(CullMode.BACK).field(RenderShaderParameters::cullMode, "cullMode"),
			Codec.enum(CompareOp::valueOf).opnull(CompareOp.GEQUAL).field(RenderShaderParameters::depthTestOp, "depthTest"),
			Blending.CODEC.opnull(Blending.DEFAULT).field(RenderShaderParameters::blending, "blending"),
			Codec.INT.optional(-1).field(RenderShaderParameters::tesselation, "tessPatches"),
			Codec.INT.optional(1).field(RenderShaderParameters::multisampling, "multisampling"),
			::RenderShaderParameters
		)
	}
}