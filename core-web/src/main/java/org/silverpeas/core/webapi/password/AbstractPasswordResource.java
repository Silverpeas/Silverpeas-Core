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
package org.silverpeas.core.webapi.password;

import org.silverpeas.core.security.authentication.password.rule.PasswordRule;
import org.silverpeas.core.security.authentication.password.service.PasswordCheck;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractPasswordResource extends RESTWebService {

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return "";
  }

  /**
   * Converts the password rule into its corresponding web entity.
   * @param rule the password rule.
   * @return the corresponding password rule entity.
   */
  protected PasswordRuleEntity asWebEntity(PasswordRule rule) {
    checkNotFoundStatus(rule);
    return PasswordRuleEntity.createFrom(rule, getLanguage());
  }

  /**
   * Converts the password check into its corresponding web entity.
   * @param check the password check.
   * @return the corresponding password check entity.
   */
  protected PasswordCheckEntity asWebEntity(PasswordCheck check) {
    checkNotFoundStatus(check);
    return PasswordCheckEntity.createFrom(check);
  }

  /**
   * Centralization
   * @param object any object
   */
  private void checkNotFoundStatus(Object object) {
    if (object == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Due to the particularity of this WEB Service according to authentication, the language is
   * handled at this level.
   * @return the language code
   */
  protected String getLanguage() {
    final String language;
    if (getUser() != null) {
      language = getUserPreferences().getLanguage();
    } else if (getHttpRequest().getLocale() != null) {
      language = DisplayI18NHelper.verifyLanguage(getHttpRequest().getLocale().getLanguage());
    } else {
      language = DisplayI18NHelper.getDefaultLanguage();
    }
    return language;
  }
}
