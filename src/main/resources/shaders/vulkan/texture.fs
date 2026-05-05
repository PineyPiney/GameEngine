// FRAGMENT SHADER INFORMATION
#version 450 core

layout(location = 1) in vec2 texCoords;

layout(set = 0, binding = 0) uniform sampler2D ourTexture;

layout(location = 0) out vec4 FragColour;

void main() {
	vec4 colour = texture(ourTexture, texCoords);
	if (colour.a == 0) discard;
	FragColour = colour;
}