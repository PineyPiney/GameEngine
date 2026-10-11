#version 430 core

struct Debug {
	mat3 debugNormalMat;
	vec3 debugNormal;
	vec3 debugTangent;
	vec3 debugBitangent;
	vec3 debugFragPos;
};

layout(triangles) in;

#ifdef OPENGL
in Debug gsIn[];

uniform int vecID;
uniform mat4 view;
uniform mat4 projection;

out vec4 colour;
#endif

#ifdef VULKAN
layout(location = 11) in Debug gsIn[];

layout(set = 0, binding = 0) uniform matrices {
	mat4 view;
	mat4 projection;
};
layout (push_constant) uniform Data {
	int vecID;
};

layout(location = 0) out vec4 colour;
#endif

layout(line_strip, max_vertices = 6) out;

void generateLine(int index){
	gsIn[index];
	mat4 vp = projection * view;
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
