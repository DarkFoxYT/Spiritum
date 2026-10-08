package net.dark.spiritum.client;

import net.dark.spiritum.entity.SentinelEntity;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.WeakHashMap;

public class SentinelRenderer
        extends MobEntityRenderer<SentinelEntity, SentinelRenderer.State, SentinelModel> {
    private final Map<SentinelEntity, Integer> awakenedAt = new WeakHashMap<>();

    public static class State extends LivingEntityRenderState {
        boolean awake;
        int attack;
        float attackRemaining;
        float awakeTime;
    }

    public SentinelRenderer(EntityRendererFactory.Context context) {
        super(context, new SentinelModel(), .45f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public Identifier getTexture(State state) {
        return Identifier.of("spiritum", "textures/entity/sentinel.png");
    }

    @Override
    public void updateRenderState(SentinelEntity entity, State state, float delta) {
        super.updateRenderState(entity, state, delta);
        state.awake = entity.isAwake();
        state.attack = entity.attackPose();
        state.attackRemaining = Math.max(0, entity.attackRemaining() - delta);
        if (state.awake)
            state.awakeTime = entity.age + delta - awakenedAt.computeIfAbsent(entity, e -> e.age);
        else {
            awakenedAt.remove(entity);
            state.awakeTime = 0;
        }
    }
}
