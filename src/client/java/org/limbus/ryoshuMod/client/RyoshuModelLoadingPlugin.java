package org.limbus.ryoshuMod.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.util.Identifier;

public class RyoshuModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        // ARAYASHIKI 아이템 모델을 강제로 로드
        Identifier modelId = new Identifier("ryoshumod", "item/arayashiki");

        // 모델 수정자를 추가해서 모델이 로드되도록 함
        pluginContext.modifyModelOnLoad().register((model, context) -> {
            if (context.id().equals(modelId)) {
                // 모델이 로드될 때 로깅
                System.out.println("[RyoshuMod] Loading ARAYASHIKI model: " + context.id());
                return model;
            }
            return model;
        });
    }
}