// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform bool ticked;
uniform vec4 colour;

out vec4 FragColour;

void main(){
	float x = abs(0.5 - texCoords.x);
	float y = abs(0.5 - texCoords.y);
	if(x > 0.45 || y > 0.45){
		FragColour = vec4(vec3(0.8), 1.0);
	}
	else{
		bool inCross = abs(x - y) < 0.1;
		FragColour = (ticked && inCross) ? colour : vec4(vec3(0.5), 1.0);
	}

}