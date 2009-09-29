package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.Vector;

public class SpaceAndChildren implements Serializable {
  private SpaceInstLight space = null;
  private Vector subspaces = new Vector();
  private Vector components = new Vector();

  public SpaceAndChildren(SpaceInstLight space) {
    this.space = space;
  }

  public void addSubSpace(SpaceInstLight subSpace) {
    subspaces.add(subSpace);
  }

  public void addComponent(ComponentInstLight component) {
    components.add(component);
  }

  public Vector getSubspaces() {
    return this.subspaces;
  }

  public Vector getComponents() {
    return this.components;
  }

  public SpaceInstLight getSpace() {
    return space;
  }
}