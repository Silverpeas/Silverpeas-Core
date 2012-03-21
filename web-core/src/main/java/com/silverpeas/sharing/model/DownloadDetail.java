/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.sharing.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "sb_filesharing_history")
public class DownloadDetail implements Serializable {

  private static final long serialVersionUID = -3552579238204831286L;
  @Id
  @GeneratedValue(generator="UNIQUE_ID_GEN")
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
  valueColumnName = "maxId", pkColumnValue = "sb_filesharing_history", allocationSize = 1)
  private long id;
  @ManyToOne
  @JoinColumn(name = "keyFile", columnDefinition="varchar(255)")
  private Ticket ticket;
  @Column(name = "downloaddate", nullable=false)
  private Long downloadDate;  
  @Column(name = "downloadIp")
  private String userIP;

  public DownloadDetail() {
  }

  public DownloadDetail(Ticket ticket, Date downloadDate, String userIP) {
    this.ticket = ticket;
    this.downloadDate = downloadDate.getTime();
    this.userIP = userIP;
  }

  public long getId() {
    return id;
  }

  public String getKeyFile() {
    return this.ticket.getToken();
  }

  public void setKeyFile(Ticket ticket) {
    this.ticket = ticket;
  }

  public Date getDownloadDate() {
    return new Date(downloadDate);
  }

  public void setDownloadDate(Date downloadDate) {
    this.downloadDate = downloadDate.getTime();
  }

  public String getUserIP() {
    return userIP;
  }

  public void setUserIP(String userIP) {
    this.userIP = userIP;
  }
}
