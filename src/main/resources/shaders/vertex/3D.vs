// VERTEX SHADER INFORMATION
#version 430 core

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec3 normalIn;
layout (location = 2) in vec2 texIn;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 fragPos;
out vec3 normal;
out vec2 texCoords;
#endif

#ifdef VULKAN

layout(binding = 0, set = 0) uniform Matrices {
	mat4 view;
	mat4 projection;
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
};

layout (location = 0) out vec3 fragPos;
layout (location = 1) out vec2 texCoords;
layout (location = 2) out vec3 normal;
#endif

void main(){

	vec4 worldPos = model * vec4(posIn, 1.0);
	gl_Position = projection * view * worldPos;

	fragPos = vec3(worldPos);
	texCoords = texIn;
	normal = normalize(mat3(transpose(inverse(model))) * normalIn);
}