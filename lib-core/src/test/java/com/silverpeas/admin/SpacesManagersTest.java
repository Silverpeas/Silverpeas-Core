/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-domains.xml", "/spring-jpa-datasource.xml"})
public class SpacesManagersTest extends AbstractTestDao {

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
  protected String getDatasetFileName() {
    return "test-spacesmanagers-dataset.xml";
  }

}