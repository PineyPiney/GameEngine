// VERTEX SHADER INFORMATION
#version 400 core

#define NR_POINT_LIGHTS 4

const int MAX_BONES = 25;
const int MAX_WEIGHTS = 4;

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec3 aTangent;

struct DirLight{vec3 ambient;vec3 diffuse;vec3 specular; };
struct PointLight{vec3 ambient;vec3 diffuse;vec3 specular;float constant;float linear;float quadratic; };
struct SpotLight{vec3 ambient;vec3 diffuse;vec3 specular;float constant;float linear;float quadratic;float cutOff;float outerCutOff; };

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

out vec3 tangentDirLightDirection;
out vec3 tangentPointLightsPositions[NR_POINT_LIGHTS];
out vec3 tangentSpotlightPosition;
out vec3 tangentSpotlightDirection;

out vec2 texCoords;

out vec3 tangentViewPos;
out vec3 tangentFragPos;

out Debug {
	mat3 debugNormalMat;
	vec3 debugNormal;
	vec3 debugTangent;
	vec3 debugBitangent;
	vec3 debugFragPos;
} vs_debug;

void main(){

	vec4 fragPos = model * vec4(aPos, 1.0);
	gl_Position = projection * view * fragPos;
	texCoords = aTexCoord;

	mat3 normalMat = transpose(inverse(mat3(model)));
	vec3 normal = aNormal;
	vec3 tangent = normalize(normalMat * aTangent);
	vec3 bitangent = cross(normal, tangent);
	mat3 TBN = transpose(mat3(tangent, bitangent, normal));

	vs_debug.debugNormalMat = normalMat;
	vs_debug.debugNormal = normal;
	vs_debug.debugTangent = tangent;
	vs_debug.debugBitangent = bitangent;
	vs_debug.debugFragPos = vec3(fragPos);

	tangentViewPos = TBN * viewPos;
	tangentFragPos = TBN * vec3(fragPos);

	tangentDirLightDirection = TBN * dirLightDirection;

	for(int i = 0; i < NR_POINT_LIGHTS; i++){
		if (pointLights[i].constant > 0 && pointLights[i].linear > 0 && pointLights[i].quadratic > 0){
			tangentPointLightsPositions[i] = TBN * pointLightsPositions[i];
		}
	}

	tangentSpotlightPosition = TBN * spotlightPosition;
	tangentSpotlightDirection = TBN * spotlightDirection;
}