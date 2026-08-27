package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.serialisation.Codec

data class RenderShaderParameters(
	var topology: InputTopology = InputTopology.TRIANGLES,
	var fillMode: PolygonMode = PolygonMode.FILL,
	var cullMode: CullMode = CullMode.BACK,
	var depthTestOp: CompareOp? = CompareOp.GEQUAL,
	var blending: Triple<Blending, Blending, BlendOp>? = null,
	var multisampling: Int = 1
) {

	fun topology(topology: InputTopology) = apply { this.topology = topology }
	fun fillMode(mode: PolygonMode) = apply { this.fillMode = mode }
	fun cullMode(mode: CullMode) = apply { this.cullMode = mode }

	companion object {
		val CODEC = Codec.map(
			Codec.enum(InputTopology::valueOf).optional(InputTopology.TRIANGLES).field(RenderShaderParameters::topology, "topology"),
			Codec.enum(PolygonMode::valueOf).optional(PolygonMode.FILL).field(RenderShaderParameters::fillMode, "fillMode"),
			Codec.enum(CullMode::valueOf).optional(CullMode.BACK).field(RenderShaderParameters::cullMode, "cullMode"),
			Codec.enum(CompareOp::valueOf).opnull(CompareOp.GEQUAL).field(RenderShaderParameters::depthTestOp, "depthTest"),
			Codec.map(
				Codec.enum(Blending::valueOf).optional(Blending.SRC_ALPHA).field(Triple<Blending, Blending, BlendOp>::first, "src"),
				Codec.enum(Blending::valueOf).optional(Blending.SRC_ALPHA).field(Triple<Blending, Blending, BlendOp>::second, "dst"),
				Codec.enum(BlendOp::valueOf).optional(BlendOp.ADD).field(Triple<Blending, Blending, BlendOp>::third, "op"),
				::Triple
			).opnull().field(RenderShaderParameters::blending, "blending"),
			Codec.INT.optional(0).field(RenderShaderParameters::multisampling, "multisampling"),
			::RenderShaderParameters
		)
	}
}