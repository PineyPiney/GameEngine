// VERTEX SHADER INFORMATION
#version 430 core

#ifdef OPENGL
layout (location = 0) in vec3 posIn;
layout (location = 1) in vec3 normalIn;
layout (location = 2) in vec2 texIn;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 fragPos;
out vec3 normal;
out vec2 texCoords;
#endif

#ifdef VULKAN

#extension GL_EXT_buffer_reference : require

struct Vertex {
	vec3 posIn;
	float normalX;
	vec2 normalYZ;
	vec2 uv;
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

layout (location = 0) out vec3 fragPos;
layout (location = 1) out vec2 texCoords;
layout (location = 2) out vec3 normal;
#endif

void main(){

	#ifdef VULKAN
	Vertex v = vertexBuffer.vertices[gl_VertexID];
	vec3 posIn = v.posIn;
	vec3 normalIn = vec3(v.normalX, v.normalYZ);
	vec2 texIn = v.uv;
	#endif

	gl_Position = projection * view * model * vec4(posIn, 1.0);

	fragPos = vec3(model * vec4(posIn, 1.0));
	texCoords = texIn;
	normal = normalIn;
}