package com.tdx.zq.tuple;

public class ThreeTuple<A, B, C> {

  public final A first;

  public final B second;

  private final C third;

  public ThreeTuple(A a, B b, C c) {
    this.first = a;
    this.second = b;
    this.third = c;
  }

}
