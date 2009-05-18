package com.stratelia.webactiv.organization;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A KeyStoreTable object manages the ST_KeyStore table.
 */

public class KeyStoreTable extends Table
{
	public KeyStoreTable(OrganizationSchema organization)
	{
		super(organization, "ST_KeyStore");
	}

	static final private String KEYSTORE_COLUMNS
      = "userKey, login, domainId";

   /**
    * Fetch the current keyStore row from a resultSet.
    */
   protected KeyStoreRow fetchKeyStore(ResultSet rs)
       throws SQLException
   {
       KeyStoreRow k = new KeyStoreRow();

       k.key = rs.getInt(1);
       k.login = rs.getString(2);
       k.domainId = rs.getInt(3);

       return k;
   }

	/**
    * Get a keystore record by userKey
    */
	public KeyStoreRow getRecordByKey(int nKey)
       throws AdminPersistenceException
	{
		return (KeyStoreRow) getUniqueRow(SELECT_RECORD_BY_KEY, nKey);
	}

   static final private String SELECT_RECORD_BY_KEY
      = "select " + KEYSTORE_COLUMNS + " from ST_KeyStore where userKey = ?";

	/**
	* Remove a keystore record with the given key
	*/
	public void removeKeyStoreRecord(int nKey)
       throws AdminPersistenceException
	{
       updateRelation(DELETE_RECORD, nKey);
	}

	static final private String DELETE_RECORD
		= "delete from ST_KeyStore where userKey = ?";

   /**
    * Fetch the current accessLevel row from a resultSet.
    */
   protected Object fetchRow(ResultSet rs)
       throws SQLException
   {
      return fetchKeyStore(rs);
   }

   /**
    * update a KeyStore
    */
   protected void prepareUpdate(String updateQuery,
                                PreparedStatement update,
                                Object row)
   {
		// not implemented
   }

   /**
    * insert a KeyStore
    */
   protected void prepareInsert(String insertQuery,
                                PreparedStatement insert,
                                Object row)
   {
		// not implemented
   }

}