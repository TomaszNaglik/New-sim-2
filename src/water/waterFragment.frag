#version 400 core

in vec4 clipSpace;
in vec2 textureCoords;
in vec3 toCameraVector;
in vec3 fromLightVector;

out vec4 out_Color;

uniform sampler2D reflectionTexture; // above water
uniform sampler2D refractionTexture; // under water
uniform sampler2D dudvMap;
uniform sampler2D normalMap;
uniform sampler2D depthMap;
uniform vec3 lightColor;

uniform float moveFactor;

const float waveStrength = 0.04f;
const float shineDamper = 20.0f;
const float reflectivity = 0.5f;

void main(void) {

	/* Projective Texture Mapping & Clipping Clipping Planes */
	vec2 ndc = (clipSpace.xy/clipSpace.w) / 2.0f + 0.5f; //normalizedDeviceCoordinates
	vec2 refractTexCoords = vec2(ndc.x, ndc.y);
	vec2 reflectTexCoords = vec2(ndc.x, -ndc.y);
	
	/* Soft Edges */
	float near = 0.1f; // this should be loaded up from master renderer, hard coded for laziness
	float far = 1000.0f; // this should be loaded up from master renderer, hard coded for laziness
	float depth = texture(depthMap, refractTexCoords).r;
	float floorDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
	depth = gl_FragCoord.z;
	float waterDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
	float waterDepth = floorDistance - waterDistance;
	
	
	
	/* DuDv maps */
	vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg*0.1;
	distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y+moveFactor);
	vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength * clamp(waterDepth / 20.0f, 0.0f, 1.0f); /*10min Soft Edges Tutorial included to get rid of edge glitch */
	refractTexCoords += totalDistortion;
	refractTexCoords = clamp(refractTexCoords, 0.001f, 0.999f);
	reflectTexCoords += totalDistortion;
	refractTexCoords.x = clamp(refractTexCoords.x, 0.001f, 0.999f);
	refractTexCoords.y = clamp(refractTexCoords.y, -0.999f, -0.001f);
	
	/* Projective Texture Mapping & Clipping Clipping Planes */
	vec4 reflectColor = texture(reflectionTexture, reflectTexCoords);
	vec4 refractColor = texture(refractionTexture, refractTexCoords);
	
	/* Normal Map */
	vec4 normalMapColor = texture(normalMap, distortedTexCoords);
	// x,y,z = r,b,g of the r,g,b
	vec3 normal = vec3(normalMapColor.r * 2.0f - 1.0f, normalMapColor.b * 3.0f, normalMapColor.g * 2.0f - 1.0f);
	normal = normalize(normal);
	
	/* Fresnel Effect */
	vec3 viewVector = normalize(toCameraVector);
	float refractiveFactor = dot(viewVector, normal); // the distance between vectors camera and pointing straight up
	refractiveFactor = pow(refractiveFactor, 0.5f); // the greater the power the more reflective it is
	refractiveFactor = clamp(refractiveFactor, 0.0f, 1.0f); // rids of black artifacts
	
	/* Normal Map continue */
	vec3 reflectedLight = reflect(normalize(fromLightVector), normal);
	float specular = max(dot(reflectedLight, viewVector), 0.0f);
	specular = pow(specular, shineDamper);
	vec3 specularHighlights = lightColor * specular * reflectivity * clamp(waterDepth / 5.0f, 0.0f, 1.0f); /* Soft Edges Tutorial included to dim specular highlights around edges to not ruin the soft edges  */




	out_Color = mix(reflectColor, refractColor, refractiveFactor); // mix gradient of fresnel effect
	out_Color = mix(out_Color, vec4(0.0f, 0.3f, 0.5f, 1.0f), 0.2) + vec4(specularHighlights, 1.0f); // mix a tint of bluish green to the water plus the normal map
	
	/* Soft Edges continue */
	out_Color.a = clamp(waterDepth / 5.0f, 0.0f, 1.0f); // increase the soft edges by increasing denominator

}