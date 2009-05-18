package com.stratelia.silverpeas.domains.ldapdriver;

import com.stratelia.webactiv.beans.admin.AdminException;

abstract public class AbstractLDAPTimeStamp extends Object implements Comparable
{
	LDAPSettings driverSettings = null;
	String timeStamp; 
	
    abstract public String toString();
    abstract public int compareTo(Object other);
    abstract public void initFromServer(String lds, String baseDN, String filter, String fallbackSortBy) throws AdminException;
}
