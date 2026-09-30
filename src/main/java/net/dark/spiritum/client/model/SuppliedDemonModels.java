package net.dark.spiritum.client.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.animation.*;

import java.util.*;

/** Converted from supplied Blockbench files by tools/import_assets.py. */
public final class SuppliedDemonModels {
    public static TexturedModelData leech() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData part1 =
                root.addChild(
                        "mouthrot",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, 22.5f, -7.5f, -0.0f, -0.0f, 0.0f));
        ModelPartData part2 =
                part1.addChild(
                        "body",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, 0.0f, 7.0f, -0.0f, -0.0f, 0.0f));
        part2.addChild(
                "cube_3",
                ModelPartBuilder.create()
                        .uv(0, 10)
                        .mirrored(false)
                        .cuboid(-2.5f, -0.5f, -5.5f, 5.0f, 2.0f, 11.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        part2.addChild(
                "cube_4",
                ModelPartBuilder.create()
                        .uv(30, 13)
                        .mirrored(false)
                        .cuboid(-2.5f, -0.5f, -6.5f, 2.0f, 2.0f, 1.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        part2.addChild(
                "cube_5",
                ModelPartBuilder.create()
                        .uv(30, 13)
                        .mirrored(true)
                        .cuboid(0.5f, -0.5f, -6.5f, 2.0f, 2.0f, 1.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part6 =
                part2.addChild(
                        "tail",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, -0.5f, 5.5f, -0.0f, -0.0f, 0.0f));
        part6.addChild(
                "cube_7",
                ModelPartBuilder.create()
                        .uv(0, 23)
                        .mirrored(false)
                        .cuboid(-2.5f, 0.0f, 0.0f, 5.0f, 0.0f, 10.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part8 =
                part2.addChild(
                        "leftwing",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-2.5f, -0.5f, -3.0f, -0.0f, -0.0f, 0.0f));
        part8.addChild(
                "cube_9",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .mirrored(false)
                        .cuboid(-9.0f, 0.0f, -4.5f, 9.0f, 0.0f, 10.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part10 =
                part2.addChild(
                        "rightwing",
                        ModelPartBuilder.create(),
                        ModelTransform.of(2.5f, -0.5f, -3.0f, -0.0f, -0.0f, 0.0f));
        part10.addChild(
                "cube_11",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .mirrored(true)
                        .cuboid(0.0f, 0.0f, -4.5f, 9.0f, 0.0f, 10.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 64);
    }

    public static Map<String, AnimationDefinition> leechAnimations() {
        Map<String, AnimationDefinition> result = new LinkedHashMap<>();
        AnimationDefinition.Builder fly = AnimationDefinition.Builder.create(0.5f).looping();
        fly.addBoneAnimation(
                "body",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-1.56f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(12.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(-12.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-1.56f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.625f,
                                AnimationHelper.createRotationalVector(12.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        fly.addBoneAnimation(
                "body",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, 2.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        fly.addBoneAnimation(
                "tail",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.08333f,
                                AnimationHelper.createRotationalVector(-13.73f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.20833f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(13.73f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        fly.addBoneAnimation(
                "leftwing",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, -67.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.08333f,
                                AnimationHelper.createRotationalVector(27.9538f, 30.2258f, 3.3129f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(-0.0f, -22.5f, 67.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, -67.5f),
                                Transformation.Interpolations.CUBIC)));
        fly.addBoneAnimation(
                "rightwing",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, 67.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.08333f,
                                AnimationHelper.createRotationalVector(
                                        27.9538f, -30.2258f, -3.3129f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(-0.0f, 22.5f, -67.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, 67.5f),
                                Transformation.Interpolations.CUBIC)));
        result.put("fly", fly.build());
        AnimationDefinition.Builder latch = AnimationDefinition.Builder.create(0.5f).looping();
        latch.addBoneAnimation(
                "tail",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(-0.0f, 10.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(-0.0f, -10.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.625f,
                                AnimationHelper.createRotationalVector(-0.0f, 10.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        latch.addBoneAnimation(
                "leftwing",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(38.73f, -67.13f, 7.93f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(
                                        45.2662f, -74.5665f, -0.0505f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(38.73f, -67.13f, 7.93f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(
                                        33.5831f, -59.7719f, 14.1562f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(38.73f, -67.13f, 7.93f),
                                Transformation.Interpolations.CUBIC)));
        latch.addBoneAnimation(
                "leftwing",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        latch.addBoneAnimation(
                "rightwing",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(38.73f, 67.13f, -7.93f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(
                                        33.5831f, 59.7719f, -14.1562f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(38.73f, 67.13f, -7.93f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(45.2662f, 74.5665f, 0.0505f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(38.73f, 67.13f, -7.93f),
                                Transformation.Interpolations.CUBIC)));
        latch.addBoneAnimation(
                "mouthrot",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(
                                        -67.6048f, 8.4369f, -7.3454f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(-62.6f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(
                                        -67.6048f, -8.4369f, 7.3454f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(-73.23f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(
                                        -67.6048f, 8.4369f, -7.3454f),
                                Transformation.Interpolations.CUBIC)));
        latch.addBoneAnimation(
                "mouthrot",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 5.0f, 1.0f),
                                Transformation.Interpolations.CUBIC)));
        result.put("latch", latch.build());
        return result;
    }

    public static TexturedModelData imp() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData part1 =
                root.addChild(
                        "main",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, 24.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        ModelPartData part2 =
                part1.addChild(
                        "body",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, -5.5f, 0.0f, -0.0f, -0.0f, 0.0f));
        part2.addChild(
                "cube_3",
                ModelPartBuilder.create()
                        .uv(0, 11)
                        .mirrored(false)
                        .cuboid(-3.0f, -5.5f, -2.0f, 6.0f, 6.0f, 4.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part4 =
                part2.addChild(
                        "leftarm",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-3.5f, -4.5f, 0.0f, -0.0f, -0.0f, 0.0f));
        part4.addChild(
                "cube_5",
                ModelPartBuilder.create()
                        .uv(0, 21)
                        .mirrored(false)
                        .cuboid(-1.5f, -1.0f, -1.5f, 2.0f, 6.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part6 =
                part2.addChild(
                        "rightarm",
                        ModelPartBuilder.create(),
                        ModelTransform.of(3.5f, -4.5f, 0.0f, -0.0f, -0.0f, 0.0f));
        part6.addChild(
                "cube_7",
                ModelPartBuilder.create()
                        .uv(0, 21)
                        .mirrored(true)
                        .cuboid(-0.5f, -1.0f, -1.5f, 2.0f, 6.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part8 =
                part2.addChild(
                        "head",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, -5.5f, 0.0f, -0.0f, -0.0f, 0.0f));
        part8.addChild(
                "cube_9",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .mirrored(false)
                        .cuboid(-4.0f, -6.0f, -2.5f, 8.0f, 6.0f, 5.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part10 =
                part8.addChild(
                        "lefthorn",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-6.0f, -3.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part10.addChild(
                "cube_11",
                ModelPartBuilder.create()
                        .uv(10, 21)
                        .mirrored(false)
                        .cuboid(2.0f, -6.0f, -1.0f, 1.0f, 1.0f, 2.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        part10.addChild(
                "cube_12",
                ModelPartBuilder.create()
                        .uv(20, 19)
                        .mirrored(false)
                        .cuboid(0.0f, -6.0f, -1.0f, 2.0f, 6.0f, 2.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part13 =
                part8.addChild(
                        "righthorn",
                        ModelPartBuilder.create(),
                        ModelTransform.of(6.0f, -3.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part13.addChild(
                "cube_14",
                ModelPartBuilder.create()
                        .uv(10, 21)
                        .mirrored(true)
                        .cuboid(-3.0f, -6.0f, -1.0f, 1.0f, 1.0f, 2.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        part13.addChild(
                "cube_15",
                ModelPartBuilder.create()
                        .uv(20, 19)
                        .mirrored(true)
                        .cuboid(-2.0f, -6.0f, -1.0f, 2.0f, 6.0f, 2.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part16 =
                part8.addChild(
                        "eyes",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, 0.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        ModelPartData part17 =
                part16.addChild(
                        "lefteye",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-2.5f, -2.5f, -2.5f, -0.0f, -0.0f, 0.0f));
        part17.addChild(
                "cube_18",
                ModelPartBuilder.create()
                        .uv(10, 24)
                        .mirrored(false)
                        .cuboid(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 0.0f, new Dilation(0.01f)),
                ModelTransform.NONE);
        ModelPartData part19 =
                part16.addChild(
                        "righteye",
                        ModelPartBuilder.create(),
                        ModelTransform.of(2.5f, -1.5f, -2.5f, -0.0f, -0.0f, 0.0f));
        part19.addChild(
                "cube_20",
                ModelPartBuilder.create()
                        .uv(10, 24)
                        .mirrored(true)
                        .cuboid(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 0.0f, new Dilation(0.01f)),
                ModelTransform.NONE);
        ModelPartData part21 =
                part1.addChild(
                        "leftleg",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-2.0f, -5.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part21.addChild(
                "cube_22",
                ModelPartBuilder.create()
                        .uv(20, 11)
                        .mirrored(false)
                        .cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 5.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part23 =
                part1.addChild(
                        "rightleg",
                        ModelPartBuilder.create(),
                        ModelTransform.of(2.0f, -5.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part23.addChild(
                "cube_24",
                ModelPartBuilder.create()
                        .uv(20, 11)
                        .mirrored(true)
                        .cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 5.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        return TexturedModelData.of(data, 32, 32);
    }

    public static Map<String, AnimationDefinition> impAnimations() {
        Map<String, AnimationDefinition> result = new LinkedHashMap<>();
        AnimationDefinition.Builder run = AnimationDefinition.Builder.create(0.5f).looping();
        run.addBoneAnimation(
                "body",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-0.0f, 5.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(0.0545f, -0.6226f, -5.0002f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(-0.0f, -5.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(0.0545f, 0.6226f, 5.0002f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-0.0f, 5.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "main",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "leftleg",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(12.19f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "leftleg",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.5f, 0.06f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "leftarm",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-90.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-90.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "leftarm",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "head",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(0.0545f, 0.6226f, 5.0002f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(-0.0f, 5.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(0.0545f, -0.6226f, -5.0002f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.375f,
                                AnimationHelper.createRotationalVector(-0.0f, -5.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(0.0545f, 0.6226f, 5.0002f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "rightleg",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createRotationalVector(12.19f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(-22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(22.5f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "rightleg",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.125f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.5f, 0.06f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.5f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "rightarm",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(-90.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-90.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        run.addBoneAnimation(
                "rightarm",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -0.5f),
                                Transformation.Interpolations.CUBIC)));
        result.put("run", run.build());
        AnimationDefinition.Builder eyes = AnimationDefinition.Builder.create(1.0f).looping();
        eyes.addBoneAnimation(
                "lefteye",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.75f,
                                AnimationHelper.createTranslationalVector(0.0f, -0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        eyes.addBoneAnimation(
                "righteye",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, -0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.75f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        result.put("eyes", eyes.build());
        return result;
    }

    public static TexturedModelData lemure() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData part1 =
                root.addChild(
                        "main",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, 18.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        ModelPartData part2 =
                part1.addChild(
                        "body",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, 0.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part2.addChild(
                "cube_3",
                ModelPartBuilder.create()
                        .uv(0, 11)
                        .mirrored(false)
                        .cuboid(-2.5f, -3.0f, -2.0f, 5.0f, 9.0f, 4.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part4 =
                part2.addChild(
                        "head",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-0.25f, -3.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part4.addChild(
                "cube_5",
                ModelPartBuilder.create()
                        .uv(1, 0)
                        .mirrored(false)
                        .cuboid(-4.25f, -5.0f, -3.0f, 9.0f, 5.0f, 6.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part6 =
                part4.addChild(
                        "lefthorn",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-2.25f, -5.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part6.addChild(
                "cube_7",
                ModelPartBuilder.create()
                        .uv(0, 24)
                        .mirrored(false)
                        .cuboid(-4.0f, -6.0f, -1.5f, 2.0f, 1.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        part6.addChild(
                "cube_8",
                ModelPartBuilder.create()
                        .uv(18, 18)
                        .mirrored(false)
                        .cuboid(-2.0f, -6.0f, -1.5f, 3.0f, 6.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part9 =
                part4.addChild(
                        "righthorn",
                        ModelPartBuilder.create(),
                        ModelTransform.of(2.75f, -5.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part9.addChild(
                "cube_10",
                ModelPartBuilder.create()
                        .uv(0, 24)
                        .mirrored(true)
                        .cuboid(2.0f, -6.0f, -1.5f, 2.0f, 1.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        part9.addChild(
                "cube_11",
                ModelPartBuilder.create()
                        .uv(18, 18)
                        .mirrored(true)
                        .cuboid(-1.0f, -6.0f, -1.5f, 3.0f, 6.0f, 3.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        ModelPartData part12 =
                part4.addChild(
                        "eyes",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.75f, -2.0f, -3.5f, -0.0f, -0.0f, 0.0f));
        ModelPartData part13 =
                part12.addChild(
                        "lefteye",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-3.5f, 0.5f, 0.5f, -0.0f, -0.0f, 0.0f));
        part13.addChild(
                "cube_14",
                ModelPartBuilder.create()
                        .uv(10, 24)
                        .mirrored(false)
                        .cuboid(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 0.0f, new Dilation(0.01f)),
                ModelTransform.NONE);
        ModelPartData part15 =
                part12.addChild(
                        "righteye",
                        ModelPartBuilder.create(),
                        ModelTransform.of(3.0f, 0.0f, 0.0f, -0.0f, -0.0f, 0.0f));
        part15.addChild(
                "cube_16",
                ModelPartBuilder.create()
                        .uv(10, 25)
                        .mirrored(false)
                        .cuboid(-2.0f, -1.0f, 0.5f, 3.0f, 1.0f, 0.0f, new Dilation(0.01f)),
                ModelTransform.NONE);
        ModelPartData part17 =
                part12.addChild(
                        "mideye",
                        ModelPartBuilder.create(),
                        ModelTransform.of(-1.5f, -1.5f, 0.5f, -0.0f, -0.0f, 0.0f));
        part17.addChild(
                "cube_18",
                ModelPartBuilder.create()
                        .uv(10, 26)
                        .mirrored(false)
                        .cuboid(-1.5f, -0.5f, 0.0f, 3.0f, 1.0f, 0.0f, new Dilation(0.01f)),
                ModelTransform.NONE);
        ModelPartData part19 =
                part2.addChild(
                        "arms",
                        ModelPartBuilder.create(),
                        ModelTransform.of(0.0f, -0.5f, -0.5f, 0.3491666f, -0.002736f, 0.0011996f));
        part19.addChild(
                "cube_20",
                ModelPartBuilder.create()
                        .uv(18, 11)
                        .mirrored(false)
                        .cuboid(-3.5f, -1.5f, -3.0f, 7.0f, 3.0f, 4.0f, new Dilation(0.0f)),
                ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 64);
    }

    public static Map<String, AnimationDefinition> lemureAnimations() {
        Map<String, AnimationDefinition> result = new LinkedHashMap<>();
        AnimationDefinition.Builder hover = AnimationDefinition.Builder.create(2.0f).looping();
        hover.addBoneAnimation(
                "head",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createRotationalVector(-0.0f, -0.0f, -5.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createRotationalVector(-5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.5f,
                                AnimationHelper.createRotationalVector(-0.0f, -0.0f, 5.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.0f,
                                AnimationHelper.createRotationalVector(5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.5f,
                                AnimationHelper.createRotationalVector(-0.0f, -0.0f, -5.0f),
                                Transformation.Interpolations.CUBIC)));
        hover.addBoneAnimation(
                "body",
                new Transformation(
                        Transformation.Targets.ROTATE,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createRotationalVector(2.81f, -0.0f, -2.81f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createRotationalVector(-0.0f, -0.0f, -5.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.75f,
                                AnimationHelper.createRotationalVector(-5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.25f,
                                AnimationHelper.createRotationalVector(-0.0f, -0.0f, 5.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.75f,
                                AnimationHelper.createRotationalVector(5.0f, -0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.0f,
                                AnimationHelper.createRotationalVector(2.81f, -0.0f, -2.81f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.25f,
                                AnimationHelper.createRotationalVector(-0.0f, -0.0f, -5.0f),
                                Transformation.Interpolations.CUBIC)));
        hover.addBoneAnimation(
                "body",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(1.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 1.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createTranslationalVector(-1.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, -1.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.0f,
                                AnimationHelper.createTranslationalVector(1.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        hover.addBoneAnimation(
                "main",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, -1.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.5f,
                                AnimationHelper.createTranslationalVector(0.0f, 1.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                2.5f,
                                AnimationHelper.createTranslationalVector(0.0f, -1.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        result.put("hover", hover.build());
        AnimationDefinition.Builder eyes = AnimationDefinition.Builder.create(1.0f).looping();
        eyes.addBoneAnimation(
                "lefteye",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.75f,
                                AnimationHelper.createTranslationalVector(0.0f, -0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        eyes.addBoneAnimation(
                "righteye",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.0f, -0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.75f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createTranslationalVector(0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        eyes.addBoneAnimation(
                "mideye",
                new Transformation(
                        Transformation.Targets.MOVE_ORIGIN,
                        new Keyframe(
                                0.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.25f,
                                AnimationHelper.createTranslationalVector(0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.5f,
                                AnimationHelper.createTranslationalVector(0.0f, -0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                0.75f,
                                AnimationHelper.createTranslationalVector(-0.25f, 0.0f, 0.0f),
                                Transformation.Interpolations.CUBIC),
                        new Keyframe(
                                1.0f,
                                AnimationHelper.createTranslationalVector(0.0f, 0.25f, 0.0f),
                                Transformation.Interpolations.CUBIC)));
        result.put("eyes", eyes.build());
        return result;
    }

    private SuppliedDemonModels() {}
}
