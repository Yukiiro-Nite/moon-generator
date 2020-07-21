package me.yukiironite;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;
import java.util.List;
import java.util.Arrays;

public class MoonChunkGenerator extends ChunkGenerator {

  public static SimplexOctaveGenerator getNoiseGenerator(World world) {
    SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
    generator.setScale(0.01D);

    return generator;
  }

  public static int getHeight(SimplexOctaveGenerator generator, double x, double z, double freq, double amp, double baseHeight) {
    return (int) ((generator.noise(x, z, freq, 0.5D, true) + 1) * amp + baseHeight);
  }

  @Override
  public List<BlockPopulator> getDefaultPopulators(World world) {
      return Arrays.asList(
        (BlockPopulator) new CraterPopulator()
      );
  }

  @Override
  public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
    ChunkData chunk = createChunkData(world);
    SimplexOctaveGenerator generator = getNoiseGenerator(world);

    for (int X = 0; X < 16; X++) {
      for (int Z = 0; Z < 16; Z++) {
        // Set biome
        for (int y = 256; y >= 0; y--) {
          biome.setBiome(X, y, Z, Biome.DESERT_HILLS);
        }

        int surfaceHeight = getHeight(generator, chunkX*16+X, chunkZ*16+Z, 1D, 30D, 128D);
        int surfaceBottomHeight = getHeight(generator, chunkX*16+X, chunkZ*16+Z, 0.5D, 5D, 70D);
        int cavernTopHeight = getHeight(generator, chunkX*16+X, chunkZ*16+Z, 10D, 20D, 50D);
        int cavernBottomHeight = getHeight(generator, chunkX*16+X, chunkZ*16+Z, 2D, 40D, 20D);

        // Set surface
        for (int y = surfaceHeight; y > surfaceHeight-4; y--)
          chunk.setBlock(X, y, Z, Material.GRAVEL);

        for (int y = surfaceHeight-4; y > surfaceBottomHeight; y--)
          chunk.setBlock(X, y, Z, Material.STONE);

        // Set cavern ceiling
        for (int y = surfaceBottomHeight; y > cavernTopHeight; y--)
          chunk.setBlock(X, y, Z, Material.BASALT);

        // Set cavern
        for (int y = cavernTopHeight; y > cavernBottomHeight; y--)
          chunk.setBlock(X, y, Z, Material.CAVE_AIR);
        
        if(random.nextDouble() < 0.01D) {
          chunk.setBlock(X, cavernTopHeight, Z, Material.CRYING_OBSIDIAN);
        }
        
        // Set core
        for (int y = cavernBottomHeight; y > 0; y--)
          chunk.setBlock(X, y, Z, Material.BLACKSTONE);
        
        // Set lava
        for (int y = 40; y > 25; y--) {
          if(chunk.getBlockData(X, y, Z).getMaterial().isAir()) {
            chunk.setBlock(X, y, Z, Material.LAVA);
          }
        }

        // Set bedrock cap
        chunk.setBlock(X, 0, Z, Material.BEDROCK);
      }
    }

    return chunk;
  }
}