/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.silverpeas.core.webapi.admin.tools.AbstractTool;


/**
 * The personal tool entity.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonalToolEntity extends AbstractPersonnalEntity {

  public static final String TYPE = "personal-tool";

  private static final long serialVersionUID = -8503056197679476051L;

  /**
   * Creates a new personal tool entity from the specified tool.
   * @param tool the tool to entitify.
   * @return new personal tool entity
   */
  public static PersonalToolEntity createFrom(final AbstractTool tool) {
    return new PersonalToolEntity(tool);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.AbstractPersonnalEntity#getUriIdentifier()
   */
  @Override
  protected String getUriIdentifier() {
    return getId();
  }

  private PersonalToolEntity(final AbstractTool tool) {
    super(TYPE, tool.getId(), tool.getNb(), "", tool.getLabel(), "", tool.getUrl());
  }
}
