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

package com.silverpeas.external.filesharing.model;

import java.io.Serializable;
import java.util.Date;

public class DownloadDetail implements Serializable {

  private static final long serialVersionUID = -3552579238204831286L;
  private int id;
  private String keyFile;
  private Date downloadDate;
  private String userIP;

  public DownloadDetail() {

  }

  public DownloadDetail(int id, String keyFile, Date downloadDate, String userIP) {
    setId(id);
    setKeyFile(keyFile);
    setDownloadDate(downloadDate);
    setUserIP(userIP);
  }

  public DownloadDetail(String keyFile, Date downloadDate, String userIP) {
    setKeyFile(keyFile);
    setDownloadDate(downloadDate);
    setUserIP(userIP);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getKeyFile() {
    return keyFile;
  }

  public void setKeyFile(String keyFile) {
    this.keyFile = keyFile;
  }

  public Date getDownloadDate() {
    return downloadDate;
  }

  public void setDownloadDate(Date downloadDate) {
    this.downloadDate = downloadDate;
  }

  public String getUserIP() {
    return userIP;
  }

  public void setUserIP(String userIP) {
    this.userIP = userIP;
  }

}
