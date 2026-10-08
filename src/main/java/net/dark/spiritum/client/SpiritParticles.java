package net.dark.spiritum.client;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.registry.ModParticles;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public final class SpiritParticles {
    private record SigilKey(BlockPos pedestal, String texture) {}

    private static final Map<SigilKey, SpiritParticle> sigils = new HashMap<>();
    private static ClientWorld sigilWorld;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    if (sigilWorld != client.world) {
                        sigils.clear();
                        sigilWorld = client.world;
                    }
                });
        var factories = ParticleFactoryRegistry.getInstance();
        factories.register(ModParticles.DOMINION_RUNE, sprites ->
                (type, world, x, y, z, nx, ny, nz, random) ->
                        new DominionRune(world,x,y,z,sprites.getSprite(random),nx,ny,nz));
        factories.register(
                ModParticles.HEXFLAME,
                sprites ->
                        (type, world, x, y, z, vx, vy, vz, random) ->
                                new SpiritParticle(
                                        world,
                                        x,
                                        y,
                                        z,
                                        sprites.getSprite(random),
                                        "hexflame_particle",
                                        false,
                                        false));
        factories.register(
                ModParticles.BIG_HEXFLAME,
                sprites ->
                        (type, world, x, y, z, vx, vy, vz, random) ->
                                new SpiritParticle(
                                        world,
                                        x,
                                        y,
                                        z,
                                        sprites.getSprite(random),
                                        "big_hexflame_particle",
                                        false,
                                        true));
        for (var sigil : ModParticles.SIGILS.entrySet())
            factories.register(
                    sigil.getValue(),
                    sprites ->
                            (type, world, x, y, z, vx, vy, vz, random) ->
                                    sigilParticle(
                                            world,
                                            x,
                                            y,
                                            z,
                                            sprites.getSprite(random),
                                            sigil.getKey() + "_rite"));
    }

    private static SpiritParticle sigilParticle(
            ClientWorld world, double x, double y, double z, Sprite sprite, String texture) {
        if (sigilWorld != world) {
            sigils.clear();
            sigilWorld = world;
        }
        var key = new SigilKey(BlockPos.ofFloored(x, y - 2, z), texture);
        var existing = sigils.get(key);
        if (existing != null && existing.isAlive()) {
            existing.refresh();
            return null;
        }
        var particle = new SpiritParticle(world, x, y, z, sprite, texture, true, false);
        particle.sigilKey = key;
        sigils.put(key, particle);
        return particle;
    }

    private static final class DominionRune extends BillboardParticle {
        private final org.joml.Quaternionf rotation;
        private final RenderType renderType = SpiritParticleShaders.type("dominion_rune", false);
        DominionRune(ClientWorld world, double x, double y, double z, Sprite sprite, double nx, double ny, double nz) {
            super(world,x,y,z,sprite);
            org.joml.Vector3f normal=new org.joml.Vector3f((float)nx,(float)ny,(float)nz);
            if(normal.lengthSquared()<.01f) normal.set(0,0,1);
            rotation=new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0,0,1),normal.normalize());
            maxAge=20; scale=.3f; collidesWithWorld=false;
            velocityX=velocityY=velocityZ=0;
            MinecraftClient.getInstance().getTextureManager().getTexture(renderType.textureAtlasLocation());
        }
        @Override public Rotator getRotator() { return (orientation,camera,delta)->orientation.set(rotation); }
        @Override protected RenderType getRenderType() { return renderType; }
    }

    private static final class SpiritParticle extends BillboardParticle {
        private final boolean sigil;
        private final RenderType coreType, bloomType;
        private boolean bloomPass;
        private SigilKey sigilKey;
        private float lastAlpha = 1;

        void refresh() {
            age = 0;
        }

        SpiritParticle(
                ClientWorld world,
                double x,
                double y,
                double z,
                Sprite sprite,
                String texture,
                boolean sigil,
                boolean strong) {
            super(world, x, y, z, sprite);
            this.sigil = sigil;
            coreType = SpiritParticleShaders.type(texture, false);
            bloomType = SpiritParticleShaders.type(texture, true);
            // Upload before drawing: first-use texture uploads cannot run inside a render pass.
            MinecraftClient.getInstance()
                    .getTextureManager()
                    .getTexture(coreType.textureAtlasLocation());
            scale = sigil ? .7f : strong ? .12f : .075f;
            maxAge = sigil ? 20 : 12;
            velocityY = sigil ? 0 : .012;
            collidesWithWorld = false;
            alpha = 1;
            zRotation = sigil ? world.getTime() * .008f : 0;
            lastZRotation = zRotation;
        }

        @Override
        public void tick() {
            lastAlpha = alpha;
            super.tick();
            if (sigil) {
                if (!(world.getBlockEntity(sigilKey.pedestal())
                                instanceof PedestalBlockEntity pedestal)
                        || pedestal.getActiveRitual().isEmpty()) {
                    markDead();
                    return;
                }
                lastZRotation = zRotation;
                zRotation += .008f;
            } else alpha = 1 - (float) age / maxAge;
        }

        @Override
        public void markDead() {
            super.markDead();
            if (sigilKey != null) sigils.remove(sigilKey, this);
        }

        @Override
        public Rotator getRotator() {
            return Rotator.ALL_AXIS;
        }

        @Override
        public void render(BillboardParticleSubmittable submit, Camera camera, float delta) {
            float originalScale = scale;
            float originalAlpha = alpha;
            alpha = MathHelper.lerp(delta, lastAlpha, alpha);
            // Pad the plane for a Gaussian blur outside the artwork's silhouette.
            bloomPass = true;
            scale = originalScale * 2;
            super.render(submit, camera, delta);
            bloomPass = false;
            scale = originalScale;
            super.render(submit, camera, delta);
            alpha = originalAlpha;
        }

        @Override
        protected RenderType getRenderType() {
            return bloomPass ? bloomType : coreType;
        }

        @Override
        protected float getMinU() {
            return bloomPass ? -.5f : 0;
        }

        @Override
        protected float getMaxU() {
            return bloomPass ? 1.5f : 1;
        }

        @Override
        protected float getMinV() {
            return bloomPass ? -.5f : 0;
        }

        @Override
        protected float getMaxV() {
            return bloomPass ? 1.5f : 1;
        }

        @Override
        protected int getBrightness(float tickProgress) {
            return 0xF000F0;
        }
    }

    private SpiritParticles() {}
}
