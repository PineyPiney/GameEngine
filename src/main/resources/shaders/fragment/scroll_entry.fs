// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform mat4 model;
uniform sampler2D ourTexture;
uniform vec2 limits;

out vec4 FragColour;

void main(){

	float height = model[1][1];
	float y = model[3][1] + (texCoords.y * height);

	if(y < limits[0] || y > limits[1]) discard;

	FragColour = texture(ourTexture, texCoords);
}