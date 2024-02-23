// FRAGMENT SHADER INFORMATION
#version 400 core

struct Material{
	sampler2D ambient;
	sampler2D diffuse;
	sampler2D specular;
	sampler2D normals;
	uint textureMask;
	bool ambDiff;
	float shininess;
	float alpha;
};

in vec2 texCoords;
in vec4 boneTint;

uniform Material material;

out vec4 FragColour;

void main(){
	// Discard transparent pixels
	vec4 texture = texture(material.ambient, texCoords);
	if(texture.a == 0) discard;

	// Make the image black and white
	vec4 colour;
	if(texture.r + texture.g + texture.b > 2) colour = vec4(1.0);
	else colour = vec4(0.0, 0.0, 0.0, 1.0);

	// Tint the black and white image
	FragColour = colour * boneTint;
}