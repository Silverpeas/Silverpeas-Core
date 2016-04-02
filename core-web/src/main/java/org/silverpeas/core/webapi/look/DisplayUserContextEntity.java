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
package org.silverpeas.core.webapi.look;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;

/**
 * The user display context entity represents display settings of the current user
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DisplayUserContextEntity extends AbstractLookEntity<DisplayUserContextEntity> {
  private static final long serialVersionUID = -1135190036189814798L;

  @XmlElement
  private UserMenuDisplay userMenuDisplay;

  @XmlElement
  private String language;

  /**
   * Creates user display context entity.
   * @param lookHelper the look helper
   * @param userPreferences the user preferences
   * @return
   */
  public static DisplayUserContextEntity createFrom(final LookHelper lookHelper,
      final UserPreferences userPreferences) {
    return new DisplayUserContextEntity(lookHelper, userPreferences);
  }

  /**
   * Instantiating a new web entity from the corresponding data
   * @param lookHelper the look helper
   * @param userPreferences the user preferences
   */
  private DisplayUserContextEntity(final LookHelper lookHelper,
      final UserPreferences userPreferences) {
    userMenuDisplay = lookHelper.getDisplayUserMenu();
    language = userPreferences.getLanguage();
  }

  protected DisplayUserContextEntity() {
  }
}
