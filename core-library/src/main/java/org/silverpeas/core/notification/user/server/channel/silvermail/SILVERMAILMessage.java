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

package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.notification.user.server.channel.SilverpeasMessage;

import java.util.Date;

public class SILVERMAILMessage extends SilverpeasMessage {
  public SILVERMAILMessage() {
    super();
  }

  private String mUrl;

  public void setUrl(String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }

  private String mSource;

  public void setSource(String source) {
    mSource = source;
  }

  public String getSource() {
    return mSource;
  }

  private Date mDate;

  public void setDate(Date date) {
    mDate = date;
  }

  public Date getDate() {
    return mDate;
  }

  private int mReaden = 0;

  public void setReaden(int readen) {
    mReaden = readen;
  }

  public int getReaden() {
    return mReaden;
  }

}