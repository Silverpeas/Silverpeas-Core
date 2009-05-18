package com.stratelia.silverpeas.domains.silverpeasdriver;

import java.io.Serializable;

import com.stratelia.webactiv.beans.admin.AbstractDomainDriver;
import com.stratelia.webactiv.beans.admin.UserFull;

public class SPUserFull extends UserFull implements Serializable
{
    /** Creates new UserFull */
    public SPUserFull() {  super();  setPasswordAvailable(true); }
    public SPUserFull(AbstractDomainDriver domainDriver)  {  super(domainDriver); setPasswordAvailable(true); }

    public void setTitle(String sTitle) {  setValue("title", sTitle); }
    public String getTitle() {  return getValue("title");  }

    public void setCompany(String sCompany) { setValue("company", sCompany);  }
    public String getCompany() {  return getValue("company");  }

    public void setPosition(String sPosition) {  setValue("position",sPosition);  }
    public String getPosition() {  return getValue("position");  }

    public void setBossId(String sBossId) {  setValue("boss",sBossId); }
    public String getBossId() {  return getValue("boss");  }

    public void setTelephone(String sTelephone) {  setValue("phone",sTelephone); }
    public String getTelephone() {  return getValue("phone");  }

    public void setHomePhone(String sHomePhone) {  setValue("homePhone",sHomePhone); }
    public String getHomePhone() {  return getValue("homePhone");  }

    public void setFax(String sFax) {  setValue("fax",sFax); }
    public String getFax() {  return getValue("fax");  }

    public void setCellularPhone(String sCellularPhone) {  setValue("cellularPhone",sCellularPhone); }
    public String getCellularPhone() {  return getValue("cellularPhone");  }

    public void setAddress(String sAddress) {  setValue("address",sAddress); }
    public String getAddress() {  return getValue("address");  }
}