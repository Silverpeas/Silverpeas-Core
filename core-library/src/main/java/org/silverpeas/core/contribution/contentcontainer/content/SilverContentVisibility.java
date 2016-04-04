/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.util.DateUtil;

import java.util.Date;

public class SilverContentVisibility {
  private String beginDate = "0000/00/00";
  private String endDate = "9999/99/99";
  private boolean isVisible = true;

  public SilverContentVisibility(String beginDate, String endDate, boolean isVisible) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(isVisible);
  }

  public SilverContentVisibility(Date beginDate, Date endDate, boolean isVisible) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(isVisible);
  }

  public SilverContentVisibility(String beginDate, String endDate) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(true);
  }

  public SilverContentVisibility(Date beginDate, Date endDate) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(true);
  }

  public SilverContentVisibility(boolean isVisible) {
    setVisibilityAttributes(isVisible);
  }

  public SilverContentVisibility() {
  }

  public void setVisibilityAttributes(String beginDate, String endDate) {
    this.beginDate = beginDate;
    this.endDate = endDate;
  }

  public void setVisibilityAttributes(Date beginDate, Date endDate, boolean isVisible) {
    setVisibilityAttributes(beginDate, endDate);
    this.isVisible = isVisible;
  }

  public void setVisibilityAttributes(Date beginDate, Date endDate) {
    if (beginDate != null) {
      this.beginDate = DateUtil.date2SQLDate(beginDate);
    }

    if (endDate != null) {
      this.endDate = DateUtil.date2SQLDate(endDate);
    }
  }

  public void setVisibilityAttributes(boolean isVisible) {
    this.isVisible = isVisible;
  }

  public String getBeginDate() {
    return this.beginDate;
  }

  public String getEndDate() {
    return this.endDate;
  }

  public int isVisible() {
    if (this.isVisible) {
      return 1;
    } else {
      return 0;
    }
  }

}