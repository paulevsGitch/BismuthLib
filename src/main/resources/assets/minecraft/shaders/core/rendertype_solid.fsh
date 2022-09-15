#version 150
#moj_import <fog.glsl>
#moj_import <colorful_fabric.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec4 FogColor;
uniform float FogStart;
uniform float FogEnd;

uniform sampler2D Sampler7;

uniform vec3 ChunkOffset;
uniform ivec3 playerSectionPos;
uniform ivec2 dataScale;
uniform int dataSide;
uniform int fastLight;
uniform float lightsBrightness;

in vec4 defaultVertex;
in vec3 colorMultiplier;
in vec3 blockPos;
in float skylight;

in vec3 meshNormal;
in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
	int textureSize = textureSize(Sampler7, 0).x;
	vec4 tex = texture(Sampler0, texCoord0);
	vec4 color = tex * ColorModulator;
	color = addColoredLight(color, vertexColor, tex, Sampler7, blockPos, playerSectionPos, ChunkOffset, dataScale, dataSide, skylight, defaultVertex, fastLight, colorMultiplier, meshNormal, lightsBrightness);
	color = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
	fragColor = color;
}
