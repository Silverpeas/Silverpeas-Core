/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.domains.silverpeasdriver;

import java.io.Serializable;

import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.UserFull;

public class SPUserFull extends UserFull implements Serializable {
  /** Creates new UserFull */
  public SPUserFull() {
    super();
    setPasswordAvailable(true);
  }

  public SPUserFull(AbstractDomainDriver domainDriver) {
    super(domainDriver);
    setPasswordAvailable(true);
  }

  public void setTitle(String sTitle) {
    setValue("title", sTitle);
  }

  public String getTitle() {
    return getValue("title");
  }

  public void setCompany(String sCompany) {
    setValue("company", sCompany);
  }

  public String getCompany() {
    return getValue("company");
  }

  public void setPosition(String sPosition) {
    setValue("position", sPosition);
  }

  public String getPosition() {
    return getValue("position");
  }

  public void setBossId(String sBossId) {
    setValue("boss", sBossId);
  }

  public String getBossId() {
    return getValue("boss");
  }

  public void setTelephone(String sTelephone) {
    setValue("phone", sTelephone);
  }

  public String getTelephone() {
    return getValue("phone");
  }

  public void setHomePhone(String sHomePhone) {
    setValue("homePhone", sHomePhone);
  }

  public String getHomePhone() {
    return getValue("homePhone");
  }

  public void setFax(String sFax) {
    setValue("fax", sFax);
  }

  public String getFax() {
    return getValue("fax");
  }

  public void setCellularPhone(String sCellularPhone) {
    setValue("cellularPhone", sCellularPhone);
  }

  public String getCellularPhone() {
    return getValue("cellularPhone");
  }

  public void setAddress(String sAddress) {
    setValue("address", sAddress);
  }

  public String getAddress() {
    return getValue("address");
  }
}