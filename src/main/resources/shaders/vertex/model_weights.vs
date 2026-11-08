// VERTEX SHADER INFORMATION
#version 430 core

const int MAX_BONES = 25;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec3 posIn;
//layout (location = 1) in vec3 normalIn;
layout (location = 2) in vec2 texIn;
layout (location = 3) in ivec4 boneIndices;
layout (location = 4) in vec4 boneWeights;

#ifdef OPENGL

uniform mat4 boneTransforms[MAX_BONES];
uniform vec3 boneColours[MAX_BONES];

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec2 texCoords;
out vec4 boneTint;
#endif

#ifdef VULKAN

layout(binding = 0, set = 0) uniform Matrices {
	mat4 view;
	mat4 projection;
	mat4 boneTransforms[MAX_BONES];
	vec3 boneColours[MAX_BONES];
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
};

layout (location = 0) out vec4 boneTint;
layout (location = 1) out vec2 texCoords;
#endif

void main(){

	vec4 pos = vec4(0.0);
	vec3 colour = vec3(0.0);

	for(int i = 0; i < MAX_WEIGHTS; i++){
		if(boneIndices[i] == -1) break;

		// BoneTransforms
		mat4 transform = boneTransforms[boneIndices[i]];
		vec4 posePos = transform * vec4(posIn, 1.0);
		pos += posePos * boneWeights[i];

		// Bone Tint
		colour += boneColours[boneIndices[i]] * boneWeights[i];
	}

	gl_Position = projection * view * model * pos;
	texCoords = texIn;
	boneTint = vec4(colour, 1.0);
}