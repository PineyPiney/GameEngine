// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec3 aPos;
layout (location = 3) in vec2 aOffset;

out vec3 rainbowColour;

void main(){

	vec2 pos = vec2(aPos.x/10, (aPos.y - 0.5)/10) * (gl_InstanceID/100.0);
	gl_Position = vec4(pos + aOffset, 0.0, 1.0);
}