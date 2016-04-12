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
package org.silverpeas.core.web.tools.agenda.model;

public class CalendarImportSettings {

  public final static int TYPE_NO_IMPORT = 0;
  public final static int TYPE_OUTLOOK_IMPORT = 1;
  public final static int TYPE_NOTES_IMPORT = 2;
  public final static int DEFAULT_DELAY = 5;
  /**
   * Id of user whose settings belong to
   */
  private int userId = -1;
  /**
   * importation will only occur on this host
   */
  private String hostName = null;
  /**
   * Synchronisation type : None, Outlook, Notes
   */
  private int synchroType = TYPE_NO_IMPORT;
  /**
   * Delay in minutes between each synchronisation
   */
  private int synchroDelay = DEFAULT_DELAY;
  private String urlIcalendar = null;
  private String loginIcalendar = null;
  private String pwdIcalendar = null;
  private String charset = "ISO-8859-1";

  public boolean isSchynchro(String host) {
    return getHostName().equalsIgnoreCase(host);
  }

  public boolean isOutlookSynchro(String host) {
    return getHostName().equalsIgnoreCase(host) && CalendarImportSettings.TYPE_OUTLOOK_IMPORT
        == getSynchroType();
  }

  /**
   * @return Returns the hostName. Importation will only occur on this host
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * @param hostName The hostName to set. Importation will only occur on this host
   */
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /**
   * @return Returns the Delay in minutes between each synchronisation.
   */
  public int getSynchroDelay() {
    return synchroDelay;
  }

  /**
   * @param synchroDelay Delay in minutes between each synchronisation.
   */
  public void setSynchroDelay(int synchroDelay) {
    this.synchroDelay = synchroDelay;
  }

  /**
   * @return Returns the Synchronisation type : None, Outlook, Notes.
   */
  public int getSynchroType() {
    return synchroType;
  }

  /**
   * @param synchroType The Synchronisation type : None, Outlook, Notes.
   */
  public void setSynchroType(int synchroType) {
    this.synchroType = synchroType;
  }

  /**
   * @return Returns the Id of user whose settings belong to.
   */
  public int getUserId() {
    return userId;
  }

  /**
   * @param userId The Id of user whose settings belong to.
   */
  public void setUserId(int userId) {
    this.userId = userId;
  }

  /**
   * @return Returns the url to iCalendar
   */
  public String getUrlIcalendar() {
    return urlIcalendar;
  }

  /**
   * @param userId The Id of user whose settings belong to.
   */
  public void setUrlIcalendar(String url) {
    this.urlIcalendar = url;
  }

  /**
   * @return Returns the login to remote iCalendar
   */
  public String getLoginIcalendar() {
    return loginIcalendar;
  }

  /**
   * @param login The Pwd to remote iCalendar
   */
  public void setLoginIcalendar(String login) {
    this.loginIcalendar = login;
  }

  /**
   * @return Returns the login to remote iCalendar
   */
  public String getPwdIcalendar() {
    return pwdIcalendar;
  }

  /**
   * @param pwd The Pwd to remote iCalendar
   */
  public void setPwdIcalendar(String pwd) {
    this.pwdIcalendar = pwd;
  }

  /**
   * @return Returns charset of remote iCalendar
   */
  public String getCharset() {
    return charset;
  }

  /**
   * @param charset to remote iCalendar
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }
}
