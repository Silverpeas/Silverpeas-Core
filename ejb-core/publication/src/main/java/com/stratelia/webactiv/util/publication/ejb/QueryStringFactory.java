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
package com.stratelia.webactiv.util.publication.ejb;

public class QueryStringFactory extends Object {

  /**
   * Hashtable which contains the specifics code encoded as key and their values are right code
   * encoded
   */
  private static String selectByBeginDateDescAndStatusAndNotLinkedToFatherId = null;
  private static String selectByFatherPK = null;
  private static String selectByFatherPKPeriodSensitive = null;
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
      String tableName) {
    synchronized(QueryStringFactory.class) {
      if (selectByBeginDateDescAndStatusAndNotLinkedToFatherId == null) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT(P.pubId), P.infoId, P.pubName, P.pubDescription, ");
        query.append("P.pubCreationDate, P.pubBeginDate, P.pubEndDate, P.pubCreatorId, ");
        query.append("P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
        query.append("	P.pubStatus, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, ");
        query.append("P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, ");
        query.append("P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, ");
        query.append("P.pubDraftOutDate FROM ").append(tableName).append(" P, ").append(tableName);
        query.append("Father F WHERE F.pubId = P.pubId AND F.instanceId = ? ");
        query.append(" AND F.nodeId <> ?  AND P.pubStatus = ? AND (");
        query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
        query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
        query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
        query.append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour ");
        query.append("AND ? < P.pubEndHour )) ORDER BY P.pubBeginDate DESC, P.pubCreationDate DESC ");
        selectByBeginDateDescAndStatusAndNotLinkedToFatherId = query.toString();
      }
    }
    return selectByBeginDateDescAndStatusAndNotLinkedToFatherId;
  }

  public static String getSelectByFatherPK(String tableName) {
    return getSelectByFatherPK(tableName, true);
  }

  public static String getSelectByFatherPK(String tableName,
      boolean periodSensitive) {
    if (periodSensitive && selectByFatherPKPeriodSensitive != null) {
      return selectByFatherPKPeriodSensitive;
    } else if (!periodSensitive && selectByFatherPK != null) {
      return selectByFatherPK;
    }

    StringBuilder query = new StringBuilder();
    query.append("SELECT P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, ");
    query.append("P.pubBeginDate, P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, ");
    query.append("P.pubKeywords, P.pubContent, P.pubStatus, ");
    query.append("P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, ");
    query.append("P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, ");
    query.append("P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, P.pubDraftOutDate FROM ");
    query.append(tableName).append(" P, ").append(tableName).append("Father F ");
    query.append("WHERE F.instanceId = ? AND F.nodeId = ? AND F.pubId = P.pubId ");
    selectByFatherPK = query.toString();
    query.append("AND (");
    query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
    query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
    query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
    query.append(
        "( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
    query.append(" ) ");
    selectByFatherPKPeriodSensitive = query.toString();

    if (periodSensitive) {
      return selectByFatherPKPeriodSensitive;
    }
    return selectByFatherPK;
  }

  public static String getSelectNotInFatherPK(String tableName) {
    synchronized (QueryStringFactory.class) {
      if (selectNotInFatherPK == null) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT P.pubId, P.infoId, P.pubName, P.pubDescription, ");
        query.append("P.pubCreationDate, P.pubBeginDate, P.pubEndDate, P.pubCreatorId, ");
        query.append("P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, P.pubStatus, ");
        query.append("P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, ");
        query.append("P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, ");
        query.append("P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang, ");
        query.append("P.pubDraftOutDate FROM ").append(tableName).append(" P, ").append(tableName);
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
    return selectNotInFatherPK;
  }

  public static String getLoadRow(String tableName) {
    synchronized (QueryStringFactory.class) {
      if (loadRow == null) {
        StringBuilder query = new StringBuilder();
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
    return loadRow;
  }

  public static String getSelectByName() {
    return selectByName;
  }

  public static String getSelectByNameAndNodeId() {
    return selectByNameAndNodeId;
  }
}
