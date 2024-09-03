// FRAGMENT SHADER INFORMATION
#version 400 core
#define NR_POINT_LIGHTS 4

struct Material{
	sampler2D ambient;
	sampler2D diffuse;
	sampler2D specular;
	sampler2D normals;
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

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;

	float constant;
	float linear;
	float quadratic;
};
struct SpotLight{
	vec3 position;
	vec3 direction;

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;

	float constant;
	float linear;
	float quadratic;

	float cutOff;
	float outerCutOff;
};

in vec3 fragPos;
in vec2 texCoords;
in vec3 normal;

uniform vec3 viewPos;
uniform Material material;

uniform DirLight dirLight;
uniform PointLight pointLights[NR_POINT_LIGHTS];
uniform SpotLight spotlight;

out vec4 FragColour;

bool hasTexture(uint texture);
vec3 CalcDirLight(DirLight light, vec3 normal, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular);
void CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular);
void CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, inout vec3 ambient, inout vec3 diffuse, inout vec3 specular);

void main(){

	// properties
	vec3 norm = hasTexture(8u) ? texture(material.normals, texCoords).xyz : normal;
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

	if(hasTexture(1u)) ambient *= texture(material.ambient, texCoords).xyz;
	else if(material.ambDiff && hasTexture(2u)) ambient *= texture(material.diffuse, texCoords).xyz;

	// diffuse
	if(hasTexture(2u)) diffuse *= texture(material.diffuse, texCoords).xyz;

	//specular
	if(hasTexture(4u)) specular *= texture(material.specular, texCoords).xyz;

	vec3 result = ambient + diffuse + specular;
	FragColour = vec4(result, texture(material.diffuse, texCoords).a * material.alpha);
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