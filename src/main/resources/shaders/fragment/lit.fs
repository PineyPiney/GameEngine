// FRAGMENT SHADER INFORMATION
#version 400 core

in vec3 pos;
in vec3 normal;

uniform float ambient;
uniform vec3 blockColour;
uniform vec3 lightPosition;

out vec4 FragColour;

void main(){

	vec3 lightDir = normalize(lightPosition - pos);
	float diff = max(dot(normal, lightDir), 0.0);

	FragColour = vec4(blockColour * (ambient + diff), 1.0);
}