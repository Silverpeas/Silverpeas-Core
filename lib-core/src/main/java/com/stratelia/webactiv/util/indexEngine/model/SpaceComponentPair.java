package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;

/**
 * A SpaceComponentPair packs in an object a space and a component names.
 */
public final class SpaceComponentPair implements Serializable {
  /**
   * The constructor set the pair which is immutable.
   */
  public SpaceComponentPair(String space, String component) {
    this.space = space;
    this.component = component;
  }

  /**
   * Return the space name.
   */
  public String getSpace() {
    return space;
  }

  /**
   * Return the component name.
   */
  public String getComponent() {
    return component;
  }

  /**
   * The equals method is re-defined so that a SpaceComponentPair can be added
   * in a Set or used as a Map key.
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof SpaceComponentPair) {
      SpaceComponentPair p = (SpaceComponentPair) o;
      return component.equals(p.component);
    } else
      return false;
  }

  /**
   * The hashCode method is re-defined so that a SpaceComponentPair can be added
   * in a Set or used as a Map key.
   */
  public int hashCode() {
    String s = "*";
    String c = "*";

    if (space != null)
      s = space;
    if (component != null)
      c = component;

    return (s + "/" + c).hashCode();
  }

  /**
   * The two parts of an SpaceComponentName are private and fixed at
   * construction time.
   */
  private final String space;
  private final String component;
}
