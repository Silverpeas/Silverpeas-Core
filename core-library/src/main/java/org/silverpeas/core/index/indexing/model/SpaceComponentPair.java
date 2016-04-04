/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.index.indexing.model;

import java.io.Serializable;

/**
 * A SpaceComponentPair packs in an object a space and a component names.
 */
public final class SpaceComponentPair implements Serializable {

  private static final long serialVersionUID = 7721088829719133520L;

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
   * The equals method is re-defined so that a SpaceComponentPair can be added in a Set or used as a
   * Map key.
   */
  public boolean equals(Object o) {
    if (o != null && o instanceof SpaceComponentPair) {
      SpaceComponentPair p = (SpaceComponentPair) o;
      return component.equals(p.component);
    } else
      return false;
  }

  /**
   * The hashCode method is re-defined so that a SpaceComponentPair can be added in a Set or used as
   * a Map key.
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
   * The two parts of an SpaceComponentName are private and fixed at construction time.
   */
  private final String space;
  private final String component;
}
