// FRAGMENT SHADER INFORMATION
#version 400 core

out vec4 FragColour;

in vec2 texCoords;

void main(){
	if(abs(texCoords.x) > 0.015 && abs(texCoords.y) > 0.015) discard;
	else FragColour = vec4(0.7, 0.7, 0.7, 0.9);
}