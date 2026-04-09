package org.limbus.ryoshuMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RyoshuMod implements ModInitializer {

    public static final String MOD_ID = "ryoshumod";

    // 🔥 아이템 등록
    public static final Item ARAYASHIKI = Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "arayashiki"),
            new Item(new Item.Settings())
    );

    // 🔥 딜레이 시스템
    static class Task {
        int delay;
        final Runnable action;

        Task(int delay, Runnable action) {
            this.delay = delay;
            this.action = action;
        }
    }

    private static final List<Task> tasks = new ArrayList<>();

    // 🔥 모드 상태
    public static final Map<UUID, Integer> modeMap = new HashMap<>();

    public static final Identifier MODE_CHANGE_PACKET = new Identifier(MOD_ID, "mode_change");

    @Override
    public void onInitialize() {
        System.out.println("[RyoshuMod] Loaded");

        // 🔧 클라이언트에서 F 키를 누르면 서버에 모드 변경 요청 전송
        ServerPlayNetworking.registerGlobalReceiver(MODE_CHANGE_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                int mode = modeMap.getOrDefault(player.getUuid(), 0);
                mode = (mode + 1) % 2;
                modeMap.put(player.getUuid(), mode);
            });
        });

        // 🔥 우클릭, 쉬프트 우클릭 처리
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(player.getMainHandStack());
            if (player.getMainHandStack().getItem() != ARAYASHIKI) return TypedActionResult.pass(player.getMainHandStack());
            if (world.isClient) return TypedActionResult.pass(player.getMainHandStack());

            if (player.isSneaking()) {
                egoSkill(player, (ServerWorld) world);
            } else {
                normalCombo(player, (ServerWorld) world);
            }
            return TypedActionResult.success(player.getMainHandStack());
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (player.getMainHandStack().getItem() != ARAYASHIKI) return ActionResult.PASS;
            if (world.isClient) return ActionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return ActionResult.PASS;

            if (player.isSneaking()) {
                shiftAttack32(player, (ServerWorld) world, target);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        // 🔁 서버 틱
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            // 딜레이 실행
            Iterator<Task> it = tasks.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                t.delay--;
                if (t.delay <= 0) {
                    t.action.run();
                    it.remove();
                }
            }

            // 🔥 지속 데미지 (스택 기반)
            for (ServerWorld world : server.getWorlds()) {

                List<ServerPlayerEntity> players = world.getPlayers();

                for (PlayerEntity attacker : players) {

                    if (!isHoldingSword(attacker)) continue;

                    for (PlayerEntity target : players) {

                        if (attacker == target) continue;

                        double dist = attacker.squaredDistanceTo(target);

                        if (dist <= 25) { // 5블록

                            target.setOnFireFor(1);

                            target.damage(
                                    attacker.getDamageSources().playerAttack(attacker),
                                    0.5f
                            );
                        }
                    }
                }
            }
        });
    }

    // 🔥 검 들었는지
    public static boolean isHoldingSword(PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        return stack.getItem() == ARAYASHIKI;
    }

    // 🔥 12콤보
    public static void normalCombo(PlayerEntity player, ServerWorld world) {

        int delay = 0;

        for (int hit = 1; hit <= 12; hit++) {

            tasks.add(new Task(delay, () -> {

                List<LivingEntity> targets = world.getEntitiesByClass(
                        LivingEntity.class,
                        player.getBoundingBox().expand(1.5),
                        e -> e != player
                );

                for (LivingEntity target : targets) {

                    target.damage(
                            player.getDamageSources().playerAttack(player),
                            0.5f
                    );
                }

            }));

            delay += 2;
        }
    }

    // 🔥 EGO 스킬
    public static void egoSkill(PlayerEntity player, ServerWorld world) {

        player.setOnFireFor(5);

        Vec3d look = player.getRotationVec(1.0f);
        Vec3d start = player.getPos();

        for (int i = 1; i <= 4; i++) {

            Vec3d pos = start.add(look.multiply(i));

            List<LivingEntity> targets = world.getEntitiesByClass(
                    LivingEntity.class,
                    new Box(pos.x - 1, pos.y - 1, pos.z - 1,
                            pos.x + 1, pos.y + 1, pos.z + 1),
                    e -> e != player
            );

            for (LivingEntity target : targets) {

                target.setOnFireFor(3);

                target.damage(
                        player.getDamageSources().playerAttack(player),
                        20f
                );

                target.setVelocity(
                        target.getVelocity().add(0, 1, 0)
                );
                target.velocityModified = true;
            }
        }

        // TP 이동
        Vec3d end = start.add(look.multiply(4));
        player.teleport(end.x, end.y, end.z);
    }

    // 🔥 32타 공격
    public static void shiftAttack32(PlayerEntity player, ServerWorld world, LivingEntity target) {

        int delay = 0;

        for (int hit = 1; hit <= 32; hit++) {

            int currentHit = hit;

            tasks.add(new Task(delay, () -> {

                if (target.isDead()) return;

                float dmg;
                switch (currentHit) {
                    case 1 -> dmg = 5f;
                    case 32 -> dmg = 10f;
                    default -> {
                        dmg = 0.3f;

                        Vec3d tp = target.getPos().add(
                                (Math.random() - 0.5) * 2,
                                0,
                                (Math.random() - 0.5) * 2
                        );

                        player.teleport(tp.x, tp.y, tp.z);
                    }
                }

                target.damage(
                        player.getDamageSources().playerAttack(player),
                        dmg
                );

                // 마지막 타격 → 플레이어 제거
                if (currentHit == 32) {
                    player.remove(RemovalReason.KILLED);
                }

            }));

            delay += 2;
        }
    }
}