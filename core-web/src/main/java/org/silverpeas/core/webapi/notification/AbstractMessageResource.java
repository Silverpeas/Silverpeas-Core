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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.notification;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.notification.message.MessageContainer;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractMessageResource extends RESTWebService {

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return "";
  }

  /**
   * Converts the message container into its corresponding web entity.
   * @param messageContainer the message container.
   * @return the corresponding message container entity.
   */
  protected MessageContainerEntity asWebEntity(MessageContainer messageContainer) {
    return MessageContainerEntity.createFrom(messageContainer);
  }

  /**
   * Due to the particularity of this WEB Service according to authentication, the language is
   * handled at this level.
   * @return
   */
  protected String getLanguage() {
    if (getUserDetail() != null) {
      return getUserPreferences().getLanguage();
    }
    return I18NHelper.defaultLanguage;
  }
}
