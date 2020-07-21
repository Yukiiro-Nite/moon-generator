package me.yukiironite;

import org.bukkit.Chunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.Material;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CraterPopulator extends BlockPopulator {
  public static Map<String, List<Crater>> cratersByWorld;

  static {
    cratersByWorld = loadAllCraters();
  }

  public void populate(World world, Random random, Chunk chunk) {
    /**
     * This crater populator has two parts.
     * part 1:
     *   - Decide if this chunk will have a crater
     *   - Add crater to list with information about which chunks will need to render it.
     * part 2:
     *   - Go through each crater
     *   - If this chunk is requested, render crater to chunk, remove chunk from pending
     *   - If all request chunks have been rendered, remove crater from list
     *   - Go through current craters checking if there are other chunks that can be rendered
     *   - Save pending craters to file so that we don't have chunk glitches
     */
    if (random.nextInt(100) < 30) {
      SimplexOctaveGenerator generator = MoonChunkGenerator.getNoiseGenerator(world);

      int x = random.nextInt(15);
      int z = random.nextInt(15);
      int y = MoonChunkGenerator.getHeight(generator, chunk.getX()*16+x, chunk.getZ()*16+z, 1D, 30D, 128D);

      chunk.getBlock(x, y, z).setType(Material.CRYING_OBSIDIAN);
    }
  }

  public void populateCrater(World world, Random random, Chunk chunk) {

  }

  public void renderCrater(World world, Chunk chunk, Crater crater) {

  }

  public static Map<String, List<Crater>> loadAllCraters() {
    HashMap<String, List<Crater>> cratersByWorld = new HashMap<String, List<Crater>>();
    String dataPath = Main.getInstance().getDataFolder().getPath() + File.separator;
    File dataPathFile = new File(dataPath);
    File[] craterFiles = dataPathFile.listFiles(new FilenameFilter(){
      public boolean accept(File dir, String name) {
        return name.endsWith("-craters.yml");
      }
    });

    Arrays.asList(craterFiles)
      .stream()
      .map(file -> {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        

        return config;
      });
  }

  public static List<Crater> loadCraters(World world) {

  }

  public static void saveCraters(World world) {

  }

  public static void addCrater(World world, Crater crater) {

  }

  public static void removeCrater(World world, Crater crater) {

  }

  public static String getDataPath() {
    return Main
      .getInstance()
      .getDataFolder()
      .getPath()
      + File.separator;
  }
}