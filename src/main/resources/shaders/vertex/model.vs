// VERTEX SHADER INFORMATION
#version 400 core

const int MAX_BONES = 25;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in ivec4 boneIndices;
layout (location = 3) in vec4 boneWeights;

uniform mat4 boneTransforms[MAX_BONES];

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 texCoords;

void main(){

	vec4 pos4 = vec4(aPos, 0.0, 1.0);
	vec4 pos = vec4(0.0);

	if(boneIndices[0] == -1){
		pos = pos4;
	}
	else{
		for(int i = 0; i < MAX_WEIGHTS; i++){
			int boneIndex = boneIndices[i];
			if(boneIndex == -1) break;

			mat4 transform = boneTransforms[boneIndex];
			vec4 posePos = transform * pos4;
			pos += posePos * boneWeights[i];
		}
	}


	gl_Position = projection * view * model * pos;
	texCoords = aTexCoord;
}