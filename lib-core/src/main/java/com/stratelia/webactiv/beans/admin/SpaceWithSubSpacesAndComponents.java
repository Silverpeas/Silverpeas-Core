package com.stratelia.webactiv.beans.admin;

import java.util.List;

/**
 * Created by Nicolas on 14/04/2017.
 */
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
