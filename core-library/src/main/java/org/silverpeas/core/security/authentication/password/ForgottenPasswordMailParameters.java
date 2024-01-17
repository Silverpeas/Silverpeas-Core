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
package org.silverpeas.core.security.authentication.password;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.ui.DisplayI18NHelper;

import java.util.HashMap;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class ForgottenPasswordMailParameters {

  private static final String KEY_DOMAIN_ID = "domainId";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_ERROR = "error";
  private static final String KEY_LINK = "link";
  private static final String KEY_LOGIN = "login";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_USER_NAME = "userName";

  private static final String TEXT_LINE_SEPARATOR = "\r\n";
  private static final String HTML_LINE_SEPARATOR = "<br>";

  private final HashMap<String, String> parametersValues;
  private String toAddress;
  private String language;

  public ForgottenPasswordMailParameters() {
    parametersValues = new HashMap<>();
  }

  public void setToAddress(String toAddress) {
    this.toAddress = toAddress;
  }

  public String getToAddress() {
    return toAddress;
  }

  public Optional<String> getMessage() {
    return ofNullable(parametersValues.get(KEY_MESSAGE));
  }

  public void setDomainId(String domainId) {
    parametersValues.put(KEY_DOMAIN_ID, domainId);
  }

  public void setEmail(String email) {
    parametersValues.put(KEY_EMAIL, email);
  }

  public void setError(String error) {
    parametersValues.put(KEY_ERROR, error);
  }

  public void setLink(String link) {
    parametersValues.put(KEY_LINK, link);
  }

  public void setLogin(String login) {
    parametersValues.put(KEY_LOGIN, login);
  }

  public void setMessage(String message) {
    parametersValues.put(KEY_MESSAGE, replaceLineSeparators(message));
  }

  public void setPassword(String password) {
    parametersValues.put(KEY_PASSWORD, password);
  }

  public void setUserName(String userName) {
    parametersValues.put(KEY_USER_NAME, userName);
  }

  public void setUserLanguage(String lang) {
    this.language = lang;
  }

  public String getUserLanguage() {
    return defaultStringIfNotDefined(language, DisplayI18NHelper.getDefaultLanguage());
  }

  public void applyTemplateData(final SilverpeasTemplate template) {
    parametersValues.forEach(template::setAttribute);
  }

  private static String replaceLineSeparators(String s) {
    StringBuilder sb = new StringBuilder();
    if (s != null) {
      int index = s.indexOf(TEXT_LINE_SEPARATOR);
      while (index != -1) {
        sb.append(s.substring(0, index)).append(HTML_LINE_SEPARATOR);
        s = s.substring(index + TEXT_LINE_SEPARATOR.length());
        index = s.indexOf(TEXT_LINE_SEPARATOR);
      }
      sb.append(s);
    }
    return sb.toString();
  }
}
