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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import com.stratelia.silverpeas.notificationserver.channel.SilverpeasMessage;
import java.util.Date;

public class SILVERMAILMessage extends SilverpeasMessage {
  public SILVERMAILMessage() {
    super();
  }

  private String m_Url;

  public void setUrl(String url) {
    m_Url = url;
  }

  public String getUrl() {
    return m_Url;
  }

  private String m_Source;

  public void setSource(String source) {
    m_Source = source;
  }

  public String getSource() {
    return m_Source;
  }

  private Date m_Date;

  public void setDate(Date date) {
    m_Date = date;
  }

  public Date getDate() {
    return m_Date;
  }

  private int m_readen = 0;

  public void setReaden(int readen) {
    m_readen = readen;
  }

  public int getReaden() {
    return m_readen;
  }

}