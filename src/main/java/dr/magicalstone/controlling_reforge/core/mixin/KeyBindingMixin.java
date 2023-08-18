package dr.magicalstone.controlling_reforge.core.mixin;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {


    @Inject(method = "onTick", at = @At(value = "HEAD"), cancellable = true)
    private static void onOnTick(int keyCode, CallbackInfo info) {
        //TODO: Patch Vanilla KeyBinding features and implement a more free KeyBinding system.
    }

    @Inject(method = "setKeyBindState", at = @At(value = "HEAD"), cancellable = true)
    private static void onSetKeyBindState(int keyCode, boolean pressed, CallbackInfo info) {
        //TODO: Patch Vanilla KeyBinding features and implement a more free KeyBinding system.
    }

    @Inject(method = "updateKeyBindState", at = @At("HEAD"), cancellable = true)
    private static void onUpdateKeyBindingState(CallbackInfo info) {
        //TODO: Patch Vanilla KeyBinding features and implement a more free KeyBinding system.
    }

    @Inject(method = "unPressAllKeys", at = @At("HEAD"), cancellable = true)
    private static void onUnPressAllKeys(CallbackInfo info) {
        //TODO: Patch Vanilla KeyBinding features and implement a more free KeyBinding system.
    }

}
