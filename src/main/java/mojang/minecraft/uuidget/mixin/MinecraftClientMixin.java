package mojang.minecraft.uuidget.mixin;

import mojang.minecraft.uuidget.ClientInitializer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void uuidget$tick(CallbackInfo ci) {
        ClientInitializer.onClientTick((MinecraftClient) (Object) this);
    }
}
