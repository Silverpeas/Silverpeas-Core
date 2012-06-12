/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.domains.ldapdriver;

import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.*;
import org.silverpeas.ldap.CreateLdapServer;
import org.silverpeas.ldap.OpenDJRule;

@CreateLdapServer(ldifConfig = "opendj/config/config.ldif", serverHome = "opendj", ldifFile =
"silverpeas-ldap.ldif")
public class LDAPDriverTest {

  @ClassRule
  public static OpenDJRule ldapRule = new OpenDJRule();
  private static ResourceLocator settings;
  private String connectionId;
  private LDAPDriver driver = new LDAPDriver();

  public LDAPDriverTest() {
  }

  @BeforeClass
  public static void prepareSettings() throws Exception {
    settings = new ResourceLocator("com.stratelia.silverpeas.domains.domainLDAP", "");
  }

  @Before
  public void prepareConnection() throws Exception {
    driver.initFromProperties(settings);
    connectionId = LDAPUtility.openConnection(driver.driverSettings);
  }

  @After
  public void closeConnection() throws AdminException {
    LDAPUtility.closeConnection(connectionId);
  }

  @Test
  public void getAllUsers() throws Exception {
    List<String> userNames = Arrays.asList(new String[]{"Nicolas", "Aaren", "Aarika", "Aaron",
              "Aartjan", "Abagael", "Abagail",
              "Abahri", "Abbas", "Abbe"});
    UserDetail[] users = driver.getAllUsers();
    assertThat(users, notNullValue());
    assertThat(users.length, is(10));
    for (UserDetail aUser : users) {
      assertThat(userNames.contains(aUser.getFirstName()), is(true));
    }
  }

  @Test
  public void getAllGroups() throws Exception {
    Group[] groups = driver.getAllGroups();
    assertThat(groups, notNullValue());
    assertThat(groups.length, is(1));
    assertThat(groups[0].getName(), is("Groupe 1"));
    assertThat(groups[0].getDescription(), is("Description du premier groupe"));
  }

  @Test
  public void getAUser() throws Exception {
    UserDetail user = driver.getUser("user.7");
    assertThat(user, notNullValue());
    assertThat(user.getFirstName(), is("Abahri"));
    assertThat(user.getLastName(), is("Abazari"));
  }

  @Test
  public void getAGroup() throws Exception {
    Group group = driver.getGroup("a95b39de-ea91-45cb-9af0-890670075d54");
    assertThat(group, notNullValue());
    assertThat(group.getName(), is("Groupe 1"));
    assertThat(group.getDescription(), is("Description du premier groupe"));
  }
}
