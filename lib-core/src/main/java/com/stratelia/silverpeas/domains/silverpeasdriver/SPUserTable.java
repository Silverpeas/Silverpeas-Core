package com.stratelia.silverpeas.domains.silverpeasdriver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stratelia.webactiv.organization.AdminPersistenceException;
import com.stratelia.webactiv.organization.Table;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * A UserTable object manages the DomainSP_User table.
 */
public class SPUserTable extends Table
{
   public SPUserTable(DomainSPSchema schema)
   {
       super(schema, "DomainSP_User");
       this.organization = schema;
   }

   static final private String USER_COLUMNS
      = "id,firstName,lastName,phone,homePhone,"
      + "cellPhone,fax,address,title,company,position,boss,"
      + "email, loginMail, login,password, passwordValid";

   /**
    * Fetch the current user row from a resultSet.
    */
   protected SPUserRow fetchUser(ResultSet rs)
       throws SQLException
   {
       String    toBoolean;
       SPUserRow u = new SPUserRow();

       u.id = rs.getInt(1);
       u.firstName = rs.getString(2);
       u.lastName = rs.getString(3);
       u.phone = rs.getString(4);
       u.homePhone = rs.getString(5);
       u.cellPhone = rs.getString(6);
       u.fax = rs.getString(7);
       u.address = rs.getString(8);
       u.title = rs.getString(9);
       u.company = rs.getString(10);
       u.position = rs.getString(11);
       u.boss = rs.getString(12);
       u.email = rs.getString(13);
       u.loginMail = rs.getString(14);
       u.login = rs.getString(15);
       u.password = rs.getString(16);
       toBoolean = rs.getString(17);
       if ((toBoolean != null) && (toBoolean.equalsIgnoreCase("Y")))
       {
           u.passwordValid = true;
       }
       else
       {
           u.passwordValid = false;
       }

       return u;
   }

   /**
    * Returns the User whith the given id.
    */
   public SPUserRow getUser(int id) throws AdminPersistenceException
   {
       return (SPUserRow) getUniqueRow(SELECT_USER_BY_ID, id);
   }

   static final private String SELECT_USER_BY_ID
      = "select "+USER_COLUMNS+" from DomainSP_User where id = ?";

   /**
    * Returns the User with the given login.
    */
   public SPUserRow getUserByLogin(String login) throws AdminPersistenceException
   {
       SPUserRow[] users =(SPUserRow[]) getRows(SELECT_USER_BY_LOGIN,
                                       new String[] {login})
                                    .toArray(new SPUserRow[0]);

       if (users.length == 0) return null;
       else if (users.length == 1) return users[0];
       else
       {
			throw new AdminPersistenceException("SPUserTable.getUserByLogin", SilverpeasException.ERROR, "admin.EX_ERR_LOGIN_FOUND_TWICE");
       }
   }

   static final private String SELECT_USER_BY_LOGIN
      = "select "+USER_COLUMNS+" from DomainSP_User where login = ?";

   /**
    * Returns all the Users.
    */
   public SPUserRow[] getAllUsers() throws AdminPersistenceException
   {
       return (SPUserRow[]) getRows(SELECT_ALL_USERS).toArray(new SPUserRow[0]);
   }

   static final private String SELECT_ALL_USERS
      = "select "+USER_COLUMNS+" from DomainSP_User";

   /**
    * Returns all the Users which compose a group.
    */
   public SPUserRow[] getDirectUsersOfGroup(int groupId)
       throws AdminPersistenceException
   {
       return (SPUserRow[]) getRows(SELECT_USERS_IN_GROUP, groupId)
           .toArray(new SPUserRow[0]);
   }

   static final private String SELECT_USERS_IN_GROUP
      = "select "+USER_COLUMNS
      + " from DomainSP_User,DomainSP_Group_User_Rel"
      + " where id = userId and groupId = ?";

   /**
    * Returns all the User ids which compose a group.
    */
   public String[] getDirectUserIdsOfGroup(int groupId)
       throws AdminPersistenceException
   {
       return (String[]) getIds(SELECT_USER_IDS_IN_GROUP, groupId)
           .toArray(new String[0]);
   }

   static final private String SELECT_USER_IDS_IN_GROUP
      = "select DomainSP_User.id"
      + " from DomainSP_User,DomainSP_Group_User_Rel"
      + " where id = userId and groupId = ?";

   /**
    * Returns the users
    * whose fields match those of the given sample space fields.
    */
   public SPUserRow[] getAllMatchingUsers(SPUserRow sampleUser)
       throws AdminPersistenceException
   {
       String[] columns = new String[] {
           "firstName",
           "lastName",
           "phone",
           "homePhone",
           "cellPhone",
           "fax",
           "address",
           "title",
           "company",
           "position",
		   "email",
		   "loginMail"
       };
       String[] values = new String[] {
           sampleUser.firstName,
           sampleUser.lastName,
           sampleUser.phone,
           sampleUser.homePhone,
           sampleUser.cellPhone,
           sampleUser.fax,
           sampleUser.address,
           sampleUser.title,
           sampleUser.company,
           sampleUser.position,
           sampleUser.email,
           sampleUser.loginMail
       };

       return (SPUserRow[]) getMatchingRows(USER_COLUMNS, columns, values)
       .toArray(new SPUserRow[0]);
   }

   /**
    * Inserts in the database a new user row.
    */
   public void createUser(SPUserRow user) throws AdminPersistenceException
   {
      insertRow(INSERT_USER, user);
   }

   static final private String INSERT_USER
      = "insert into DomainSP_User"
      + "("
      + " id,firstName,lastName,phone,homePhone,"
      + " cellPhone,fax,address,title,company,position,boss,"
      + " email, loginMail, login,password,passwordValid"
      + ") values ("
      + " ?,?,?,?,?,"
      + " ?,?,?,?,?,?,?,"
      + " ?,?,?,?,?"
      + ")";

   protected void prepareInsert(String insertQuery,
                                PreparedStatement insert,
                                Object row)
       throws SQLException
   {
       SPUserRow u = (SPUserRow) row;
       if (u.id == -1)
       {
          u.id = getNextId();
       }

       insert.setInt(1, u.id);
       insert.setString(2, truncate(u.firstName,100));
       insert.setString(3, truncate(u.lastName,100));
       insert.setString(4, truncate(u.phone,20));
       insert.setString(5, truncate(u.homePhone,20));
       insert.setString(6, truncate(u.cellPhone,20));
       insert.setString(7, truncate(u.fax,20));
       insert.setString(8, truncate(u.address,500));
       insert.setString(9, truncate(u.title,100));
       insert.setString(10, truncate(u.company,100));
       insert.setString(11, truncate(u.position,100));
       insert.setString(12, truncate(u.boss,100));
       insert.setString(13, truncate(u.email,100));
       insert.setString(14, truncate(u.loginMail,100));
       insert.setString(15, truncate(u.login,50));
       insert.setString(16, truncate(u.password,32));
       if (u.passwordValid)
       {
           insert.setString(17, "Y");
       }
       else
       {
           insert.setString(17, "N");
       }
   }

   /**
    * Update a user row.
    */
   public void updateUserDetailOnly(SPUserRow user) throws AdminPersistenceException
   {
        PreparedStatement statement = null;

       try
       {
          statement = this.organization.getStatement(UPDATE_USER_DETAIL_ONLY);
          synchronized (statement)
          {
              statement.setString(1, truncate(user.firstName,100));
              statement.setString(2, truncate(user.lastName,100));
              statement.setString(3, truncate(user.email,100));
              statement.setString(4, truncate(user.login,50));
              statement.setInt(5, user.id);
              statement.executeUpdate();
          }
       }
       catch (SQLException e)
       {
 		  throw new AdminPersistenceException("SPUserTable.updateUserDetailOnly", SilverpeasException.ERROR, "admin.EX_ERR_UPDATE", e);
       }
       finally
       {
            organization.releaseStatement(statement);
       }
   }

   static final private String UPDATE_USER_DETAIL_ONLY
      = "update DomainSP_User set"
      + " firstName = ?,"
      + " lastName = ?,"
      + " email = ?,"
      + " login = ?"
      + " where id = ?";

   /**
    * Update a user row.
    */
   public void updateUser(SPUserRow user) throws AdminPersistenceException
   {
      updateRow(UPDATE_USER, user);
   }

   static final private String UPDATE_USER
      = "update DomainSP_User set"
      + " firstName = ?,"
      + " lastName = ?,"
      + " phone = ?,"
      + " homePhone = ?,"
      + " cellPhone = ?,"
      + " fax = ?,"
      + " address = ?,"
      + " title = ?,"
      + " company = ?,"
      + " position = ?,"
      + " boss = ?,"
      + " email = ?,"
      + " loginMail = ?,"
      + " login = ?,"
      + " password = ?,"
      + " passwordValid = ?"
      + " where id = ?";

   protected void prepareUpdate(String updateQuery,
                                PreparedStatement update,
                                Object row)
       throws SQLException
   {
       SPUserRow u = (SPUserRow) row;

       update.setString(1, truncate(u.firstName,100));
       update.setString(2, truncate(u.lastName,100));
       update.setString(3, truncate(u.phone,20));
       update.setString(4, truncate(u.homePhone,20));
       update.setString(5, truncate(u.cellPhone,20));
       update.setString(6, truncate(u.fax,20));
       update.setString(7, truncate(u.address,500));
       update.setString(8, truncate(u.title,100));
       update.setString(9, truncate(u.company,100));
       update.setString(10, truncate(u.position,100));
       update.setString(11, truncate(u.boss,100));
       update.setString(12, truncate(u.email,100));
       update.setString(13, truncate(u.loginMail,100));
       update.setString(14, truncate(u.login,50));
       update.setString(15, truncate(u.password,32));
       if (u.passwordValid)
       {
           update.setString(16, "Y");
       }
       else
       {
           update.setString(16, "N");
       }

       update.setInt(17, u.id);
   }

   /**
    * Removes a user row.
    */
   public void removeUser(int id) throws AdminPersistenceException
   {

       SPUserRow user = getUser(id);
       if (user == null) return;

       SPGroupRow[] groups = organization.group.getDirectGroupsOfUser(id);
       for (int i=0; i<groups.length ; i++)
       {
           organization.group.removeUserFromGroup(id, groups[i].id);
       }

	   updateRelation(DELETE_USER, id);
   }

   static final private String DELETE_USER
      = "delete from DomainSP_User where id = ?";

   /**
    * Fetch the current user row from a resultSet.
    */
   protected Object fetchRow(ResultSet rs)
       throws SQLException
   {
      return fetchUser(rs);
   }

   private DomainSPSchema organization = null;
}
