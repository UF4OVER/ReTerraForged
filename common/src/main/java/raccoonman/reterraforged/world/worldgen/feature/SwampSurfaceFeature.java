package raccoonman.reterraforged.world.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import raccoonman.reterraforged.world.worldgen.feature.SwampSurfaceFeature.Config;
import raccoonman.reterraforged.world.worldgen.noise.module.Noise;
import raccoonman.reterraforged.world.worldgen.noise.module.Noises;

public class SwampSurfaceFeature extends Feature<Config> {
	private static final Noise MATERIAL_NOISE = makeMaterialNoise();
	// 替代已弃用的 Biome.BIOME_INFO_NOISE：用于 swamp surface 的判定噪声（低频 2D）
	private static final Noise BIOME_INFO_NOISE = makeBiomeInfoNoise();

	public SwampSurfaceFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<Config> ctx) {
		Config config = ctx.config();
		BlockPos origin = ctx.origin();
		ChunkPos chunkPos = new ChunkPos(origin);
		ChunkAccess chunk = ctx.level().getChunk(origin);
		ChunkGenerator generator = ctx.chunkGenerator();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int waterY = generator.getSeaLevel() - 1;
		
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				int worldX = chunkPos.getBlockX(x);
				int worldZ = chunkPos.getBlockZ(z);
				int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
				float nx = (float) (worldX * 0.25D);
				float nz = (float) (worldZ * 0.25D);
				double noise = BIOME_INFO_NOISE.compute(nx, nz, 0);
				BlockState filler = getMaterial(worldX, waterY, worldZ, waterY, config);

				if(chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(surfaceY), QuartPos.fromBlock(z)).is(Biomes.SWAMP)) {
			        if (noise > 0.0D) {
			            for (int y = surfaceY; y >= surfaceY - 10; --y) {
			                pos.set(x, y, z);
			                if (chunk.getBlockState(pos).isAir()) {
			                    continue;
			                }

			                if (y == waterY && !chunk.getFluidState(pos).isEmpty()) {
			                    chunk.setBlockState(pos, filler, false);
			                }
			                break;
			            }
			        }
			        
			        int y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
			        if (y <= waterY) {
			            chunk.setBlockState(pos.set(x, y, z), getMaterial(x, y, z, waterY, config), false);
			        }					
				}
			}	
		}
		return false;
	}

    private static BlockState getMaterial(int x, int y, int z, int waterY, Config config) {
        float value = MATERIAL_NOISE.compute(x, z, 0);
        if (value > 0.6) {
            if (value < 0.75 && y < waterY) {
                return config.clayMaterial();
            }
            return config.gravelMaterial();
        }
        return config.dirtMaterial();
    }
    
    private static Noise makeMaterialNoise() {
    	Noise base = Noises.simplex(23, 40, 2);
    	return Noises.warpWhite(base, 213, 2, 4);
    }

	private static Noise makeBiomeInfoNoise() {
		// 低频 2D 噪声；scale 越大越“平缓”。原版 BIOME_INFO_NOISE 属于非常低频。
		return Noises.simplex(2345, 256, 1);
	}

    public record Config(BlockState clayMaterial, BlockState gravelMaterial, BlockState dirtMaterial) implements FeatureConfiguration {
    	public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		BlockState.CODEC.fieldOf("clay_material").forGetter(Config::clayMaterial),
    		BlockState.CODEC.fieldOf("gravel_material").forGetter(Config::gravelMaterial),
    		BlockState.CODEC.fieldOf("dirt_material").forGetter(Config::dirtMaterial)
    	).apply(instance, Config::new));
    }
}
