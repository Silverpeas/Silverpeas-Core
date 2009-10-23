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
package com.stratelia.silverpeas.domains.ldapdriver;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

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
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * This class contains some usefull static functions to access to LDAP elements
 *
 * @author tleroi
 */

public class LDAPUtility extends Object {
  public final static int MAX_NB_RETRY_CONNECT = 10;
  public final static int MAX_NB_RETRY_TIMELIMIT = 5;
  public final static int STRING_REPLACE_FROM = 0;
  public final static int STRING_REPLACE_TO = 1;
  public final static String BASEDN_SEPARATOR = ";;";

  static Hashtable connectInfos = new Hashtable();
  static int connexionsLastId = 0;

  static int stringReplaceSize = 0;
  static String[][] stringReplace = null;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.stratelia.silverpeas.domains.allLDAP", "");
    String res;

    // Char replacement settings for DN
    // --------------------------------
    res = rs.getString("stringReplace.Number", "0");
    stringReplaceSize = Integer.parseInt(res);
    stringReplace = new String[stringReplaceSize][2];
    for (int i = 0; i < stringReplaceSize; i++) {
      stringReplace[i][STRING_REPLACE_FROM] = rs.getString("stringReplace_"
          + Integer.toString(i + 1) + ".From", "");
      stringReplace[i][STRING_REPLACE_TO] = rs.getString("stringReplace_"
          + Integer.toString(i + 1) + ".To", "");
    }
  }

  /**
   * Method declaration
   *
   *
   * @param driverSettings
   *
   * @return
   *
   * @throws AdminException
   *
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
   *
   *
   * @param connectionId
   *
   * @return
   *
   * @throws AdminException
   *
   * @see
   */
  static public LDAPConnection getConnection(String connectionId)
      throws AdminException {
    return ((LDAPConnectInfos) connectInfos.get(connectionId)).connection;
  }

  /**
   * Method declaration
   *
   *
   * @param connectionId
   * @param ex
   *
   * @return
   *
   * @throws AdminException
   *
   * @see
   */
  static public boolean recoverConnection(String connectionId, LDAPException ex)
      throws AdminException {
    int nbRetry = 0;
    boolean reOpened = false;

    if (ex.getResultCode() == LDAPException.CONNECT_ERROR) {
      if (((LDAPConnectInfos) connectInfos.get(connectionId)).incErrorCpt()) {
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
   *
   *
   * @param connectionId
   *
   * @throws AdminException
   *
   * @see
   */
  static public void closeConnection(String connectionId) throws AdminException {
    InternalCloseConnection(connectionId);
    connectInfos.remove(connectionId);
  }

  /**
   * Method declaration
   *
   *
   * @param connectionId
   *
   * @throws AdminException
   *
   * @see
   */
  static private void InternalOpenConnection(String connectionId)
      throws AdminException {
    LDAPConnection valret = null;
    LDAPSettings driverSettings = ((LDAPConnectInfos) connectInfos
        .get(connectionId)).driverSettings;

    try {
      if (driverSettings.isLDAPSecured()) {
        valret = new LDAPConnection(new LDAPJSSESecureSocketFactory());
      } else {
        valret = new LDAPConnection();
      }
      valret
          .connect(driverSettings.getLDAPHost(), driverSettings.getLDAPPort());
      valret.bind(driverSettings.getLDAPProtocolVer(), driverSettings
          .getLDAPAccessLoginDN(), driverSettings.getLDAPAccessPasswd());
      valret.setConstraints(driverSettings.getSearchConstraints(false));
      ((LDAPConnectInfos) connectInfos.get(connectionId)).connection = valret;
    } catch (LDAPException e) {
      try {
        if (valret != null) {
          valret.disconnect();
        }
      } catch (LDAPException ee) {
        SilverTrace.error("admin", "LDAPUtility.openConnection",
            "admin.EX_ERR_LDAP_GENERAL",
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
   *
   *
   * @param connectionId
   *
   * @throws AdminException
   *
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
   * Returns the first value of a specific attribute from an entry. If this
   * attribute have multiple values, only the first is returned
   *
   * @param theEntry
   *          the LDAP entry
   * @param attributeName
   *          the name of the attribute to retreive
   * @return the first value as a string
   * @throws LDAPException
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
   * Search and returns the first Entry that match the parameters baseDN, scope
   * and filter
   *
   * @param ld
   *          the LDAP connection
   * @param baseDN
   *          the base DN for the search
   * @param scope
   *          the scope (LDAPConnection.SCOPE_BASE, LDAPConnection.SCOPE_ONE or
   *          LDAPConnection.SCOPE_SUB)
   * @param filter
   *          the filter for the search (if null, use '(objectClass=*)' )
   * @return the first entry found
   * @throws LDAPException
   * @throws LDAPReferralException
   *           if a referral problem occur
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
   *
   * @param theEntry
   *          entry to read the attribute
   * @param theAttributeName
   *          name of the attribute to retreive
   * @return the attribute's values as string
   * @throws LDAPException
   *           , AdminException
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
                theStr.append("\\\\" + asString.substring(6));
              } else {
                if (asString.length() == 0) {
                  theStr.append("\\\\" + "00");
                } else if (asString.length() == 1) {
                  theStr.append("\\\\" + "0" + asString);
                } else {
                  theStr.append("\\\\" + asString);
                }
              }
            }
            valret[j] = theStr.toString();
            SilverTrace.info("admin", "LDAPUtility.getAttributeValues()",
                "root.MSG_GEN_PARAM_VALUE", "Attribute : " + theAttributeName
                    + " PARSED VALUE = " + theStr);
          }
          return valret;
        } else
          return theAttr.getStringValueArray();
      }
    }
  }

  static public String dblBackSlashesForDNInFilters(String theDN) {

    return normalizeFilterValue(replaceMultiple(theDN, "\\", "\\\\"));
  }

  static public String normalizeFilterValue(String theFilter) {
    String valret = theFilter;

    for (int i = 0; i < stringReplaceSize; i++) {
      valret = replaceMultiple(valret, stringReplace[i][STRING_REPLACE_FROM],
          stringReplace[i][STRING_REPLACE_TO]);
    }
    return valret;
  }

  static protected String replaceMultiple(String theStringSrc,
      String fromString, String toString) {
    int fromIndex = 0;
    int bsPos;
    StringBuffer sb = null;

    if ((theStringSrc == null) || (theStringSrc.length() <= 0)) {
      return "";
    }
    bsPos = theStringSrc.indexOf(fromString, fromIndex);
    if (bsPos != -1) {
      sb = new StringBuffer(theStringSrc.length());
      while (bsPos != -1) {
        sb.append(theStringSrc.substring(fromIndex, bsPos) + toString);
        fromIndex = bsPos + fromString.length(); // Skip the string to replace
        bsPos = theStringSrc.indexOf(fromString, fromIndex);
      }
      if (fromIndex < theStringSrc.length()) {
        sb.append(theStringSrc.substring(fromIndex, theStringSrc.length())); // Copy
        // the
        // end
        // of
        // the
        // String
      }
      return sb.toString();
    } else {
      return theStringSrc;
    }
  }

  /**
   * Method declaration
   *
   *
   * @param lds
   * @param baseDN
   * @param scope
   * @param filter
   * @param varToSort
   *
   * @return
   *
   * @throws AdminException
   *
   * @see
   */
  static public LDAPEntry[] search1000Plus(String lds, String baseDN,
      int scope, String filter, String varToSort, String[] args)
      throws AdminException {
    SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPConnection ld = getConnection(lds);
    LDAPSearchResults res = null;
    LDAPEntry entry = null;
    Vector entriesVector = new Vector();
    int nbReaded = 0;
    LDAPSearchConstraints cons = null;
    LDAPSortKey[] keys = new LDAPSortKey[1];
    String theFullFilter = filter;
    boolean notTheFirst = false;
    LDAPException lastException = null;

    try {
      LDAPSettings driverSettings = ((LDAPConnectInfos) connectInfos.get(lds)).driverSettings;
      SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
          "root.MSG_GEN_PARAM_VALUE", "LDAPImpl = "
              + driverSettings.getLDAPImpl());
      if (args != null)
        SilverTrace.info("admin", "LDAPUtility.search1000Plus()",
            "root.MSG_GEN_PARAM_VALUE", "args = " + args.toString());
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
            res = ld
                .search(baseDNs[j], scope, theFullFilter, args, false, cons);
            while (res.hasMore()) {
              entry = (LDAPEntry) res.next();
              if (notTheFirst) {
                // res.next();
                notTheFirst = false;
              } else {
                SynchroReport.debug("LDAPUtility.search1000Plus()", "élément #"
                    + nbReaded + " : " + entry.getDN(), null);
                SilverTrace.debug("admin", "LDAPUtility.search1000Plus()",
                    "root.MSG_GEN_PARAM_VALUE", "élément #" + nbReaded + " : "
                        + entry.getDN());
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
          res = null;
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
    return (LDAPEntry[]) entriesVector.toArray(new LDAPEntry[0]);
  }

  static public AbstractLDAPTimeStamp getTimeStamp(String lds, String baseDN,
      int scope, String filter, String timeStampVar, String minTimeStamp)
      throws AdminException {
    SilverTrace.info("admin", "LDAPUtility.getTimeStamp()",
        "root.MSG_GEN_ENTER_METHOD");
    LDAPSettings driverSettings = ((LDAPConnectInfos) connectInfos.get(lds)).driverSettings;
    LDAPEntry[] theEntries = search1000Plus(lds, baseDN, scope, "(&("
        + timeStampVar + ">=" + minTimeStamp + ")" + filter + ")",
        timeStampVar, null);

    if (theEntries.length > 0) {
      // Problem is : the search1000Plus function sorts normaly by descending
      // order. BUT most LDAP server can't performs this type of order (like
      // Active Directory)
      // So, it may be ordered in the opposite way....
      AbstractLDAPTimeStamp firstVal = driverSettings
          .newLDAPTimeStamp(getFirstAttributeValue(theEntries[0], timeStampVar));
      AbstractLDAPTimeStamp lastVal = driverSettings
          .newLDAPTimeStamp(getFirstAttributeValue(
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
      if (baseDN == null)
        baseDN = "";
      baseDNs[0] = baseDN;
      return baseDNs;
    }

    StringTokenizer st = new StringTokenizer(baseDN, BASEDN_SEPARATOR);
    Vector baseDNs = new Vector();
    while (st.hasMoreTokens()) {
      baseDNs.add(st.nextToken());
    }
    return (String[]) (baseDNs.toArray(new String[0]));
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