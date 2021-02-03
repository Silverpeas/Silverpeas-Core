/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.look;

import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author Nicolas Eysseric
 */
public class DefaultLayoutConfiguration implements LayoutConfiguration {

  private SettingBundle settings;
  private String headerURL;
  private String bodyURL;
  private String bodyNavigationURL;

  public DefaultLayoutConfiguration(SettingBundle settings) {
    this.settings = settings;
  }

  @Override
  public String getHeaderURL() {
    return defaultStringIfNotDefined(headerURL,
        settings.getString("layout.header.url", "/admin/jsp/silverpeas-header-part.jsp"));
  }

  @Override
  public String getBodyURL() {
    return defaultStringIfNotDefined(bodyURL,
        settings.getString("layout.body.url", "/admin/jsp/silverpeas-body-part.jsp"));
  }

  @Override
  public String getBodyNavigationURL() {
    return defaultStringIfNotDefined(bodyNavigationURL,
        settings.getString("layout.body.navigation.url", "/admin/jsp/silverpeas-navigation-part.jsp"));
  }

  public void setHeaderURL(String url) {
    headerURL = url;
  }

  public void setBodyURL(String url) {
    bodyURL = url;
  }

  public void setBodyNavigationURL(String url) {
    bodyNavigationURL = url;
  }
}
