/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.web.jobsearch;

import org.silverpeas.core.util.DateUtil;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class SearchResult {

  private String name = null;
  private String desc = null;
  private LocalDate creaDate = null;
  private String creaName = null;
  private List<String> path = null;
  private String url = null;

  public SearchResult() {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public LocalDate getCreaDate() {
    return creaDate;
  }

  public void setCreaDate(Date creaDate) {
    this.creaDate = DateUtil.toLocalDate(creaDate);
  }

  public void setCreaDate(LocalDate creaDate) {
    this.creaDate = creaDate;
  }

  public String getCreaName() {
    return creaName;
  }

  public void setCreaName(String creaName) {
    this.creaName = creaName;
  }

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
