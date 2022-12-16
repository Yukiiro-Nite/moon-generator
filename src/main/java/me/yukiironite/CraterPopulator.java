package me.yukiironite;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.UUID;

public class CraterPopulator extends BlockPopulator {
  public static Map<String, Craters> cratersByWorld;
  public static MaterialGradient craterMaterial;

  static {
    cratersByWorld = loadAllCraters();
    craterMaterial = new MaterialGradient();

    craterMaterial.nodes.add(new GradientNode(Material.ANCIENT_DEBRIS, 0, 0, 0.05, 0.1));
    craterMaterial.nodes.add(new GradientNode(Material.LAVA, 0, 0, 0.05, 1));
    craterMaterial.nodes.add(new GradientNode(Material.MAGMA_BLOCK, 0, 0.05, 0.1, 1));
    craterMaterial.nodes.add(new GradientNode(Material.CRYING_OBSIDIAN, 0.05, 0.1, 0.15, 0.25));
    craterMaterial.nodes.add(new GradientNode(Material.OBSIDIAN, 0.05, 0.2, 0.3, 1));
    craterMaterial.nodes.add(new GradientNode(Material.BLACKSTONE, 0.2, 0.3, 0.43, 1));
    craterMaterial.nodes.add(new GradientNode(Material.BASALT, 0.3, 0.43, 0.52, 1));
    craterMaterial.nodes.add(new GradientNode(Material.DEAD_BUBBLE_CORAL_BLOCK, 0.43, 0.52, 1.0, 1));
    craterMaterial.nodes.add(new GradientNode(Material.DEAD_BRAIN_CORAL_BLOCK, 0.52, 1.0, 1.0, 1));
  }

  public void populate(World world, Random random, Chunk chunk) {
    FileConfiguration config = Main.getInstance().getConfig();
    boolean generateCraters = config.getBoolean("generate-craters");
    /**
     * This crater populator has two parts. part 1: - Decide if this chunk will have
     * a crater - Add crater to list with information about which chunks will need
     * to render it. part 2: - Go through each crater - If this chunk is requested,
     * render crater to chunk, remove chunk from pending - If all request chunks
     * have been rendered, remove crater from list - Go through current craters
     * checking if there are other chunks that can be rendered - Save pending
     * craters to file so that we don't have chunk glitches
     */
    if(generateCraters) {
      populateCrater(world, random, chunk);
      renderCraters(world, random);
    }
  }

  public void populateCrater(World world, Random random, Chunk chunk) {
    FileConfiguration config = Main.getInstance().getConfig();
    int chunksPerCrater = config.getInt("chunks-per-crater", 1024);
    Craters worldCraters = cratersByWorld
      .get(world.getUID()
      .toString());
    boolean hasPendingCrater = worldCraters == null
      ? false
      : worldCraters
        .craters
        .stream()
        .anyMatch(crater -> crater.isChunkInRange(chunk));
    if (!hasPendingCrater && random.nextInt(chunksPerCrater) <= 1) {
      int depth = random.nextInt(25) + 3;
      int innerX = random.nextInt(15);
      int innerZ = random.nextInt(15);
      int blockX = chunk.getX() * 16 + innerX;
      int blockZ = chunk.getZ() * 16 + innerZ;

      Crater crater = new Crater(blockX, blockZ, depth);
      addCrater(world, crater);
    }
  }

  public void renderCraters(World world, Random random) {
    String worldId = world.getUID().toString();
    Craters craters = cratersByWorld.get(worldId);
    if(craters == null) return;

    int size = craters.size();
    int counter = 0;

    while(!craters.isEmpty() && counter < size) {
      Crater crater = craters.take();
      if(crater == null) break;

      ArrayList<Vector> chunksToRemove = new ArrayList<>();
      for(Vector chunkPosition : crater.chunksToRender) {
        if(chunkPosition == null) {
          System.out.println("Chunk position is null for some reason...");
        } else {
          int blockX = chunkPosition.getBlockX();
          int blockZ = chunkPosition.getBlockZ();
          boolean isGenerated = world.isChunkGenerated(blockX, blockZ);
          if(isGenerated) {
            Chunk chunk = world.getChunkAt(chunkPosition.getBlockX(), chunkPosition.getBlockZ());
            renderCrater(world, random, chunk, crater);
            chunksToRemove.add(chunkPosition);
          }
        }
      }

      for(Vector toRemove : chunksToRemove) {
        crater.removeChunk(toRemove);
      }

      if(crater.chunksToRender.size() > 0) {
        craters.add(crater);
      }

      counter++;
    }
  }

  public void renderCrater(World world, Random random, Chunk chunk, Crater crater) {
    int chunkX = chunk.getX();
    int chunkZ = chunk.getZ();
    int blockX, blockY, blockZ;

    for (int innerX = 0; innerX < 16; innerX++) {
      blockX = chunkX * 16 + innerX;
      for (int innerZ = 0; innerZ < 16; innerZ++) {
        blockZ = chunkZ * 16 + innerZ;
        if(crater.isBlockInRange(blockX, blockZ)) {
          blockY = world.getHighestBlockYAt(blockX, blockZ);
          int offset = crater.getHeightOffset(blockX, blockZ);
          int craterLevel = Math.max(0, Math.min(255, blockY + offset));
          double distanceRatio = crater.getDistance(blockX, blockZ) / crater.radius;

          for(int y = craterLevel; y >= craterLevel - 5; y--) {
            chunk.getBlock(innerX, y, innerZ)
              .setType(getCraterMaterial(random, distanceRatio));
          }

          if(craterLevel > blockY) {
            for(int y = craterLevel-6; y >= blockY; y--) {
              chunk.getBlock(innerX, y, innerZ).setType(Material.DEAD_BUBBLE_CORAL_BLOCK);
            }
          } else {
            for(int y = craterLevel+1; y <= blockY; y++) {
              chunk.getBlock(innerX, y, innerZ).setType(Material.AIR);
            }
          }
        }
      }
    }
  }

  public Material getCraterMaterial(Random r, double d) {
    return craterMaterial.getMaterial(r, d);
  }

  public static Map<String, Craters> loadAllCraters() {
    File dataPathFile = new File(getDataPath());
    File[] craterFiles = dataPathFile.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith("-craters.yml");
      }
    });

    if(craterFiles == null) {
      return new HashMap<>();
    }

    return Arrays.asList(craterFiles).stream().map(file -> {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
      ConfigurationSection cratersConfig = config.getConfigurationSection("serializedCraters");

      return new Craters(cratersConfig);
    }).filter(craters -> {
      System.out.println("Filterning crater configs: " + craters);
      UUID worldId = UUID.fromString(craters.world);
      boolean hasWorld = Bukkit.getWorld(worldId) != null;

      if (!hasWorld) {
        removeConfig(worldId);
      }

      return hasWorld;
    }).collect(Collectors.toMap(craters -> craters.world, craters -> craters));
  }

  public static Craters loadCraters(World world) {
    String worldId = world.getUID().toString();
    String configPath = getDataPath() + worldId + "-craters.yml";
    File file = new File(configPath);
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
    ConfigurationSection cratersConfig = config.getConfigurationSection("serializedCraters");

    return new Craters(cratersConfig);
  }

  public static void saveAll() {
    Bukkit.getWorlds()
      .stream()
      .forEach(world -> saveCraters(world));
  }

  public static void saveCraters(World world) {
    String worldId = world.getUID().toString();
    String configPath = getDataPath() + worldId + "-craters.yml";
    File file = new File(configPath);
    Craters cratersToSave = cratersByWorld.get(worldId);

    if(cratersToSave != null) {
      YamlConfiguration config = new YamlConfiguration();

      config.set("serializedCraters", cratersToSave.serialize());
      try {
        config.save(file);
      } catch (IOException e) {
        String message = String.format("Problem saving craters for %s [%s]", world.getName(), worldId);
        Main.getInstance().getLogger().severe(message);
      }
    }
  }

  public static void addCrater(World world, Crater crater) {
    System.out.println("Adding crater at (" + crater.x + ", " + crater.z + ")");
    String worldId = world.getUID().toString();
    if(cratersByWorld.containsKey(worldId)) {
      cratersByWorld.get(worldId).add(crater);
    } else {
      ArrayList<Crater> craters = new ArrayList<>();
      craters.add(crater);
      Craters worldCraters = new Craters(worldId,  craters);

      cratersByWorld.put(worldId, worldCraters);
    }

    saveCraters(world);
  }

  public static void removeConfig(UUID worldId) {
    String configPath = getDataPath() + worldId.toString() + "-craters.yml";
    File file = new File(configPath);

    file.delete();
  }

  public static String getDataPath() {
    return Main
      .getInstance()
      .getDataFolder()
      .getPath()
      + File.separator;
  }
}
