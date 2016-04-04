/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.util.StringUtil;

public class QueryStringFactory {

  /**
   * Hashtable which contains the specifics code encoded as key and their values are right code
   * encoded
   */
  private static String selectByBeginDateDescAndStatusAndNotLinkedToFatherId = null;
  private static String selectByFatherPK = null;
  private static String selectByFatherPKPeriodSensitive = null;
  private static String selectByFatherPKAndUserId = null;
  private static String selectByFatherPKPeriodSensitiveAndUserId = null;
  private static String loadRow = null;
  private final static String selectByName;
  private static final String selectByNameAndNodeId;
  private static String selectNotInFatherPK = null;

  static {
    StringBuilder query = new StringBuilder();
    query.append("SELECT * FROM sb_publication_publi pub, sb_publication_publifather pubnode");
    query.append(" WHERE (pub.pubid = pubnode.pubid)");
    query.append(" AND pub.pubname = ?");
    query.append(" AND pub.instanceId = ?");
    query.append(" AND pubnode.nodeid = ?");
    selectByNameAndNodeId = query.toString();

    query = new StringBuilder();
    query.append("SELECT * FROM sb_publication_publi WHERE pubName = ? AND instanceId = ? ");
    selectByName = query.toString();
  }

  public static String getSelectByBeginDateDescAndStatusAndNotLinkedToFatherId(
      final String tableName) {
    if (selectByBeginDateDescAndStatusAndNotLinkedToFatherId == null) {
      synchronized (QueryStringFactory.class) {
        if (selectByBeginDateDescAndStatusAndNotLinkedToFatherId == null) {
          final StringBuilder query = new StringBuilder();
          query.append("SELECT DISTINCT(P.pubId), P.infoId, P.pubName, P.pubDescription, ");
          query.append("P.pubCreationDate, P.pubBeginDate, P.pubEndDate, P.pubCreatorId, ");
          query.append("P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
          query.append("P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, ");
          query.append("P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, ");
          query
              .append("P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, ");
          query.append("P.pubDraftOutDate FROM ").append(tableName).append(" P, ")
              .append(tableName);
          query.append("Father F WHERE F.pubId = P.pubId AND F.instanceId = ? ");
          query.append(" AND F.nodeId <> ?  AND P.pubStatus = ? AND (");
          query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
          query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
          query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
          query.append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour ");
          query
              .append("AND ? < P.pubEndHour )) ORDER BY P.pubBeginDate DESC, P.pubCreationDate DESC ");
          selectByBeginDateDescAndStatusAndNotLinkedToFatherId = query.toString();
        }
      }
    }
    return selectByBeginDateDescAndStatusAndNotLinkedToFatherId;
  }

  public static String getSelectByFatherPK(final String tableName) {
    return getSelectByFatherPK(tableName, true, null);
  }

  public static String getSelectByFatherPK(final String tableName, final boolean periodSensitive,
      final String userId) {

    // Initialization
    if (selectByFatherPKPeriodSensitiveAndUserId == null) {
      synchronized (QueryStringFactory.class) {
        if (selectByFatherPKPeriodSensitiveAndUserId == null) {
          selectByFatherPK = buildSelectByFatherPK(tableName, false, false);
          selectByFatherPKPeriodSensitive = buildSelectByFatherPK(tableName, true, false);
          selectByFatherPKAndUserId = buildSelectByFatherPK(tableName, false, true);
          selectByFatherPKPeriodSensitiveAndUserId = buildSelectByFatherPK(tableName, true, true);
        }
      }
    }

    if (StringUtil.isDefined(userId)) {
      if (periodSensitive) {
        return selectByFatherPKPeriodSensitiveAndUserId;
      }
      return selectByFatherPKAndUserId;
    } else {
      if (periodSensitive) {
        return selectByFatherPKPeriodSensitive;
      }
      return selectByFatherPK;
    }
  }

  /**
   * Centralizes common SQL blocks
   * @param tableName
   * @param isPeriodSensitive
   * @param isUserId
   * @return
   */
  private static String buildSelectByFatherPK(final String tableName,
      final boolean isPeriodSensitive, final boolean isUserId) {

    final StringBuilder query = new StringBuilder();

    // common select
    query.append("SELECT P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, ");
    query.append("P.pubBeginDate, P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, ");
    query.append("P.pubKeywords, P.pubContent, P.pubStatus, ");
    query.append("P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, ");
    query.append("P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, ");
    query.append("P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, ");
    query.append("P.pubDraftOutDate, F.pubOrder FROM ");
    query.append(tableName).append(" P, ").append(tableName).append("Father F ");
    query.append("WHERE F.instanceId = ? AND F.nodeId = ? AND F.pubId = P.pubId ");

    // adding user id clause if necessary
    if (isUserId) {
      query.append("AND (P.pubUpdaterId = ? OR P.pubCreatorId = ? ) ");
    }

    // adding sensitive periode clause if necessary
    if (isPeriodSensitive) {
      query.append("AND (");
      query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
      query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
      query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
      query
          .append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
      query.append(" ) ");
    }

    // adding order by clause
    return query.append(" ORDER BY F.pubOrder ASC").toString();
  }

  public static String getSelectNotInFatherPK(final String tableName) {
    if (selectNotInFatherPK == null) {
      synchronized (QueryStringFactory.class) {
        if (selectNotInFatherPK == null) {
          final StringBuilder query = new StringBuilder();
          query.append("SELECT DISTINCT P.pubId, P.infoId, P.pubName, P.pubDescription, ");
          query.append("P.pubCreationDate, P.pubBeginDate, P.pubEndDate, P.pubCreatorId, ");
          query.append("P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, P.pubStatus, ");
          query.append("P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, ");
          query.append("P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, ");
          query.append("P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, ");
          query.append("P.pubDraftOutDate FROM ").append(tableName).append(" P, ")
              .append(tableName);
          query.append("Father F WHERE F.instanceId = ? AND F.nodeId <> ? ");
          query.append("AND F.pubId = P.pubId AND (");
          query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
          query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
          query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
          query.append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour ");
          query.append(" AND ? < P.pubEndHour )) ");
          selectNotInFatherPK = query.toString();
        }
      }
    }
    return selectNotInFatherPK;
  }

  public static String getLoadRow(final String tableName) {
    if (loadRow == null) {
      synchronized (QueryStringFactory.class) {
        if (loadRow == null) {
          final StringBuilder query = new StringBuilder();
          query.append("select pubid, infoid, pubname, pubdescription, pubcreationdate, "
              + "pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords,"
              + "pubcontent, pubstatus, pubupdatedate,"
              + "instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour,"
              + "pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus,"
              + "lang, pubDraftOutDate ");
          query.append(" from ").append(tableName);
          query.append(" where pubId = ? ");
          loadRow = query.toString();
        }
      }
    }
    return loadRow;
  }

  public static String getSelectByName() {
    return selectByName;
  }

  public static String getSelectByNameAndNodeId() {
    return selectByNameAndNodeId;
  }
}
