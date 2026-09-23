// VERTEX SHADER INFORMATION
#version 430 core

#ifdef OPENGL
layout (location = 0) in vec3 posIn;
layout (location = 1) in vec2 texIn;

out vec2 texCoords;
#endif

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

struct Vertex {
	float px;
	float py;
	float pz;
	float tx;
	float ty;
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

layout (location = 0) out vec2 texCoords;
#endif

void main(){

	#ifdef VULKAN
	Vertex v = vertexBuffer.vertices[gl_VertexID];
	vec3 posIn = vec3(v.px, v.py, v.pz);
	vec2 texIn = vec2(v.tx, v.ty);
	#endif

	gl_Position = vec4(posIn, 1.0);
	texCoords = texIn;
}