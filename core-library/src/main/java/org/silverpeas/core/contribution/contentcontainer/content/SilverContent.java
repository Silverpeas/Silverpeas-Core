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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.i18n.AbstractI18NBean;

public class SilverContent extends AbstractI18NBean<SilverContentI18N>
    implements SilverContentInterface {

  private static final long serialVersionUID = 1L;
  private String silverContentURL;

  @Override
  protected Class<SilverContentI18N> getTranslationType() {
    return SilverContentI18N.class;
  }

  public SilverContent(String name, String description, String url) {
    setName(name);
    setDescription(description);
    setURL(url);
  }

  @Override
  public String getURL() {
    return silverContentURL;
  }

  public void setURL(String url) {
    silverContentURL = url;
  }

  @Override
  public String getId() {
    return "unknown";
  }

  @Override
  public String getInstanceId() {
    return "unknown";
  }

  @Override
  public String getTitle() {
    return getName();
  }

  @Override
  public String getDate() {
    return "unknown";
  }

  @Override
  public String getSilverCreationDate() {
    return "unknown";
  }

  @Override
  public String getIconUrl() {
    return "unknown";
  }

  @Override
  public String getCreatorId() {
    return "unknown";
  }

  public String toString() {
    return ("silverContent contains Name = " + getName() + ", Description = " + getDescription() +
        ", Url = " + getURL());
  }

}