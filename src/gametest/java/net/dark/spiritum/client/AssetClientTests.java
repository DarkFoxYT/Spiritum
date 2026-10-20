package net.dark.spiritum.client;

import net.dark.spiritum.entity.PoppetEntity;
import net.dark.spiritum.entity.SentinelEntity;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.network.TunedBladeTargetPayload;
import net.dark.spiritum.registry.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.SpawnReason;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AssetClientTests implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(
                client -> {
                    SentinelModel model = new SentinelModel();
                    SentinelRenderer.State state = new SentinelRenderer.State();
                    model.setAngles(state);
                    state.awake = true;
                    for (int attack = 0; attack <= 2; attack++) {
                        state.attack = attack;
                        int duration = attack == 1 ? SentinelEntity.SLASH_DURATION : SentinelEntity.THRUST_DURATION;
                        for (int frame = 0; frame <= duration * 2; frame++) {
                            state.awakeTime = frame;
                            state.attackRemaining =
                                    Math.max(0, duration - frame * .5f);
                            state.limbSwingAnimationProgress = frame * .2f;
                            state.limbSwingAmplitude = .8f;
                            model.setAngles(state);
                            var main = model.getRootPart().getChild("main");
                            if (Math.abs(main.originX) > .001 || Math.abs(main.originZ) > .001)
                                throw new AssertionError("Sentinel animation must stay on its hitbox");
                        }
                    }
                    for (boolean slim : new boolean[] {false, true}) {
                        PlayerEntityModel playerModel = new PlayerEntityModel(
                                TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, slim), 64, 64).createModel(), slim);
                        for (Arm mainArm : Arm.values()) {
                            for (Hand hand : Hand.values()) {
                                PlayerEntityRenderState playerState = new PlayerEntityRenderState();
                                playerState.mainArm = mainArm;
                                playerState.activeHand = hand;
                                playerState.isUsingItem = true;
                                playerState.rightArmPose = BipedEntityModel.ArmPose.ITEM;
                                playerState.leftArmPose = BipedEntityModel.ArmPose.ITEM;
                                Arm heldArm = hand == Hand.MAIN_HAND ? mainArm : mainArm.getOpposite();
                                if (heldArm == Arm.RIGHT) playerState.rightHandItem = new ItemStack(ModContent.TUNED_BLADE);
                                else playerState.leftHandItem = new ItemStack(ModContent.TUNED_BLADE);
                                playerModel.setAngles(playerState);
                                var armPart = heldArm == Arm.RIGHT ? playerModel.rightArm : playerModel.leftArm;
                                if (Math.abs(armPart.pitch + Math.PI / 2 - playerModel.head.pitch) > .001)
                                    throw new AssertionError("Active blade arm points forward for either hand");
                                playerState.isUsingItem = false;
                                playerModel.setAngles(playerState);
                                if (Math.abs(armPart.pitch + Math.PI / 2) < .1)
                                    throw new AssertionError("Releasing blade restores the ordinary arm pose");
                            }
                        }
                    }
                });
        try (var world = context.worldBuilder().create()) {
            var server = world.getServer();
            server.runCommand("time set day");
            server.runCommand("tp @a 0 -59 7 180 4");
            server.runOnServer(
                    s -> {
                        var level = s.getOverworld();
                        var sentinel = ModEntities.SINGING_ROD.create(level, SpawnReason.COMMAND);
                        sentinel.setPosition(0, -59, 0);
                        level.spawnEntity(sentinel);
                        level.setBlockState(
                                new BlockPos(3, -59, 0),
                                ModContent.RITUAL_PEDESTAL.getDefaultState());
                        level.setBlockState(
                                new BlockPos(-3, -59, 0), ModContent.ALCHEMY_VAT.getDefaultState());
                        level.setBlockState(
                                new BlockPos(-1, -59, 0),
                                ModContent.HEXED_CANDLE.getDefaultState());
                        var player = s.getPlayerManager().getPlayerList().getFirst();
                        player.setStackInHand(
                                Hand.MAIN_HAND, new ItemStack(ModContent.RETROSONUS));
                        ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
                        SpiritBinding.bind(gem, player);
                        SpiritBinding.socket(player.getMainHandStack(), gem);
                    });
            world.getClientWorld().waitForChunksRender();
            context.waitTicks(5);
            context.takeScreenshot("retrosonus-and-singing-rod");
            context.getInput().holdKey(options -> options.useKey);
            context.waitFor(client -> client.player.isUsingItem());
            context.waitTicks(5);
            context.takeScreenshot("retrosonus-tracking");
            float before = context.computeOnClient(client -> client.player.getYaw());
            server.runOnServer(
                    s ->
                            ServerPlayNetworking.send(
                                    s.getPlayerManager().getPlayerList().getFirst(),
                                    new TunedBladeTargetPayload(before + 90, 0)));
            context.waitFor(client -> client.player.getYaw() > before + .05f);
            float after = context.computeOnClient(client -> client.player.getYaw());
            if (after >= before + 90)
                throw new AssertionError("Blade direction must turn gradually instead of snapping");
            context.getInput().releaseKey(options -> options.useKey);
            context.waitTicks(2);
            float released = context.computeOnClient(client -> client.player.getYaw());
            server.runOnServer(
                    s ->
                            ServerPlayNetworking.send(
                                    s.getPlayerManager().getPlayerList().getFirst(),
                                    new TunedBladeTargetPayload(released + 90, 0)));
            context.waitTicks(4);
            context.runOnClient(
                    client -> {
                        if (Math.abs(client.player.getYaw() - released) > .01f)
                            throw new AssertionError(
                                    "Releasing the blade must stop camera guidance");
                    });
            int dollId =
                    server.computeOnServer(
                            s -> {
                                var player = s.getPlayerManager().getPlayerList().getFirst();
                                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                ItemStack stack = new ItemStack(ModContent.VOODOO_POPPET);
                                ItemStack gem = new ItemStack(ModContent.SPIRIT_GEM);
                                SpiritBinding.bind(gem, player);
                                SpiritBinding.socket(stack, gem);
                                var doll =
                                        ModEntities.POPPET.create(
                                                s.getOverworld(), SpawnReason.COMMAND);
                                doll.setPosition(player.getEntityPos().add(0, .7, 2));
                                doll.launch(stack, player, Vec3d.ZERO);
                                s.getOverworld().spawnEntity(doll);
                                return doll.getId();
                            });
            context.waitTicks(20);
            context.getInput().holdKey(options -> options.sneakKey);
            context.waitTicks(3);
            server.runOnServer(
                    s -> {
                        var player = s.getPlayerManager().getPlayerList().getFirst();
                        var doll = (PoppetEntity) s.getOverworld().getEntityById(dollId);
                        Vec3d direction =
                                doll.getBoundingBox().getCenter().subtract(player.getEyePos());
                        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
                        float pitch =
                                (float)
                                        -Math.toDegrees(
                                                Math.atan2(
                                                        direction.y, direction.horizontalLength()));
                        player.networkHandler.requestTeleport(
                                player.getX(), player.getY(), player.getZ(), yaw, pitch);
                    });
            context.waitFor(
                    client ->
                            client.crosshairTarget instanceof EntityHitResult hit
                                    && hit.getEntity().getId() == dollId);
            context.takeScreenshot("poppet-ragdoll");
            context.getInput().pressKey(options -> options.useKey);
            context.waitFor(client -> client.world.getEntityById(dollId) == null);
            context.getInput().releaseKey(options -> options.sneakKey);
            server.runOnServer(
                    s -> {
                        var player = s.getPlayerManager().getPlayerList().getFirst();
                        ItemStack recovered = ItemStack.EMPTY;
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            ItemStack candidate = player.getInventory().getStack(i);
                            if (candidate.isOf(ModContent.VOODOO_POPPET)) recovered = candidate;
                        }
                        if (recovered.isEmpty()
                                || !SpiritBinding.player(SpiritBinding.socket(recovered))
                                        .orElseThrow()
                                        .equals(player.getUuid()))
                            throw new AssertionError(
                                    "Sneak-right-click must recover the poppet and its binding over"
                                        + " the network");
                    });
        }
    }
}
