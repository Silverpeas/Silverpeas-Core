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

package com.silverpeas.look;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;

public class TopItem {

  public static final int SPACE = 0;
  public static final int COMPONENT = 1;

  private String label;
  private String componentId;
  private String spaceId;
  private String subSpaceId;
  private String url;

  public TopItem() {

  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getSubSpaceId() {
    return subSpaceId;
  }

  public void setSubSpaceId(String subSpaceId) {
    this.subSpaceId = subSpaceId;
  }

  public boolean isComponent() {
    return StringUtil.isDefined(getComponentId());
  }

  public boolean isSpace() {
    return !isComponent() && StringUtil.isDefined(getSpaceId());
  }

  public String getUrl() {
    if (isSpace())
      return "/dt?SpaceId=" + getSubSpaceId();
    else if (isComponent())
      return URLManager.getURL(null, getComponentId()) + "Main";
    else {
      if (StringUtil.isDefined(url)) {
        return url;
      }
      return "#";
    }
  }

  public String getId() {
    if (isComponent())
      return getComponentId();
    else if (isSpace())
      return getSubSpaceId();
    else
      return "anotherId";
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
