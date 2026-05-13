package mojang.minecraft.uuidget.mixin;

import mojang.minecraft.uuidget.ClientInitializer;
import mojang.minecraft.uuidget.HealthTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void uuidget$join(GameJoinS2CPacket packet, CallbackInfo ci) {
        ClientInitializer.onTransfer();
    }

    @Inject(method = "onEntityStatus", at = @At("HEAD"))
    private void uuidget$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) {
            Entity entity = packet.getEntity(mc.world);
            if (entity != null) {
                HealthTracker.onEntityStatus(entity.getId(), packet.getStatus());
            }
        }
    }

    @Inject(method = "onTitle", at = @At("HEAD"))
    private void uuidget$title(TitleS2CPacket packet, CallbackInfo ci) {
        ClientInitializer.onTitleReceived(packet.text().getString());
    }

    @Inject(method = "onSubtitle", at = @At("HEAD"))
    private void uuidget$subtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        ClientInitializer.onTitleReceived(packet.text().getString());
    }
}
