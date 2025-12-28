package raccoonman.reterraforged.world.worldgen.biome.modifier;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import raccoonman.reterraforged.registries.RTFBuiltInRegistries;

// 还有其他世界生成库可以做这件事，也没那么糟糕
@Deprecated
public interface BiomeModifier {
    public static final Codec<BiomeModifier> CODEC = RTFBuiltInRegistries.BIOME_MODIFIER_TYPE.byNameCodec().dispatch(BiomeModifier::codec, Function.identity());
	
	Codec<? extends BiomeModifier> codec();
}
