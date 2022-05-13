// FRAGMENT SHADER INFORMATION
#version 460 core

in vec2 pos;

uniform mat4 model;
out vec4 FragColour;

void main(){
	vec4 worldPos = model * vec4(pos, 0.0, 0.0);
	if(abs(0.5 - mod(worldPos.x, 1.0)) > 0.48 || abs(0.5 - mod(worldPos.y, 1.0)) > 0.48) {
		FragColour = vec4(0.0);
	}
	else discard;

}