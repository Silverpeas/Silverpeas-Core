package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.List;

public class SpaceWithSubSpacesAndComponents {

  private SpaceInstLight space;
  private List<SpaceWithSubSpacesAndComponents> subSpaces;
  private List<ComponentInstLight> components;

  public SpaceWithSubSpacesAndComponents(SpaceInstLight space) {
    this.space = space;
  }

  public List<SpaceWithSubSpacesAndComponents> getSubSpaces() {
    return subSpaces;
  }

  public void setSubSpaces(final List<SpaceWithSubSpacesAndComponents> subSpaces) {
    this.subSpaces = subSpaces;
  }

  public List<ComponentInstLight> getComponents() {
    return components;
  }

  public void setComponents(final List<ComponentInstLight> components) {
    this.components = components;
  }

  public SpaceInstLight getSpace() {
    return space;
  }

}
