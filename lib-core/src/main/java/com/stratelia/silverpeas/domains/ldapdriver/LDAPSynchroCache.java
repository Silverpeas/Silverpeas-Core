package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.Hashtable;

import com.novell.ldap.LDAPEntry;

public class LDAPSynchroCache {
  protected Hashtable userSpecificIds = null;
  protected LDAPSettings driverSettings = null;

  public void init(LDAPSettings driverSettings) {
    this.driverSettings = driverSettings;
  }

  public void beginSynchronization() {
    if (driverSettings.isSynchroCacheEnabled()) {
      userSpecificIds = new Hashtable();
    }
    // Else keep it to null
  }

  public void endSynchronization() {
    if (userSpecificIds != null) {
      userSpecificIds.clear();
      userSpecificIds = null;
    }
  }

  public void addUser(LDAPEntry userEntry) {
    if (userSpecificIds != null) {
      userSpecificIds.put(userEntry.getDN(), LDAPUtility
          .getFirstAttributeValue(userEntry, driverSettings.getUsersIdField()));
    }
  }

  public void addUser(String theDN, String theId) {
    if (userSpecificIds != null) {
      userSpecificIds.put(theDN, theId);
    }
  }

  public String getUserId(String theDN) {
    if (userSpecificIds != null) {
      return (String) (userSpecificIds.get(theDN));
    } else {
      return null;
    }
  }
}