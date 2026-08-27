// VERTEX SHADER INFORMATION
#version 430 core

#ifdef OPENGL
layout (location = 0) in vec3 posIn;
layout (location = 2) in vec2 texIn;

out vec2 texCoords;
#endif

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

struct Vertex {
	vec3 posIn;
	float uvX;
	vec3 normalIn;
	float uvY;
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

layout (location = 1) out vec2 texCoords;
#endif

void main(){
	#ifdef VULKAN
	vec3 posIn = vertexBuffer.vertices[gl_VertexID].posIn;
	vec2 texIn = vec2(vertexBuffer.vertices[gl_VertexID].uvX, vertexBuffer.vertices[gl_VertexID].uvY);
	#endif

	gl_Position = vec4(posIn, 1.0);
	texCoords = texIn;
}