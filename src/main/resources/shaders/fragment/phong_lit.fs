// FRAGMENT SHADER INFORMATION
#version 430 core
#define NR_POINT_LIGHTS 4

struct Material{
	uint textureMask;
	bool ambDiff;
	float shininess;
	float alpha;
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

uniform vec3 viewPos;
uniform Material material;
uniform sampler2D ambientTexture;
uniform sampler2D diffuseTexture;
uniform sampler2D specularTexture;
uniform sampler2D normalsTexture;

uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotlight;

out vec4 FragColour;
#endif


#ifdef VULKAN
layout(location = 0) in vec3 fragPos;
layout(location = 1) in vec2 texCoords;
layout(location = 2) in vec3 normal;

// Bindings match PhongMaterial.TextureType ordinals
layout(set = 1, binding = 0) uniform sampler2D ambientTexture;
layout(set = 1, binding = 1) uniform sampler2D diffuseTexture;
layout(set = 1, binding = 2) uniform sampler2D specularTexture;
layout(set = 1, binding = 3) uniform sampler2D normalsTexture;

layout(set = 2, binding = 0) uniform Lights {
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
vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular);
void CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular);
void CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular);

void main(){

	// properties
	vec3 norm = hasTexture(8u) ? texture(normalsTexture, texCoords).xyz : normal;
	vec3 viewDir = normalize(viewPos - fragPos);

	vec3 ambient = vec3(0.0);
	vec3 diffuse = vec3(0.0);
	vec3 specular = vec3(0.0);

	// == =====================================================
	// Our lighting is set up in 3 phases: directional, point lights and an optional flashlight
	// For each phase, a calculate function is defined that calculates the corresponding color
	// per lamp. In the main() function we take all the calculated colors and sum them up for
	// this fragment's final color.
	// == =====================================================
	// phase 1: directional lighting
	CalcDirLight(dirLight, norm, viewDir, ambient, diffuse, specular);
	// phase 2: point lights
	for(int i = 0; i < NR_POINT_LIGHTS; i++)
		if(pointLights[i].constant > 0 && pointLights[i].linear > 0 && pointLights[i].quadratic > 0)
			CalcPointLight(pointLights[i], norm, fragPos, viewDir, ambient, diffuse, specular);
	// phase 3: spot light
	if(spotlight.constant > 0 && spotlight.linear > 0 && spotlight.quadratic > 0)
		CalcSpotLight(spotlight, norm, fragPos, viewDir, ambient, diffuse, specular);

	if (hasTexture(1u)) ambient *= texture(ambientTexture, texCoords).xyz;
	else if (material.ambDiff && hasTexture(2u)) ambient *= texture(diffuseTexture, texCoords).xyz;

	// diffuse
	if (hasTexture(2u)) diffuse *= texture(diffuseTexture, texCoords).xyz;

	//specular
	if (hasTexture(4u)) specular *= texture(specularTexture, texCoords).xyz;

	vec3 result = ambient + diffuse + specular;
	FragColour = vec4(result, texture(diffuseTexture, texCoords).a * material.alpha);
}

bool hasTexture(uint texture){
	return (material.textureMask & texture) == texture;
}

vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(-light.direction);
	// diffuse shading
	float diff = max(dot(normal, lightDir), 0.0);
	// specular shading
	vec3 reflectDir = reflect(-lightDir, normal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
	// combine results
	ambient += light.ambient;
	diffuse += light.diffuse  * diff;
	specular += light.specular * spec;

	return ambient + diffuse + specular;
}

void CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(light.position - fragPos);
	// diffuse shading
	float diff = max(dot(normal, lightDir), 0.0);
	// specular shading
	vec3 reflectDir = reflect(-lightDir, normal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
	// attenuation
	float distance    = length(light.position - fragPos);
	float attenuation = 1.0 / (light.constant + light.linear * distance +
	light.quadratic * (distance * distance));
	// combine results
	ambient += light.ambient * attenuation;
	diffuse += light.diffuse * attenuation  * diff;
	specular += light.specular * attenuation * spec;
}

// calculates the color when using a spot light.
void CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular)
{
	vec3 lightDir = normalize(fragPos - light.position);
	// diffuse shading
	float diff = max(dot(normal, -lightDir), 0.0);
	// specular shading
	vec3 reflectDir = reflect(lightDir, normal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
	// attenuation
	float distance = length(light.position - fragPos);
	float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
	// spotlight intensity
	float theta = dot(lightDir, normalize(light.direction));
	float epsilon = light.cutOff - light.outerCutOff;
	float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
	// combine results
	ambient += light.ambient * attenuation;
	diffuse += light.diffuse * attenuation * diff * intensity;
	specular += light.specular * attenuation * spec * intensity;
}