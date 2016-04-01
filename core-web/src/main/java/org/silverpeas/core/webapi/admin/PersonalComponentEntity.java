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

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInst;

/**
 * The personal component entity.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonalComponentEntity extends AbstractPersonnalEntity {

  public static final String TYPE = "personal-component";

  private static final long serialVersionUID = -8503056197679476051L;

  /**
   * Creates a new personal component entity from the specified WAComponent.
   * @param component the tool to entitify.
   * @return new personal component entity
   */
  public static PersonalComponentEntity createFrom(final WAComponent component,
      final String componentLabel, final String language) {
    return new PersonalComponentEntity(component, componentLabel, language);
  }

  /**
   * Creates a new personal component entity from the specified ComponentInst.
   * @param component the tool to entitify.
   * @return new personal component entity
   */
  public static PersonalComponentEntity createFrom(final ComponentInst component) {
    return new PersonalComponentEntity(component);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.AbstractPersonnalEntity#getUriIdentifier()
   */
  @Override
  protected String getUriIdentifier() {
    return getName();
  }

  private PersonalComponentEntity(final WAComponent component, final String componentLabel,
      final String language) {
    super(TYPE, "", 0, component.getName(), componentLabel, component.getDescription()
        .get(language), "");
  }

  private PersonalComponentEntity(final ComponentInst component) {
    super(TYPE, component.getId(), 0, component.getName(), component.getLabel(), component
        .getDescription(), URLUtil.getURL(component.getName(), null, component.getName() +
        component.getId()) +
        "Main");
  }

  protected PersonalComponentEntity() {
    // Nothing to do (Tests)
  }
}
