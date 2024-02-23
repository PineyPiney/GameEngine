// FRAGMENT SHADER INFORMATION
#version 400 core

uniform vec4 colour;
uniform vec2 limits;
uniform ivec2 viewport;

out vec4 FragColour;

void main(){

	float y = (gl_FragCoord.y * 2.0 / viewport.y) - 1.0;

	if(y < limits[0] || y > limits[1]) discard;

	FragColour = colour;
}