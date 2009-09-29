package com.stratelia.webactiv.calendar.model;

public class Category implements java.io.Serializable {

  private String id;
  private String name;

  public Category(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }
}