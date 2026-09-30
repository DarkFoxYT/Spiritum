#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

vec4 sourceAt(vec2 uv) {
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return vec4(0.0);
    return texture(Sampler0, uv);
}

void main() {
#ifdef SPIRIT_BLOOM
    // Blur premultiplied artwork, keeping transparent edge RGB out of the halo.
    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec3 glow = vec3(0.0);
    float totalWeight = 0.0;
    for (int x = -4; x <= 4; ++x) {
        for (int y = -4; y <= 4; ++y) {
            vec2 offset = vec2(float(x), float(y));
            float weight = exp(-dot(offset, offset) / 8.0);
            vec4 sampleColor = sourceAt(texCoord0 + offset * texel);
            glow += sampleColor.rgb * sampleColor.a * weight;
            totalWeight += weight;
        }
    }
    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    ivec2 dimensions = textureSize(Sampler0, 0);
    ivec2 pixel = clamp(ivec2(texCoord0 * dimensions), ivec2(0), dimensions - ivec2(1));
    bool inside = all(greaterThanEqual(texCoord0, vec2(0.0))) && all(lessThanEqual(texCoord0, vec2(1.0)));
    float artworkAlpha = inside ? texelFetch(Sampler0, pixel, 0).a : 0.0;
    // Leave every opaque source pixel untouched: the halo only surrounds the original artwork.
    vec3 emission = glow / totalWeight * vertexColor.rgb * vertexColor.a * ColorModulator.rgb * 0.3 * (1.0 - artworkAlpha);
    // The additive pass contributes radiance only; no world-wide post-processing is required.
    fragColor = vec4(emission * (1.0 - fog), 0.0);
#else
    ivec2 dimensions = textureSize(Sampler0, 0);
    ivec2 pixel = clamp(ivec2(texCoord0 * dimensions), ivec2(0), dimensions - ivec2(1));
    vec4 color = texelFetch(Sampler0, pixel, 0) * vertexColor * ColorModulator;
    if (color.a < 0.01) discard;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#endif
}
