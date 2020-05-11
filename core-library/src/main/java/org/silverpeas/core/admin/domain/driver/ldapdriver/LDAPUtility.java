/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

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
import org.silverpeas.core.admin.domain.synchro.SynchroDomainReport;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class contains some useful static functions to access to LDAP elements
 *
 * @author tleroi
 */
public class LDAPUtility {

  private static final MessageFormat LDAP_ERROR = new MessageFormat("LDAP Error #{0}: {1}");

  private static final int MAX_NB_RETRY_CONNECT = 10;
  private static final String BASEDN_SEPARATOR = ";;";
  private static final Map<String, LDAPConnectInfo> connectInfos = new HashMap<>();
  private static final String LDAPUTILITY_SEARCH1000_PLUS = "LDAPUtility.search1000Plus()";
  private static int connexionsLastId = 0;

  private LDAPUtility() {

  }

  public static String openConnection(LDAPSettings driverSettings) throws AdminException {
    String newId;
    synchronized (connectInfos) {
      newId = Integer.toString(connexionsLastId);
      connexionsLastId = connexionsLastId + 1;
      if (connexionsLastId > 1000000) {
        connexionsLastId = 0;
      }
    }
    connectInfos.put(newId, new LDAPConnectInfo(driverSettings));
    internalOpenConnection(newId);
    return newId;
  }

  public static LDAPConnection getConnection(String connectionId) {
    return connectInfos.get(connectionId).getConnection();
  }

  private static boolean recoverConnection(String connectionId, LDAPException ex) {
    int nbRetry = 0;
    boolean reOpened = false;

    if (ex.getResultCode() == LDAPException.CONNECT_ERROR &&
        connectInfos.get(connectionId).incErrorCpt()) {
      SilverLogger.getLogger(LDAPUtility.class).warn("LDAP connection {0} lost", connectionId);
      try {
        internalCloseConnection(connectionId);
      } catch (AdminException e) {
        SilverLogger.getLogger(LDAPUtility.class).warn(e);
      }
      while (!reOpened && nbRetry < MAX_NB_RETRY_CONNECT) {
        try {
          sleepCurrentThread();
          internalOpenConnection(connectionId);
          reOpened = true;
        } catch (AdminException e) {
          nbRetry++;
          SilverLogger.getLogger(LDAPUtility.class)
              .warn("Error in retry " + nbRetry + ": " + e.getMessage());
        }
      }
    }
    return reOpened;
  }

  private static void sleepCurrentThread() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public static void closeConnection(String connectionId) throws AdminException {
    internalCloseConnection(connectionId);
    connectInfos.remove(connectionId);
  }

  private static void internalOpenConnection(String connectionId) throws AdminException {
    LDAPSettings driverSettings = connectInfos.get(connectionId).getSettings();
    LDAPConnection valret;
    if (driverSettings.isLDAPSecured()) {
      valret = new LDAPConnection(new LDAPJSSESecureSocketFactory());
    } else {
      valret = new LDAPConnection();
    }
    try {
      valret.connect(driverSettings.getLDAPHost(), driverSettings.getLDAPPort());
      byte[] passwd = driverSettings.getLDAPAccessPasswd();
      valret.bind(driverSettings.getLDAPProtocolVer(), driverSettings.getLDAPAccessLoginDN(),
          passwd);
      valret.setConstraints(driverSettings.getSearchConstraints(false));
      connectInfos.get(connectionId).setConnection(valret);
    } catch (LDAPException e) {
      try {
        valret.disconnect();
      } catch (LDAPException ee) {
        SilverLogger.getLogger(LDAPUtility.class)
            .error("Error while closing connection " + connectionId + ". Error LDAP #" +
                e.getResultCode() + " " + e.getLDAPErrorMessage(), ee);
      }
      throw new AdminException("Connection error with Host : "
          + driverSettings.getLDAPHost() + " Port : " + driverSettings.getLDAPPort()
          + " LDAPLogin : " + driverSettings.getLDAPAccessLoginDN() + " ProtocolVer : "
          + driverSettings.getLDAPProtocolVer(), e);
    }
  }

  private static void internalCloseConnection(String connectionId)
      throws AdminException {
    LDAPConnection toClose = getConnection(connectionId);

    if (toClose != null && toClose.isConnected()) {
      try {
        toClose.disconnect();
      } catch (LDAPException e) {
        throw new AdminException("Fail to close connection " + connectionId + ". " +
            LDAP_ERROR.format(
                new Object[]{Integer.toString(e.getResultCode()), e.getLDAPErrorMessage()}), e);
      }
    }
  }

  /**
   * Returns the first value of a specific attribute from an entry. If this attribute have multiple
   * values, only the first is returned
   *
   * @param theEntry the LDAP entry
   * @param attributeName the name of the attribute to retreive
   * @return the first value as a string
   */
  public static String getFirstAttributeValue(LDAPEntry theEntry, String attributeName) {
    String[] stringVals = getAttributeValues(theEntry, attributeName);
    if (stringVals != null && stringVals.length > 0) {
      return stringVals[0];
    }
    return "";
  }

  /**
   * Search and returns the first Entry that match the parameters baseDN, scope and filter
   *
   * @param lds the LDAP connection name
   * @param baseDN the base DN for the search
   * @param scope the scope (LDAPConnection.SCOPE_BASE, LDAPConnection.SCOPE_ONE or
   * LDAPConnection.SCOPE_SUB)
   * @param filter the filter for the search (if null, use '(objectClass=*)' )
   * @param attrs hidden attributes
   * @return the first entry found
   * @throws AdminException if a problem occur
   */
  public static LDAPEntry getFirstEntryFromSearch(String lds, String baseDN,
      int scope, String filter, String[] attrs) throws AdminException {
    LDAPConnection connection = getConnection(lds);
    String sureFilter;

    if (!StringUtil.isDefined(filter)) {
      sureFilter = "(objectClass=*)";
    } else {
      sureFilter = filter;
    }
    // Return only one entry

    LDAPSearchConstraints sc = connection.getSearchConstraints();
    sc.setBatchSize(1);
    sc.setMaxResults(1);
    // Modif LBE : as more than on baseDN can be set, iterate on all baseDNs
    // and stop when first entry is found
    String[] baseDNs = extractBaseDNs(baseDN);
    LDAPEntry theEntry = null;
    for (String baseDN1 : baseDNs) {
      try {
        LDAPSearchResults res = connection.search(baseDN1, scope, sureFilter, attrs, false, sc);
        if (res.hasMore()) {
          theEntry = res.next();
          break;
        }
      } catch (LDAPReferralException re) {
        throw new AdminException(LDAP_ERROR.format(
            new Object[]{Integer.toString(re.getResultCode()), re.getLDAPErrorMessage()}), re);
      } catch (LDAPException e) {
        if (LDAPUtility.recoverConnection(lds, e)) {
          return getFirstEntryFromSearch(lds, baseDN, scope, filter, attrs);
        } else {
          SilverLogger.getLogger(LDAPUtility.class)
              .error("Error LDAP #" + e.getResultCode() + " " + e.getLDAPErrorMessage(), e);
        }
      }
    }
    return theEntry;
  }

  static boolean isAGuid(String attName) {
    return "objectGUID".equalsIgnoreCase(attName) || "GUID".equalsIgnoreCase(attName);
  }

  /**
   * Reads the values of an attribute and return the strings
   *
   * @param theEntry entry to read the attribute
   * @param theAttributeName name of the attribute to retreive
   * @return the attribute's values as string
   */
  static String[] getAttributeValues(LDAPEntry theEntry, String theAttributeName) {
    if (theEntry == null || !StringUtil.isDefined(theAttributeName)) {
      return ArrayUtil.emptyStringArray();
    }

    LDAPAttribute theAttr = theEntry.getAttribute(theAttributeName);
    if (theAttr == null) {
      return ArrayUtil.emptyStringArray();
    }

    if (isAGuid(theAttributeName)) {
      byte[][] allBytes = theAttr.getByteValueArray();
      byte[] asBytes;
      StringBuilder theStr;
      String[] valret = new String[theAttr.size()];

      for (int j = 0; j < theAttr.size(); j++) {
        theStr = new StringBuilder(50);
        asBytes = allBytes[j];
        decodeBytesAttributeValue(asBytes, theStr);
        valret[j] = theStr.toString();
      }
      return valret;
    } else {
      return theAttr.getStringValueArray();
    }
  }

  private static void decodeBytesAttributeValue(final byte[] asBytes, final StringBuilder theStr) {
    for (byte asByte : asBytes) {
      final String asString = Integer.toHexString(asByte);
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
  }

  public static String dblBackSlashesForDNInFilters(String theDN) {
    return escapeDN(theDN);
  }

  public static String normalizeFilterValue(String theFilter) {
    return escapeLDAPSearchFilter(theFilter);
  }

  /**
   * Escaping DN to prevent LDAP injection. Based on
   * http://blogs.sun.com/shankar/entry/what_is_ldap_injection
   *
   * @param name the DN to be espaced.
   * @return the escaped DN.
   */
  static String escapeDN(String name) {
    StringBuilder sb = new StringBuilder();
    if (name.length() > 0 && (name.charAt(0) == ' ' || name.charAt(0) == '#')) {
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
    if (name.length() > 1 && name.charAt(name.length() - 1) == ' ') {
      sb.insert(sb.length() - 1, '\\'); // add the trailing backslash if needed
    }
    return sb.toString();
  }

  /**
   * Escaping search filter to prevent LDAP injection. Based on
   * http://blogs.sun.com/shankar/entry/what_is_ldap_injection rfc 2254 actually addresses how to fix
   * these ldap injection bugs in section 4 on page 4 Character ASCII value
   * --------------------------- * 0x2a ( 0x28 ) 0x29 \ 0x5c NUL 0x00
   *
   * @param filter the search filter to be escaped.
   * @return the escaped search filter.
   */
  static String escapeLDAPSearchFilter(String filter) {
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
        case '%':
          sb.append("*");
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
    return sb.toString().replace("\\5c*", "%");
  }

  /**
   * Unescaping search filter.
   * @param filter the search filter to be unescaped.
   * @return the escaped search filter.
   * @see #escapeLDAPSearchFilter(String)
   */
  static String unescapeLDAPSearchFilter(String filter) {
    String unescapedFilter = filter;
    unescapedFilter = unescapedFilter.replace("\\5c", "\\");
    unescapedFilter = unescapedFilter.replace("\\2a", "*");
    unescapedFilter = unescapedFilter.replace("\\28", "(");
    unescapedFilter = unescapedFilter.replace("\\29", ")");
    unescapedFilter = unescapedFilter.replace("\\00", "\u0000");
    return unescapedFilter;
  }

  static LDAPEntry[] search1000Plus(String lds, String baseDN, int scope, String filter,
      String varToSort, String[] args) throws AdminException {

    LDAPConnection ld = getConnection(lds);
    List<LDAPEntry> ldapEntries = new ArrayList<>();
    LDAPSortKey[] keys = new LDAPSortKey[1];
    try {
      LDAPSettings driverSettings = connectInfos.get(lds).getSettings();
      LDAPSearchConstraints constraints;
      if (!driverSettings.isSortControlSupported()) {
        // OpenLDAP doesn't support sorts during search. RFC 2891 not supported.
        constraints = null;
      } else {
        keys[0] = new LDAPSortKey(varToSort);
        // Create a LDAPSortControl object - Fail if cannot sort
        LDAPSortControl sort = new LDAPSortControl(keys, true);
        // Set sorted request on server
        constraints = ld.getSearchConstraints();
        constraints.setControls(sort);
      }

      LDAPSearchContext context = new LDAPSearchContext().setVarToSort(varToSort);
      LDAPSearchQuery query = new LDAPSearchQuery().setScope(scope)
          .setAttrs(args)
          .setConstraints(constraints);

      String[] baseDNs = extractBaseDNs(baseDN);
      for (String baseDN1 : baseDNs) {
        query.setBaseDN(baseDN1);
        // filter can be changed by search, so use the initial one for each DN
        query.setFilter(filter);

        while (query.getFilter() != null) {
          SynchroDomainReport.debug(LDAPUTILITY_SEARCH1000_PLUS,
              "Requête sur le domaine LDAP distant (protocole v" + ld.getProtocolVersion() +
                  "), BaseDN=" + baseDN1 + " scope=" + Integer.toString(scope) + " Filter=" +
                  query.getFilter());

          internalLdapSearch(ld, query, context, ldapEntries);
        }
      }
    } catch (LDAPReferralException re) {
      SynchroDomainReport.error(LDAPUTILITY_SEARCH1000_PLUS,
          "Référence (referral) retournée mais pas suivie !", re);
      throw new AdminException(LDAP_ERROR.format(
          new Object[]{Integer.toString(re.getResultCode()), re.getLDAPErrorMessage()}), re);
    } catch (LDAPException e) {
      SynchroDomainReport.debug(LDAPUTILITY_SEARCH1000_PLUS,
          "Une exception générale est survenue : #" + e.getResultCode() + " "
          + e.getLDAPErrorMessage());
      if (LDAPUtility.recoverConnection(lds, e)) {
        return search1000Plus(lds, baseDN, scope, filter, varToSort, args);
      } else {
        throw new AdminException(LDAP_ERROR.format(
            new Object[]{Integer.toString(e.getResultCode()), e.getLDAPErrorMessage()}), e);
      }
    }
    return ldapEntries.toArray(new LDAPEntry[0]);
  }

  private static void internalLdapSearch(final LDAPConnection ld, final LDAPSearchQuery query,
      final LDAPSearchContext context, final List<LDAPEntry> results) throws LDAPException {
    LDAPEntry entry = null;
    try {
      LDAPSearchResults res =
          ld.search(query.getBaseDN(), query.getScope(), query.getFilter(), query.getAttrs(), false,
              query.getConstraints());
      while (res.hasMore()) {
        entry = res.next();
        if (context.isNotTheFirst()) {
          context.setNotTheFirst(false);
        } else {
          SynchroDomainReport.debug(LDAPUTILITY_SEARCH1000_PLUS,
              "élément #" + context.getNbReaded() + " : " + entry.getDN());
          results.add(entry);
          context.incNbReaded();
        }
      }
    } catch (LDAPException le) {
      if (le.getResultCode() == LDAPException.SIZE_LIMIT_EXCEEDED) {
        context.setSizeLimitReached(true);
        SynchroDomainReport.debug(LDAPUTILITY_SEARCH1000_PLUS, "Size Limit Reached...");
      } else if (le.getResultCode() == LDAPException.TIME_LIMIT_EXCEEDED) {
        context.setTimeLimitReached(true).incNbRetryTimeLimit().setLastException(le);
        SynchroDomainReport.debug(LDAPUTILITY_SEARCH1000_PLUS,
            "Time Limit Reached (#" + context.getNbRetryLimit() + ")");
      } else {
        SilverLogger.getLogger(LDAPUtility.class)
            .error("Error LDAP #" + le.getResultCode() + " " + le.getLDAPErrorMessage(), le);
        throw le;
      }
    }
    if (context.isSizeLimitReached() ||
        (context.isTimeLimitReached() && context.isNotMaxNbRetryTimeLimitReached())) {
      context.setNotTheFirst(true).setSizeLimitReached(false).setTimeLimitReached(false);
      query.setFilter("(&" + query.getFilter() + "(" + context.getVarToSort() + ">=" +
          LDAPUtility.getFirstAttributeValue(entry, context.getVarToSort()) + "))");
    } else if (context.isTimeLimitReached() && context.isMaxNbRetryTimeLimitReached()) {
      throw context.getLastException();
    } else {
      query.setFilter(null);
    }
  }

  static String[] extractBaseDNs(String baseDN) {
    // if no separator, return a array with only the baseDN
    if (!StringUtil.isDefined(baseDN) || !baseDN.contains(BASEDN_SEPARATOR)) {
      String[] baseDNs = new String[1];
      if (baseDN == null) {
        baseDN = "";
      }
      baseDNs[0] = baseDN;
      return baseDNs;
    }

    StringTokenizer st = new StringTokenizer(baseDN, BASEDN_SEPARATOR);
    List<String> baseDNs = new ArrayList<>();
    while (st.hasMoreTokens()) {
      baseDNs.add(st.nextToken());
    }
    return baseDNs.toArray(new String[0]);
  }
}

class LDAPConnectInfo {

  public static final int MAX_NB_ERROR_CONNECT = 20;
  private LDAPSettings driverSettings;
  private LDAPConnection connection;
  private int errorCpt;

  public LDAPConnectInfo(LDAPSettings driverSettings) {
    this.driverSettings = driverSettings;
    this.connection = null;
    this.errorCpt = 0;
  }

  public boolean incErrorCpt() {
    errorCpt = errorCpt + 1;
    return errorCpt < MAX_NB_ERROR_CONNECT;
  }

  public LDAPConnection getConnection() {
    return this.connection;
  }

  public void setConnection(final LDAPConnection connection) {
    this.connection = connection;
  }

  public LDAPSettings getSettings() {
    return driverSettings;
  }
}

class LDAPSearchQuery {
  private String baseDN;
  private int scope;
  private String filter;
  private String[] attrs;
  private LDAPSearchConstraints constraints;

  public String getBaseDN() {
    return baseDN;
  }

  public LDAPSearchQuery setBaseDN(final String baseDN) {
    this.baseDN = baseDN;
    return this;
  }

  public int getScope() {
    return scope;
  }

  public LDAPSearchQuery setScope(final int scope) {
    this.scope = scope;
    return this;
  }

  public String getFilter() {
    return filter;
  }

  public LDAPSearchQuery setFilter(final String filter) {
    this.filter = filter;
    return this;
  }

  public String[] getAttrs() {
    return attrs;
  }

  public LDAPSearchQuery setAttrs(final String[] attrs) {
    this.attrs = attrs;
    return this;
  }

  public LDAPSearchConstraints getConstraints() {
    return constraints;
  }

  public LDAPSearchQuery setConstraints(final LDAPSearchConstraints constraints) {
    this.constraints = constraints;
    return this;
  }
}

class LDAPSearchContext {

  private static final int MAX_NB_RETRY_TIME_LIMIT = 5;

  private boolean sizeLimitReached = false;
  private boolean timeLimitReached = false;
  private int nbRetryTimeLimit = 0;
  private LDAPException lastException = null;
  private int nbReaded = 0;
  private boolean notTheFirst = false;
  private String varToSort = "";

  public boolean isMaxNbRetryTimeLimitReached() {
    return nbRetryTimeLimit > MAX_NB_RETRY_TIME_LIMIT;
  }

  public boolean isNotMaxNbRetryTimeLimitReached() {
    return nbRetryTimeLimit <= MAX_NB_RETRY_TIME_LIMIT;
  }

  public boolean isSizeLimitReached() {
    return sizeLimitReached;
  }

  public LDAPSearchContext setSizeLimitReached(final boolean sizeLimitReached) {
    this.sizeLimitReached = sizeLimitReached;
    return this;
  }

  public boolean isTimeLimitReached() {
    return timeLimitReached;
  }

  public LDAPSearchContext setTimeLimitReached(final boolean timeLimitReached) {
    this.timeLimitReached = timeLimitReached;
    return this;
  }

  public int getNbRetryLimit() {
    return nbRetryTimeLimit;
  }

  public LDAPSearchContext incNbRetryTimeLimit() {
    this.nbRetryTimeLimit++;
    return this;
  }

  public LDAPException getLastException() {
    return lastException;
  }

  public LDAPSearchContext setLastException(final LDAPException lastException) {
    this.lastException = lastException;
    return this;
  }

  public int getNbReaded() {
    return nbReaded;
  }

  public LDAPSearchContext incNbReaded() {
    this.nbReaded++;
    return this;
  }

  public boolean isNotTheFirst() {
    return notTheFirst;
  }

  public LDAPSearchContext setNotTheFirst(final boolean notTheFirst) {
    this.notTheFirst = notTheFirst;
    return this;
  }

  public String getVarToSort() {
    return varToSort;
  }

  public LDAPSearchContext setVarToSort(final String varToSort) {
    this.varToSort = varToSort;
    return this;
  }
}