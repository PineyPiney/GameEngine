// FRAGMENT SHADER INFORMATION
#version 430 core

struct Material{
	uint textureMask;
	bool ambDiff;
	float shininess;
	float alpha;
};

#ifdef OPENGL
in vec2 texCoords;
in vec4 boneTint;

uniform Material material;
uniform sampler2D ambientTexture;
uniform sampler2D diffuseTexture;
uniform sampler2D specularTexture;
uniform sampler2D normalsTexture;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec4 boneTint;
layout(location = 1) in vec2 texCoords;

layout(set = 1, binding = 1) uniform sampler2D ambientTexture;
layout(set = 1, binding = 2) uniform sampler2D diffuseTexture;
layout(set = 1, binding = 3) uniform sampler2D specularTexture;
layout(set = 1, binding = 4) uniform sampler2D normalsTexture;

layout(push_constant) uniform MaterialBlock{
	layout(offset = 72) Material material;
};

layout(location = 0) out vec4 FragColour;
#endif

void main(){
	// Discard transparent pixels
	vec4 texture = texture(ambientTexture, texCoords);
	if(texture.a == 0) discard;

	// Make the image black and white
	vec4 colour;
	if(texture.r + texture.g + texture.b > 2) colour = vec4(1.0);
	else colour = vec4(0.0, 0.0, 0.0, 1.0);

	// Tint the black and white image
	FragColour = colour * boneTint;
}