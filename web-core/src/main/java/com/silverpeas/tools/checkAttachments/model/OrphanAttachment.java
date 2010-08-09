package com.silverpeas.tools.checkAttachments.model;


public class OrphanAttachment
{
  public String getPhysicalName() {
    return physicalName;
  }
  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }
  public long getSize() {
    return size;
  }
  public void setSize(long size) {
    this.size = size;
  }
  public String getSpaceLabel() {
    return spaceLabel;
  }
  public void setSpaceLabel(String spaceLabel) {
    this.spaceLabel = spaceLabel;
  }
  public String getComponentLabel() {
    return componentLabel;
  }
  public void setComponentLabel(String componentLabel) {
    this.componentLabel = componentLabel;
  }
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }
  private String physicalName = null;
  private long size;
  private String spaceLabel = null;
  private String componentLabel = null;
  private String context = null;
  private String path = null;
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  
}