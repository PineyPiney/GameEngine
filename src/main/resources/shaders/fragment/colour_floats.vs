// VERTEX SHADER INFORMATION
#version 430 core

#ifdef OPENGL
layout (location = 0) in vec3 posIn;
layout (location = 1) in vec4 colourIn;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 colour;
#endif

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

struct Vertex {
	float x;
	float y;
	float z;
	float r;
	float g;
	float b;
	float a;
};

layout(buffer_reference, std430) readonly buffer VertexBuffer{
	Vertex vertices[];
};

layout(binding = 0, set = 0) uniform Matrices {
	mat4 view;
	mat4 projection;
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
	VertexBuffer vertexBuffer;
};

layout (location = 0) out vec4 colour;
#endif

const float _255 = 0.00392156862;

void main(){

	#ifdef VULKAN
	Vertex v = vertexBuffer.vertices[gl_VertexID];
	vec3 posIn = vec3(v.x, v.y, v.z);
	vec4 colourIn = vec4(v.r, v.g, v.b, v.a);
	#endif

	gl_Position = projection * view * model * vec4(posIn, 1.0);
	colour = colourIn;
}