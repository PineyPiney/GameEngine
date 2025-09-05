// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform vec3 topColour;
uniform vec3 bottomColour;

uniform float delta;
uniform mat4 model;

out vec4 FragColour;

void main(){

	vec2 scale = vec2(model[0][0], model[1][1]);
	float aspect = scale.x / scale.y;
	vec3 colour = mix(bottomColour, topColour, texCoords.y);
	float x, y;
	if(aspect >= 1.0){
		x = (abs(texCoords.x - .5) * 2.0 * aspect) + delta - aspect;
		y = (abs(texCoords.y - .5) * 2.0) + delta - 1.0;
	}
	else {
		aspect = 1.0 / aspect;
		x = (abs(texCoords.x - .5) * 2.0) + delta - 1.0;
		y = (abs(texCoords.y - .5) * 2.0 * aspect) + delta - aspect;
	}

	if(x < 0 || y < 0){
		FragColour = vec4(colour, 1.0);
		return;
	}
	float a = sqrt(x*x + y*y) < delta ? 1.0 : 0.0;
	FragColour = vec4(colour, a);
}