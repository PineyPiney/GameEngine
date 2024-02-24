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

in vec3 fragPos;
in vec2 texCoords;
in vec3 normal;

uniform Material material;

out vec4 FragColour;

void main(){
	if(material.alpha == 0.0) discard;
	vec4 colour = texture(material.diffuse, texCoords);
	if(colour.a == 0.0) discard;
	FragColour = vec4(colour.r, colour.g, colour.b, colour.a * material.alpha);
}