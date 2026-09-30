package net.dark.spiritum.client.model;

import net.dark.spiritum.client.DemonRenderers.State;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

import java.util.*;

public final class DemonModel extends EntityModel<State> implements ModelWithArms<State> {
    private final String kind;
    private final Map<String, Animation> animations = new LinkedHashMap<>();

    public DemonModel(ModelPart root, String kind) {
        super(root);
        this.kind = kind;
        var definitions =
                switch (kind) {
                    case "leech" -> SuppliedDemonModels.leechAnimations();
                    case "imp" -> SuppliedDemonModels.impAnimations();
                    default -> SuppliedDemonModels.lemureAnimations();
                };
        definitions.forEach(
                (name, definition) -> animations.put(name, definition.createAnimation(root)));
    }

    @Override
    public void setAngles(State state) {
        super.setAngles(state);
        long time = (long) (state.age * 50);
        if (kind.equals("leech")) animations.get(state.latched ? "latch" : "fly").apply(time, 1);
        else if (kind.equals("lemure")) animations.get("hover").apply(time, 1);
        else
            animations
                    .get("run")
                    .applyWalking(state.limbSwingAnimationProgress, state.limbSwingAmplitude, 1, 1);
        if (animations.containsKey("eyes")) animations.get("eyes").apply(time, 1);
    }

    public void showOnlyEyes() {
        root.traverse().forEach(part -> part.hidden = true);
        ModelPart eyes = root.createPartGetter().apply("eyes");
        if (eyes != null) eyes.traverse().forEach(part -> part.hidden = false);
    }

    @Override
    public void setArmAngle(State state, Arm arm, MatrixStack matrices) {
        root.applyTransform(matrices);
        ModelPart main = root.getChild("main");
        main.applyTransform(matrices);
        ModelPart body = main.getChild("body");
        body.applyTransform(matrices);
        body.getChild(arm == Arm.RIGHT ? "rightarm" : "leftarm").applyTransform(matrices);
        matrices.translate(0, .25, 0);
        matrices.scale(.6f, .6f, .6f);
    }
}
