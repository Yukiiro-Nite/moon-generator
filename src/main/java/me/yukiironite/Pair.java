package me.yukiironite;

// feels strange that I had to write one of these...
// why isn't this a part of utils?
public class Pair<X, Y> { 
  public final X x; 
  public final Y y; 
  public Pair(X x, Y y) { 
    this.x = x; 
    this.y = y; 
  } 
} 