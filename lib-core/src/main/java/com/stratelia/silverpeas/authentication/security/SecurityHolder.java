package com.stratelia.silverpeas.authentication.security;

import java.util.HashMap;


public class SecurityHolder
{
	
	private static SecurityCache securityCache = new SecurityCache();
	private static HashMap persistentCache = new HashMap();
	
	public static void addData(String securityId, String userId, String domainId)
	{
		addData(securityId, userId, domainId, false);
	}
	
	public static void addData(String securityId, String userId, String domainId,
		boolean persistent)
	{
		if (persistent)
		{
			persistentCache.put(securityId, new SecurityData(userId, domainId));
		}
		else
		{
			securityCache.addData(securityId, userId, domainId);
		}
	}
	
	public static SecurityData getData(String securityId)
	{
		SecurityData securityData = securityCache.getData(securityId);
		if (securityData == null)
		{
			securityData = getPersistentData(securityId);
		}
		return securityData;
	}
	
	private static SecurityData getPersistentData(String securityId)
	{
		return (SecurityData)persistentCache.get(securityId);
	}

}
