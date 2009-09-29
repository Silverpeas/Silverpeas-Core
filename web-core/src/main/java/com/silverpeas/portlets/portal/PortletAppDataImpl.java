package com.silverpeas.portlets.portal;

public class PortletAppDataImpl implements PortletAppData {

  private String name;
  private String label;
  private String description;

  public PortletAppDataImpl(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
