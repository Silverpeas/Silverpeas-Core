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

package com.silverpeas.admin;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.dbunit.JndiBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;

public class SpacesManagersTest extends JndiBasedDBTestCase {

  private String jndiName = "";

  @Override
  protected void setUp() throws Exception {
    prepareJndi();
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
    InitialContext ic = new InitialContext(env);
    Properties props = new Properties();
    props.load(SpacesManagersTest.class.getClassLoader()
        .getResourceAsStream("jdbc.properties"));
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
    super.setUp();
  }

  /**
   * Creates the directory for JNDI files System provider
   * @throws IOException
   */
  protected void prepareJndi() throws IOException {
    Properties jndiProperties = new Properties();
    jndiProperties.load(JndiBasedDBTestCase.class.getClassLoader().getResourceAsStream(
        "jndi.properties"));
    String jndiDirectoryPath = jndiProperties.getProperty(Context.PROVIDER_URL).substring(7);
    File jndiDirectory = new File(jndiDirectoryPath);
    if (!jndiDirectory.exists()) {
      jndiDirectory.mkdirs();
      jndiDirectory.mkdir();
    }
  }

  @Override
  protected String getLookupName() {
    return jndiName;
  }

  @Override
  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        SpacesManagersTest.class
        .getResourceAsStream("test-spacesmanagers-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  private AdminController getAdminController() {
    AdminController ac = new AdminController("1");
    ac.reloadAdminCache();
    return ac;
  }

  @Test
  public void testAddRemoveSpaceManager() {
    AdminController ac = getAdminController();
    
    //add profile
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName("Manager");
    profile.addUser("2");
    String profileId = ac.addSpaceProfileInst(profile, "1");
    assertEquals("4", profileId);
    
    //check manager
    profile = ac.getSpaceProfileInst("4");
    assertEquals(1, profile.getNumUser());
    
    //remove manager
    profile.getAllUsers().clear();
    ac.updateSpaceProfileInst(profile, "1");
    profile = ac.getSpaceProfileInst("4");
    assertEquals(0, profile.getNumUser());
  }
  
  @Test
  public void testGetManageableSpaces() {
    AdminController ac = getAdminController();
    
    String[] spaceIds = ac.getUserManageableSpaceIds("1");
    assertEquals(2, spaceIds.length);
    
    spaceIds = ac.getUserManageableSpaceIds("2");
    assertEquals(4, spaceIds.length);
    
    spaceIds = ac.getUserManageableSubSpaceIds("1", "WA1");
    assertNotNull(spaceIds);
    assertEquals(2, spaceIds.length);
    
    spaceIds = ac.getUserManageableSubSpaceIds("2", "WA1");
    assertEquals(2, spaceIds.length);
    
    spaceIds = ac.getUserManageableSubSpaceIds("2", "WA2");
    assertEquals(1, spaceIds.length);
    assertEquals("3", spaceIds[0]);
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

}