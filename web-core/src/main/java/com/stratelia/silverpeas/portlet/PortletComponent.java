package com.stratelia.silverpeas.portlet;

public class PortletComponent {
  public int id = -1;
  public int spaceId = -1;
  public String name = "";
  public String componentName = "";
  public String description = "";

  public PortletComponent(int theId, int theSpaceId, String theName,
      String theComponentName, String theDescription) {
    id = theId;
    spaceId = theSpaceId;
    name = theName;
    componentName = theComponentName;
    description = theDescription;
  }
}
