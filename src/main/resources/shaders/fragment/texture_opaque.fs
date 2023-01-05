// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;

out vec4 FragColour;

void main(){
	FragColour = texture(ourTexture, texCoords);
}