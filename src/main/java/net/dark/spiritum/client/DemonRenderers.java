package net.dark.spiritum.client;

import net.dark.spiritum.client.model.*;
import net.dark.spiritum.entity.*;
import net.dark.spiritum.registry.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;

public final class DemonRenderers {
    private static final EntityModelLayer LEECH = layer("leech"),
            IMP = layer("imp"),
            LEMURE = layer("lemure");

    private static EntityModelLayer layer(String name) {
        return new EntityModelLayer(Identifier.of("spiritum", name + "_demon"), "main");
    }

    public static class State extends ArmedEntityRenderState {
        public boolean latched;
    }

    public static void initialize() {
        EntityRendererFactories.register(ModEntities.POPPET, PoppetRenderer::new);
        EntityRendererFactories.register(ModEntities.SENTINEL, SentinelRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(LEECH, SuppliedDemonModels::leech);
        EntityModelLayerRegistry.registerModelLayer(IMP, SuppliedDemonModels::imp);
        EntityModelLayerRegistry.registerModelLayer(LEMURE, SuppliedDemonModels::lemure);
        EntityRendererFactories.register(
                ModEntities.LEECH, context -> new DemonRenderer<>(context, LEECH, "leech", false));
        EntityRendererFactories.register(
                ModEntities.IMP, context -> new DemonRenderer<>(context, IMP, "imp", true));
        EntityRendererFactories.register(
                ModEntities.LEMURE,
                context -> new DemonRenderer<>(context, LEMURE, "lemure", false));
        EntityRendererFactories.register(
                ModEntities.SPIRIT_ENERGY,
                context -> new FlyingItemEntityRenderer<>(context, .35f, true));
    }

    private static class DemonRenderer<T extends OwnedDemonEntity>
            extends MobEntityRenderer<T, State, DemonModel> {
        private final Identifier texture;

        DemonRenderer(
                EntityRendererFactory.Context context,
                EntityModelLayer layer,
                String kind,
                boolean carriesItems) {
            super(context, new DemonModel(context.getPart(layer), kind), .25f);
            texture = Identifier.of("spiritum", "textures/entity/" + kind + "_demon.png");
            DemonModel eyes = new DemonModel(context.getPart(layer), kind);
            eyes.showOnlyEyes();
            addFeature(new EyesFeatureRenderer<State, DemonModel>(this) {
                @Override
                public DemonModel getContextModel() { return eyes; }

                @Override
                public RenderLayer getEyesTexture() { return RenderLayers.eyes(texture); }
            });
            if (carriesItems) addFeature(new HeldItemFeatureRenderer<>(this));
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public Identifier getTexture(State state) {
            return texture;
        }

        @Override
        public void updateRenderState(T demon, State state, float delta) {
            super.updateRenderState(demon, state, delta);
            ArmedEntityRenderState.updateRenderState(demon, state, itemModelResolver, delta);
            state.latched = demon instanceof LeechDemonEntity leech && leech.isLatched();
            if (demon instanceof ImpDemonEntity imp)
                itemModelResolver.clearAndUpdate(
                        state.rightHandItemState,
                        imp.getCarriedStack(),
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        demon.getEntityWorld(),
                        demon,
                        demon.getId());
        }
    }

    private DemonRenderers() {}
}
