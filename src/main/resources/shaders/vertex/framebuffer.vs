// VERTEX SHADER INFORMATION
#version 430 core

#ifdef OPENGL
layout (location = 0) in vec2 posIn;
layout (location = 1) in vec2 texIn;

out vec2 texCoords;
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

//push constants block
layout(push_constant) uniform constants
{
	VertexBuffer vertexBuffer;
};

layout (location = 0) out vec2 texCoords;
#endif


void main(){

	#ifdef VULKAN
	vec2 posIn = vertexBuffer.vertices[gl_VertexID].posIn;
	vec2 texIn = vertexBuffer.vertices[gl_VertexID].texIn;
	#endif

	gl_Position = vec4(posIn, 0.0, 1.0);
	texCoords = texIn;
}