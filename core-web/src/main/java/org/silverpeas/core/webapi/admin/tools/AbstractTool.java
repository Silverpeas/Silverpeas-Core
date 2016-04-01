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
package org.silverpeas.core.webapi.admin.tools;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractTool {

  private final String language;
  private final LookHelper lookHelper;
  private final String visibleKey;
  private final String id;
  private final String labelKey;
  private final String urlComponentName;
  private final String urlSuffix;

  protected AbstractTool(final String language, final LookHelper lookHelper,
      final String visibleKey, final String id, final String labelKey, final String urlComponentName) {
    this(language, lookHelper, visibleKey, id, labelKey, urlComponentName, "Main");
  }

  protected AbstractTool(final String language, final LookHelper lookHelper,
      final String visibleKey, final String id, final String labelKey,
      final String urlComponentName, final String urlSuffix) {
    this.language = language;
    this.lookHelper = lookHelper;
    this.visibleKey = visibleKey;
    this.id = id;
    this.labelKey = labelKey;
    this.urlComponentName = urlComponentName;
    this.urlSuffix = urlSuffix;
  }

  protected LocalizationBundle getMessages() {
    return ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle",
        language);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return getMessages().getString(labelKey);
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return URLUtil.getURL(urlComponentName, null, "") + urlSuffix;
  }

  /**
   * @return the nb
   */
  public int getNb() {
    return 0;
  }

  /**
   * @return the nb
   */
  public boolean isVisible() {
    return getLookHelper().getSettings(visibleKey, true);
  }

  /**
   * @return the lookHelper
   */
  protected LookHelper getLookHelper() {
    return lookHelper;
  }
}
