package me.yukiironite;

public class Crater {
  public int depth;
  public int x;
  public int z;

  public Crater(int x, int z, int depth) {
    this.x = x;
    this.z = z;
    this.depth = depth;
  }

  public boolean isInRange(int xOffset, int zOffset) {
    double maxDistance = depth * 3; // This works up to depths around 100
    double distance = Math.hypot(xOffset-x, zOffset-z);

    return distance <= maxDistance;
  }

  public int getHeightOffset(int xOffset, int zOffset) {
    double distance = Math.hypot(xOffset-x, zOffset-z);
    double amp = depth * 1.6;
    // using a modified inverse mexican hat
    // https://en.wikipedia.org/wiki/Mexican_hat_wavelet
    double heightOffset = -depth
      * (1 - Math.pow(distance / amp, 2))
      * Math.pow(Math.E, -1 * (Math.pow(x, 4) / Math.pow(2 * amp, 4)));
    
    return (int) Math.round(heightOffset);
  }
}