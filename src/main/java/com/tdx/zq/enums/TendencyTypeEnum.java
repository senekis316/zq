package com.tdx.zq.enums;

public enum TendencyTypeEnum {

  UP, DOWN;

  @Override
  public String toString() {
    return "\"" + this.name() + "\"";
  }
}
