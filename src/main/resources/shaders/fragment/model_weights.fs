// FRAGMENT SHADER INFORMATION
#version 460 core

in vec2 texCoords;
in vec4 boneTint;

uniform sampler2D ourTexture;

out vec4 FragColour;

void main(){
	// Discard transparent pixels
	vec4 texture = texture(ourTexture, texCoords);
	if(texture.a == 0) discard;

	// Make the image black and white
	float b = sqrt(texture.r + texture.g + texture.b) / sqrt(3);
	vec4 colour = vec4(b, b, b, 1.0);

	// Tint the black and white image
	FragColour = colour * boneTint;
}