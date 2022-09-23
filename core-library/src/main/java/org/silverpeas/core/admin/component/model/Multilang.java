/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * A message translated in several languages, each of them supported by Silverpeas.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "L10nType", propOrder = { "message" })
public class Multilang {

  protected List<Message> message;

  /**
   * Gets the value of the message property.
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore, any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the message property.
   * <p>
   * For example, to add a new item, do as follows:
   *
   * <pre>
   * getMessage().add(newItem);
   * </pre>
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Message }
   */
  public List<Message> getMessage() {
    if (message == null) {
      message = new ArrayList<>();
    }
    return this.message;
  }

}
