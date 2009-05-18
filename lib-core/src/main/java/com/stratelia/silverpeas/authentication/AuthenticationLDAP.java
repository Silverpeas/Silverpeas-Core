/*
 * AuthenticationLDAP.java
 *
 * Created on 6 aout 2001
 */
 
package com.stratelia.silverpeas.authentication;

import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/** 
 * This class performs the LDAP authentification 
 * @author  tleroi
 * @version 
 */
public class AuthenticationLDAP extends Authentication
{
	private final static int 	INTERVALS_PER_MILLISECOND= 1000000/100; 
	private final static long 	MILLISECONDS_BETWEEN_1601_AND_1970= Long.parseLong("11644473600000");
	private final static String BASEDN_SEPARATOR = ";;";
	
	protected boolean m_IsSecured = false;
    protected boolean m_MustAlertPasswordExpiration = false;
    protected String  m_PwdLastSetFieldName;
    protected int  	  m_PwdMaxAge;
    protected int     m_PwdExpirationReminderDelay;
	protected String  m_Host;
	protected int     m_Port;
	protected String  m_AccessLogin;
	protected String  m_AccessPasswd;
    protected String  m_UserBaseDN;
    protected String  m_UserLoginFieldName;
    protected LDAPConnection    m_LDAPConnection = null;

    public void init(String authenticationServerName, ResourceLocator propFile)
    {
        // Lecture du fichier de proprietes
        m_IsSecured = getBooleanProperty(propFile, authenticationServerName + ".LDAPSecured", false);
        m_Host = propFile.getString(authenticationServerName + ".LDAPHost");
        if (m_IsSecured)
        {
            m_Port = Integer.parseInt(propFile.getString(authenticationServerName + ".LDAPSecuredPort"));
        }
        else
        {
            m_Port = Integer.parseInt(propFile.getString(authenticationServerName + ".LDAPPort"));
        }
        m_AccessLogin = propFile.getString(authenticationServerName + ".LDAPAccessLogin");
        m_AccessPasswd = propFile.getString(authenticationServerName + ".LDAPAccessPasswd");
        m_UserBaseDN = propFile.getString(authenticationServerName + ".LDAPUserBaseDN");
        m_UserLoginFieldName = propFile.getString(authenticationServerName + ".LDAPUserLoginFieldName");

        // get parameters about user alert if password is about to expire
        m_MustAlertPasswordExpiration = getBooleanProperty(propFile, authenticationServerName + ".MustAlertPasswordExpiration", false);
        if ( m_MustAlertPasswordExpiration )
        {
        	String propValue = null;
        	
        	m_PwdLastSetFieldName = propFile.getString(authenticationServerName + ".LDAPPwdLastSetFieldName");
 
        	propValue = propFile.getString(authenticationServerName + ".LDAPPwdMaxAge");
        	m_PwdMaxAge = (propValue == null) ? Integer.MAX_VALUE : Integer.parseInt(propValue);
        	
        	propValue = propFile.getString(authenticationServerName + ".PwdExpirationReminderDelay");
        	m_PwdExpirationReminderDelay = (propValue == null) ? 5 : Integer.parseInt(propValue);
        	
        	if (m_PwdLastSetFieldName == null)
        		m_MustAlertPasswordExpiration = false;
        }
        
        SilverTrace.info("authentication","AuthenticationLDAP.internalAuthentication()","root.MSG_GEN_PARAM_VALUE","javax.net.ssl.trustStore = " + System.getProperty("javax.net.ssl.trustStore"));
    }

    protected void openConnection() throws AuthenticationException
    {
		boolean doConnect = false;

        // Connect to server
        if (m_LDAPConnection == null)
		{
            if (m_IsSecured)
            {
                m_LDAPConnection = new LDAPConnection(new LDAPJSSESecureSocketFactory());
            }
            else
            {
			    m_LDAPConnection = new LDAPConnection();
            }
			doConnect = true;
		}
		else if (m_LDAPConnection.isConnected() == false)
		{
			doConnect = true;
		}
		if (doConnect)
		{
            try
            {
                m_LDAPConnection.connect(m_Host,m_Port);
            }
            catch(LDAPException ex)
            {
                throw new AuthenticationHostException("AuthenticationLDAP.openConnection()",SilverpeasException.ERROR,"root.EX_CONNECTION_OPEN_FAILED","Host=" + m_Host + ";Port=" + Integer.toString(m_Port),ex);
            }
		}
    }

    protected void closeConnection() throws AuthenticationException
    {
        // disconnect from the  server
        try
        {
            if ((m_LDAPConnection != null) && m_LDAPConnection.isConnected())
            {
                m_LDAPConnection.disconnect();
            }
            m_LDAPConnection = null;
        }
        catch(Exception ex)
        {
            throw new AuthenticationHostException("AuthenticationLDAP.closeConnection()",SilverpeasException.ERROR,"root.EX_CONNECTION_CLOSE_FAILED","Host=" + m_Host + ";Port=" + Integer.toString(m_Port),ex);
        }
    }

    protected void internalAuthentication(String login,String passwd) throws AuthenticationException
    {
        LDAPEntry           fe = null;
        LDAPSearchResults   res = null;
        String              userFullDN = null;
        String              searchString = m_UserLoginFieldName + "=" + login;
        String[]            attrNames;
        int					nbDaysBeforeExpiration = 0;
        
        // retrieve or not password last set date 
        if (m_MustAlertPasswordExpiration)
        	attrNames = new String[] {"uid", m_PwdLastSetFieldName};
        else
        	attrNames = new String[] {"uid"};
        
        try {
			// Bind as the admin for the search
			m_LDAPConnection.bind(m_AccessLogin, m_AccessPasswd);
		} catch (LDAPException e) {
			throw new AuthenticationHostException("AuthenticationLDAP.internalAuthentication()", SilverpeasException.ERROR, "authentication.EX_LDAP_ACCESS_ERROR", e);
		}

        String[] baseDNs = extractBaseDNs(m_UserBaseDN);
        for (int b=0; b<baseDNs.length; b++)
        {
	        try
	        {
	            SilverTrace.info("authentication","AuthenticationLDAP.internalAuthentication()","root.MSG_GEN_PARAM_VALUE","UserFilter="+searchString+", baseDN = "+baseDNs[b]);
	            res = m_LDAPConnection.search(baseDNs[b], LDAPConnection.SCOPE_SUB, searchString, attrNames, false);
	            if (!res.hasMore())
	            {
	                //throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.ERROR,"authentication.EX_USER_NOT_FOUND","User=" + login + ";LoginField=" + m_UserLoginFieldName);
	            }
	            if (res.hasMore())
	            {
		            fe = res.next();
		            if (fe != null)
		            {
			            userFullDN = (String)fe.getDN();
			            
			            SilverTrace.debug("authentication","AuthenticationLDAP.internalAuthentication()","root.MSG_GEN_PARAM_VALUE","m_MustAlertPasswordExpiration=" + m_MustAlertPasswordExpiration);
			            if (m_MustAlertPasswordExpiration)
			            	nbDaysBeforeExpiration = calculateDaysBeforeExpiration(fe);
		            }
	            }
	        }
	        catch(LDAPException ex)
	        {
	            throw new AuthenticationHostException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.ERROR,"authentication.EX_LDAP_ACCESS_ERROR",ex);
	        }
        }
        if (userFullDN == null)
        {
        	throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.ERROR,"authentication.EX_USER_NOT_FOUND","User=" + login + ";LoginField=" + m_UserLoginFieldName);
        }
        
        if (passwd == null || passwd.length() <= 0)
        {
            throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.ERROR,"authentication.EX_PWD_EMPTY","User=" + login);
        }
        try
        {
            SilverTrace.info("authentication","AuthenticationLDAP.internalAuthentication()","authentication.MSG_TRY_TO_AUTHENTICATE_USER","UserDN=" + userFullDN);
            m_LDAPConnection.bind(userFullDN,passwd);
            SilverTrace.info("authentication","AuthenticationLDAP.internalAuthentication()","authentication.MSG_USER_AUTHENTIFIED","User=" + login);
        }
        catch(LDAPException ex)
        {
            throw new AuthenticationBadCredentialException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.ERROR,"authentication.EX_AUTHENTICATION_BAD_CREDENTIAL","User=" + login,ex);
        }
        
        if ( m_MustAlertPasswordExpiration && (nbDaysBeforeExpiration < m_PwdExpirationReminderDelay) )
        {
        	throw new AuthenticationPasswordAboutToExpireException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.WARNING,"authentication.EX_AUTHENTICATION_PASSWORD_ABOUT_TO_EXPIRE","User=" + login);
        }
    }
    
    /**
     * Given an user ldap entry, compute the numbers of days before password expiration
     * @param fe	the user ldap entry
     * 
     * @return duration in days
     */
    private int calculateDaysBeforeExpiration(LDAPEntry fe) 
    {
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_ENTER_METHOD");
    	LDAPAttribute pwdLastSetAttr = fe.getAttribute(m_PwdLastSetFieldName);

    	// if password last set attribute is not found, return max value : user won't be notified
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_PARAM_VALUE","pwdLastSetAttr is null ? " + (pwdLastSetAttr == null));
    	if (pwdLastSetAttr == null)
    		return Integer.MAX_VALUE;
    	
    	// convert ldap value
    	long lastSetValue = Long.parseLong( pwdLastSetAttr.getStringValue() );
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_PARAM_VALUE","lastSetValue = " + lastSetValue);
    	lastSetValue = lastSetValue / INTERVALS_PER_MILLISECOND ;
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_PARAM_VALUE","lastSetValue = " + lastSetValue);
    	lastSetValue -= MILLISECONDS_BETWEEN_1601_AND_1970;
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_PARAM_VALUE","lastSetValue = " + lastSetValue);
    	
    	Date pwdLastSet = new Date(lastSetValue);
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_PARAM_VALUE","pwdLastSet = " + DateUtil.getOutputDateAndHour(pwdLastSet, "fr"));
    	Date now = new Date();
    	
    	long delayInMilliseconds = pwdLastSet.getTime() - now.getTime();
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_PARAM_VALUE","delayInMilliseconds = " + delayInMilliseconds);
    	int delayInDays = Math.round( (float) ( (delayInMilliseconds / (1000 * 3600 * 24 )) + m_PwdMaxAge ) );
    	
    	SilverTrace.debug("authentication","AuthenticationLDAP.calculateDaysBeforeExpiration()","root.MSG_GEN_EXIT_METHOD", "delayInDays = "+delayInDays);
    	
		return delayInDays;
	}

	/**
     * Overrides Authentication.internalChangePassword to offer password update capabilities
     * 
     * @param login							user login
     * @param oldPassword					user old password
     * @param newPassword					user new password
     * 
     * @throws AuthenticationException		
     */
    protected void internalChangePassword(String login, String oldPassword, String newPassword) throws AuthenticationException
     {
    	// Connection must be secure, checking it...
    	if (!m_IsSecured) 
    	{
    		Exception e = new UnsupportedOperationException("LDAP connection must be secured to allow password update");
    		throw new AuthenticationException("AuthenticationLDAP.changePassword", SilverpeasException.ERROR, "authentication.EX_CANT_CHANGE_USERPASSWORD", e);
    	}
    	
        LDAPEntry           fe = null;
        LDAPSearchResults   res = null;
        String              userFullDN = null;
        String              searchString = m_UserLoginFieldName + "=" + login;
        String[] strAttributes = { "sAMAccountName", "memberOf" };

        try
        {
            // Bind as the admin for the search
            m_LDAPConnection.bind(m_AccessLogin,m_AccessPasswd);

            // Get user DN
            SilverTrace.info("authentication","AuthenticationLDAP.changePassword()","root.MSG_GEN_PARAM_VALUE","UserFilter=" + searchString);
            res = m_LDAPConnection.search(m_UserBaseDN, LDAPConnection.SCOPE_SUB, searchString, strAttributes, false);
            if (!res.hasMore())
            {
                throw new AuthenticationBadCredentialException("AuthenticationLDAP.changePassword()",SilverpeasException.ERROR,"authentication.EX_USER_NOT_FOUND","User=" + login + ";LoginField=" + m_UserLoginFieldName);
            }
            fe = res.next();
            userFullDN = (String)fe.getDN();
            
            //re bind with the requested user
            m_LDAPConnection.bind(userFullDN, oldPassword);
           
			// Convert password to UTF-16LE
			String newQuotedPassword = "\"" + newPassword + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
			String oldQuotedPassword = "\"" + oldPassword + "\"";
			byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");

            //prepare password change
			LDAPModification[] mods = new LDAPModification[2];
			mods[0] = new LDAPModification(LDAPModification.DELETE,	new LDAPAttribute("unicodePwd", oldUnicodePassword));
			mods[1] = new LDAPModification(LDAPModification.ADD, new LDAPAttribute("unicodePwd", newUnicodePassword));

			// Perform the update
			m_LDAPConnection.modify(userFullDN, mods);
        }
        catch(Exception ex)
        {
            throw new AuthenticationHostException("AuthenticationLDAP.internalAuthentication()",SilverpeasException.ERROR,"authentication.EX_LDAP_ACCESS_ERROR",ex);
        }
    }
    
    static String[] extractBaseDNs(String baseDN)
    {
    	// if no separator, return a array with only the baseDN
		if (baseDN.indexOf(BASEDN_SEPARATOR)==-1)
		{
			String[] baseDNs = new String[1];
			baseDNs[0] = baseDN;
			return baseDNs;
		}    	
		
		StringTokenizer st = new StringTokenizer(baseDN, BASEDN_SEPARATOR);
		Vector baseDNs = new Vector();
		while (st.hasMoreTokens())
		{
			baseDNs.add(st.nextToken());
		}
		return (String []) (baseDNs.toArray(new String[0]));
    }
    
}
