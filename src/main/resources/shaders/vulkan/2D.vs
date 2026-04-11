#version 450

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

struct Vertex {
	vec3 position;
	float uv_x;
	vec3 normal;
	float uv_y;
	vec4 color;
};

layout(buffer_reference, std430) readonly buffer VertexBuffer{
	Vertex vertices[];
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
	VertexBuffer vertexBuffer;
};

layout (location = 0) out vec3 outColor;
layout (location = 1) out vec2 outUV;

void main()
{
	//load vertex data from device adress
	Vertex v = vertexBuffer.vertices[gl_VertexID];

	//output data
	gl_Position = model * vec4(v.position, 1.0f);

	outColor = v.color.xyz;
	outUV = vec2(v.uv_x, v.uv_y);
}

#endif
#ifdef OPENGL

void main() {

}

#endif
