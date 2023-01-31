// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform float green;
uniform float blue;

out vec4 FragColour;

void main(){
	FragColour = vec4(texCoords.x, green, blue, 1.0);
}