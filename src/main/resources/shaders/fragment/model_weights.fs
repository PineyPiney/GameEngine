// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;
in vec4 boneTint;

uniform sampler2D ourTexture;

out vec4 FragColour;

void main(){
	// Discard transparent pixels
	vec4 texture = texture(ourTexture, texCoords);
	if(texture.a == 0) discard;

	// Make the image black and white
	vec4 colour;
	if(texture.r + texture.g + texture.b > 2) colour = vec4(1.0);
	else colour = vec4(0.0, 0.0, 0.0, 1.0);

	// Tint the black and white image
	FragColour = colour * boneTint;
}