package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

public class InfoAttachmentDetail extends InfoItemDetail implements Serializable{
  
  private String physicalName = null;
  private String logicalName = null;
  private String description = null;
  private String type = null;
  private long size;
  
  public InfoAttachmentDetail(InfoPK infoPK, String order, String id, String physicalName, String logicalName, String description, String type, long size) {
    super(infoPK, order, id);
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.description = description;
    this.type = type;
    this.size = size;
  }
  
  public String getPhysicalName() {
    return physicalName;
  }
  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }
  
  public String getLogicalName() {
    return logicalName;
  }
  public void setLogicalName(String logicalName) {
    this.logicalName = logicalName;
  }
  
  public String getDescription() {
    return description;
  }
  public void setDescription(String desc) {
    this.description = desc;
  }

  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  public long getSize() {
    return size;
  }  
  public void setSize(long size) {
    this.size = size;
  }

}