// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform float red;
uniform float green;

out vec4 FragColour;

void main(){
	FragColour = vec4(red, green, texCoords.x, 1.0);
}