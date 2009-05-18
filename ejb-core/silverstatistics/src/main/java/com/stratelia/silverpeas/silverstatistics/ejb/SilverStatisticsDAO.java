// TODO : reporter dans CVS (done)
package com.stratelia.silverpeas.silverstatistics.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

/**
 * This is the alimentation statistics DAO Object
 *
 *
 * @author sleroux
 */
public class SilverStatisticsDAO
{
	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param StatsType
	 * @param valueKeys
	 * @param conf
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	private static void insertDataStats(Connection con, String StatsType, ArrayList valueKeys, StatisticsConfig conf)
		throws SQLException
	{
		StringBuffer insertStatementBuf = new StringBuffer("INSERT INTO " + conf.getTableName(StatsType) + "(");
		String insertStatement;
		PreparedStatement prepStmt = null;
		int i = 0;

		Collection theKeys = conf.getAllKeys(StatsType);
		Iterator iteratorKeys = theKeys.iterator();

		while (iteratorKeys.hasNext())
		{
			insertStatementBuf.append((String) (iteratorKeys.next()));
			if (iteratorKeys.hasNext())
			{
				insertStatementBuf.append(",");
			}
		}
		insertStatementBuf.append(") ");

		insertStatementBuf.append("VALUES(?");
		for (int j = 0; j < conf.getNumberOfKeys(StatsType) - 1; j++)
		{
			insertStatementBuf.append(",?");
		}
		insertStatementBuf.append(")");
		insertStatement = insertStatementBuf.toString();

		try
		{
			String currentKey = null;
			String currentType = null;

			SilverTrace.info(
				"silverstatistics",
				"SilverStatisticsDAO.insertDataStats",
				"root.MSG_GEN_PARAM_VALUE",
				"insertStatement=" + insertStatement);
			prepStmt = con.prepareStatement(insertStatement);
			iteratorKeys = theKeys.iterator();
			while (iteratorKeys.hasNext())
			{
				i++;
				currentKey = (String) iteratorKeys.next();
				currentType = conf.getKeyType(StatsType, currentKey);
				if (currentType.equals("DECIMAL"))
				{
					long tmpLong = 0;
					try
					{
						String tmpString = (String) valueKeys.get(i - 1);
						if (tmpString.equals("") || tmpString == null)
						{
							if (!conf.isCumulKey(StatsType, currentKey))
								prepStmt.setNull(i, java.sql.Types.DECIMAL);
							else
								prepStmt.setLong(i, 0);
						}
						else
						{
							tmpLong = new Long(tmpString).longValue();
							prepStmt.setLong(i, tmpLong);
						}
					}
					catch (NumberFormatException e)
					{
						prepStmt.setLong(i, 0);
					}
				}
				if (currentType.equals("INTEGER"))
				{
					int tmpInt = 0;
					try
					{
						String tmpString = (String) valueKeys.get(i - 1);
						if (tmpString.equals("") || tmpString == null)
						{
							if (!conf.isCumulKey(StatsType, currentKey))
								prepStmt.setNull(i, java.sql.Types.INTEGER);
							else
								prepStmt.setInt(i, 0);
						}
						else
						{
							tmpInt = new Integer(tmpString).intValue();
							prepStmt.setInt(i, tmpInt);
						}
					}
					catch (NumberFormatException e)
					{
						prepStmt.setInt(i, 0);
					}
				}
				if (currentType.equals("VARCHAR"))
				{
					String tmpString = (String) valueKeys.get(i - 1);
					if (tmpString.equals("") || tmpString == null)
					{
						prepStmt.setNull(i, java.sql.Types.VARCHAR);
					}
					else
					{
						prepStmt.setString(i, (String) valueKeys.get(i - 1));
					}
				}
			}
			prepStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param con
	 * @param StatsType
	 * @param valueKeys
	 * @param conf
	 *
	 * @return
	 *
	 * @throws SQLException
	 *
	 * @see
	 */
	public static void putDataStats(Connection con, String StatsType, ArrayList valueKeys, StatisticsConfig conf)
		throws SQLException
	{
		StringBuffer selectStatementBuf = new StringBuffer("SELECT ");
		StringBuffer updateStatementBuf = new StringBuffer("UPDATE ");
		String tableName = conf.getTableName(StatsType);
		String selectStatement;
		String updateStatement;
		String keyNameCurrent;
		String currentType;
		Statement stmt = null;
		ResultSet rs = null;
		boolean rowExist = false;
		boolean firstKeyInWhere = true;
		boolean STOPPUTSTAT = false;
		int k = 0;
		int countCumulKey = 0;
		int intToAdd = 0;
		long longToAdd = 0;
		PreparedStatement pstmt = null;
		updateStatementBuf.append(tableName);
		updateStatementBuf.append(" SET ");

		Collection theKeys = conf.getAllKeys(StatsType);
		Iterator iteratorKeys = theKeys.iterator();
		while (iteratorKeys.hasNext())
		{
			keyNameCurrent = (String) iteratorKeys.next();
			selectStatementBuf.append(keyNameCurrent);
			if (iteratorKeys.hasNext())
				selectStatementBuf.append(",");
			if (conf.isCumulKey(StatsType, keyNameCurrent))
			{
				updateStatementBuf.append(keyNameCurrent);
				updateStatementBuf.append("=" + keyNameCurrent + "+? ,");
			}
		}

		updateStatementBuf.deleteCharAt(updateStatementBuf.length() - 1);

		selectStatementBuf.append(" FROM " + tableName + " WHERE ");
		updateStatementBuf.append(" WHERE ");

		iteratorKeys = theKeys.iterator();
		while (iteratorKeys.hasNext())
		{
			keyNameCurrent = (String) iteratorKeys.next();
			if (!conf.isCumulKey(StatsType, keyNameCurrent))
			{
				if (!firstKeyInWhere)
				{
					selectStatementBuf.append(" AND ");
					updateStatementBuf.append(" AND ");
				}
				selectStatementBuf.append(keyNameCurrent);
				updateStatementBuf.append(keyNameCurrent);
				currentType = conf.getKeyType(StatsType, keyNameCurrent);
				if (currentType.equals("DECIMAL"))
				{
					try
					{
						new Long(((String) valueKeys.get(k)));
					}
					catch (Exception e)
					{
						STOPPUTSTAT = true;
					}

					if (((String) valueKeys.get(k)).equals("") || valueKeys.get(k) == null)
					{
						selectStatementBuf.append("=" + "NULL");
						updateStatementBuf.append("=" + "NULL");
					}
					else
					{
						selectStatementBuf.append("=" + (String) valueKeys.get(k));
						updateStatementBuf.append("=" + (String) valueKeys.get(k));
					}
				}
				if (currentType.equals("INTEGER"))
				{
					try
					{
						new Integer(((String) valueKeys.get(k)));
					}
					catch (Exception e)
					{
						STOPPUTSTAT = true;
					}

					if (((String) valueKeys.get(k)).equals("") || valueKeys.get(k) == null)
					{
						selectStatementBuf.append("=" + "NULL");
						updateStatementBuf.append("=" + "NULL");
					}
					else
					{
						selectStatementBuf.append("=" + (String) valueKeys.get(k));
						updateStatementBuf.append("=" + (String) valueKeys.get(k));
					}
				}
				if (currentType.equals("VARCHAR"))
				{
					if (((String) valueKeys.get(k)).equals("") || valueKeys.get(k) == null)
					{
						selectStatementBuf.append("=" + "NULL");
						updateStatementBuf.append("=" + "NULL");
					}
					else
					{
						selectStatementBuf.append("='" + (String) valueKeys.get(k) + "'");
						updateStatementBuf.append("='" + (String) valueKeys.get(k) + "'");
					}
				}
				firstKeyInWhere = false;
			}
			k++;
		}

		selectStatement = selectStatementBuf.toString();
		updateStatement = updateStatementBuf.toString();
		SilverTrace.info(
			"silverstatistics",
			"SilverStatisticsDAO.putDataStats",
			"root.MSG_GEN_PARAM_VALUE",
			"selectStatement=" + selectStatement);
		SilverTrace.info(
			"silverstatistics",
			"SilverStatisticsDAO.putDataStats",
			"root.MSG_GEN_PARAM_VALUE",
			"updateStatementBuf=" + updateStatementBuf);
		stmt = con.createStatement();

		try
		{
			if (STOPPUTSTAT == false)
			{
				rs = stmt.executeQuery(selectStatement);

				while (rs.next())
				{
					countCumulKey = 0;
					if (pstmt != null)
						pstmt.close();
					pstmt = con.prepareStatement(updateStatement);

					rowExist = true;
					iteratorKeys = theKeys.iterator();
					while (iteratorKeys.hasNext())
					{
						keyNameCurrent = (String) iteratorKeys.next();

						if (conf.isCumulKey(StatsType, keyNameCurrent))
						{
							countCumulKey++;
							currentType = conf.getKeyType(StatsType, keyNameCurrent);
							if (currentType.equals("INTEGER"))
							{
								try
								{
									intToAdd =
										new Integer((String) valueKeys.get(conf.indexOfKey(StatsType, keyNameCurrent)))
											.intValue();
								}
								catch (NumberFormatException e)
								{
									intToAdd = 0;
								}
								//if ((conf.getModeCumul(StatsType)).equals("Add"))
								//    pstmt.setInt(countCumulKey, rs.getInt(keyNameCurrent)+intToAdd);
								//if ((conf.getModeCumul(StatsType)).equals("Replace"))
								pstmt.setInt(countCumulKey, intToAdd);
							}
							if (currentType.equals("DECIMAL"))
							{
								try
								{
									longToAdd =
										new Long((String) valueKeys.get(conf.indexOfKey(StatsType, keyNameCurrent)))
											.longValue();
								}
								catch (NumberFormatException e)
								{
									longToAdd = 0;
								}
								//if ((conf.getModeCumul(StatsType)).equals("Add"))
								//    pstmt.setLong(countCumulKey, (rs.getLong(keyNameCurrent) + longToAdd));
								//if ((conf.getModeCumul(StatsType)).equals("Replace"))
								pstmt.setLong(countCumulKey, longToAdd);
							}
						}
					}
					pstmt.executeUpdate();
				}
			}
		}
		finally
		{
			DBUtil.close(rs, stmt);
			DBUtil.close(pstmt);
			if ((!STOPPUTSTAT) && (!rowExist))
				insertDataStats(con, StatsType, valueKeys, conf);
		}
	}

}
