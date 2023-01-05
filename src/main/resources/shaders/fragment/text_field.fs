// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform mat4 model;
uniform sampler2D ourTexture;
uniform vec3 colour;

// The horizontal edges of the field
// Ranges from 0 to Window#width
uniform vec2 limits;

out vec4 FragColour;

void main(){

	float x = gl_FragCoord.x;

	if(x < limits[0] || x > limits[1]) discard;

	vec4 texture = texture(ourTexture, texCoords);
	if(texture.r + texture.g + texture.b < 0.05) discard;

	FragColour = texture * vec4(colour, 1.0);
}