package me.yukiironite;

public class NumberUtil {
  public static double lerp(double value, double fromStart, double fromEnd, double toStart, double toEnd) {
    double fromRange = fromEnd - fromStart;
    double toRange = toEnd - toStart;
    double scale = toRange / fromRange;

    return (value - fromStart) * scale + toStart;
  }
}