// FRAGMENT SHADER INFORMATION
#version 430 core

struct Material{
	uint textureMask;
	bool ambDiff;
	float shininess;
	float alpha;
};

#ifdef OPENGL
in vec3 fragPos;
in vec2 texCoords;
in vec3 normal;

uniform Material material;
uniform sampler2D ambientTexture;
uniform sampler2D diffuseTexture;
uniform sampler2D specularTexture;
uniform sampler2D normalsTexture;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 texCoords;
layout(location = 2) in vec3 normal;

layout(set = 1, binding = 0) uniform sampler2D ambientTexture;
layout(set = 1, binding = 1) uniform sampler2D diffuseTexture;
layout(set = 1, binding = 2) uniform sampler2D specularTexture;
layout(set = 1, binding = 3) uniform sampler2D normalsTexture;

layout(push_constant) uniform Data {
	layout(offset = 80) Material material;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	if(material.alpha == 0.0) discard;
	vec4 colour = texture(diffuseTexture, texCoords);
	if(colour.a == 0.0) discard;
	FragColour = vec4(colour.r, colour.g, colour.b, colour.a * material.alpha);
}