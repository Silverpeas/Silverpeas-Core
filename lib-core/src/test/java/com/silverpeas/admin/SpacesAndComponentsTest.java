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

import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;

public class SpacesAndComponentsTest extends JndiBasedDBTestCase {

  private String jndiName = "";

  @Override
  protected void setUp() throws Exception {
    prepareJndi();
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
    InitialContext ic = new InitialContext(env);
    Properties props = new Properties();
    props.load(SpacesAndComponentsTest.class.getClassLoader()
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
        SpacesAndComponentsTest.class
        .getResourceAsStream("test-spacesandcomponents-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  private AdminController getAdminController() {
    return new AdminController("1");
  }

  @Test
  public void testAddSpace() {
    AdminController ac = getAdminController();
    
    // test space creation
    SpaceInst space = new SpaceInst();
    space.setCreatorUserId("1");
    space.setName("Space 2");
    String spaceId = ac.addSpaceInst(space);
    assertNotNull(spaceId);
    assertEquals("WA3", spaceId);

    // test subspace creation
    SpaceInst subspace = new SpaceInst();
    subspace.setCreatorUserId("1");
    subspace.setName("Space 2 - 1");
    subspace.setDomainFatherId("3");
    String subSpaceId = ac.addSpaceInst(subspace);
    assertNotNull(subSpaceId);
    assertEquals("WA4", subSpaceId);
    
    //test subspace of root space
    String[] subSpaceIds = ac.getAllSubSpaceIds("WA3");
    assertEquals(1, subSpaceIds.length);
    
    //test level calculation
    subspace = ac.getSpaceInstById("WA4");
    assertEquals(1, subspace.getLevel());
  }

  @Test
  public void testUpdateSpace() {
    AdminController ac = getAdminController();
    SpaceInst space = ac.getSpaceInstById("WA1");
    String desc = "New description";
    space.setDescription(desc);
    ac.updateSpaceInst(space);
    
    space = ac.getSpaceInstById("1");
    assertEquals(desc, space.getDescription());
  }

  @Test
  public void testDeleteSpace() {
    AdminController ac = getAdminController();
    
    ac.deleteSpaceInstById("WA1", false);
    
    SpaceInst space = ac.getSpaceInstById("WA1");
    assertEquals("R", space.getStatus());
    
    ac.deleteSpaceInstById("WA1", true);
    space = ac.getSpaceInstById("WA1");
    assertNull(space);
  }

  @Test
  public void testAddComponent() {
    /*AdminController ac = getAdminController();
    
    ComponentInst component = new ComponentInst();
    component.setCreatorUserId("1");
    component.setDomainFatherId("WA2");
    component.setLabel("Mon nouveau composant");
    component.setName("kmelia");
    
    String id = ac.addComponentInst(component);
    assertEquals("kmelia2", id);*/
  }

  @Test
  public void testUpdateComponent() {
    AdminController ac = getAdminController();
    
    ComponentInst component = ac.getComponentInst("kmelia1");
    String desc = "New description";
    component.setDescription(desc);
    ac.updateComponentInst(component);
    
    component = ac.getComponentInst("kmelia1");
    assertEquals(desc, component.getDescription());
  }

  @Test
  public void testDeleteComponent() {
    AdminController ac = getAdminController();
    
    ac.deleteComponentInst("kmelia1", false);
    
    ComponentInst component = ac.getComponentInst("kmelia1");
    assertEquals("R", component.getStatus());
    
    ac.deleteComponentInst("kmelia1", true);
    component = ac.getComponentInst("kmelia1");
    assertEquals("", component.getName());
  }
  
  @Test
  public void testProfileInheritance()
  {
    AdminController ac = getAdminController();
    
    //set space profile (admin)
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA1");
    profile.setName("admin");
    profile.addUser("1");
    String profileId = ac.addSpaceProfileInst(profile, "1");
    assertEquals("1", profileId);
    
    //test inheritance
    assertEquals(true, ac.isComponentAvailable("almanach2", "1"));
    
    //test if subspace is available
    /*String[] subSpaceIds = ac.getAllSubSpaceIds("WA1", "1");
    assertEquals(1, subSpaceIds.length);*/
    
    //remove user from space profile
    profile = ac.getSpaceProfileInst(profileId);
    profile.removeAllUsers();
    ac.updateSpaceProfileInst(profile, "1");
    
    //test inheritance
    assertEquals(false, ac.isComponentAvailable("almanach2", "1"));
  }

  @Test
  public void testSpaceManager() throws AdminException {
    AdminController ac = getAdminController();
    
    //set user1 as space manager
    SpaceProfileInst profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName("Manager");
    profile.addUser("1");
    String profileId = ac.addSpaceProfileInst(profile, "1");
    assertEquals("1", profileId);
    
    //set user2 as simple reader on space
    profile = new SpaceProfileInst();
    profile.setSpaceFatherId("WA2");
    profile.setName("reader");
    profile.addUser("2");
    profileId = ac.addSpaceProfileInst(profile, "1");
    assertEquals("2", profileId);
    
    //test if user1 is manager of at least one space
    Admin admin = new Admin();
    String[] managerIds = admin.getUserManageableSpaceIds("1");
    assertEquals(1, managerIds.length);
    
    //test if user2 cannot manage spaces
    managerIds = admin.getUserManageableSpaceIds("2");
    assertEquals(0, managerIds.length);
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

}