package net.dark.spiritum.client;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/** Local emissive sprite and Gaussian bloom passes; leaves the world's shader pipeline intact. */
public final class SpiritParticleShaders {
    private static RenderPipeline.Builder base(String name) {
        return RenderPipeline.builder(RenderPipelines.TRANSFORMS_PROJECTION_FOG_SNIPPET)
                .withLocation(Identifier.of("spiritum", "pipeline/" + name))
                .withVertexShader(Identifier.of("spiritum", "core/spirit_particle"))
                .withFragmentShader(Identifier.of("spiritum", "core/spirit_particle"))
                .withSampler("Sampler0")
                .withVertexFormat(
                        VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, VertexFormat.DrawMode.QUADS)
                .withCull(false)
                .withDepthWrite(false);
    }

    private static final RenderPipeline CORE =
            RenderPipelines.register(
                    base("emissive_particle").withBlend(BlendFunction.TRANSLUCENT).build());
    private static final RenderPipeline BLOOM =
            RenderPipelines.register(
                    base("particle_bloom")
                            .withShaderDefine("SPIRIT_BLOOM")
                            .withBlend(BlendFunction.ADDITIVE)
                            .build());

    public static BillboardParticle.RenderType type(String texture, boolean bloom) {
        return new BillboardParticle.RenderType(
                true,
                Identifier.of("spiritum", "textures/particle/" + texture + ".png"),
                bloom ? BLOOM : CORE);
    }

    private SpiritParticleShaders() {}
}
