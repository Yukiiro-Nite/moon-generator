package me.yukiironite;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;

@SerializableAs("Craters")
public class Craters implements ConfigurationSerializable {
  public String world;
  public ConcurrentLinkedQueue<Crater> craters;

  public Craters(String world, List<Crater> craters) {
    this.world = world;
    this.craters = new ConcurrentLinkedQueue<>();
    for(Crater crater : craters) {
      this.craters.add(crater);
    }
  }

  public Craters(Map<String, Object> config) {
    System.out.println("Craters key set: " + config.keySet());
    Object worldObj = config.get("world");
    System.out.println("World object is a: " + worldObj.getClass().getName());
    this.world = (String) config.get("world");
    this.craters = new ConcurrentLinkedQueue<>();

    try {
      List<Map<String, Object>> cratersConfig = (List<Map<String, Object>>) config.get("craters");
      cratersConfig
        .stream()
        .map(Crater::new)
        .forEach(crater -> this.craters.add(crater));
    } catch (ClassCastException e) {
      Main.getInstance().getLogger().severe("Problem reading world crater list data, setting craters to empty list.");
    }
  }

  public Craters(ConfigurationSection config) {
    this.world = config.getString("world");
    this.craters = new ConcurrentLinkedQueue<>();

    try {
      List<Map<String, Object>> cratersConfig = (List<Map<String, Object>>) config.getList("craters");
      cratersConfig
        .stream()
        .map(Crater::new)
        .forEach(crater -> this.craters.add(crater));
    } catch (ClassCastException e) {
      Main.getInstance().getLogger().severe("Problem reading world crater list data, setting craters to empty list.");
    }
    
  }

  public static Craters deserialize(Map<String, Object> config) {
    return new Craters(config);
  }

  public Map<String, Object> serialize() {
    HashMap<String, Object> output = new HashMap<>();
    List<Map<String, Object>> cratersOutput = craters
      .stream()
      .map(Crater::serialize)
      .collect(Collectors.toList());

    output.put("world", world);
    output.put("craters", cratersOutput);

    return output;
  }

  public void add(Crater crater) {
    craters.add(crater);
  }

  public Crater take() {
    return craters.poll();
  }

  public boolean isEmpty() {
    return craters.isEmpty();
  }

  public int size() {
    return craters.size();
  }

  @Override
  public String toString() {
    String s = "";

    s += "Craters for world: " + this.world + ", ";
    s += "Craters to render: " + craters.size();

    return s;
  }
}