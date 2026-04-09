package org.limbus.ryoshuMod.client;

import org.limbus.ryoshuMod.RyoshuMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.lwjgl.glfw.GLFW;

public class RyoshuModClient implements ClientModInitializer {

    public static KeyBinding modeKey;

    @Override
    public void onInitializeClient() {

        // 🔥 모델 강제 로드
        forceLoadModel();

        // 🔥 F키 (모드 전환)
        modeKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.ryoshu.mode",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_F,
                        "category.ryoshu"
                )
        );

        // 🔥 클라이언트 틱 이벤트
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            PlayerEntity player = client.player;
            World world = player.getWorld();

            // F 눌렀을 때 서버에 모드 변경 요청 전송
            if (modeKey.wasPressed()) {
                ClientPlayNetworking.send(
                        new Identifier(RyoshuMod.MOD_ID, "mode_change"),
                        PacketByteBufs.empty()
                );
            }

            // 좌클릭 감지 (파티클용 예시)
            if (client.options.attackKey.wasPressed()) {

                if (RyoshuMod.isHoldingSword(player)) {

                    // 👉 여기선 클라니까 이펙트만
                    world.addParticle(
                            net.minecraft.particle.ParticleTypes.CRIT,
                            player.getX(),
                            player.getY() + 1,
                            player.getZ(),
                            0, 0.1, 0
                    );
                }
            }
        });
    }

    // 🔥 모델 강제 로드
    private void forceLoadModel() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getResourceManager() != null && client.getBakedModelManager() != null) {
            Identifier modelId = new Identifier(RyoshuMod.MOD_ID, "item/arayashiki");
            try {
                // 모델 리소스가 존재하는지 확인
                if (client.getResourceManager().getResource(modelId).isPresent()) {
                    System.out.println("[RyoshuMod] ARAYASHIKI model resource found: " + modelId);

                    // 베이크된 모델이 있는지 확인
                    BakedModel bakedModel = client.getBakedModelManager().getModel(modelId);
                    if (bakedModel != null) {
                        System.out.println("[RyoshuMod] ARAYASHIKI baked model loaded successfully");
                    } else {
                        System.out.println("[RyoshuMod] ARAYASHIKI baked model is null");
                    }
                } else {
                    System.out.println("[RyoshuMod] ARAYASHIKI model resource NOT found: " + modelId);
                }
            } catch (Exception e) {
                System.out.println("[RyoshuMod] Error checking ARAYASHIKI model: " + e.getMessage());
            }
        }
    }
}