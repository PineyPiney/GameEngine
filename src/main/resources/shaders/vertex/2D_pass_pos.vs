// VERTEX SHADER INFORMATION
#version 430 core

#ifdef OPENGL
layout (location = 0) in vec2 posIn;

uniform mat4 model;
uniform mat4 guiProjection;

out vec2 pos;
#endif

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

struct Vertex {
	vec2 posIn;
	vec2 texIn;
};

layout(buffer_reference, std430) readonly buffer VertexBuffer{
	Vertex vertices[];
};

layout(binding = 0, set = 0) uniform Matrices {
	mat4 guiProjection;
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
	VertexBuffer vertexBuffer;
};

layout (location = 0) out vec2 pos;
#endif

void main(){

	#ifdef VULKAN
	vec2 posIn = vertexBuffer.vertices[gl_VertexID].posIn;
	#endif

	gl_Position = guiProjection * model * vec4(posIn, 0.0, 1.0);
	pos = posIn;
}