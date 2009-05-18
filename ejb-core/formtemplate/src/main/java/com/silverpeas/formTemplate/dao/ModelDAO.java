package com.silverpeas.formTemplate.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class ModelDAO
{
	public static void addModel(Connection con, String instanceId, String modelId)
			throws SQLException, UtilException
	{
		// ajout d'un modèle
		PreparedStatement prepStmt = null;
		try
		{
			// création de la requete
			String query = "insert into st_instance_ModelUsed values (?,?)";
			// initialisation des paramètres
			
			prepStmt = con.prepareStatement(query);
			prepStmt.setString(1, instanceId);
			prepStmt.setString(2,modelId);
			prepStmt.executeUpdate();
		}
		finally
		{
			// fermeture
			DBUtil.close(prepStmt);
		}
	}
	
	public static void deleteModel(Connection con, String instanceId)
		throws SQLException, UtilException
	{
		// suppression de tous les modèles
		PreparedStatement prepStmt = null;
		try
		{
			// création de la requete
			String query = "delete from st_instance_ModelUsed where instanceId = ? ";
			// initialisation des paramètres
			
			prepStmt = con.prepareStatement(query);
			prepStmt.setString(1, instanceId);
			prepStmt.executeUpdate();
		}
		finally
		{
			// fermeture
			DBUtil.close(prepStmt);
		}
	}
	
	public static Collection getModelUsed(Connection con, String instanceId) throws SQLException, UtilException
	{
		ArrayList listModel = new ArrayList();
		String query = "select modelId from st_instance_ModelUsed where instanceId = ?";
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(query);
			prepStmt.setString(1, instanceId);
			rs = prepStmt.executeQuery();
			while (rs.next())
			{
				String modelId = rs.getString(1);
				listModel.add(modelId);
			}
		}
		finally
		{
			// fermeture
			DBUtil.close(rs, prepStmt);
		}
		return listModel;
	}
		
}
