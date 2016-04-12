/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.token;

import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

import java.util.Arrays;
import java.util.List;

/**
 * A template from which a Javascript script is generated for setting the synchronizer tokens in the
 * context of a web page.
 *
 * @author mmoquillon
 */
public class TokenSettingTemplate {

  /**
   * Silverpeas template from which is generated the Javascript script for setting the session token
   * for each form in a web page.
   */
  private static final String TEMPLATE_NAME = "tokenSetting_js";
  private static final String TEMPLATE_PATH = "token";
  /**
   * The name of the parameter that set the name of the creation menu item container id.
   */
  public static final String CREATION_MENU_CONTAINER_ID = "CREATION_MENU_CONTAINER_ID";
  /**
   * The name of the parameter that set the name of the session token in the HTTP requests.
   */
  public static final String SESSION_TOKEN_NAME_PARAMETER = "SESSION_TOKEN_NAME";
  /**
   * The name of the parameter that set the value of the session token in the HTTP requests.
   */
  public static final String SESSION_TOKEN_VALUE_PARAMETER = "SESSION_TOKEN_VALUE";
  /**
   * The name of the parameter that set the name of the navigation token in the HTTP requests.
   */
  public static final String NAVIGATION_TOKEN_NAME_PARAMETER = "NAV_TOKEN_NAME";
  /**
   * The name of the parameter that set the value of the navigation token in the HTTP requests.
   */
  public static final String NAVIGATION_TOKEN_VALUE_PARAMETER = "NAV_TOKEN_VALUE";
  /**
   * The name of the parameter that set the expiration timestamp of the cookies in which is stored
   * the token.
   */
  public static final String EXPIRATION_TIMESTAMP_PARAMETER = "EXPIRATION_TIME";
  /**
   * The name of the parameter that set the cookie in which is stored the token as secured.
   */
  public static final String SECURED_COOKIE_PARAMETER = "SECURED";

  public String apply(Parameter... parameters) {
    return apply(Arrays.asList(parameters));
  }

  public String apply(List<Parameter> parameters) {
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(
        TokenSettingTemplate.TEMPLATE_PATH);
    for (Parameter parameter : parameters) {
      template.setAttribute(parameter.name(), parameter.value());
    }
    return template.applyFileTemplate(TEMPLATE_NAME);
  }

  public static class Parameter {

    private final String name;
    private final String value;

    public Parameter(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String name() {
      return name;
    }

    public String value() {
      return value;
    }
  }
}
