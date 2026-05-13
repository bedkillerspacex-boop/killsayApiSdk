package mojang.minecraft.uuidget.mixin;

import com.mojang.authlib.GameProfile;
import mojang.minecraft.uuidget.ClientInitializer;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {
    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void uuidget$chat(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        ClientInitializer.onChatMessage(message.getContent().getString());
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void uuidget$game(Text message, boolean overlay, CallbackInfo ci) {
        ClientInitializer.onGameMessage(message, overlay);
    }
}
