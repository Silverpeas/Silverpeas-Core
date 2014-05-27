package com.silverpeas.thumbnail;

public class ThumbnailSettings {

  private boolean mandatory;
  private int width = -1;
  private int height = -1;
  
  public boolean isMandatory() {
    return mandatory;
  }
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }
  public int getWidth() {
    return width;
  }
  public void setWidth(int width) {
    this.width = width;
  }
  public int getHeight() {
    return height;
  }
  public void setHeight(int height) {
    this.height = height;
  }
  
  
}
