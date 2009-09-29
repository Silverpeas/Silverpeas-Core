/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.ccpp;

import java.util.Set;

import javax.ccpp.Attribute;
import javax.ccpp.Component;
import javax.ccpp.Profile;
import javax.ccpp.ProfileDescription;

/**
 * The DefaultCCPPProfile provides a dummy implementation of the CCPP Profile
 * object.
 */
public class DefaultCCPPProfile implements Profile {

  /**
   * Retrieves the attribute with the specified name contained within this
   * profile. If an attribute is not unique within this profile, then the
   * attribute returned is implementation specific. In this case, the attribute
   * should instead be retrieved with
   * <code>getComponent(String).getAttribute(String)</code>.
   * <p>
   * The object returned by this method, if not <code>null</code>, is an element
   * of the <code>Set</code> returned by <code>getAttributes()</code>. The
   * following expression will return <code>true</code>:
   * <code>name.equals(getAttribute(name).getName())</code>.
   * </p>
   * 
   * @param name
   *          the name of the attribute to retrieve
   * @return the attribute with the specified name, or <code>null</code> if no
   *         such attribute exists within this profile
   */
  public Attribute getAttribute(String name) {
    return null;
  }

  /**
   * Retrieves all the attributes contained within this profile as a
   * <code>Set</code> of <code>Attribute</code> instances.
   * 
   * @return all the attributes contained within this profile
   */
  public Set getAttributes() {
    return null;
  }

  /**
   * Retrieves the component with the specified local type (relative to the
   * vocabulary) contained within this profile. The object returned by this
   * method, if not <code>null</code>, is an element of the <code>Set</code>
   * returned by <code>getComponents()</code>. The following expression will
   * return <code>true</code>:
   * <code>localtype.equals(getComponent(localtype).getDescription().getLocalType())</code>
   * .
   * 
   * @param localtype
   *          the local type of the component to retrieve as returned by
   *          <code>Component.getDescription().getLocalType()</code>
   * @return the component with the specified local type, or <code>null</code>
   *         if no such component exists within this profile
   */
  public Component getComponent(String localtype) {
    return null;
  }

  /**
   * Retrieves all the components contained within this profile as a
   * <code>Set</code> of <code>Component</code> instances.
   * 
   * @return all the components contained within this profile
   */
  public Set getComponents() {
    return null;
  }

  /**
   * Retrieves the object describing this profile.
   * 
   * @return this profile's description object
   */
  public ProfileDescription getDescription() {
    return null;
  }
}
