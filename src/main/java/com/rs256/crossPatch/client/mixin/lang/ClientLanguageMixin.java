package com.rs256.crossPatch.client.mixin.lang;

import com.rs256.crossPatch.Reference;
import com.rs256.crossPatch.client.config.lang.CrossPatchI18n;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bridges CrossPatch's nested i18n source (loaded by {@link CrossPatchI18n}) into
 * the vanilla translation lookup. CrossPatch ships its translations as a nested
 * JSON under {@code assets/crosspatch/i18n/} rather than a flat {@code lang/} file,
 * so vanilla never sees those keys.
 *
 * <p>The inject runs at {@code RETURN}: when the lookup missed (vanilla returns the
 * key unchanged) and the key belongs to CrossPatch, the nested value is substituted.
 * Anything vanilla — or a resource pack overriding {@code lang/} — already resolved
 * is left untouched, so external overrides keep priority.
 */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
    @Inject(
            method = "getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void crosspatch_fallbackToNested(
            String key,
            String defaultValue,
            CallbackInfoReturnable<String> cir
    ) {
        if (key == null || !key.startsWith(Reference.MOD_ID + ".")) {
            return;
        }

        // Only fill in genuine misses (return value still equals the key/default),
        // so vanilla and resource-pack translations keep priority.
        if (!key.equals(cir.getReturnValue())) {
            return;
        }

        String nested = CrossPatchI18n.find(key);

        if (nested != null) {
            cir.setReturnValue(nested);
        }
    }
}
