// VERTEX SHADER INFORMATION
#version 430 core

const int MAX_BONES = 26;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec3 normalIn;
layout (location = 2) in vec2 texIn;
layout (location = 3) in ivec4 boneIndices;
layout (location = 4) in vec4 boneWeights;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform mat4 boneTransforms[MAX_BONES];

out vec3 fragPos;
out vec2 texCoords;
out vec3 normal;
#endif

#ifdef VULKAN

layout(binding = 0, set = 0) uniform Matrices {
	mat4 view;
	mat4 projection;
	mat4 boneTransforms[MAX_BONES];
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
};

layout (location = 0) out vec3 fragPos;
layout (location = 1) out vec2 texCoords;
layout (location = 2) out vec3 normal;
#endif

void main(){

	vec4 pos4 = vec4(posIn, 1.0);
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


	fragPos = vec3(model * pos);
	gl_Position = projection * view * model * pos;
	texCoords = texIn;
	normal = normalize(mat3(transpose(inverse(model))) * normalIn);
}