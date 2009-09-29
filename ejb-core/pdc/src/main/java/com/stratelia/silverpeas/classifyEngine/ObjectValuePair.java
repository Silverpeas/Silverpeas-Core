package com.stratelia.silverpeas.classifyEngine;

public class ObjectValuePair {

  private Integer objectId = null;
  private String instanceId = null;
  private String valuePath = null;

  public ObjectValuePair(int objectId, String valuePath, String instanceId) {
    this.objectId = new Integer(objectId);
    this.valuePath = valuePath;
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Integer getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = new Integer(objectId);
  }

  public String getValuePath() {
    return valuePath;
  }

  public void setValuePath(String valuePath) {
    this.valuePath = valuePath;
  }
}
