package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;

public class LDAPTimeStampNDS extends AbstractLDAPTimeStamp {
  public LDAPTimeStampNDS(LDAPSettings ds, String theValue) {
    driverSettings = ds;
    timeStamp = theValue;
  }

  public String toString() {
    return timeStamp;
  }

  public int compareTo(Object other) {
    return timeStamp.compareTo(((LDAPTimeStampNDS) other).timeStamp);
  }

  public void initFromServer(String lds, String baseDN, String filter,
      String fallbackSortBy) throws AdminException {
    SilverTrace.info("admin", "LDAPTimeStampNDS.initFromServer()",
        "root.MSG_GEN_ENTER_METHOD");
    String[] ttv = { driverSettings.getTimeStampVar(), fallbackSortBy };

    LDAPEntry[] theEntries = LDAPUtility.search1000Plus(lds, baseDN,
        driverSettings.getScope(), "(&(" + driverSettings.getTimeStampVar()
            + ">=" + timeStamp + ")" + filter + ")", fallbackSortBy, ttv);
    SilverTrace.info("admin", "LDAPTimeStampNDS.initFromServer()",
        "root.MSG_GEN_PARAM_VALUE", "# entries = " + theEntries.length);

    String ttCurrent = "";
    for (int i = 0; i < theEntries.length; i++) {
      ttCurrent = LDAPUtility.getFirstAttributeValue(theEntries[i],
          driverSettings.getTimeStampVar());
      if (ttCurrent.compareTo(timeStamp) > 0)
        timeStamp = ttCurrent;
    }
  }
}
