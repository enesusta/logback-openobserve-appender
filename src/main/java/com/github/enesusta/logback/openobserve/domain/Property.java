package com.github.enesusta.logback.openobserve.domain;

public class Property {
  private String name;
  private String value;
  private boolean allowEmpty;
  private Type type = Type.STRING;

  public enum Type {
    STRING,
    INT,
    FLOAT,
    BOOLEAN
  }

  public Property() {
  }

  public Property(String name, String value, boolean allowEmpty) {
    this.name = name;
    this.value = value;
    this.allowEmpty = allowEmpty;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public boolean isAllowEmpty() {
    return allowEmpty;
  }

  public Type getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setAllowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
  }

  public void setType(String type) {
    try {
      this.type = Enum.valueOf(Type.class, type.toUpperCase());
    } catch (IllegalArgumentException e) {
      this.type = Type.STRING;
    }
  }
}
