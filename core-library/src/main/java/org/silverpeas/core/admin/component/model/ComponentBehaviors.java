/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific behavior of a WA component. Such behavior denotes a specific and transverse mechanism in
 * Silverpeas. When a WA component support a behavior, then Silverpeas bootstraps for each instance
 * of this component the related mechanism. For a glance of the behaviors available in Silverpeas,
 * see {@link ComponentBehavior} enum.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "behavior"
})
public class ComponentBehaviors {

  @XmlElement(required = true)
  @XmlSchemaType(name = "string")
  protected List<ComponentBehavior> behavior;

  /**
   * Gets the value of the behavior property.
   *
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore, any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the behavior property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getBehavior().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list {@link ComponentBehavior }
   */
  public List<ComponentBehavior> getBehavior() {
    if (behavior == null) {
      behavior = new ArrayList<>();
    }
    return this.behavior;
  }
}
