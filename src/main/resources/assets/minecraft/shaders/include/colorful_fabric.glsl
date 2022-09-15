#version 150

vec3 rgbToHSV(vec3 color) {
	vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
	vec4 p = mix(vec4(color.bg, k.wz), vec4(color.gb, k.xy), step(color.b, color.g));
	vec4 q = mix(vec4(p.xyw, color.r), vec4(color.r, p.yzx), step(p.x, color.r));
	float d = q.x - min(q.w, q.y);
	float e = 1.0e-10;
	return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsvToRGB(vec3 color) {
	vec4 k = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
	vec3 p = abs(fract(color.xxx + k.xyz) * 6.0 - k.www);
	return color.z * mix(k.xxx, clamp(p - k.xxx, 0.0, 1.0), color.y);
}

// Value near 254
bool isEmissive(float alpha) {
	return 0.9960784 < alpha && alpha < 0.9960785;
}

int getSectionIndex(int sectionX, int sectionY, int sectionZ, int scaleX, int scaleY) {
	return ((sectionX * scaleY) + sectionY) * scaleX + sectionZ;
}

int wrap(int value, int side) {
	int offset = int(value / side) * side;
	if (offset > value) {
		offset -= side;
	}
	float delta = float(value - offset) / side;
	return int(delta * side);
}

vec2 indexToUV(int textureSize, int index, int dataSide) {
	float scale = 64.0 / textureSize;
	float x = wrap(index, dataSide) * scale;
	float y = int(index / dataSide) * scale;
	return vec2(x, y);
}

vec3 getColor(ivec3 sectionPos, ivec3 blockPos, int textureSize, int sideX, int sideY, sampler2D sampler, ivec3 playerSectionPos, ivec2 dataScale, int dataSide) {
	ivec3 spos = ivec3(sectionPos);
	ivec3 bpos = ivec3(blockPos);
	
	if (blockPos.x < 0) {
		spos.x -= 1;
		bpos.x += 16;
	}
	else if (blockPos.x > 15) {
		spos.x += 1;
		bpos.x -= 16;
	}
	
	if (blockPos.y < 0) {
		spos.y -= 1;
		bpos.y += 16;
	}
	else if (blockPos.y > 15) {
		spos.y += 1;
		bpos.y -= 16;
	}
	
	if (blockPos.z < 0) {
		spos.z -= 1;
		bpos.z += 16;
	}
	else if (blockPos.z > 15) {
		spos.z += 1;
		bpos.z -= 16;
	}
	
	vec3 color = vec3(0.0);
	
	if (abs(spos.x) <= sideX && abs(spos.y) <= sideY && abs(spos.z) <= sideX) {
		spos.x = wrap(spos.x + playerSectionPos.x, dataScale.x);
		spos.y = wrap(spos.y + playerSectionPos.y, dataScale.y);
		spos.z = wrap(spos.z + playerSectionPos.z, dataScale.x);
		
		int index = ((spos.x * dataScale.y) + spos.y) * dataScale.x + spos.z;
		vec2 uv = indexToUV(textureSize, index, dataSide);
		int blockIndex = bpos.x * 256 + bpos.y * 16 + bpos.z;
		uv.x += (wrap(blockIndex, 64) + 0.5) / textureSize;
		uv.y += (floor(blockIndex / 64.0) + 0.5) / textureSize;
		
		color = texture(sampler, uv).rgb;
	}
	
	return color;
}

vec3 getInterpolatedColor(sampler2D sampler, vec3 blockPos, ivec3 playerSectionPos, vec3 chunkOffset, ivec2 dataScale, int dataSide) {
	int textureSize = textureSize(sampler, 0).x;
	
	ivec3 sectionPos = ivec3(
		int(ceil(chunkOffset.x / 16.0)),
		int(ceil(chunkOffset.y / 16.0)),
		int(ceil(chunkOffset.z / 16.0))
	);
	
	int sideX = int(dataScale.x * 0.5);
	int sideY = int(dataScale.y * 0.5);
	
	vec3 bpos = blockPos - vec3(0.5);
	int bx1 = int(floor(bpos.x));
	int by1 = int(floor(bpos.y));
	int bz1 = int(floor(bpos.z));
	int bx2 = bx1 + 1;
	int by2 = by1 + 1;
	int bz2 = bz1 + 1;
	float dx = bpos.x - bx1;
	float dy = bpos.y - by1;
	float dz = bpos.z - bz1;
	
	vec3 a = getColor(sectionPos, ivec3(bx1, by1, bz1), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 b = getColor(sectionPos, ivec3(bx2, by1, bz1), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 c = getColor(sectionPos, ivec3(bx1, by2, bz1), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 d = getColor(sectionPos, ivec3(bx2, by2, bz1), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 e = getColor(sectionPos, ivec3(bx1, by1, bz2), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 f = getColor(sectionPos, ivec3(bx2, by1, bz2), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 g = getColor(sectionPos, ivec3(bx1, by2, bz2), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	vec3 h = getColor(sectionPos, ivec3(bx2, by2, bz2), textureSize, sideX, sideY, sampler, playerSectionPos, dataScale, dataSide);
	
	a = mix(a, b, dx);
	b = mix(c, d, dx);
	c = mix(e, f, dx);
	d = mix(g, h, dx);
	
	a = mix(a, b, dy);
	b = mix(c, d, dy);
	
	return mix(a, b, dz);
}

float getDistanceMix(vec3 blockPos, vec3 chunkOffset, ivec2 dataScale) {
	vec3 relPos = abs((chunkOffset + blockPos) / 16.0);
	vec2 sides = floor(vec2(dataScale) / 2.0) - vec2(1.0);
	
	float dx = relPos.x - sides.x;
	float dy = relPos.y - sides.y;
	float dz = relPos.z - sides.x;
	float m = max(dy, max(dx, dz));
	return clamp(m, 0.0, 1.0);
}

vec4 addColoredLight(vec4 color, vec4 vertex, vec4 tex, sampler2D sampler, vec3 blockPos, ivec3 playerSectionPos, vec3 chunkOffset, ivec2 dataScale, int dataSide, float skylight, vec4 defaultVertex, int fastLight, vec3 colorMultiplier, vec3 normal, float brightness) {
	//return vec4(skylight, skylight, skylight, 1.0);
	
	vec4 def = tex * defaultVertex;
	vec3 light1 = getInterpolatedColor(sampler, blockPos, playerSectionPos, chunkOffset, dataScale, dataSide);
	vec3 light2 = getInterpolatedColor(sampler, blockPos + normal * 0.5, playerSectionPos, chunkOffset, dataScale, dataSide);
	vec3 coloredLight = max(light1, light2) * (1.0 - skylight) * brightness;
	float m = getDistanceMix(blockPos, chunkOffset, dataScale);
	
	vec3 hsv = rgbToHSV(colorMultiplier.rgb);
	hsv.z = 1.0;
	hsv.y = min(hsv.y, 0.5);
	hsv = hsvToRGB(hsv);
	vec4 col;
	
	if (fastLight > 0) {
		float intensity = max(defaultVertex.r, max(defaultVertex.g, defaultVertex.b));
		intensity = clamp((intensity - 0.25) * 2.0, 0.0, 1.0);
		intensity = mix(1.3, 0.5, intensity);
		coloredLight = clamp(coloredLight * intensity, vec3(0.0), vec3(1.1)) * hsv;
		
		hsv = rgbToHSV(coloredLight);
		hsv.y = clamp(hsv.y * 1.2, 0.0, 1.0);
		coloredLight = hsvToRGB(hsv);
		
		def.rgb += coloredLight * tex.rgb * (1.0 - m);
		col = def;
	}
	else {
		vec3 back = tex.rgb * vertex.rgb;
		coloredLight = coloredLight * hsv;//clamp(coloredLight * 1.5, vec3(0.0), vec3(1.5)) * hsv;
		
		vertex.rgb = clamp(vertex.rgb - coloredLight * 0.15, vec3(0.0), vec3(1.0));
		
		hsv = rgbToHSV(coloredLight);
		hsv.y = clamp(hsv.y * 1.2, 0.0, 1.0);
		coloredLight = hsvToRGB(hsv);
		
		float mx = max(vertex.r, max(vertex.g, vertex.b));
		mx = clamp((mx - 0.2) * 1.5, 0.0, 1.0) * 0.5;
		mx *= max(coloredLight.r, max(coloredLight.g, coloredLight.b));
		col = color * vertex + vec4((coloredLight - mx) * tex.rgb * 1.1, 0.0);
		
		col = mix(col, def, m);
	}
	
	if (isEmissive(tex.a)) {
		hsv = rgbToHSV(col.rgb);
		hsv.z = 1.0;
		col.rgb = hsvToRGB(hsv);
		col.a = 1.0;
	}
	
	return col;
}