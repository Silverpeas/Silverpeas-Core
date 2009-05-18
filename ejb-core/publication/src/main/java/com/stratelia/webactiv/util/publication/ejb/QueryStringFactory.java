/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.publication.ejb;

public class QueryStringFactory extends Object
{
    /**
     * Hashtable which contains the specifics code encoded as key and their values are right code encoded
     */
    private static String selectByBeginDateDescAndStatusAndNotLinkedToFatherId	= null;
    private static String selectByFatherPK										= null;
    private static String selectByFatherPKPeriodSensitive						= null;
	private static String loadRow												= null;
	private static String selectByName											= null;
	private static String selectByNameAndNodeId									= null;
    private static String selectNotInFatherPK										= null;

	public static String getSelectByBeginDateDescAndStatusAndNotLinkedToFatherId(String tableName) {
		if (selectByBeginDateDescAndStatusAndNotLinkedToFatherId == null) {
			StringBuffer query = new StringBuffer();
			query.append("select  distinct(P.pubId), P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
			query.append("        P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
			query.append("		 P.pubStatus, P.pubImage, P.pubImageMimeType, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang ");
			query.append(" from ").append(tableName).append(" P, ").append(tableName).append("Father F ");
			query.append(" where F.pubId = P.pubId ");
			query.append(" and F.instanceId = ? ");
			query.append(" and F.nodeId <> ? ");
			query.append(" and P.pubStatus = ? ");
			query.append("and (");
				query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
				query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
				query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
				query.append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
			query.append(" ) ");
			query.append(" order by P.pubBeginDate DESC, P.pubCreationDate DESC ");
			selectByBeginDateDescAndStatusAndNotLinkedToFatherId = query.toString();
		}
		return selectByBeginDateDescAndStatusAndNotLinkedToFatherId;
	}

	public static String getSelectByFatherPK(String tableName) {
		return getSelectByFatherPK(tableName, true);
	}

	public static String getSelectByFatherPK(String tableName, boolean periodSensitive) {
		if (periodSensitive && selectByFatherPKPeriodSensitive != null)
			return selectByFatherPKPeriodSensitive;
		else if (!periodSensitive && selectByFatherPK != null)
			return selectByFatherPK;

		StringBuffer query = new StringBuffer();
		query.append("select  P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
		query.append("        P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
		query.append("		 P.pubStatus, P.pubImage, P.pubImageMimeType, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang ");
		query.append("from ").append(tableName).append(" P, ").append(tableName).append("Father  F ");
		query.append("where F.instanceId = ? ");
		query.append("and F.nodeId = ? ");
		query.append("and F.pubId = P.pubId ");
		selectByFatherPK = query.toString();
		query.append("and (");
			query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
			query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
			query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
			query.append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
		query.append(" ) ");
		selectByFatherPKPeriodSensitive = query.toString();

		if (periodSensitive)
			return selectByFatherPKPeriodSensitive;
		else
			return selectByFatherPK;
	}

	public static String getSelectNotInFatherPK(String tableName) {
		if (selectNotInFatherPK == null) {
			StringBuffer query = new StringBuffer();
			query.append("select distinct P.pubId, P.infoId, P.pubName, P.pubDescription, P.pubCreationDate, P.pubBeginDate, ");
			query.append("        P.pubEndDate, P.pubCreatorId, P.pubImportance, P.pubVersion, P.pubKeywords, P.pubContent, ");
			query.append("		 P.pubStatus, P.pubImage, P.pubImageMimeType, P.pubUpdateDate, P.instanceId, P.pubUpdaterId, P.pubValidateDate, P.pubValidatorId, P.pubBeginHour, P.pubEndHour, P.pubAuthor, P.pubTargetValidatorId, P.pubCloneId, P.pubCloneStatus, P.lang ");
			query.append("from ").append(tableName).append(" P, ").append(tableName).append("Father  F ");
			query.append("where F.instanceId = ? ");
			query.append("and F.nodeId <> ? ");
			query.append("and F.pubId = P.pubId ");
			query.append("and (");
				query.append("( ? > P.pubBeginDate AND ? < P.pubEndDate ) OR ");
				query.append("( ? = P.pubBeginDate AND ? < P.pubEndDate AND ? > P.pubBeginHour ) OR ");
				query.append("( ? > P.pubBeginDate AND ? = P.pubEndDate AND ? < P.pubEndHour ) OR ");
				query.append("( ? = P.pubBeginDate AND ? = P.pubEndDate AND ? > P.pubBeginHour AND ? < P.pubEndHour )");
			query.append(" ) ");
			selectNotInFatherPK = query.toString();
		}
		return selectNotInFatherPK;
	}

	public static String getLoadRow(String tableName) {
		if (loadRow == null) {
			StringBuffer query = new StringBuffer();
			query.append("select pubid, infoid, pubname, pubdescription, pubcreationdate, " +
					"pubbegindate, pubenddate, pubcreatorid, pubimportance, pubversion, pubkeywords," +
					"pubcontent, pubstatus, pubimage, pubimagemimetype, pubupdatedate," +
					"instanceid, pubupdaterid, pubvalidatedate, pubvalidatorid, pubbeginhour," +
					"pubendhour, pubauthor, pubtargetvalidatorid, pubcloneid, pubclonestatus," +
					"lang ");
			query.append(" from ").append(tableName);
			query.append(" where pubId = ? ");
			loadRow = query.toString();
		}
		return loadRow;
	}

	public static String getSelectByName(String tableName) {
		if (selectByName == null) {
			StringBuffer query = new StringBuffer();
			query.append("select * ");
			query.append("from ").append(tableName);
			query.append(" where pubName = ? ");
			query.append(" and instanceId = ? ");
			selectByName = query.toString();
		}
		return selectByName;
	}

	public static String getSelectByNameAndNodeId() {
		if (selectByNameAndNodeId == null) {
			StringBuffer query = new StringBuffer();
			query.append("SELECT * FROM sb_publication_publi pub, sb_publication_publifather pubnode");
			query.append(" WHERE (pub.pubid = pubnode.pubid)");
			query.append(" AND pub.pubname = ?");
			query.append(" AND pub.instanceId = ?");
			query.append(" AND pubnode.nodeid = ?");
			selectByNameAndNodeId = query.toString();
		}
		return selectByNameAndNodeId;
	}

}