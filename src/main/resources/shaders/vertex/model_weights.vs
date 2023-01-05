// VERTEX SHADER INFORMATION
#version 400 core

const int MAX_BONES = 25;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in ivec4 boneIndices;
layout (location = 3) in vec4 boneWeights;

uniform mat4 boneTransforms[MAX_BONES];
uniform vec3 boneColours[MAX_BONES];

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 texCoords;
out vec4 boneTint;

void main(){

	vec4 pos = vec4(0.0);
	vec3 colour = vec3(0.0);

	for(int i = 0; i < MAX_WEIGHTS; i++){
		if(boneIndices[i] == -1) break;

		// BoneTransforms
		mat4 transform = boneTransforms[boneIndices[i]];
		vec4 posePos = transform * vec4(aPos, 0.0, 1.0);
		pos += posePos * boneWeights[i];

		// Bone Tint
		colour += boneColours[boneIndices[i]] * boneWeights[i];
	}

	gl_Position = projection * view * model * pos;
	texCoords = aTexCoord;
	boneTint = vec4(colour, 1.0);
}