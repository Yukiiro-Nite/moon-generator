package me.yukiironite;

import org.bukkit.Chunk;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

public class Crater implements ConfigurationSerializable {
  public int depth;
  public int x;
  public int z;
  public int radius;
  public List<Vector> chunksToRender;

  public Crater(Map<String, Object> config) {
    this.depth = (int) config.get("depth");
    this.x = (int) config.get("x");
    this.z = (int) config.get("z");
    this.radius = this.depth * 4;

    try {
      List<Map<String, Object>> chunksConfig = (List<Map<String, Object>>) config.get("chunksToRender");
      chunksToRender = chunksConfig
        .stream()
        .map(Vector::deserialize)
        .collect(Collectors.toList());
    } catch (ClassCastException e) {
      Main.getInstance().getLogger().severe("Problem reading crater chunksToRender data, setting chunksToRender to empty list.");
      this.chunksToRender = new ArrayList<Vector>();
    }
  }

  public Crater(int x, int z, int depth) {
    this.depth = depth;
    this.x = x;
    this.z = z;
    this.radius = this.depth * 4;
    this.chunksToRender = this.getInRangeChunks();
  }

  public boolean isBlockInRange(int blockX, int blockZ) {
    return getDistance(blockX, blockZ) <= this.radius;
  }

  public boolean isChunkInRange(Chunk chunk) {
    int chunkWidth = 16;
    int chunkHeight = 16;
    int blockX = chunk.getX() * chunkWidth;
    int blockZ = chunk.getZ() * chunkHeight;

    int testX = this.x;
    int testZ = this.z;

    if(this.x < blockX) {
      testX = blockX;
    } else if (this.x >= blockX + chunkWidth) {
      testX = blockX + chunkWidth;
    }

    if(this.z < blockZ) {
      testZ = blockZ;
    } else if (this.z >= blockZ + chunkWidth) {
      testZ = blockZ + chunkWidth;
    }

    return getDistance(testX, testZ) <= this.radius;
  }

  public List<Vector> getInRangeChunks() {
    ArrayList<Vector> chunksInRange = new ArrayList<>();
    int minChunkX = (int) Math.floor((this.x - this.radius) / 16.0);
    int maxChunkX = (int) Math.ceil((this.x + this.radius) / 16.0);
    int minChunkZ = (int) Math.floor((this.z - this.radius) / 16.0);
    int maxChunkZ = (int) Math.ceil((this.z + this.radius) / 16.0);

    for(int x = minChunkX; x <= maxChunkX; x++) {
      for(int z = minChunkZ; z <= maxChunkZ; z++) {
        chunksInRange.add(new Vector(x, 0, z));
      }
    }

    return chunksInRange;
  }

  public double getDistance(int blockX, int blockZ) {
    return Math.hypot(blockX-x, blockZ-z);
  }

  public int getHeightOffset(int blockX, int blockZ) {
    return parabolicCrater(getDistance(blockX, blockZ));
  }

  // using a modified inverse mexican hat
  // https://en.wikipedia.org/wiki/Mexican_hat_wavelet
  public int mexicanHat(double distance) {
    double amp = depth * 2;
    double value = -depth
      * (1 - 1.5 * Math.pow(distance / amp, 2))
      * Math.pow(Math.E, -1 * (Math.pow(distance, 4) / (2 * Math.pow(amp, 4))));
    
    return (int) Math.round(value);
  }

  public int parabolicCrater(double distance) {
    double parabola = (1.0 / (3.0 * this.depth)) * Math.pow(distance, 2) - this.depth;
    double cone = -Math.abs(0.25 * distance) + this.depth;
    double value = Math.min(parabola, cone);

    return (int) Math.round(value);
  }

  public Map<String, Object> serialize() {
    HashMap<String, Object> output = new HashMap<>();
    List<Map<String, Object>> chunksOutput = chunksToRender
      .stream()
      .map(Vector::serialize)
      .collect(Collectors.toList());

    output.put("depth", this.depth);
    output.put("x", this.x);
    output.put("z", this.z);
    output.put("chunksToRender", chunksOutput);

    return output;
  }

  public void removeChunk(Vector chunkPosition) {
    chunksToRender.remove(chunkPosition);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Crater) {
      Crater crater = (Crater) obj;

      return this.depth == crater.depth
        && this.x == crater.x
        && this.z == crater.x;
    } else {
      return false;
    }
  }
}