/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.contentManager;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;

public class SilverContent extends AbstractI18NBean implements SilverContentInterface {

  private static final long serialVersionUID = 1L;
  private String silverContentName;
  private String silverContentDescription;
  private String silverContentURL;

  public SilverContent(String name, String description, String url) {
    setName(name);
    setDescription(description);
    setURL(url);
  }

  public String getName() {
    return silverContentName;
  }

  public String getName(String language) {
    if (!I18NHelper.isI18N)
      return getName();

    SilverContentI18N p = (SilverContentI18N) getTranslations().get(language);
    if (p == null)
      p = (SilverContentI18N) getNextTranslation();

    return p.getName();
  }

  public String getDescription() {
    return silverContentDescription;
  }

  public String getDescription(String language) {
    if (!I18NHelper.isI18N)
      return getDescription();

    SilverContentI18N p = (SilverContentI18N) getTranslations().get(language);
    if (p == null)
      p = (SilverContentI18N) getNextTranslation();

    return p.getDescription();
  }

  public String getURL() {
    return silverContentURL;
  }

  public void setName(String name) {
    silverContentName = name;
  }

  public void setDescription(String description) {
    silverContentDescription = description;
  }

  public void setURL(String url) {
    silverContentURL = url;
  }

  public String getId() {
    return "unknown";
  }

  public String getInstanceId() {
    return "unknown";
  }

  public String getTitle() {
    return getName();
  }

  public String getDate() {
    return "unknown";
  }

  public String getSilverCreationDate() {
    return "unknown";
  }

  public String getIconUrl() {
    return "unknown";
  }

  public String getCreatorId() {
    return "unknown";
  }

  public String toString() {
    return ("silverContent contains Name = " + getName() + ", Description = "
        + getDescription() + ", Url = " + getURL());
  }

}