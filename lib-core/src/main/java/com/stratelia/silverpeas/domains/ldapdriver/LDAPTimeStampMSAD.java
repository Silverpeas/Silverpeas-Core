package com.stratelia.silverpeas.domains.ldapdriver;

import com.novell.ldap.LDAPEntry;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;

public class LDAPTimeStampMSAD extends AbstractLDAPTimeStamp
{
	long lTimeStamp; 
	
	public LDAPTimeStampMSAD(LDAPSettings ds, String theValue)
	{
		driverSettings = ds;
		timeStamp = theValue;
		lTimeStamp = Long.parseLong(theValue);
	}
    
	public String toString()
    {
		return timeStamp;
    }

    public int compareTo(Object other)
    {
    	if (lTimeStamp > ((LDAPTimeStampMSAD)other).lTimeStamp)
    		return 1;
    	else if (lTimeStamp < ((LDAPTimeStampMSAD)other).lTimeStamp)
    		return -1;
    	else
    		return 0;
    }

    public void initFromServer(String lds, String baseDN, String filter, String fallbackSortBy) throws AdminException
    { 
		SilverTrace.info("admin", "LDAPTimeStampMSAD.initFromServer()", "root.MSG_GEN_ENTER_METHOD");
        LDAPEntry[]    theEntries = LDAPUtility.search1000Plus(lds, baseDN, driverSettings.getScope(), "(&(" + driverSettings.getTimeStampVar() + ">=" + timeStamp + ")" + filter + ")", driverSettings.getTimeStampVar());
        if (theEntries.length > 0)
        {
            // Problem is : the search1000Plus function sorts normaly by descending order. BUT most LDAP server can't performs this type of order (like Active Directory)
            // So, it may be ordered in the oposite way....
            long firstVal = Long.parseLong(LDAPUtility.getFirstAttributeValue(theEntries[0],driverSettings.getTimeStampVar()));
            long lastVal = Long.parseLong(LDAPUtility.getFirstAttributeValue(theEntries[theEntries.length-1],driverSettings.getTimeStampVar()));
            if (firstVal >= lastVal)
            {
            	lTimeStamp = firstVal;
            }
            else
            {
            	lTimeStamp = lastVal;
            }
        	timeStamp = Long.toString(lTimeStamp); 
        }
    }
}
