package com.stratelia.silverpeas.pdc.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import com.stratelia.silverpeas.pdc.model.AxisHeaderI18N;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;


/**
 * Class declaration
 *
 *
 * @author
 */
public class AxisHeaderI18NDAO
{

	private static String PdcAxisI18NTable = "SB_Pdc_AxisI18N";

	static final private String COLUMNS = "id,AxisId,Lang,Name,Description";
	
	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public AxisHeaderI18NDAO()
	{
		
	}

	/*
	 * 
	 */
	public List getTranslations(Connection con, int axisId) throws PersistenceException, SQLException
	{
		String selectQuery = "select * from " + PdcAxisI18NTable + " where AxisId = ?";
		Vector allTranslations = new Vector();
		
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			prepStmt.setInt(1, axisId);

			rs = prepStmt.executeQuery();

			AxisHeaderI18N translation = null;
			while (rs.next())
			{
				translation = new AxisHeaderI18N();
				translation.setId(rs.getInt(1));
				translation.setObjectId(Integer.toString(rs.getInt(2)));
				translation.setLanguage(rs.getString(3));
				translation.setName(rs.getString(4));
				translation.setDescription(rs.getString(5));
				
				SilverTrace.info("Pdc", "AxisHeaderI18NDAO.getTranslations", "root.MSG_GEN_PARAM_VALUE", "translation "
						+ translation.getId() + " for axisId = " + axisId);
				allTranslations.add(translation); 
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}

		return allTranslations;
	}

	public void createTranslation(Connection con, AxisHeaderI18N translation) throws SQLException, UtilException
	{
		String selectQuery = "insert into " + PdcAxisI18NTable + "(" + COLUMNS + ") values  (?, ?, ?, ?, ?)";
		PreparedStatement prepStmt = null;
		int id = -1;
		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			id = DBUtil.getNextId(PdcAxisI18NTable, "id");
			prepStmt.setInt(1, id);
			prepStmt.setInt(2, Integer.parseInt(translation.getObjectId()));
			prepStmt.setString(3, translation.getLanguage());
			prepStmt.setString(4, translation.getName());
			prepStmt.setString(5, translation.getDescription());
			
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}
	
	public void updateTranslation(Connection con, AxisHeaderI18N translation) throws SQLException, UtilException
	{
		String selectQuery = "update " + PdcAxisI18NTable + " set name = ?, description = ? where id = ? ";
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			prepStmt.setString(1, translation.getName());
			prepStmt.setString(2, translation.getDescription());
			prepStmt.setInt(3, translation.getId());

			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
	}
	
	public void deleteTranslation(Connection con, int translationId) throws SQLException, UtilException
	{
		String selectQuery = "delete from " + PdcAxisI18NTable + " where id = ?";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			prepStmt.setInt(1, translationId);
			
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}
	
	public void deleteTranslations(Connection con, int axisId) throws SQLException, UtilException
	{
		String selectQuery = "delete from " + PdcAxisI18NTable + " where axisId = ?";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			prepStmt.setInt(1, axisId);
			
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	}