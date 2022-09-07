#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;
uniform int fastLight;
uniform float timeOfDay;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

out vec3 blockPos;
out vec3 colorMultiplier;
out vec4 defaultVertex;
out float skylight;
out vec3 meshNormal;

void main() {
	vec3 pos = Position + ChunkOffset;
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
	
	meshNormal = Normal;
	skylight = clamp(UV2.y / 150.0, 0.0, 1.0) * clamp(sin(1.5707 - timeOfDay * 6.2831) * 3.0, 0.0, 1.0);
	defaultVertex = Color * minecraft_sample_lightmap(Sampler2, UV2);
	float offset = (1 - fastLight) * clamp((max(Normal.x, max(Normal.y, Normal.z)) - 0.9) * 10.0, 0.0, 1.0);
	blockPos = Position;
	colorMultiplier = Color.rgb;
	
	vertexDistance = fog_distance(ModelViewMat, pos, FogShape);
	vertexColor = Color * minecraft_sample_lightmap(Sampler2, ivec2(0, UV2.y));
	texCoord0 = UV0;
	normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
