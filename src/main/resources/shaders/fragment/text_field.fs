// FRAGMENT SHADER INFORMATION
#version 460 core

in vec2 texCoords;

uniform mat4 model;
uniform sampler2D ourTexture;
uniform vec3 colour;
uniform vec2 limits;
// texture_section is the x value of the left of the TextQuad, and the width of the quad
uniform vec2 texture_section;

out vec4 FragColour;

void main(){

	float width = model[0][0];
	float relTex = (texCoords.x - texture_section.x) / texture_section.y;
	float x = model[3][0] + (relTex * width);

	if(x < limits[0] || x > limits[1]) discard;

	vec4 texture = texture(ourTexture, texCoords);
	if(texture.r + texture.g + texture.b < 0.05) discard;

	FragColour = texture * vec4(colour, 1.0);
}