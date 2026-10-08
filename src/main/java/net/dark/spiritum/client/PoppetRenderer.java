package net.dark.spiritum.client;

import net.dark.spiritum.entity.PoppetEntity;
import net.dark.spiritum.entity.poppet.PoppetModelDefinition;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

public class PoppetRenderer extends EntityRenderer<PoppetEntity, PoppetRenderer.State> {
    private static final Identifier TEXTURE =
            Identifier.of("spiritum", "textures/entity/poppet.png");
    private final ModelPart[] parts = new ModelPart[6];
    private final Map<PoppetEntity, Pose> poses = new WeakHashMap<>();
    private static final Quaternionf MODEL_AXES =
            new Quaternionf().rotationX(-(float) Math.PI / 2).rotateZ((float) Math.PI);

    public static class State extends EntityRenderState {
        final Vector3f[] positions = new Vector3f[6];
        final Quaternionf[] rotations = new Quaternionf[6];

        State() {
            for (int i = 0; i < 6; i++) {
                positions[i] = new Vector3f();
                rotations[i] = new Quaternionf();
            }
        }
    }

    public PoppetRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = .25f;
        for (int i = 0; i < 6; i++) {
            ModelData data = new ModelData();
            var part = PoppetModelDefinition.PARTS.get(i);
            float width = (float) part.half().x * 32;
            float height = (float) part.half().z * 32;
            float depth = (float) part.half().y * 32;
            data.getRoot()
                    .addChild(
                            "piece",
                            ModelPartBuilder.create()
                                    .uv(part.u(), part.v())
                                    .mirrored(part.mirrored())
                                    .cuboid(
                                            -width / 2,
                                            -height / 2,
                                            -depth / 2,
                                            width,
                                            height,
                                            depth),
                            ModelTransform.NONE);
            parts[i] =
                    TexturedModelData.of(
                                    data,
                                    PoppetModelDefinition.TEXTURE_WIDTH,
                                    PoppetModelDefinition.TEXTURE_HEIGHT)
                            .createModel()
                            .getChild("piece");
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PoppetEntity entity, State state, float delta) {
        super.updateRenderState(entity, state, delta);
        Pose pose = poses.computeIfAbsent(entity, Pose::new);
        pose.capture(entity);
        for (int i = 0; i < 6; i++) {
            var last = pose.previousPositions[i];
            var current = pose.positions[i];
            state.positions[i].set(
                    (float) (last.x + (current.x - last.x) * delta - state.x),
                    (float) (last.y + (current.y - last.y) * delta - state.y),
                    (float) (last.z + (current.z - last.z) * delta - state.z));
            pose.previousRotations[i].slerp(pose.rotations[i], delta, state.rotations[i]);
        }
    }

    @Override
    public void render(
            State state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera) {
        for (int i = 0; i < 6; i++) {
            matrices.push();
            Vector3f p = state.positions[i];
            matrices.translate(p.x, p.y, p.z);
            matrices.multiply(state.rotations[i]);
            matrices.multiply(MODEL_AXES);
            queue.submitModelPart(
                    parts[i],
                    matrices,
                    RenderLayers.entityCutout(TEXTURE),
                    state.light,
                    OverlayTexture.DEFAULT_UV,
                    null);
            matrices.pop();
        }
        super.render(state, matrices, queue, camera);
    }

    private static final class Pose {
        final org.joml.Vector3d[] previousPositions = new org.joml.Vector3d[6];
        final org.joml.Vector3d[] positions = new org.joml.Vector3d[6];
        final Quaternionf[] previousRotations = new Quaternionf[6];
        final Quaternionf[] rotations = new Quaternionf[6];
        int tick = Integer.MIN_VALUE;

        Pose(PoppetEntity entity) {
            for (int i = 0; i < 6; i++) {
                previousPositions[i] = new org.joml.Vector3d();
                positions[i] = new org.joml.Vector3d();
                previousRotations[i] = new Quaternionf();
                rotations[i] = new Quaternionf();
            }
            capture(entity);
        }

        void capture(PoppetEntity entity) {
            if (tick == entity.age) return;
            boolean reset = tick != entity.age - 1;
            for (int i = 0; i < 6; i++) {
                previousPositions[i].set(positions[i]);
                previousRotations[i].set(rotations[i]);
                var offset = entity.partPosition(i);
                positions[i].set(
                        entity.getX() + offset.x(),
                        entity.getY() + offset.y(),
                        entity.getZ() + offset.z());
                rotations[i].set(entity.partRotation(i));
                if (reset) {
                    previousPositions[i].set(positions[i]);
                    previousRotations[i].set(rotations[i]);
                }
            }
            tick = entity.age;
        }
    }
}
