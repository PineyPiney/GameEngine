// FRAGMENT SHADER INFORMATION
#version 400 core
#define NR_POINT_LIGHTS 4

#define PI 3.14159265359
#define PI_1 0.31830988618

vec3 colours[16] = vec3[](
vec3(0), 			vec3(.5, .5, .5),
vec3(.5, 0, 0), 	vec3(1, .5, .5),
vec3(.5, .5, 0), 	vec3(1, 1, .5),
vec3(0, .5, 0), 	vec3(.5, 1, .5),
vec3(0, .5, .5), 	vec3(.5, 1, 1),
vec3(0, 0, .5), 	vec3(.5, .5, 1),
vec3(.5, 0, .5), 	vec3(1, .5, 1),
vec3(.5, .5, .5), 	vec3(1, 1, 1)
);

struct Material{
	sampler2D baseColour;
	sampler2D metallicRoughness;
	sampler2D normals;
	sampler2D occlusion;
	sampler2D emissive;

	uint textureMask;
	vec4 baseColourFactor;
	float metallicFactor;
	float roughnessFactor;
	float emissiveFactor;

	float sheen;
	float sheenTint;
	float anisotropic;
	float specular;
	float specTint;
};

struct PixelMat {
	vec4 baseColour;
	float roughness;
	float metallic;
};
struct OutputPowers {
	float sheenPower;
};

struct DirLight{

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;
};
struct PointLight{

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;

	float constant;
	float linear;
	float quadratic;
};
struct SpotLight{
	vec3 ambient;
	vec3 diffuse;
	vec3 specular;

	float constant;
	float linear;
	float quadratic;

	float cutOff;
	float outerCutOff;
};

in vec2 texCoords;

in vec3 tangentViewPos;
in vec3 tangentFragPos;

in vec3 tangentDirLightDirection;
in vec3 tangentPointLightsPositions[NR_POINT_LIGHTS];
in vec3 tangentSpotlightPosition;
in vec3 tangentSpotlightDirection;

uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotLight;

// MANUAL
uniform Material material;

uniform bool doFresnel;

out vec4 FragColour;

in Debug {
	mat3 debugNormalMat;
	vec3 debugNormal;
	vec3 debugTangent;
	vec3 debugBitangent;
	vec3 debugFragPos;
};

void calculateTangents(vec3 mappedNormal);

bool hasTexture(uint texture);

float luminance(vec3 colour){
	return dot(colour, vec3(.299, .587, .114));
}

float schlick(float x){
	float c = clamp(1.0 - x, 0.0, 1.0);
	return c*c*c*c*c;
}

float calculateFresnel(float roughness, float tIH, float tNI);

void CalcDirLight(vec3 lightDir, DirLight light, inout vec3 diffuse, inout vec3 specular);
void CalcPointLight(vec3 lightPos, PointLight light, inout vec3 diffuse, inout vec3 specular);
void CalcSpotLight(vec3 lightPos, vec3 lightDir, SpotLight light, inout vec3 diffuse, inout vec3 specular);

PixelMat pm;
OutputPowers outputs;

float surfLum;
vec3 normal, tangent, bitangent, viewDir;
float tNV;
vec3 sheenColour, specColour;

void main(){

	pm.baseColour = hasTexture(1u) ? texture(material.baseColour, texCoords) * material.baseColourFactor : material.baseColourFactor;

	vec2 mr = hasTexture(2u) ? texture(material.metallicRoughness, texCoords).yz : vec2(1);
	pm.metallic = mr.y * material.metallicFactor;
	pm.roughness = mr.x * material.roughnessFactor;

	// properties
	surfLum = luminance(vec3(pm.baseColour));
	vec3 tintColour = surfLum > 0 ? vec3(pm.baseColour) / surfLum : vec3(1.0);
	sheenColour = mix(vec3(1.0), tintColour, material.sheenTint);
	specColour = mix(material.specular * .16 * mix(vec3(1.0), tintColour, material.specTint), pm.baseColour.xyz, pm.metallic);

	normal = hasTexture(4u) ? normalize(texture(material.normals, texCoords).xyz * 2  - vec3(1)) : vec3(0, 0, 1);
	calculateTangents(normal);

	//FragColour = vec4((debugTangent * .5) + vec3(.5), 1.0);
	//return;

	viewDir = normalize(tangentViewPos - tangentFragPos);
	tNV = dot(viewDir, normal);

	vec3 diffuse = vec3(0.0);
	vec3 specular = vec3(0.0);

	// == =====================================================
	// Our lighting is set up in 3 phases: directional, point lights and an optional flashlight
	// For each phase, a calculate function is defined that calculates the corresponding color
	// per lamp. In the main() function we take all the calculated colors and sum them up for
	// this fragment's final color.
	// == =====================================================

	// phase 1: directional lighting
	//CalcDirLight(dirLight, diffuse, specular);


	// phase 2: point lights
	for(int i = 0; i < NR_POINT_LIGHTS; i++)
		if(pointLights[i].constant > 0 && pointLights[i].linear > 0 && pointLights[i].quadratic > 0)
			CalcPointLight(tangentPointLightsPositions[i], pointLights[i], diffuse, specular);


	// phase 3: spot light
	//if(spotlight.constant > 0 && spotlight.linear > 0 && spotlight.quadratic > 0)
	//	CalcSpotLight(spotlight, diffuse, specular);

	//vec3 result = ((diffuse * pm.baseColour.xyz) + (outputs.sheenPower * material.sheen * sheenColour)) + specular;
	vec3 result = (diffuse * (1.0 - pm.metallic)) + specular;
	FragColour = vec4(result, pm.baseColour.a);

	// For Smooth: FragColour = vec4(vec3(.5 + (.5 * dot(normal, normalize(vec3(1.0))))), 1.0);
}


void calculateTangents(vec3 mappedNormal){
	if(mappedNormal == vec3(0, 0, 1)){
		tangent = vec3(1, 0, 0);
		bitangent = vec3(0, 1, 0);
		return;
	}
	else{
		vec3 v = cross(mappedNormal, vec3(0, 0, 1));
		float s = length(v);
		float c = dot(mappedNormal, vec3(0, 0, 1));
		mat3 vSkew = mat3(
			0, -v.z, v.y,
			v.z, 0, -v.x,
			-v.y, v.x, 0);
		mat3 mat = mat3(1) + vSkew + (vSkew * vSkew / (1 + c));

		tangent = normalize(mat * vec3(1, 0, 0));
		bitangent = normalize(mat * vec3(0, 1, 0));
		// COMPLETELY UNTESTED
	}
}

bool hasTexture(uint texture){
	return (material.textureMask & texture) == texture;
}

float clampDot(vec3 x, vec3 y){
	return clamp(dot(x, y), 0.0, 1.0);
}

float calculateFresnel(float roughness, float tIH, float tNI){
	float F0 = 1.0;
	float F90 = .5 + (2 * roughness * tIH * tIH);
	return mix(F0, F90, schlick(tNI)) * mix(F0, F90, schlick(tNV));
}

float GTR(float tNH, float a){
	float a2 = a * a;
	float t = 1.0 + ((a2 - 1.0) * tNH * tNH);
	return (a2 - 1.0) / (PI * log(a2) * t);
}

float anisotropicGTR(float tNH, float tHT, float tHB, float x, float y){
	return 1.0 / (PI * x * y * sqrt(sqrt(tHT / x) + sqrt(tHB / y) + sqrt(tNH)));
}

float smithGGX(float tNI, float tNV, float alpha2){
	float a = tNV * sqrt(alpha2 + tNI * (tNI - alpha2 * tNI));
	float b = tNI * sqrt(alpha2 + tNV * (tNV - alpha2 * tNV));

	return 0.5f / (a + b);
}

float anisotropicSmithGGX(float tNS, float tST, float tSB, float x, float y){
	return 1.0 / (tNS + sqrt(sqrt(tST * x) + sqrt(tSB * y) + sqrt(tNS)));
}

void CalcDirLight(vec3 lightDir, DirLight light, inout vec3 diffuse, inout vec3 specular)
{
	lightDir = -lightDir;

	// diffuse shading
	float diff = max(dot(normal, lightDir), 0.0);

	// specular shading
	vec3 reflectDir = reflect(-lightDir, normal);

	// combine results
	diffuse += light.diffuse * diff;
	//specular += light.specular;
}

void CalcPointLight(vec3 lightPos, PointLight light, inout vec3 diffuse, inout vec3 specular)
{
	// diffuse
	vec3 lightDir = normalize(lightPos - tangentFragPos);
	vec3 halfway = normalize(viewDir + lightDir);

	float tIH = clampDot(lightDir, halfway);
	float tNI = clampDot(normal, lightDir);
	float tNH = clampDot(normal, halfway);

	float fresnel = doFresnel ? calculateFresnel(pm.roughness, tIH, tNI) : 1.0;
	float fresnelPower = schlick(tIH);
	vec3 sheen = sheenColour * fresnelPower * material.sheen;

	// specular shading
	float alpha2 = pm.roughness * pm.roughness;
	float aspRat = sqrt(1.0 - material.anisotropic * .9);
	float tanAlpha = max(.001, alpha2 / aspRat);
	float biAlpha = max(.001, alpha2 * aspRat);
	//float ds = anisotropicGTR(tNH, abs(dot(halfway, tangent)), abs(dot(halfway, bitangent)), tanAlpha, biAlpha);
	float ds = GTR(tNH, max(.001, alpha2));

	vec3 specFresnel = mix(specColour, vec3(1.0), fresnelPower);

	float gAlpha2 = sqrt(.5 + (pm.roughness * .5));
	float gTanAlpha = max(.001, gAlpha2 / aspRat);
	float gBiAlpha = max(.001, gAlpha2 * aspRat);
	//float g = anisotropicSmithGGX(tNI, abs(dot(lightDir, tangent)), abs(dot(lightDir, bitangent)), gTanAlpha, gBiAlpha);
	//g *= anisotropicSmithGGX(tNV, abs(dot(viewDir, tangent)), abs(dot(viewDir, bitangent)), gTanAlpha, gBiAlpha);
	float g = smithGGX(tNI, tNV, gAlpha2);


	// attenuation
	float distance = length(lightPos - tangentFragPos);
	float attenuation = 1.0 / (light.constant + light.linear * distance +
	light.quadratic * (distance * distance));

	// combine results
	diffuse += ((light.diffuse * fresnel * pm.baseColour.xyz) + sheen) * tNI * attenuation;
	specular += light.specular * ds * g * specFresnel * tNI * attenuation;
	//specular += vec3(abs(dot(halfway, bitangent))) * material.specTint * 10.0;
	//specular = (vec3(1) + normalize(lightPos)) * .5;// * material.specTint * 10.0;
}

// calculates the color when using a spot light.
void CalcSpotLight(vec3 lightPos, vec3 spotlightDir, SpotLight light, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(tangentFragPos - tangentFragPos);
	// diffuse shading
	float diff = max(dot(normal, -lightDir), 0.0);
	// specular shading
	vec3 reflectDir = reflect(lightDir, normal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
	// attenuation
	float distance = length(lightPos - tangentFragPos);
	float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
	// spotlight intensity
	float theta = dot(lightDir, normalize(spotlightDir));
	float epsilon = light.cutOff - light.outerCutOff;
	float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
	// combine results
	diffuse += light.diffuse * attenuation * diff * intensity;
	//specular += light.specular * attenuation * spec * intensity;
}