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
package org.silverpeas.core.contribution.publication.model;

import java.util.Calendar;
import java.util.Date;

import org.silverpeas.core.util.DateUtil;

public class Visibility {

  private boolean notYetVisible = false;
  private boolean noMoreVisible = false;
  private Date beginDateAndHour = null;
  private Date endDateAndHour = null;

  public Visibility(Date beginDate, String beginHour, Date endDate, String endHour) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date today = calendar.getTime();

    Date dBegin = DateUtil.getDate(beginDate, beginHour);
    Date dEnd = DateUtil.getDate(endDate, endHour);

    setBeginDateAndHour(dBegin);
    setEndDateAndHour(dEnd);

    if (dBegin != null && dBegin.after(today)) {
      this.notYetVisible = true;
    } else if (dEnd != null && dEnd.before(today)) {
      this.noMoreVisible = true;
    }
  }

  public boolean isNoMoreVisible() {
    return noMoreVisible;
  }

  public boolean isNotYetVisible() {
    return notYetVisible;
  }

  public boolean isVisible() {
    return !(notYetVisible || noMoreVisible);
  }

  public Date getBeginDateAndHour() {
    if (beginDateAndHour != null) {
      return (Date) beginDateAndHour.clone();
    }
    return null;
  }

  public void setBeginDateAndHour(Date beginDateAndHour) {
    if (beginDateAndHour != null) {
      this.beginDateAndHour = (Date) beginDateAndHour.clone();
    } else {
      this.beginDateAndHour = null;
    }
  }

  public Date getEndDateAndHour() {
    if (endDateAndHour != null) {
      return (Date) endDateAndHour.clone();
    }
    return null;
  }

  public void setEndDateAndHour(Date endDateAndHour) {
    if (endDateAndHour != null) {
      this.endDateAndHour = (Date) endDateAndHour.clone();
    } else {
      this.endDateAndHour = null;
    }
  }

}
