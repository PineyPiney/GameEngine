// VERTEX SHADER INFORMATION
#version 460 core

const int MAX_BONES = 25;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in ivec4 boneIndices;
layout (location = 4) in vec4 boneWeights;

uniform mat4 boneTransforms[MAX_BONES];

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 texCoords;

void main(){

	vec4 pos = vec4(0.0);

	for(int i = 0; i < MAX_WEIGHTS; i++){
		mat4 transform = boneTransforms[boneIndices[i]];
		vec4 posePos = transform * vec4(aPos, 1.0);
		pos += posePos * boneWeights[i];
	}

	gl_Position = projection * view * model * pos;
	texCoords = aTexCoord;
}