package me.yukiironite;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Material;

import java.util.ArrayList;

public class MaterialGradient {
  public List<GradientNode> nodes;

  public MaterialGradient() {
    this.nodes = new ArrayList<>();
  }

  public Material getMaterial(Random r, double pos) {
    List<Pair<Double, Material>> materials = nodes
      .stream()
      .map(node -> new Pair<>(node.valueAt(pos), node.material))
      .filter(pair -> pair.x > 0)
      .collect(Collectors.toList());
    double total = materials.stream().collect(Collectors.summingDouble(pair -> pair.x));
    double lastStep = 0;
    double selection = r.nextDouble();

    for(Pair<Double, Material> pair : materials) {
      double nextStep = lastStep + (pair.x / total);

      if(selection > lastStep && selection <= nextStep) {
        return pair.y;
      } else {
        lastStep = nextStep;
      }
    }

    return Material.AIR;
  }
}