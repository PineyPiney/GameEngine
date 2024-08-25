// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform vec4 backgroundColour;
uniform sampler2D ourTexture;

out vec4 FragColour;

void main(){
	vec4 colour = texture(ourTexture, texCoords);
	FragColour = (colour * colour.a) + (backgroundColour * (1.0 - colour.a));
}