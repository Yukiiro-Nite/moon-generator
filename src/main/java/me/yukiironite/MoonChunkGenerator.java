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
  public static MaterialGradient upperCavernMaterial;
  public static MaterialGradient coreMaterial;

  static {
    upperCavernMaterial = new MaterialGradient();
    upperCavernMaterial.nodes.add(new GradientNode(Material.STONE, 0.0, 0.0, 1.0, 1.0));
    upperCavernMaterial.nodes.add(new GradientNode(Material.BLACKSTONE, 0.1, 1.0, 1.0, 2.0));
    upperCavernMaterial.nodes.add(new GradientNode(Material.GILDED_BLACKSTONE, 0.2, 1.0, 1.0, 0.25));
    upperCavernMaterial.nodes.add(new GradientNode(Material.COAL_ORE, 0.0, 0.0, 0.2, 0.05));
    upperCavernMaterial.nodes.add(new GradientNode(Material.REDSTONE_ORE, 0.0, 1.0, 1.0, 0.025));
    upperCavernMaterial.nodes.add(new GradientNode(Material.LAPIS_ORE, 0.0, 1.0, 1.0, 0.025));
    upperCavernMaterial.nodes.add(new GradientNode(Material.IRON_ORE, 0.0, 0.3, 1.0, 0.05));
    upperCavernMaterial.nodes.add(new GradientNode(Material.GOLD_ORE, 0.2, 1.0, 1.0, 0.05));

    coreMaterial = new MaterialGradient();
    coreMaterial.nodes.add(new GradientNode(Material.BLACKSTONE, -1.0, -1.0, 1.0, 2.0));
    coreMaterial.nodes.add(new GradientNode(Material.GILDED_BLACKSTONE, -0.9, -0.4, 1.0, 0.05));
    coreMaterial.nodes.add(new GradientNode(Material.OBSIDIAN, -0.5, 0.0, 0.5, 1.0));
    coreMaterial.nodes.add(new GradientNode(Material.MAGMA_BLOCK, -0.4, 0.0, 0.4, 1.0));
    coreMaterial.nodes.add(new GradientNode(Material.LAVA, -0.3, 0.0, 0.3, 1.0));
    coreMaterial.nodes.add(new GradientNode(Material.ANCIENT_DEBRIS, -0.2, 0.0, 0.2, 0.025));
    coreMaterial.nodes.add(new GradientNode(Material.DIAMOND_ORE, -0.2, 0.0, 0.2, 0.05));
  }

  public static SimplexOctaveGenerator getNoiseGenerator(World world) {
    SimplexOctaveGenerator generator = new SimplexOctaveGenerator(world, 8);
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
    int blockX, blockZ;

    for (int X = 0; X < 16; X++) {
      blockX = chunkX*16+X;
      for (int Z = 0; Z < 16; Z++) {
        blockZ = chunkZ*16+Z;

        int surfaceHeight = getHeight(generator, blockX, blockZ, 1D, 30D, 128D);
        int surfaceBottomHeight = getHeight(generator, blockX, blockZ, 0.5D, 5D, 70D);
        int cavernTopHeight = getHeight(generator, blockX, blockZ, 10D, 20D, 50D);
        int cavernBottomHeight = getHeight(generator, blockX, blockZ, 2D, 40D, 20D);
        double noise;
        double depth;

        // Set biome
        for (int y = 255; y >= surfaceBottomHeight; y--) {
          biome.setBiome(X, y, Z, Biome.DESERT_HILLS);
        }

        // Set surface
        for (int y = surfaceHeight; y > surfaceHeight-4; y--)
          chunk.setBlock(X, y, Z, Material.GRAVEL);
        
        // Set upper caverns
        for (int y = surfaceHeight-4; y > surfaceBottomHeight; y--) {
          biome.setBiome(X, y, Z, Biome.SOUL_SAND_VALLEY);
          if(y >= surfaceHeight-6) {
            chunk.setBlock(X, y, Z, Material.STONE);
          } else {
            noise = generator.noise(blockX / 2.0, y, blockZ / 2.0, 0.0, 1.375, 4.0, true);
            if(noise >= 0.0) {
              chunk.setBlock(X, y, Z,
                upperCavernMaterial.getMaterial(random, noise));
            } else {
              chunk.setBlock(X, y, Z, Material.CAVE_AIR);
            }
          }
          // chunk.setBlock(X, y, Z,
          //   upperCavernMaterial.getMaterial(random, NumberUtil.mix(0.0, noise, depth)));
        }

        // Set cavern ceiling
        for (int y = surfaceBottomHeight; y > cavernTopHeight; y--) {
          biome.setBiome(X, y, Z, Biome.BASALT_DELTAS);
          chunk.setBlock(X, y, Z, Material.BASALT);
        }

        // Set cavern
        for (int y = cavernTopHeight; y > cavernBottomHeight; y--) {
          biome.setBiome(X, y, Z, Biome.BASALT_DELTAS);
          chunk.setBlock(X, y, Z, Material.CAVE_AIR);
        }
        
        if(random.nextDouble() < 0.01D) {
          chunk.setBlock(X, cavernTopHeight, Z, Material.CRYING_OBSIDIAN);
        }
        
        // Set core
        for (int y = cavernBottomHeight; y > 0; y--) {
          biome.setBiome(X, y, Z, Biome.SOUL_SAND_VALLEY);
          depth = 1 - Math.pow((double)y / cavernBottomHeight, 4);
          noise = generator.noise(X, y, Z, 0.0, 1.375, 4.0, true);

          chunk.setBlock(X, y, Z,
            coreMaterial.getMaterial(random, NumberUtil.mix(-1.0, noise, depth)));
        }
        
        // Set lava
        // for (int y = 40; y > 25; y--) {
        //   if(chunk.getBlockData(X, y, Z).getMaterial().isAir()) {
        //     chunk.setBlock(X, y, Z, Material.LAVA);
        //   }
        // }

        // Set bedrock cap
        chunk.setBlock(X, 0, Z, Material.BEDROCK);
      }
    }

    return chunk;
  }
}