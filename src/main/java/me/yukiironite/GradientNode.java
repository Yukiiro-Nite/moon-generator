package me.yukiironite;

import org.bukkit.Material;

public class GradientNode {
  public Material material;
  public double min;
  public double center;
  public double max;
  public double weight;

  public GradientNode(Material material, double min, double center, double max, double weight) {
    this.material = material;
    this.min = min;
    this.center = center;
    this.max = max;
    this.weight = weight;
  }

  public double valueAt(double pos) {
    return pos < center
      ? NumberUtil.lerp(pos, min, center, 0, weight)
      : NumberUtil.lerp(pos, max, center, 0, weight);
  }
}