/**
 * Titre : Silverpeas<p>
 * Description : This object provides the function of files attached<p>
 * Copyright : Copyright (c) Jean-Claude Groccia<p>
 * Société : Stratelia<p>
 * @author Jean-Claude Groccia
 * @version 1.0
 */
package com.stratelia.webactiv.util.attachment.model;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.exception.UtilException;

public class AttachmentDAO
{
	// the date format used in database to represent a date
	//private static java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");
	private static String attachmentTableName = "SB_Attachment_Attachment";
	private static String attachmentTableColumns =
		" attachmentId, attachmentPhysicalName, attachmentLogicalName, attachmentDescription, attachmentType, " +
		"attachmentSize, attachmentContext, attachmentForeignkey, instanceId, attachmentCreationDate, attachmentAuthor, " +
		"attachmentTitle, attachmentInfo, attachmentOrderNum, workerId, cloneId, lang, reservationDate, alertDate, expiryDate ";
	private final static int nameMaxLength = 100;  

	public AttachmentDAO()
	{
	}

	private static AttachmentDetail result2AttachmentDetail(ResultSet rs) throws SQLException
	{
		String instanceId = rs.getString("instanceId");
		String id = Integer.toString(rs.getInt("attachmentId"));
		AttachmentPK pk = new AttachmentPK(id, instanceId);
		
		AttachmentDetail attachDetail = new AttachmentDetail(pk);

		attachDetail.setPhysicalName(rs.getString("attachmentPhysicalName"));
		attachDetail.setLogicalName(rs.getString("attachmentLogicalName"));
		attachDetail.setDescription(rs.getString("attachmentDescription"));
		attachDetail.setSize(Long.parseLong(rs.getString("attachmentSize")));
		attachDetail.setType(rs.getString("attachmentType"));
		attachDetail.setContext(rs.getString("attachmentContext"));
		String u = rs.getString("attachmentCreationDate");
		if (u != null)
		{
			try
			{
				attachDetail.setCreationDate(DateUtil.parse(rs.getString("attachmentCreationDate")));
			}
			catch (java.text.ParseException e)
			{
				throw new SQLException(
					"AttachmentDAO.result2AttachmentDetail() : internal error : creationDate format unknown for attachment.pk = "
						+ pk
						+ " : "
						+ e.toString());
			}
		}
		else
			attachDetail.setCreationDate(new Date());
		attachDetail.setAuthor(rs.getString("attachmentAuthor"));
		attachDetail.setTitle(rs.getString("attachmentTitle"));
		attachDetail.setInfo(rs.getString("attachmentInfo"));
		attachDetail.setOrderNum(rs.getInt("attachmentOrderNum"));
		attachDetail.setWorkerId(rs.getString("workerId"));
		attachDetail.setInstanceId(instanceId);
		
		AttachmentPK fk = new AttachmentPK(rs.getString("attachmentForeignKey"), instanceId);
		attachDetail.setForeignKey(fk);
		
		attachDetail.setCloneId(rs.getString("cloneId"));
		
		attachDetail.setLanguage(rs.getString("lang"));
		
		String rd = rs.getString("reservationDate");
		if (rd != null)
		{
			try
			{
				attachDetail.setReservationDate(DateUtil.parse(rs.getString("reservationDate")));
			}
			catch (java.text.ParseException e)
			{
				throw new SQLException(
					"AttachmentDAO.result2AttachmentDetail() : internal error : reservationDate format unknown for attachment.pk = "
						+ pk
						+ " : "
						+ e.toString());
			}
		}
		else
			attachDetail.setReservationDate(null);
		
		// récupération de la date d'alerte
		String ad = rs.getString("alertDate");
		if (ad != null)
		{
			try
			{
				attachDetail.setAlertDate(DateUtil.parse(rs.getString("alertDate")));
			}
			catch (java.text.ParseException e)
			{
				throw new SQLException(
					"AttachmentDAO.result2AttachmentDetail() : internal error : alertDate format unknown for attachment.pk = "
						+ pk
						+ " : "
						+ e.toString());
			}
		}
		else
			attachDetail.setAlertDate(null);
		
		// récupération de la date d'expiration
		String ed = rs.getString("expiryDate");
		if (ed != null)
		{
			try
			{
				attachDetail.setExpiryDate(DateUtil.parse(rs.getString("expiryDate")));
			}
			catch (java.text.ParseException e)
			{
				throw new SQLException(
					"AttachmentDAO.result2AttachmentDetail() : internal error : expiryDate format unknown for attachment.pk = "
						+ pk
						+ " : "
						+ e.toString());
			}
		}
		else
			attachDetail.setExpiryDate(null);
		
		return attachDetail;
	}

	public static AttachmentDetail insertRow(Connection con, AttachmentDetail attach) throws SQLException, UtilException
	{
		int id = DBUtil.getNextId(attachmentTableName, "attachmentId");

		attach.getPK().setId(String.valueOf(id));

		// First get the max orderNum
		AttachmentDetail ad = findLast(con, attach);
		if (ad != null)
			attach.setOrderNum(ad.getOrderNum() + 1);

		String insertQuery = "insert into " + attachmentTableName + " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ?, ?, ?, ?, ?)";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(insertQuery);
			prepStmt.setInt(1, id);
			prepStmt.setString(2, attach.getPhysicalName());
			prepStmt.setString(3, attach.getLogicalName());
			prepStmt.setString(4, attach.getDescription());
			prepStmt.setString(5, attach.getType());
			prepStmt.setString(6, String.valueOf(attach.getSize()));
			prepStmt.setString(7, attach.getContext());

			// nous n'avons besoin que de l'id de l'objet utilsateur car le nom de la table identifie le space et le component
			prepStmt.setString(8, attach.getForeignKey().getId());
			prepStmt.setString(9, attach.getPK().getComponentName());
			prepStmt.setString(10, DateUtil.date2SQLDate(attach.getCreationDate()));
			
			prepStmt.setString(11, attach.getAuthor());
			prepStmt.setString(12, StringUtil.truncate(attach.getTitle(), nameMaxLength));
			prepStmt.setString(13, attach.getInfo());
			prepStmt.setInt(14, attach.getOrderNum());
			prepStmt.setString(15, attach.getWorkerId());
			prepStmt.setString(16, attach.getCloneId());
			prepStmt.setString(17, attach.getLanguage());
			if (attach.getReservationDate() != null)
				prepStmt.setString(18, DateUtil.date2SQLDate(attach.getReservationDate()));
			else
				prepStmt.setString(18, null);
			if (attach.getAlertDate() != null)
				prepStmt.setString(19, DateUtil.date2SQLDate(attach.getAlertDate()));
			else
				prepStmt.setString(19, null);
			if (attach.getExpiryDate() != null)
				prepStmt.setString(20, DateUtil.date2SQLDate(attach.getExpiryDate()));
			else
				prepStmt.setString(20, null);
			
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
		
		//set translations
		setTranslations(con, attach);
		
		return attach;
	}

	public static void updateRow(Connection con, AttachmentDetail attach) throws SQLException
	{
		String updateQuery = "update " + attachmentTableName + " set attachmentTitle = ?, attachmentInfo = ?, " +
				"attachmentPhysicalName = ?, attachmentLogicalName = ?, attachmentDescription = ?, attachmentSize = ?, " +
				"attachmentType = ?, attachmentContext = ?, attachmentCreationDate = ?, attachmentAuthor = ?, " +
				"attachmentOrderNum = ?, workerId = ?, instanceId = ?, lang = ?, reservationDate = ? , alertDate = ?, expiryDate = ? " +
				" where attachmentId = ? ";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(updateQuery);
			prepStmt.setString(1, StringUtil.truncate(attach.getTitle(), nameMaxLength));
			prepStmt.setString(2, attach.getInfo());
			prepStmt.setString(3, attach.getPhysicalName());
			prepStmt.setString(4, attach.getLogicalName());
			prepStmt.setString(5, attach.getDescription());
			prepStmt.setString(6, Long.toString(attach.getSize()));
			prepStmt.setString(7, attach.getType());
			prepStmt.setString(8, attach.getContext());
			if (attach.getCreationDate() == null)
			{
				prepStmt.setString(9, DateUtil.today2SQLDate());
			}
			else
			{
				prepStmt.setString(9, DateUtil.date2SQLDate(attach.getCreationDate()));
			}
			prepStmt.setString(10, attach.getAuthor());
			prepStmt.setInt(11, attach.getOrderNum());
			prepStmt.setString(12, attach.getWorkerId());
			prepStmt.setString(13, attach.getInstanceId());
			
			if (!StringUtil.isDefined(attach.getLanguage()) || I18NHelper.isDefaultLanguage(attach.getLanguage()))
				prepStmt.setNull(14, Types.VARCHAR);
			else
				prepStmt.setString(14, attach.getLanguage());
			
			if (attach.getReservationDate() == null)
			{
				prepStmt.setString(15, null);
			}
			else
			{
				prepStmt.setString(15, DateUtil.date2SQLDate(attach.getReservationDate()));
			}
			if (attach.getAlertDate() == null)
			{
				prepStmt.setString(16, null);
			}
			else
			{
				prepStmt.setString(16, DateUtil.date2SQLDate(attach.getAlertDate()));
			}
			if (attach.getExpiryDate() == null)
			{
				prepStmt.setString(17, null);
			}
			else
			{
				prepStmt.setString(17, DateUtil.date2SQLDate(attach.getExpiryDate()));
			}
			
			
			prepStmt.setInt(18, new Integer(attach.getPK().getId()).intValue());
			
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}		
	}
	
	public static void updateForeignKey(Connection con, AttachmentPK pk, String foreignKey) throws SQLException
	{
		String updateQuery = "update " + attachmentTableName + " set attachmentForeignkey = ? where attachmentId = ? ";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(updateQuery);
			
			prepStmt.setString(1, foreignKey);
			prepStmt.setInt(2, Integer.parseInt(pk.getId()));
		
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}		
	}

	public static AttachmentDetail findByPrimaryKey(Connection con, AttachmentPK pk) throws SQLException
	{
		ResultSet rs = null;
		PreparedStatement prepStmt = null;

		AttachmentDetail attachDetail;
		try
		{
			StringBuffer selectQuery = new StringBuffer();
		    selectQuery.append("select ").append(attachmentTableColumns);
			selectQuery.append(" from ").append(attachmentTableName);
			selectQuery.append(" where attachmentId = ? ");
			
			SilverTrace.info(
				"attachment",
				"AttachmentDAO.loadRow()",
				"root.MSG_GEN_PARAM_VALUE",
				"selectQuery = " + selectQuery.toString() + " with attachmentId = " + pk.getId());
			
			attachDetail = null;
			
			prepStmt = con.prepareStatement(selectQuery.toString());
			prepStmt.setInt(1, new Integer(pk.getId()).intValue());
			
			rs = prepStmt.executeQuery();
			if (rs.next())
			{
				attachDetail = result2AttachmentDetail(rs);
				
				//set translations
				setTranslations(con, attachDetail);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		
		return attachDetail;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param foreignKey
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static Vector findByForeignKey(Connection con, WAPrimaryKey foreignKey) throws SQLException
	{
		StringBuffer selectStatement = new StringBuffer();
		selectStatement.append("select ").append(attachmentTableColumns);
		selectStatement.append(" from ").append(attachmentTableName);
		selectStatement.append(" where attachmentForeignKey= ? and instanceId= ? ");
		selectStatement.append(" order by attachmentOrderNum, attachmentId ");

		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		
		Vector attachments;
		try
		{
			attachments = new Vector();
			prepStmt = con.prepareStatement(selectStatement.toString());
			
			// this "id" is the "id" of customer object.
			// The row id in the table defined by getTableName()
			prepStmt.setString(1, foreignKey.getId());
			prepStmt.setString(2, foreignKey.getComponentName());
			
			rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
				
				//set translations
				setTranslations(con, attachmentDetail);
			
				attachments.addElement(attachmentDetail);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		return attachments;
	}
	
	private static void setTranslations(Connection con, AttachmentDetail attachmentDetail) throws SQLException
	{
		AttachmentDetailI18N translation = new AttachmentDetailI18N(attachmentDetail);
		attachmentDetail.addTranslation(translation);
		
		if (I18NHelper.isI18N)
		{
			List translations = AttachmentI18NDAO.getTranslations(con, attachmentDetail.getPK());
		
			attachmentDetail.setTranslations(translations);
		}
	}
	
	public static Vector findByWorkerId(Connection con, String workerId) throws SQLException
	{
		StringBuffer selectStatement = new StringBuffer();
		selectStatement.append("select ").append(attachmentTableColumns);
		selectStatement.append(" from ").append(attachmentTableName);
		selectStatement.append(" where workerId = ? ");

		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		
		Vector attachments;
		try
		{
			attachments = new Vector();
			prepStmt = con.prepareStatement(selectStatement.toString());
			
			prepStmt.setString(1, workerId);
			
			rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
				
				//set translations
				setTranslations(con, attachmentDetail);
			
				attachments.addElement(attachmentDetail);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		return attachments;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param foreignKey
	 * @param nameAttribut
	 * @param valueAttribut
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static Vector findByPKAndParam(
		Connection con,
		WAPrimaryKey foreignKey,
		String nameAttribut,
		String valueAttribut)
		throws SQLException
	{
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		selectQuery.append(" where attachmentForeignKey = ? ");
		selectQuery.append(" and attachment").append(nameAttribut).append(" = ? ");
		selectQuery.append(" and instanceId = ? ");
		selectQuery.append(" order by attachmentOrderNum, attachmentId ");
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		
		Vector attachments = new Vector();

		try
		{
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, foreignKey.getId());
			prepStmt.setString(2, valueAttribut);
			prepStmt.setString(3, foreignKey.getComponentName());
			
			rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
				
				setTranslations(con, attachmentDetail);
			
				attachments.addElement(attachmentDetail);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}
		
		return attachments;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param foreignKey
	 * @param context
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static Vector findByPKAndContext(Connection con, WAPrimaryKey foreignKey, String context)
		throws SQLException
	{
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		selectQuery.append(" where attachmentForeignKey = ? ");
		if (context != null) 
			selectQuery.append(" and attachmentContext like '").append(context).append("%'"); 
		selectQuery.append(" and instanceId = ? ");
		selectQuery.append(" order by attachmentOrderNum, attachmentId ");

		SilverTrace.info(
			"attachment",
			"AttachmentDAO.findByPKAndContext()",
			"root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + selectQuery.toString());

		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		int i = 0;
		
		Vector attachments;
		try
		{
			attachments = new Vector();
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, foreignKey.getId());
			prepStmt.setString(2, foreignKey.getComponentName());
			
			rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
				setTranslations(con, attachmentDetail);
				
				if (attachmentDetail.getOrderNum() != i)
				{
					attachmentDetail.setOrderNum(i);
					updateRow(con,attachmentDetail);
				}
				i++;
				attachments.addElement(attachmentDetail);
			}
			
			SilverTrace.info(
				"attachment",
				"AttachmentDAO.findByPKAndContext()",
				"root.MSG_GEN_PARAM_VALUE",
				"attachments.size() = " + attachments.size());
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}

		return attachments;
	}

	public static AttachmentDetail findPrevious(Connection con, AttachmentDetail ad) throws SQLException
	{
		WAPrimaryKey foreignKey = ad.getForeignKey();
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		selectQuery.append(" where attachmentForeignKey = ? ");
		if (ad.getContext() != null) 
			selectQuery.append(" and attachmentContext like '").append(ad.getContext()).append("%'"); 
		selectQuery.append(" and instanceId = ? ");
		selectQuery.append(" and attachmentOrderNum < ?");
		selectQuery.append(" order by attachmentOrderNum DESC, attachmentId DESC");
	
		SilverTrace.info(
			"attachment",
			"AttachmentDAO.findPrevious()",
			"root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + selectQuery.toString());
	
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		AttachmentDetail attachmentDetail = null;
		
		try
		{
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, foreignKey.getId());
			prepStmt.setString(2, foreignKey.getComponentName());
			prepStmt.setInt(3, ad.getOrderNum());
			
			rs = prepStmt.executeQuery();
			
			if (rs.next())
			{
				attachmentDetail = result2AttachmentDetail(rs);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}
		return attachmentDetail;
	}
	
	public static AttachmentDetail findNext(Connection con, AttachmentDetail ad) throws SQLException
	{
		WAPrimaryKey foreignKey = ad.getForeignKey();
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		selectQuery.append(" where attachmentForeignKey = ? ");
		if (ad.getContext() != null) 
			selectQuery.append(" and attachmentContext like '").append(ad.getContext()).append("%'"); 
		selectQuery.append(" and instanceId = ? ");
		selectQuery.append(" and attachmentOrderNum > ?");
		selectQuery.append(" order by attachmentOrderNum, attachmentId");
	
		SilverTrace.info(
			"attachment",
			"AttachmentDAO.findNext()",
			"root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + selectQuery.toString());
	
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		AttachmentDetail attachmentDetail = null;
		
		try
		{
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, foreignKey.getId());
			prepStmt.setString(2, foreignKey.getComponentName());
			prepStmt.setInt(3, ad.getOrderNum());
			
			rs = prepStmt.executeQuery();
			
			if (rs.next())
			{
				attachmentDetail = result2AttachmentDetail(rs);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}
		return attachmentDetail;
	}
	
	public static AttachmentDetail findLast(Connection con, AttachmentDetail ad) throws SQLException
	{
		WAPrimaryKey foreignKey = ad.getForeignKey();
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		selectQuery.append(" where attachmentForeignKey = ? ");
		if (ad.getContext() != null) 
			selectQuery.append(" and attachmentContext like '").append(ad.getContext()).append("%'"); 
		selectQuery.append(" and instanceId = ? ");
		selectQuery.append(" order by attachmentOrderNum DESC, attachmentId DESC");
	
		SilverTrace.info(
			"attachment",
			"AttachmentDAO.findNext()",
			"root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + selectQuery.toString());
	
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		AttachmentDetail attachmentDetail = null;
		
		try
		{
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, foreignKey.getId());
			prepStmt.setString(2, foreignKey.getComponentName());
			
			rs = prepStmt.executeQuery();
			
			if (rs.next())
			{
				attachmentDetail = result2AttachmentDetail(rs);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}
		return attachmentDetail;
	}

	public static void deleteAttachment(Connection con, AttachmentPK pk) throws SQLException
	{
		PreparedStatement prepStmt = null;

		try
		{
			String deleteQuery = "delete from " + attachmentTableName + " where attachmentId = ? ";
			
			prepStmt = con.prepareStatement(deleteQuery);
			prepStmt.setInt(1, new Integer(pk.getId()).intValue());
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}
	
	public static Collection getAllAttachmentByDate(Connection con, Date date, boolean alert) throws SQLException
	{
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		if (alert)
			selectQuery.append(" where alertDate = ? ");
		else
			selectQuery.append(" where expiryDate = ? ");

		SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()", "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());

		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		
		Collection attachments;
		try
		{
			attachments = new ArrayList();
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, DateUtil.date2SQLDate(date));
			
			rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
				setTranslations(con, attachmentDetail);
				
				attachments.add(attachmentDetail);
			}
			
			SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()", "root.MSG_GEN_PARAM_VALUE", "attachments.size() = " + attachments.size());
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}

		return attachments;
	}
	
	public static Collection getAllAttachmentToLib(Connection con, Date date) throws SQLException
	{
		StringBuffer selectQuery = new StringBuffer();
		selectQuery.append("select ").append(attachmentTableColumns);
		selectQuery.append(" from ").append(attachmentTableName);
		selectQuery.append(" where expiryDate < ? ");

		SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()", "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());

		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		
		Collection attachments;
		try
		{
			attachments = new ArrayList();
			prepStmt = con.prepareStatement(selectQuery.toString());
			
			prepStmt.setString(1, DateUtil.date2SQLDate(date));
			
			rs = prepStmt.executeQuery();
			
			while (rs.next())
			{
				AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
				setTranslations(con, attachmentDetail);
				
				attachments.add(attachmentDetail);
			}
			
			SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()", "root.MSG_GEN_PARAM_VALUE", "attachments.size() = " + attachments.size());
		}
		finally
		{
			DBUtil.close(rs, prepStmt);			
		}

		return attachments;
	}

}