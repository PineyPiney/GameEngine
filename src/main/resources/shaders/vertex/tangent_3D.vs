// VERTEX SHADER INFORMATION
#version 430 core

#define NR_POINT_LIGHTS 4

struct DirLight{
	vec3 ambient;
	vec3 diffuse;
	vec3 specular;
};

struct PointLight{
	vec3 ambient;
	float constant;
	vec3 diffuse;
	float linear;
	vec3 specular;
	float quadratic;
};

struct SpotLight{
	vec3 ambient;
	float constant;
	vec3 diffuse;
	float linear;
	vec3 specular;
	float quadratic;

	float cutOff;
	float outerCutOff;
};

struct Debug {
	mat3 debugNormalMat;
	vec3 debugNormal;
	vec3 debugTangent;
	vec3 debugBitangent;
	vec3 debugFragPos;
};

layout (location = 0) in vec3 posIn;
layout (location = 1) in vec3 normalIn;
layout (location = 2) in vec2 texIn;
layout (location = 3) in vec3 tangentIn;

#ifdef OPENGL

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform vec3 viewPos;

uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotlight;

uniform vec3 dirLightDirection;
uniform vec3 pointLightsPositions[NR_POINT_LIGHTS];
uniform vec3 spotlightPosition;
uniform vec3 spotlightDirection;

out vec2 texCoords;

out vec3 tangentViewPos;
out vec3 tangentFragPos;

out vec3 tangentDirLightDirection;
out vec3 tangentPointLightsPositions[NR_POINT_LIGHTS];
out vec3 tangentSpotlightPosition;
out vec3 tangentSpotlightDirection;

out Debug debug;
#endif

#ifdef VULKAN

layout(binding = 0, set = 0) uniform Matrices {
	mat4 view;
	mat4 projection;
};

layout(set = 0, binding = 1) uniform Lights {
	DirLight dirLight;
	PointLight pointLights[NR_POINT_LIGHTS];
	SpotLight spotlight;
};

layout(set = 0, binding = 2) uniform LightVectors {
	vec3 dirLightDirection;
	vec3 pointLightsPositions[NR_POINT_LIGHTS];
	vec3 spotlightPosition;
	vec3 spotlightDirection;
};

//push constants block
layout(push_constant) uniform constants
{
	mat4 model;
	vec3 viewPos;
};

layout(location = 1) out vec2 texCoords;

layout(location = 2) out vec3 tangentViewPos;
layout(location = 3) out vec3 tangentFragPos;

layout(location = 4) out vec3 tangentDirLightDirection;
layout(location = 5) out vec3 tangentPointLightsPositions[NR_POINT_LIGHTS];
layout(location = 9) out vec3 tangentSpotlightPosition;
layout(location = 10) out vec3 tangentSpotlightDirection;

layout(location = 11) out Debug debug;
#endif

void main(){

	vec4 fragPos = model * vec4(posIn, 1.0);
	gl_Position = projection * view * fragPos;
	texCoords = texIn;

	mat3 normalMat = transpose(inverse(mat3(model)));
	vec3 normal = normalIn;
	vec3 tangent = normalize(normalMat * tangentIn);
	vec3 bitangent = cross(normal, tangent);
	mat3 TBN = transpose(mat3(tangent, bitangent, normal));

	debug.debugNormalMat = normalMat;
	debug.debugNormal = normal;
	debug.debugTangent = tangent;
	debug.debugBitangent = bitangent;
	debug.debugFragPos = vec3(fragPos);

	tangentViewPos = TBN * viewPos;
	tangentFragPos = TBN * vec3(fragPos);

	tangentDirLightDirection = TBN * dirLightDirection;

	for (int i = 0; i < NR_POINT_LIGHTS; i++){
		if (pointLights[i].constant > 0 && pointLights[i].linear > 0 && pointLights[i].quadratic > 0){
			tangentPointLightsPositions[i] = TBN * pointLightsPositions[i];
		}
	}

	tangentSpotlightPosition = TBN * spotlightPosition;
	tangentSpotlightDirection = TBN * spotlightDirection;
}