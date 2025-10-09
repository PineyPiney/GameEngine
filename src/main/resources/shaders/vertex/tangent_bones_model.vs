// VERTEX SHADER INFORMATION
#version 400 core

const int MAX_BONES = 25;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec4 aTangent;
layout (location = 4) in uint boneIndices;
layout (location = 5) in vec4 boneWeights;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform mat4 boneTransforms[MAX_BONES];

out vec3 fragPos;
out vec2 texCoords;
out vec3 normal;

uint boneIndex(int index);

void main(){

	vec4 pos4 = vec4(aPos, 1.0);
	vec4 pos = vec4(0.0);

	if(true){
		pos = pos4;
	}
	else{
		for(int i = 0; i < MAX_WEIGHTS; i++){
            float boneWeight = boneWeights[i];
            if(boneWeight == 0.0) break;

			uint boneIndex = boneIndex(i);
			mat4 transform = boneTransforms[boneIndex];
			vec4 posePos = transform * pos4;
			pos += posePos * boneWeight;
		}
	}


	fragPos = vec3(model * pos);
	gl_Position = projection * view * model * pos;
	texCoords = aTexCoord;
	normal = normalize(mat3(transpose(inverse(model))) * aNormal);
}

uint boneIndex(int index){
    return (boneIndices >> (8 * index) & 255u);
}