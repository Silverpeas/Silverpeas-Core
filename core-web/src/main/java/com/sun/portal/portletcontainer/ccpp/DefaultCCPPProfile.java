/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.ccpp;

import java.util.Set;

import javax.ccpp.Attribute;
import javax.ccpp.Component;
import javax.ccpp.Profile;
import javax.ccpp.ProfileDescription;

/**
 * The DefaultCCPPProfile provides a dummy implementation of the CCPP Profile object.
 */
public class DefaultCCPPProfile implements Profile {

  /**
   * Retrieves the attribute with the specified name contained within this profile. If an attribute
   * is not unique within this profile, then the attribute returned is implementation specific. In
   * this case, the attribute should instead be retrieved with
   * <code>getComponent(String).getAttribute(String)</code>.
   * <p>
   * The object returned by this method, if not <code>null</code>, is an element of the
   * <code>Set</code> returned by <code>getAttributes()</code>. The following expression will return
   * <code>true</code>: <code>name.equals(getAttribute(name).getName())</code>.
   * </p>
   * @param name the name of the attribute to retrieve
   * @return the attribute with the specified name, or <code>null</code> if no such attribute exists
   * within this profile
   */
  public Attribute getAttribute(String name) {
    return null;
  }

  /**
   * Retrieves all the attributes contained within this profile as a <code>Set</code> of
   * <code>Attribute</code> instances.
   * @return all the attributes contained within this profile
   */
  public Set getAttributes() {
    return null;
  }

  /**
   * Retrieves the component with the specified localResourceLocator type (relative to the
   * vocabulary) contained within this profile. The object returned by this method, if not
   * <code>null</code>, is an element of the <code>Set</code> returned by
   * <code>getComponents()</code>. The following expression will return <code>true</code>:
   * <code>localtype.equals(getComponent(localtype).getDescription().getLocalType())</code> .
   * @param localtype the localResourceLocator type of the component to retrieve as returned by
   * <code>Component.getDescription().getLocalType()</code>
   * @return the component with the specified localResourceLocator type, or <code>null</code> if no
   * such component exists within this profile
   */
  public Component getComponent(String localtype) {
    return null;
  }

  /**
   * Retrieves all the components contained within this profile as a <code>Set</code> of
   * <code>Component</code> instances.
   * @return all the components contained within this profile
   */
  public Set getComponents() {
    return null;
  }

  /**
   * Retrieves the object describing this profile.
   * @return this profile's description object
   */
  public ProfileDescription getDescription() {
    return null;
  }
}
