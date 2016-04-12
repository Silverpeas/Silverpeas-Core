/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media.image.thumbnail;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import org.silverpeas.core.util.StringUtil;

public class ThumbnailSettings {

  protected static final int DEFAULT_SIZE = 150;
  protected static final String APP_PARAM_WIDTH = "thumbnailWidthSize";
  protected static final String APP_PARAM_HEIGHT = "thumbnailHeightSize";
  private static final String APP_PARAM_MANDATORY = "thumbnailMandatory";

  private boolean mandatory;
  private int width = -1;
  private int height = -1;

  public boolean isMandatory() {
    return mandatory;
  }
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }
  public int getWidth() {
    return width;
  }
  public void setWidth(int width) {
    this.width = width;
  }
  public int getHeight() {
    return height;
  }
  public void setHeight(int height) {
    this.height = height;
  }

  public static ThumbnailSettings getInstance(String componentId, int defaultWidth,
      int defaultHeight) {
    ThumbnailSettings settings = new ThumbnailSettings();
    settings.setMandatory(StringUtil.getBooleanValue(getComponentParameterValue(
        APP_PARAM_MANDATORY, componentId)));
    int width = getInt(getComponentParameterValue(APP_PARAM_WIDTH, componentId));
    int height = getInt(getComponentParameterValue(APP_PARAM_HEIGHT, componentId));

    if (width == -1 && height == -1) {
      // get global settings if undefined on instance level
      if (defaultWidth == -1 && defaultHeight == -1) {
        defaultWidth = defaultHeight = DEFAULT_SIZE;
      } else if (defaultWidth != -1 && defaultHeight == -1) {
        defaultHeight = defaultWidth;
      } else if (defaultWidth == -1 && defaultHeight != -1) {
        defaultWidth = defaultHeight;
      }
      width = defaultWidth;
      height = defaultHeight;
    } else if (width != -1 && height == -1) {
      height = width;
    } else if (width == -1 && height != -1) {
      width = height;
    }
    settings.setWidth(width);
    settings.setHeight(height);
    return settings;
  }

  private static String getComponentParameterValue(String parameterName, String componentId) {
    return OrganizationControllerProvider.getOrganisationController().getComponentParameterValue(
        componentId, parameterName);
  }

  private static int getInt(String str) {
    if (StringUtil.isInteger(str)) {
      return Integer.parseInt(str);
    }
    return -1;
  }

}