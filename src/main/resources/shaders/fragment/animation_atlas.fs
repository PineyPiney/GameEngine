// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform float origin;

out vec4 FragColour;

void main(){
	vec4 texColour = texture(ourTexture, vec2(texCoords.x + origin, texCoords.y));
	if(texColour.a <= 0.2) discard;
	FragColour = texColour;
}