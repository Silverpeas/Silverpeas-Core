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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.silverpeas.domains.ldapdriver;

import com.google.common.base.Charsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPReferralException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.controls.LDAPSortControl;
import com.novell.ldap.controls.LDAPSortKey;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.SynchroReport;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.nio.charset.Charset;

/**
 * This class contains some usefull static functions to access to LDAP elements
 * @author tleroi
 */
public class LDAPUtility {

  public final static int MAX_NB_RETRY_CONNECT = 10;
  public final static int MAX_NB_RETRY_TIMELIMIT = 5;
  public final static String BASEDN_SEPARATOR = ";;";

  final static Map<String, LDAPConnectInfos> connectInfos = new HashMap<String, LDAPConnectInfos>();
  static int connexionsLastId = 0;

  /**
   * Method declaration
   * @param driverSettings
   * @return
   * @throws AdminException
   * @see
   */
  static public String openConnection(LDAPSettings driverSettings)
      throws AdminException {
    String newId = null;

    synchronized (connectInfos) {
      newId = Integer.toString(connexionsLastId);
      connexionsLastId = connexionsLastId + 1;
      if (connexionsLastId > 1000000) {
        connexionsLastId = 0;
      }
    }
    connectInfos.put(newId, new LDAPConnectInfos(driverSettings));
    InternalOpenConnection(newId);
    return newId;
  }

  /**
   * Method declaration
   * @param connectionId
   * @return
   * @throws AdminException
   * @see
   */
  static public LDAPConnection getConnection(String connectionId)
      throws AdminException {
    return (connectInfos.get(connectionId)).connection;
  }

  /**
   * Method declaration
   * @param connectionId
   * @param ex
   * @return
   * @throws AdminException
   * @see
   */
  static public boolean recoverConnection(String connectionId, LDAPException ex)
      throws AdminException {
    int nbRetry = 0;
    boolean reOpened = false;

    if (ex.getResultCode() == LDAPException.CONNECT_ERROR) {
      if ((connectInfos.get(connectionId)).incErrorCpt()) {
        SilverTrace.warn("admin", "LDAPUtility.recoverConnection",
            "admin.EX_ERR_LDAP_CONNECTION_LOST",
            "ConnectionId=" + connectionId, ex);
        try {
          InternalCloseConnection(connectionId);
        } catch (AdminException e) {
          SilverTrace.warn("admin", "LDAPUtility.recoverConnection",
              "admin.EX_ERR_LDAP_GENERAL", e);
        }
        while ((reOpened == false) && (nbRetry < MAX_NB_RETRY_CONNECT)) {
          try {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            InternalOpenConnection(connectionId);
            reOpened = true;
          } catch (AdminException e) {
            nbRetry++;
            SilverTrace.warn("admin", "LDAPUtility.recoverConnection",
                "admin.EX_ERR_LDAP_GENERAL", "Retry#="
                + Integer.toString(nbRetry), e);
          }
        }
      }
    }
    return reOpened;
  }

  /**
   * Method declaration
   * @param connectionId
   * @throws AdminException
   * @see
   */
  static public void closeConnection(String connectionId) throws AdminException {
    InternalCloseConnection(connectionId);
    connectInfos.remove(connectionId);
  }

  /**
   * Method declaration
   * @param connectionId
   * @throws AdminException
   * @see
   */
  static private void InternalOpenConnection(String connectionId)
      throws AdminException {
    LDAPConnection valret = null;
    LDAPSettings driverSettings = (connectInfos.get(connectionId)).driverSettings;

    try {
      if (driverSettings.isLDAPSecured()) {
        valret = new LDAPConnection(new LDAPJSSESecureSocketFactory());
      } else {
        valret = new LDAPConnection();
      }
      valret.connect(driverSettings.getLDAPHost(), driverSettings.getLDAPPort());
      valret.bind(driverSettings.getLDAPProtocolVer(), driverSettings.getLDAPAccessLoginDN(),
          driverSettings.getLDAPAccessPasswd().getBytes(Charsets.UTF_8));
      valret.setConstraints(driverSettings.getSearchConstraints(false));
      (connectInfos.get(connectionId)).connection = valret;
    } catch (LDAPException e) {
      try {
        if (valret != null) {
          valret.disconnect();
        }
      } catch (LDAPException ee) {
        SilverTrace.error("admin", "LDAPUtility.openConnection", "admin.EX_ERR_LDAP_GENERAL",
            "ERROR CLOSING CONNECTION : ConnectionId=" + connectionId
            + " Error LDAP #" + Integer.toString(e.getResultCode()) + " "
            + e.getLDAPErrorMessage(), ee);
      }
      throw new AdminException("LDAPUtility.openConnection",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GENERAL", "Host : "
          + driverSettings.getLDAPHost() + " Port : "
          + Integer.toString(driverSettings.getLDAPPort())
          + " LDAPLogin : " + driverSettings.getLDAPAccessLoginDN()
          + " ProtocolVer : "
          + Integer.toString(driverSettings.getLDAPProtocolVer()), e);
    }
  }

  /**
   * Method declaration
   * @param connectionId
   * @throws AdminException
   * @see
   */
  static private void InternalCloseConnection(String connectionId)
      throws AdminException {
    LDAPConnection toClose = getConnection(connectionId);

    if ((toClose != null) && (toClose.isConnected())) {
      try {
        toClose.disconnect();
      } catch (LDAPException e) {
        throw new AdminException("LDAPUtility.closeConnection",
            SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GENERAL",
            "ConnectionId=" + connectionId + " Error LDAP #"
            + Integer.toString(e.getResultCode()) + " "
            + e.getLDAPErrorMessage(), e);
      }
    }
  }

  /**
   * Returns the first value of a specific attribute from an entry. If this attribute have multiple
   * values, only the first is returned
   * @param theEntry the LDAP entry
   * @param attributeName the name of the attribute to retreive
   * @return the first value as a string
   */
  static public String getFirstAttributeValue(LDAPEntry theEntry,
      String attributeName) {
    String[] stringVals = null;

    stringVals = getAttributeValues(theEntry, attributeName);
    if (stringVals.length > 0) {
      return stringVals[0];
    } else {
      return "";
    }
  }

  /**
   * Search and returns the first Entry that match the parameters baseDN, scope and filter
   * @param lds the LDAP connection name
   * @param baseDN the base DN for the search
   * @param scope the scope (LDAPConnection.SCOPE_BASE, LDAPConnection.SCOPE_ONE or
   * LDAPConnection.SCOPE_SUB)
   * @param filter the filter for the search (if null, use '(objectClass=*)' )
   * @param attrs hidden attributes
   * @return the first entry found
   * @throws AdminException if a problem occur
   */
  static public LDAPEntry getFirstEntryFromSearch(String lds, String baseDN,
      int scope, String filter, String[] attrs) throws AdminException {
    LDAPConnection ld = getConnection(lds);
    LDAPSearchResults res = null;
    LDAPEntry theEntry = null;
    String sureFilter;
    LDAPSearchConstraints sc = ld.getSearchConstraints();

    try {
      if ((filter == null) || (filter.length() == 0)) {
        sureFilter = "(objectClass=*)";
      } else {
        sureFilter = filter;
      }
      // Return only one entry
      sc.setBatchSize(1);
      sc.setMaxResults(1);
      SilverTrace.debug("admin", "LDAPUtility.getFirstEntryFromSearch()",
          "LDAP query", "BaseDN=" + baseDN + " scope="
          + Integer.toString(scope) + " Filter=" + sureFilter);
      // SynchroReport.debug("LDAPUtility.getFirstEntryFromSearch()",
      // "Requête LDAP : BaseDN="+baseDN+" scope="+Integer.toString(scope)+" Filter="+sureFilter,null);
      // Modif LBE : as more than on baseDN can be set, iterate on all baseDNs
      // and stop when first entry is found
      String[] baseDNs = extractBaseDNs(baseDN);
      for (int i = 0; i < baseDNs.length; i++) {
        res = ld.search(baseDNs[i], scope, sureFilter, attrs, false, sc);
        if (res.hasMore()) {
          theEntry = res.next();
          SilverTrace.debug("admin", "LDAPUtility.getFirstEntryFromSearch()",
              "Entry Founded : ", theEntry.getDN());
          break;
        }
      }
    } catch (LDAPReferralException re) {
      throw new AdminException("LDAPUtility.getFirstEntryFromSearch",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_REFERRAL", "#"
          + Integer.toString(re.getResultCode()) + " "
          + re.getLDAPErrorMessage(), re);
    } catch (LDAPException e) {
      if (LDAPUtility.recoverConnection(lds, e)) {
        return getFirstEntryFromSearch(lds, baseDN, scope, filter, attrs);
      } else {
        throw new AdminException("LDAPUtility.getFirstEntryFromSearch",
            SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GENERAL", "#"
            + Integer.toString(e.getResultCode()) + " "
            + e.getLDAPErrorMessage(), e);
      }
    }
    return theEntry;
  }

  static public boolean isAGuid(String attName) {
    return "objectGUID".equalsIgnoreCase(attName)
        || "GUID".equalsIgnoreCase(attName);
  }

  /**
   * Reads the values of an attribute and return the strings
   * @param theEntry entry to read the attribute
   * @param theAttributeName name of the attribute to retreive
   * @return the attribute's values as string
   */
  static public String[] getAttributeValues(LDAPEntry theEntry,
      String theAttributeName) {
    SilverTrace.debug("admin", "LDAPUtility.getAttributeValues()",
        "root.MSG_GEN_ENTER_METHOD", "theAttributeName = " + theAttributeName);
    LDAPAttribute theAttr;

    if (theEntry == null || !StringUtil.isDefined(theAttributeName)) {
      return new String[0];
    } else {
      theAttr = theEntry.getAttribute(theAttributeName);
      if (theAttr == null) {
        SilverTrace.debug("admin", "LDAPUtility.getAttributeValues()",
            "root.MSG_GEN_PARAM_VALUE", "Attribute : " + theAttributeName
            + " is null !!!");
        return new String[0];
      } else {
        SilverTrace.debug("admin", "LDAPUtility.getAttributeValues()",
            "root.MSG_GEN_PARAM_VALUE", "Attribute : " + theAttributeName
            + " size = " + Integer.toString(theAttr.size()));
        if (isAGuid(theAttributeName)) {
          byte[][] allBytes = theAttr.getByteValueArray();
          byte[] asBytes;
          String asString;
          StringBuffer theStr;
          String[] valret = new String[theAttr.size()];

          for (int j = 0; j < theAttr.size(); j++) {
            theStr = new StringBuffer(50);
            asBytes = allBytes[j];
            for (int i = 0; i < asBytes.length; i++) {
              asString = Integer.toHexString(asBytes[i]);
              if (asString.length() > 3) {
                theStr.append("\\\\").append(asString.substring(6));
              } else {
                if (asString.length() == 0) {
                  theStr.append("\\\\").append("00");
                } else if (asString.length() == 1) {
                  theStr.append("\\\\0").append(asString);
                } else {
                  theStr.append("\\\\").append(asString);
                }
              }
            }
            valret[j] = theStr.toString();
            SilverTrace.info("admin", "LDAPUtility.getAttributeValues()",
                "root.MSG_GEN_PARAM_VALUE", "Attribute : " + theAttributeName
                + " PARSED VALUE = " + theStr);
          }
          return valret;
        } else {
          return theAttr.getStringValueArray();
        }
      }
    }
  }

  public static String dblBackSlashesForDNInFilters(String theDN) {
    return escapeDN(theDN);
  }

  static public String normalizeFilterValue(String theFilter) {
    return escapeLDAPSearchFilter(theFilter);
  }

  /**
   * Escaping DN to prevent LDAP injection. Based on
   * http://blogs.sun.com/shankar/entry/what_is_ldap_injection
   * @param name the DN to be espaced.
   * @return the escaped DN.
   */
  public static String escapeDN(String name) {
    StringBuilder sb = new StringBuilder();
    if ((name.length() > 0) && ((name.charAt(0) == ' ') || (name.charAt(0) == '#'))) {
      sb.append('\\'); // add the leading backslash if needed
    }
    for (int i = 0; i < name.length(); i++) {
      char curChar = name.charAt(i);
      switch (curChar) {
        case '\\':
          sb.append("\\\\");
          break;
        case ',':
          sb.append("\\,");
          break;
        case '+':
          sb.append("\\+");
          break;
        case '"':
          sb.append("\\\"");
          break;
        case '<':
          sb.append("\\<");
          break;
        case '>':
          sb.append("\\>");
          break;
        case ';':
          sb.append("\\;");
          break;
        default:
          sb.append(curChar);
      }
    }
    if ((name.length() > 1) && (name.charAt(name.length() - 1) == ' ')) {
      sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
    }
    return sb.toString();
  }

  /**
   * Escaping search filter to prevent LDAP injection. Based on
   * http://blogs.sun.com/shankar/entry/what_is_ldap_injection rfc 2254 actually adresses how to fix
   * these ldap injection bugs in section 4 on page 4 Character ASCII value
   * --------------------------- * 0x2a ( 0x28 ) 0x29 \ 0x5c NUL 0x00
   * @param filter the search filter to be espaced.
   * @return the escaped search filter.
   */
  public static String escapeLDAPSearchFilter(String filter) {
    StringBuilder sb = new StringBuilder(); // If using JDK >= 1.5 consider using StringBuilder
    for (int i = 0; i < filter.length(); i++) {
      char curChar = filter.charAt(i);
      switch (curChar) {
        case '\\':
          sb.append("\\5c");
          break;
        case '*':
          sb.append("\\2a");
          break;
        case '(':
          sb.append("\\28");
          break;
        case ')':
          sb.append("\\29");
          break;
        case '\u0000':
          sb.append("\\00");
          break;
        default:
          sb.append(curChar);
      }
    }
    return sb.toString();
  }

  /**
   * Method declaration
   * @param lds
   * @param baseDN
   * @param scope
   * @param filter
   * @param varToSort
   * @param args
   * @return
   * @throws AdminException
   * @see
   */
  static public LDAPEntry[] search1000Plus(String lds, String baseDN,
      int scope, String filter, String varToSort, String[] args)
      throws AdminException {
    SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPConnection ld = getConnection(lds);
    List<LDAPEntry> entriesVector = new ArrayList<LDAPEntry>();
    int nbReaded = 0;
    LDAPSearchConstraints cons = null;
    LDAPSortKey[] keys = new LDAPSortKey[1];
    String theFullFilter = filter;
    boolean notTheFirst = false;
    LDAPException lastException = null;

    try {
      LDAPSettings driverSettings = (connectInfos.get(lds)).driverSettings;
      SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
          "root.MSG_GEN_PARAM_VALUE", "LDAPImpl = "
          + driverSettings.getLDAPImpl());
      if (args != null) {
        SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
            "root.MSG_GEN_PARAM_VALUE", "args = " + args.toString());
      }
      if (driverSettings.getLDAPImpl() != null
          && "openldap".equalsIgnoreCase(driverSettings.getLDAPImpl())) {
        // OpenLDAP doesn't support sorts during search. RFC 2891 not supported.
        cons = null;
      } else {
        keys[0] = new LDAPSortKey(varToSort);
        // Create a LDAPSortControl object - Fail if cannot sort
        LDAPSortControl sort = new LDAPSortControl(keys, true);

        // Set sorted request on server
        cons = ld.getSearchConstraints();
        cons.setControls(sort);
      }

      boolean sizeLimitReached = false;
      boolean timeLimitReached = false;
      int nbRetryTimeLimit = 0;

      // Modif LBE : as more than on baseDN can be set, iterate on all baseDNs
      String[] baseDNs = extractBaseDNs(baseDN);
      LDAPEntry entry = null;
      for (int j = 0; j < baseDNs.length; j++) {
        theFullFilter = filter;
        while (theFullFilter != null) {
          SilverTrace.debug("admin", "LDAPUtility.search1000Plus()",
              "LDAP query", "BaseDN=" + baseDNs[j] + " scope="
              + Integer.toString(scope) + " Filter=" + theFullFilter);
          SynchroReport.debug("LDAPUtility.search1000Plus()",
              "Requête sur le domaine LDAP distant (protocole v"
              + ld.getProtocolVersion() + "), BaseDN=" + baseDNs[j]
              + " scope=" + Integer.toString(scope) + " Filter="
              + theFullFilter, null);

          try {
            LDAPSearchResults res = ld.search(baseDNs[j], scope, theFullFilter, args, false, cons);
            while (res.hasMore()) {
              entry = res.next();
              if (notTheFirst) {
                // res.next();
                notTheFirst = false;
              } else {
                SynchroReport.debug("LDAPUtility.search1000Plus()",
                    "élément #" + nbReaded + " : " + entry.getDN(), null);
                SilverTrace.debug("admin", "LDAPUtility.search1000Plus()",
                    "root.MSG_GEN_PARAM_VALUE", "élément #" + nbReaded
                    + " : " + entry.getDN());
                entriesVector.add(entry);
                nbReaded++;
              }
            }
          } catch (LDAPException le) {
            if (le.getResultCode() == LDAPException.SIZE_LIMIT_EXCEEDED) {
              sizeLimitReached = true;
              SynchroReport.debug("LDAPUtility.search1000Plus()",
                  "Size Limit Reached...", null);
              SilverTrace.debug("admin", "LDAPUtility.search1000Plus()",
                  "root.MSG_GEN_PARAM_VALUE", "Size Limit Reached...");
            } else if (le.getResultCode() == LDAPException.TIME_LIMIT_EXCEEDED) {
              timeLimitReached = true;
              nbRetryTimeLimit++;
              lastException = le;
              SynchroReport.debug("LDAPUtility.search1000Plus()",
                  "Time Limit Reached (#" + nbRetryTimeLimit + ")", null);
              SilverTrace.debug("admin", "LDAPUtility.search1000Plus()",
                  "root.MSG_GEN_PARAM_VALUE", "Time Limit Reached (#"
                  + nbRetryTimeLimit + ")");
            } else {
              SilverTrace.error("admin", "LDAPUtility.search1000Plus",
                  "admin.EX_ERR_LDAP_REFERRAL", "#"
                  + Integer.toString(le.getResultCode()) + " "
                  + le.getLDAPErrorMessage(), le);
            }
          }
          if (sizeLimitReached
              || (timeLimitReached && nbRetryTimeLimit <= MAX_NB_RETRY_TIMELIMIT)) {
            notTheFirst = true;
            sizeLimitReached = false;
            timeLimitReached = false;
            theFullFilter = "(&" + filter + "(" + varToSort + ">="
                + LDAPUtility.getFirstAttributeValue(entry, varToSort) + "))";
            SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
                "root.MSG_GEN_PARAM_VALUE", "SIZE LIMIT REACHED : "
                + theFullFilter);
          } else if (timeLimitReached
              && (nbRetryTimeLimit > MAX_NB_RETRY_TIMELIMIT)) {
            throw lastException;
          } else {
            theFullFilter = null;
          }
        }
      }
    } catch (LDAPReferralException re) {
      SynchroReport.error("LDAPUtility.search1000Plus()",
          "Référence (referral) retournée mais pas suivie !", re);
      throw new AdminException("LDAPUtility.search1000Plus",
          SilverpeasException.ERROR, "admin.EX_ERR_LDAP_REFERRAL", "#"
          + Integer.toString(re.getResultCode()) + " "
          + re.getLDAPErrorMessage(), re);
    } catch (LDAPException e) {
      SynchroReport.debug("LDAPUtility.search1000Plus()",
          "Une exception générale est survenue : #" + e.getResultCode() + " "
          + e.getLDAPErrorMessage(), null);
      SilverTrace.debug("admin", "LDAPUtility.search1000Plus()",
          "Une exception générale est survenue : #" + e.getResultCode() + " "
          + e.getLDAPErrorMessage());
      if (LDAPUtility.recoverConnection(lds, e)) {
        return search1000Plus(lds, baseDN, scope, filter, varToSort, args);
      } else {
        throw new AdminException("LDAPUtility.search1000Plus",
            SilverpeasException.ERROR, "admin.EX_ERR_LDAP_GENERAL", "#"
            + Integer.toString(e.getResultCode()) + " "
            + e.getLDAPErrorMessage(), e);
      }
    }
    return entriesVector.toArray(new LDAPEntry[entriesVector.size()]);
  }

  static public AbstractLDAPTimeStamp getTimeStamp(String lds, String baseDN,
      int scope, String filter, String timeStampVar, String minTimeStamp)
      throws AdminException {
    SilverTrace.info("admin", "LDAPUtility.getTimeStamp()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPSettings driverSettings = (connectInfos.get(lds)).driverSettings;
    LDAPEntry[] theEntries = search1000Plus(lds, baseDN, scope, "(&("
        + timeStampVar + ">=" + minTimeStamp + ")" + filter + ")",
        timeStampVar, null);

    if (theEntries.length > 0) {
      // Problem is : the search1000Plus function sorts normaly by descending
      // order. BUT most LDAP server can't performs this type of order (like
      // Active Directory)
      // So, it may be ordered in the opposite way....
      AbstractLDAPTimeStamp firstVal =
          driverSettings.newLDAPTimeStamp(getFirstAttributeValue(theEntries[0], timeStampVar));
      AbstractLDAPTimeStamp lastVal = driverSettings.newLDAPTimeStamp(getFirstAttributeValue(
          theEntries[theEntries.length - 1], timeStampVar));
      if (firstVal.compareTo(lastVal) >= 0) {
        return firstVal;
      } else {
        return lastVal;
      }
    } else {
      return driverSettings.newLDAPTimeStamp(minTimeStamp);
    }
  }

  static String[] extractBaseDNs(String baseDN) {
    // if no separator, return a array with only the baseDN
    if (!StringUtil.isDefined(baseDN) || baseDN.indexOf(BASEDN_SEPARATOR) == -1) {
      String[] baseDNs = new String[1];
      if (baseDN == null) {
        baseDN = "";
      }
      baseDNs[0] = baseDN;
      return baseDNs;
    }

    StringTokenizer st = new StringTokenizer(baseDN, BASEDN_SEPARATOR);
    List<String> baseDNs = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      baseDNs.add(st.nextToken());
    }
    return (baseDNs.toArray(new String[baseDNs.size()]));
  }
}

class LDAPConnectInfos {

  public final static int MAX_NB_ERROR_CONNECT = 20;
  public LDAPSettings driverSettings = null;
  public LDAPConnection connection = null;
  public int errorCpt = 0;

  public LDAPConnectInfos(LDAPSettings driverSettings) {
    this.driverSettings = driverSettings;
    this.connection = null;
    this.errorCpt = 0;
  }

  public boolean incErrorCpt() {
    errorCpt = errorCpt + 1;
    return (errorCpt < MAX_NB_ERROR_CONNECT);
  }
}
