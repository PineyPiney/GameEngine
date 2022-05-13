// FRAGMENT SHADER INFORMATION
#version 460 core

in vec2 texCoords;
in mat4 Fmodel;

uniform sampler2D ourTexture;
uniform vec4 colour;
uniform vec2 limits;
// texture_section is the y value of the bottom of the texture quad, and the height of the quad
uniform vec2 texture_section;

out vec4 FragColour;

void main(){

	float height = Fmodel[1][1];
	// relTex is the height from the bottom of the TextQuad, from 0 to 1
	float relTex = (texCoords.y - texture_section.x) / texture_section.y;
	float y = Fmodel[3][1] + (relTex * height);

	if(y < limits[0] || y > limits[1]) discard;

	vec4 texture = texture(ourTexture, texCoords);
	if(texture.r + texture.g + texture.b < 0.05) discard;

	FragColour = vec4(texture.r * colour.r, texture.g * colour.g, texture.b * colour.b, texture.a * colour.a);
}