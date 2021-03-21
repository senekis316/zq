package com.tdx.zq.enums;

public enum TendencyTypeEnum {

  UP, DOWN;

  @Override
  public String toString() {
    return "\"" + this.name() + (this.name().equals(TendencyTypeEnum.UP.name()) ? "  " : "") + "\"";
  }
}
