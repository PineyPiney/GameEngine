// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;

out vec4 FragColour;

void main(){
	vec4 colour = texture(ourTexture, texCoords);
	if(colour.a == 0) discard;
	FragColour = colour;
}