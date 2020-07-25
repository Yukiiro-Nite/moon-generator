package me.yukiironite;

public class NumberUtil {
  public static double lerp(double value, double fromStart, double fromEnd, double toStart, double toEnd) {
    double fromRange = fromEnd - fromStart;
    double toRange = toEnd - toStart;
    double scale = toRange / fromRange;

    return (value - fromStart) * scale + toStart;
  }

  public static double mix(double from, double to, double amount) {
    double clamppedAmount = clamp(amount, 0, 1);
    return from * (1-clamppedAmount) + to * clamppedAmount;
  }

  public static double clamp(double val, double from, double to) {
    if(from > to) {
      return clamp(val, to, from);
    } else {
      return Math.max(from, Math.min(to, val));
    }
  }
}