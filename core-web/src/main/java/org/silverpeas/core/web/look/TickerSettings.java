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

import java.util.HashMap;
import java.util.Map;

public class TickerSettings {

  private String label = "";
  private Map<String, String> params = new HashMap<>();
  private boolean linkOnItem = false;
  private int refreshDelay = 60;
  private boolean displayDescription = false;
  private int displayLimit = 10;

  public TickerSettings(SettingBundle settings) {
    for (String key : settings.keySet()) {
      if (key.startsWith("ticker.plugin")) {
        String param = settings.getString(key, null);
        if (param != null) {
          params.put(key.substring(key.lastIndexOf('.')+1), param);
        }
      }
    }
    linkOnItem = settings.getBoolean("ticker.linkOnItem", false);
    refreshDelay = settings.getInteger("ticker.autocheck.delay", 60);
    displayDescription = settings.getBoolean("ticker.items.description", false);
    displayLimit = settings.getInteger("ticker.display.limit", 10);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public String getParam(String key, String defaultValue) {
    if (!StringUtil.isDefined(params.get(key))) {
      return defaultValue;
    }
    return params.get(key);
  }

  public boolean isLinkOnItem() {
    return linkOnItem;
  }

  public int getRefreshDelay() {
    return refreshDelay;
  }

  public int getDisplayLimit() {
    return displayLimit;
  }

  public boolean isDescriptionDisplayed() {
    return displayDescription;
  }

}