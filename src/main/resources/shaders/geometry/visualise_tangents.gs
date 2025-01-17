#version 400 core

layout(triangles) in;
layout(line_strip, max_vertices = 6) out;

in Debug {
	mat3 debugNormalMat;
	vec3 debugNormal;
	vec3 debugTangent;
	vec3 debugBitangent;
	vec3 debugFragPos;
} gsIn[];

uniform int vecID;

uniform mat4 view;
uniform mat4 projection;
mat4 vp = projection * view;

out vec4 colour;

void generateLine(int index){
	gsIn[index];
	gl_Position = vp * vec4(gsIn[index].debugFragPos, 1.0);
	colour = vec4(vec3(0), 1);
	EmitVertex();
	vec3 vec;
	switch(vecID){
		case 0: vec = gsIn[index].debugNormal; break;
		case 1: vec = gsIn[index].debugTangent; break;
		case 2: vec = gsIn[index].debugBitangent; break;
	}
	gl_Position = vp * vec4(gsIn[index].debugFragPos + (vec * .2), 1.0);
	colour = vec4(1);
	EmitVertex();
	EndPrimitive();
}

void main() {
	generateLine(0);
	generateLine(1);
	generateLine(2);
}
