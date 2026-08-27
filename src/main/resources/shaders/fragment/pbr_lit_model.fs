// FRAGMENT SHADER INFORMATION
#version 430 core
#define NR_POINT_LIGHTS 4

struct Material{
	vec4 baseColourFactor;
	uint textureMask;
	float metallicFactor;
	float roughnessFactor;
	float emissiveFactor;
};

struct PixelMat {
	vec4 baseColour;
	float roughness;
	float metallic;
};

struct DirLight{
	vec3 direction;

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;
};
struct PointLight{
	vec3 position;
	float constant;

	vec3 ambient;
	float linear;
	vec3 diffuse;
	float quadratic;
	vec3 specular;

};
struct SpotLight{
	vec3 position;
	float constant;
	vec3 direction;
	float linear;

	vec3 ambient;
	float quadratic;
	vec3 diffuse;
	float cutOff;
	vec3 specular;
	float outerCutOff;
};

#ifdef OPENGL
in vec3 fragPos;
in vec2 texCoords;
in vec3 normal;

// MANUAL
uniform Material material;
uniform sampler2D baseColourTexture;
uniform sampler2D metallicRoughnessTexture;
uniform sampler2D normalsTexture;
uniform sampler2D occlusionTexture;
uniform sampler2D emissiveTexture;

uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotlight;

uniform vec3 viewPos;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec3 fragPos;
layout(location = 1) in vec2 texCoords;
layout(location = 2) in vec3 normal;

layout(set = 1, binding = 1) uniform sampler2D baseColourTexture;
layout(set = 1, binding = 2) uniform sampler2D metallicRoughnessTexture;
layout(set = 1, binding = 3) uniform sampler2D normalsTexture;
layout(set = 1, binding = 4) uniform sampler2D occlusionTexture;
layout(set = 1, binding = 5) uniform sampler2D emissiveTexture;

layout(set = 1, binding = 6) uniform Lights {
	DirLight dirLight;
	PointLight pointLights[NR_POINT_LIGHTS];
	SpotLight spotlight;
};

layout(push_constant) uniform Data {
	layout(offset = 80) Material material;
	vec3 viewPos;
};

layout(location = 0) out vec4 FragColour;
#endif

bool hasTexture(uint texture);

float calculateFresnel(float roughness, vec3 incidence, vec3 halfway, vec3 normal, vec3 outgoing){
	float F0 = 1.0;
	float thetaD = dot(incidence, halfway);
	float F90 = .5 + (2 * roughness * thetaD * thetaD);
	return mix(F0, F90, dot(normal, incidence)) * mix(F0, F90, dot(outgoing, incidence));
}

void CalcDirLight(DirLight light, vec3 normal, vec3 viewDir, inout vec3 diffuse, inout vec3 specular);
void CalcPointLight(PointLight light, PixelMat pm, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 diffuse, inout vec3 specular);
void CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 diffuse, inout vec3 specular);

void main(){

	PixelMat pointMaterial;

	// properties
	vec3 norm = hasTexture(4u) ? texture(normalsTexture, texCoords).xyz : normal;
	vec3 viewDir = normalize(viewPos - fragPos);

	pointMaterial.baseColour = hasTexture(1u) ? texture(baseColourTexture, texCoords) * material.baseColourFactor : material.baseColourFactor;

	vec2 mr = hasTexture(2u) ? texture(metallicRoughnessTexture, texCoords).yz : vec2(1);
	pointMaterial.metallic = mr.y * material.metallicFactor;
	pointMaterial.roughness = mr.x * material.roughnessFactor;

	vec3 diffuse = vec3(0.0);
	vec3 specular = vec3(0.0);

	// == =====================================================
	// Our lighting is set up in 3 phases: directional, point lights and an optional flashlight
	// For each phase, a calculate function is defined that calculates the corresponding color
	// per lamp. In the main() function we take all the calculated colors and sum them up for
	// this fragment's final color.
	// == =====================================================

	// phase 1: directional lighting
	//CalcDirLight(dirLight, norm, viewDir, diffuse, specular);


	// phase 2: point lights
	for(int i = 0; i < NR_POINT_LIGHTS; i++)
		if(pointLights[i].constant > 0 && pointLights[i].linear > 0 && pointLights[i].quadratic > 0)
			CalcPointLight(pointLights[i], pointMaterial, norm, fragPos, viewDir, diffuse, specular);


	// phase 3: spot light
	//if(spotlight.constant > 0 && spotlight.linear > 0 && spotlight.quadratic > 0)
	//	CalcSpotLight(spotlight, norm, fragPos, viewDir, ambient, diffuse, specular);

	vec3 result = (diffuse * pointMaterial.baseColour.xyz) + specular;
	FragColour = vec4(result, pointMaterial.baseColour.a);

	// For Smooth: FragColour = vec4(vec3(.5 + (.5 * dot(normal, normalize(vec3(1.0))))), 1.0);
}

bool hasTexture(uint texture){
	return (material.textureMask & texture) == texture;
}

void CalcDirLight(DirLight light, vec3 normal, vec3 viewDir, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(-light.direction);

	// diffuse shading
	float diff = max(dot(normal, lightDir), 0.0);

	// specular shading
	vec3 reflectDir = reflect(-lightDir, normal);

	// combine results
	diffuse += light.diffuse * diff;
	//specular += light.specular;
}

void CalcPointLight(PointLight light, PixelMat pm, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(light.position - fragPos);
	vec3 halfway = (viewDir + lightDir) * .5;
	// diffuse shading
	float diff = max(dot(normal, lightDir), 0.0);
	// specular shading
	vec3 reflectDir = reflect(-lightDir, normal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
	// attenuation
	float distance    = length(light.position - fragPos);
	float attenuation = 1.0 / (light.constant + light.linear * distance +
	light.quadratic * (distance * distance));

	float fresnel = calculateFresnel(pm.roughness, lightDir, normal, halfway, viewDir);
	// combine results
	diffuse += light.diffuse * diff * fresnel;
	specular += light.specular * attenuation * spec;
}

// calculates the color when using a spot light.
void CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(fragPos - light.position);
	// diffuse shading
	float diff = max(dot(normal, -lightDir), 0.0);
	// specular shading
	vec3 reflectDir = reflect(lightDir, normal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
	// attenuation
	float distance = length(light.position - fragPos);
	float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
	// spotlight intensity
	float theta = dot(lightDir, normalize(light.direction));
	float epsilon = light.cutOff - light.outerCutOff;
	float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
	// combine results
	diffuse += light.diffuse * attenuation * diff * intensity;
	specular += light.specular * attenuation * spec * intensity;
}

/*

			// Bottom Point
			PointData(Vec3(0f, -ngr, -m), Vec2(187f, 11f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(557f, 81f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(927f, 5f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1297f, 2f) * pixSize),
			PointData(Vec3(0f, -ngr, -m), Vec2(1668f, 0f) * pixSize),

			// Lower Ring
			PointData(Vec3(0f, -ngr, m), Vec2(6f, 347f) * pixSize),
			PointData(Vec3(ngr, -m, 0f), Vec2(texDivs.x * 3f, texDivs.y + .01f)),
			PointData(Vec3(m, 0f, -ngr), Vec2(texDivs.x * 5f, texDivs.y + .01f)),
			PointData(Vec3(-m, 0f, -ngr), Vec2(texDivs.x * 7f, texDivs.y + .01f)),
			PointData(Vec3(-ngr, -m, 0f), Vec2(texDivs.x * 9f, texDivs.y + .01f)),
			PointData(Vec3(0f, -ngr, m), Vec2(1857f, 337f) * pixSize),

			// Upper Ring
			PointData(Vec3(-m, 0f, ngr), Vec2(191f, 680f) * pixSize),
			PointData(Vec3(m, 0f, ngr), Vec2(texDivs.x * 2f, texDivs.y * 2f - .01f)),
			PointData(Vec3(ngr, m, 0f), Vec2(texDivs.x * 4f, texDivs.y * 2f - .01f)),
			PointData(Vec3(0f, ngr, -m), Vec2(texDivs.x * 6f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-ngr, m, 0f), Vec2(texDivs.x * 8f, texDivs.y * 2f - .01f)),
			PointData(Vec3(-m, 0f, ngr), Vec2(2041f, 670f) * pixSize),

			PointData(Vec3(0f, ngr, m), Vec2(379f, 1017f)  * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(749f, 1014f)  * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1120f, 1011f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1490f, 1008f) * pixSize),
			PointData(Vec3(0f, ngr, m), Vec2(1860f, 1005f) * pixSize),
*/