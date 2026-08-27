// FRAGMENT SHADER INFORMATION
#version 430 core

#ifdef OPENGL
in vec3 pos;
in vec3 normal;

uniform float ambient;
uniform vec3 blockColour;
uniform vec3 lightPosition;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec3 pos;
layout(location = 2) in vec3 normal;

layout(push_constant) uniform Data {
	vec3 blockColour;
	float ambient;
	vec3 lightPosition;
};

layout(location = 0) out vec4 FragColour;
#endif


void main(){

	vec3 lightDir = normalize(lightPosition - pos);
	float diff = max(dot(normal, lightDir), 0.0);

	FragColour = vec4(blockColour * (ambient + diff), 1.0);
}