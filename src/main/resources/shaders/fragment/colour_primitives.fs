// FRAGMENT SHADER INFORMATION
#version 400 core

vec3 colours[16] = vec3[16](
vec3(0), 			vec3(.5, .5, .5),
vec3(.5, 0, 0), 	vec3(1, .5, .5),
vec3(.5, .5, 0), 	vec3(1, 1, .5),
vec3(0, .5, 0), 	vec3(.5, 1, .5),
vec3(0, .5, .5), 	vec3(.5, 1, 1),
vec3(0, 0, .5), 	vec3(.5, .5, 1),
vec3(.5, 0, .5), 	vec3(1, .5, 1),
vec3(.5, .5, .5), 	vec3(1, 1, 1)
);

out vec4 FragColour;

void main(){
	FragColour = vec4(colours[gl_PrimitiveID % colours.length()], 1);
}