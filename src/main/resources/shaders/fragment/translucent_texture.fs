// FRAGMENT SHADER INFORMATION
#version 460 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform float alpha;

out vec4 FragColour;

void main(){
	vec4 colour = texture(ourTexture, texCoords);
	FragColour = vec4(vec3(colour), colour.a * alpha);
}