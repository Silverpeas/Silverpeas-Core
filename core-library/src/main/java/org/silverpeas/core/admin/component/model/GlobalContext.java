package org.silverpeas.core.admin.component.model;

public class GlobalContext {

  private String spaceId;
  private String componentId;
  private String componentName;

  public GlobalContext(String spaceId) {
    setSpaceId(spaceId);
  }

  public GlobalContext(String spaceId, String componentId) {
   setSpaceId(spaceId);
   setComponentId(componentId);
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

}
