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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.stratelia.webactiv.organization;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

public class UserFavoriteSpaceDAOImplTest extends JndiBasedDBTestCase {

  private String jndiName = "";

  protected void setUp() throws Exception {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.fscontext.RefFSContextFactory");
    InitialContext ic = new InitialContext(env);
    Properties props = new Properties();
    props.load(UserFavoriteSpaceDAOImplTest.class.getClassLoader().getResourceAsStream(
        "jdbc.properties"));
    // Construct BasicDataSource reference
    Reference ref = new Reference("javax.sql.DataSource",
        "org.apache.commons.dbcp.BasicDataSourceFactory", null);
    ref.add(new StringRefAddr("driverClassName", props
        .getProperty("driverClassName")));
    ref.add(new StringRefAddr("url", props.getProperty("url")));
    ref.add(new StringRefAddr("username", props.getProperty("username")));
    ref.add(new StringRefAddr("password", props.getProperty("password")));
    ref.add(new StringRefAddr("maxActive", "4"));
    ref.add(new StringRefAddr("maxWait", "5000"));
    ref.add(new StringRefAddr("removeAbandoned", "true"));
    ref.add(new StringRefAddr("removeAbandonedTimeout", "5000"));
    ic.rebind(props.getProperty("jndi.name"), ref);
    jndiName = props.getProperty("jndi.name");
  }

  protected String getLookupName() {
    return jndiName;
  }

  protected Properties getJNDIProperties() {
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.fscontext.RefFSContextFactory");
    return env;
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        UserFavoriteSpaceDAOImplTest.class
        .getResourceAsStream("test-favoritespace-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  public void testFillDb() {
    IDatabaseConnection connection = null;
    try {
      connection = getDatabaseTester().getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Test
  public void testGetListUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(1, listUFS.size());
  }

  @Test
  public void testAddUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    UserFavoriteSpaceVO ufsVO = new UserFavoriteSpaceVO(0, 2);
    boolean result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(true, result);

    // Check the new records inside database
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(2, listUFS.size());
    
    // Check database constraint on existing userid and space id
    ufsVO = new UserFavoriteSpaceVO(10, 10);
    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(false, result);
    
    // Check default userFavoriteSpaceVO 
    ufsVO = new UserFavoriteSpaceVO();
    assertEquals(-1, ufsVO.getSpaceId());
    assertEquals(-1, ufsVO.getUserId());
    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(false, result);
  }
  

  @Test
  public void testRemoveUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    UserFavoriteSpaceVO ufsVO = new UserFavoriteSpaceVO(0, 2);
    boolean result = ufsDAO.removeUserFavoriteSpace(ufsVO);
    assertEquals(true, result);

    // Check result
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(1, listUFS.size());

    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(true, result);

    // Delete all favorite space of current user
    ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(0,-1));
    listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(0, listUFS.size());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /*
   * @AfterClass public static void oneTimeTearDown() { // one-time cleanup code }
   */

}
