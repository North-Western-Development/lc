package li.cil.oc2.common.mixin;

import li.cil.oc2.client.ClientSetup;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Optional;

@Mixin(SpriteResourceLoader.class)
public class AtlasAdder {
    @ModifyVariable(
        method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;",
        at = @At("STORE"),
        ordinal = 0
    )
    private static List<SpriteSource> appendSprites(List<SpriteSource> sprites, ResourceManager resourceManager, ResourceLocation atlas){
        ClientSetup.collectSprites(atlas, location -> sprites.add(new SingleFile(location, Optional.empty())));
        return sprites;
    }
}
