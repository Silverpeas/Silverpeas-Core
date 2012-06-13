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

import com.google.common.base.Charsets;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;
import com.silverpeas.util.PathTestUtil;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.Group;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opends.messages.Message;
import org.opends.server.admin.std.server.BackendCfg;
import org.opends.server.api.Backend;
import org.opends.server.config.ConfigException;
import org.opends.server.core.LockFileManager;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.tools.BackendToolUtils;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.InitializationException;
import org.opends.server.types.LDIFImportConfig;
import org.opends.server.util.EmbeddedUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LDAPGroupAllRootTest {
  
  private static final LDAPSettings settings = new LDAPSettings();
  
  public static void startLdapServer() throws InitializationException, ConfigException,
      FileNotFoundException, DirectoryException {
    File directoryServerRoot = new File(PathTestUtil.TARGET_DIR + "test-classes"
        + File.separatorChar + "opendj");
    new File(directoryServerRoot, "db").mkdir();
    new File(directoryServerRoot, "locks").mkdir();
    new File(directoryServerRoot, "logs").mkdir();
    DirectoryEnvironmentConfig envConfig = new DirectoryEnvironmentConfig();
    envConfig.setServerRoot(directoryServerRoot);
    envConfig.setConfigFile(new File(PathTestUtil.TARGET_DIR + "test-classes" + File.separatorChar
        + "opendj" + File.separatorChar + "config" + File.separatorChar + "config.ldif"));
    envConfig.setDisableConnectionHandlers(false);
    envConfig.setMaintainConfigArchive(false);
    // Start the OpenDS server.
    if (EmbeddedUtils.isRunning()) {
      return;
    } else {
      EmbeddedUtils.startServer(envConfig);
    }
    // Get an internal, root connection to the OpenDS instance.
    InternalClientConnection internalConnection = InternalClientConnection.getRootConnection();
    if (internalConnection == null) {
      System.out.println("OpenDJ cannot get internal connection (null)");
      throw new RuntimeException("OpenDS cannot get internal connection (null)");
    }
  }
  
  public static void loadLdifs() throws InitializationException, ConfigException,
      FileNotFoundException, DirectoryException {
    
    LDIFImportConfig importConfig = new LDIFImportConfig(new FileInputStream(new File(
        PathTestUtil.TARGET_DIR + "test-classes", "silverpeas-ldap.ldif")));
    importConfig.setAppendToExistingData(true);
    // importConfig.setReplaceExistingEntries(true);
    importConfig.setCompressed(false);
    //importConfig.setClearBackend(true);
    importConfig.setEncrypted(false);
    importConfig.setValidateSchema(false);
    importConfig.setSkipDNValidation(false);
    ArrayList<Backend> backendList = new ArrayList<Backend>();
    ArrayList<BackendCfg> entryList = new ArrayList<BackendCfg>();
    ArrayList<List<DN>> dnList = new ArrayList<List<DN>>();
    BackendToolUtils.getBackends(backendList, entryList, dnList);
    Backend backend = null;
    for (Backend b : backendList) {
      if ("userRoot".equals(b.getBackendID())) {
        backend = b;
        break;
      }
    }
    if (backend != null) {
      String lockFile = LockFileManager.getBackendLockFileName(backend);
      StringBuilder failureReason = new StringBuilder();
      if (!LockFileManager.acquireExclusiveLock(lockFile, failureReason)) {
        throw new RuntimeException("OpenDS cannot get lock the backend " + backend.getBackendID()
            + " " + failureReason);
      }
      backend.importLDIF(importConfig);
      lockFile = LockFileManager.getBackendLockFileName(backend);
      failureReason = new StringBuilder();
      if (!LockFileManager.releaseLock(lockFile, failureReason)) {
        throw new RuntimeException("OpenDS cannot release the lock the backend " + backend.
            getBackendID() + " " + failureReason);
      }
    }
    startLdapServer();
  }
  private String connectionId;
  private LDAPGroupAllRoot instance = new LDAPGroupAllRoot();
  
  public LDAPGroupAllRootTest() {
  }
  
  @BeforeClass
  public static void prepareSettings() throws Exception {
    loadLdifs();
    
    settings.LDAPHost = "localhost";
    settings.LDAPPort = 1389;
    settings.LDAPSearchRecurs = true;
    settings.LDAPUserBaseDN = "dc=silverpeas,dc=org";
    settings.LDAPAccessLoginDN = "cn=Directory Manager,cn=Root DNs,cn=config";
    settings.LDAPAccessPasswd = "password";
    settings.LDAPOpAttributesUsed = true;
    
    settings.usersClassName = "person";
    settings.usersFilter = "";
    settings.usersIdField = "uid";
    settings.usersLoginField = "uid";
    settings.usersFirstNameField = "givenName";
    settings.usersLastNameField = "sn";
    settings.usersEmailField = "mail";
    
    settings.groupsClassName = "groupOfUniqueNames";
    settings.groupsFilter = "(uniqueMember=*)";
    settings.groupsIdField = "entryUUID";
    settings.groupsType = "com.stratelia.silverpeas.domains.ldapdriver.LDAPGroupAllRoot";
    settings.groupsMemberField = "uniqueMember";
    settings.groupsSpecificGroupsBaseDN = "dc=silverpeas,dc=org";
    settings.groupsInheritProfiles = false;
    settings.groupsNamingDepth = 2;
    settings.groupsIncludeEmptyGroups = true;
    settings.groupsNameField = "cn";
    settings.groupsDescriptionField = "description";
    
    
    
    settings.LDAPDefaultSearchConstraints = settings.getSearchConstraints(true);
    settings.LDAPDefaultConstraints = settings.getConstraints(true);
  }
  
  @Before
  public void prepareConnection() throws Exception {
    connectionId = LDAPUtility.openConnection(settings);
    LDAPSynchroCache cache = new LDAPSynchroCache();
    cache.init(settings);
    instance.init(settings, cache);
  }
  
  @After
  public void closeConnection() throws AdminException {
    LDAPUtility.closeConnection(connectionId);
    
  }
  
  @AfterClass
  public static void stopLdapServer() {
    if (EmbeddedUtils.isRunning()) {
      EmbeddedUtils.stopServer(LDAPGroupAllRootTest.class.getName(), Message.EMPTY);
    }
  }

  /**
   * Test of getMemberGroupIds method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetMemberGroupIds() throws Exception {
    String userId = "user.1";
    boolean isGroup = false;
    List<String> result = instance.getMemberGroupIds(connectionId, userId, isGroup);
    assertThat(result, is(not(nullValue())));
    assertThat(result, hasSize(1));
    assertThat(result.get(0), is("a95b39de-ea91-45cb-9af0-890670075d54"));
  }

  /**
   * Test of getGroupMemberGroupIds method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetGroupMemberGroupIds() throws Exception {
    String groupId = "a95b39de-ea91-45cb-9af0-890670075d54";
    String[] result = instance.getGroupMemberGroupIds(connectionId, groupId);
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(0));
  }

  /**
   * Test of getUserMemberGroupIds method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetUserMemberGroupIds() throws Exception {
    String userId = "user.9";
    String[] result = instance.getUserMemberGroupIds(connectionId, userId);
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(0));
    
    userId = "user.1";
    result = instance.getUserMemberGroupIds(connectionId, userId);
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(1));
    assertThat(result, arrayContaining("a95b39de-ea91-45cb-9af0-890670075d54"));
  }

  /**
   * Test of getUserIds method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetUserIds() throws Exception {
    LDAPEntry groupEntry = mock(LDAPEntry.class);
    LDAPAttribute uuidAttribute = mock(LDAPAttribute.class);
    when(uuidAttribute.getName()).thenReturn("entryUUID");
    when(uuidAttribute.getStringValue()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54");
    when(uuidAttribute.getStringValueArray()).thenReturn(new String[]{
          "a95b39de-ea91-45cb-9af0-890670075d54"});
    when(uuidAttribute.getByteValue()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54".getBytes(
        Charsets.UTF_8));
    when(uuidAttribute.size()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54".getBytes(
        Charsets.UTF_8).length);
    
    LDAPAttribute uniqueMembers = mock(LDAPAttribute.class);
    when(uniqueMembers.getName()).thenReturn("uniqueMember");
    when(uniqueMembers.getStringValueArray()).thenReturn(new String[]{
          "uid=user.0,ou=People,dc=silverpeas,dc=org",
          "uid=user.1,ou=People,dc=silverpeas,dc=org",
          "uid=user.2,ou=People,dc=silverpeas,dc=org",
          "uid=user.3,ou=People,dc=silverpeas,dc=org",
          "uid=user.4,ou=People,dc=silverpeas,dc=org"
        });
    when(groupEntry.getAttribute("entryUUID")).thenReturn(uuidAttribute);
    when(groupEntry.getAttribute("uniqueMember")).thenReturn(uniqueMembers);
    
    String[] result = instance.getUserIds(connectionId, groupEntry);
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(5));
    assertThat(result, arrayContainingInAnyOrder("user.0", "user.1", "user.2", "user.3", "user.4"));
  }

  /**
   * Test of getTRUEUserIds method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetTRUEUserIds() throws Exception {
    LDAPEntry groupEntry = mock(LDAPEntry.class);
    LDAPAttribute uuidAttribute = mock(LDAPAttribute.class);
    when(uuidAttribute.getName()).thenReturn("entryUUID");
    when(uuidAttribute.getStringValue()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54");
    when(uuidAttribute.getStringValueArray()).thenReturn(new String[]{
          "a95b39de-ea91-45cb-9af0-890670075d54"});
    when(uuidAttribute.getByteValue()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54".getBytes(
        Charsets.UTF_8));
    when(uuidAttribute.size()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54".getBytes(
        Charsets.UTF_8).length);
    
    LDAPAttribute uniqueMembers = mock(LDAPAttribute.class);
    when(uniqueMembers.getName()).thenReturn("uniqueMember");
    when(uniqueMembers.getStringValueArray()).thenReturn(new String[]{
          "uid=user.0,ou=People,dc=silverpeas,dc=org",
          "uid=user.1,ou=People,dc=silverpeas,dc=org",
          "uid=user.2,ou=People,dc=silverpeas,dc=org",
          "uid=user.3,ou=People,dc=silverpeas,dc=org",
          "uid=user.4,ou=People,dc=silverpeas,dc=org"
        });
    when(groupEntry.getAttribute("entryUUID")).thenReturn(uuidAttribute);
    when(groupEntry.getAttribute("uniqueMember")).thenReturn(uniqueMembers);
    List<String> result = instance.getTRUEUserIds(connectionId, groupEntry);
    assertThat(result, is(not(nullValue())));
    assertThat(result, hasSize(5));
    assertThat(result, containsInAnyOrder("user.0", "user.1", "user.2", "user.3", "user.4"));
  }

  /**
   * Test of getChildGroupsEntry method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetChildGroupsEntry() throws Exception {
    LDAPEntry[] result = instance.getChildGroupsEntry(connectionId,
        "a95b39de-ea91-45cb-9af0-890670075d54", "");
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(0));
    result = instance.getChildGroupsEntry(connectionId, "", "");
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(1));
    assertThat(result[0].getDN(), is("cn=Groupe 1,dc=silverpeas,dc=org"));
  }

  /**
   * Test of getTRUEChildGroupsEntry method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetTRUEChildGroupsEntry() {
    LDAPEntry groupEntry = mock(LDAPEntry.class);
    LDAPAttribute uuidAttribute = mock(LDAPAttribute.class);
    when(uuidAttribute.getName()).thenReturn("entryUUID");
    when(uuidAttribute.getStringValue()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54");
    when(uuidAttribute.getStringValueArray()).thenReturn(new String[]{
          "a95b39de-ea91-45cb-9af0-890670075d54"});
    when(uuidAttribute.getByteValue()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54".getBytes(
        Charsets.UTF_8));
    when(uuidAttribute.size()).thenReturn("a95b39de-ea91-45cb-9af0-890670075d54".getBytes(
        Charsets.UTF_8).length);
    
    LDAPAttribute uniqueMembers = mock(LDAPAttribute.class);
    when(uniqueMembers.getName()).thenReturn("uniqueMember");
    when(uniqueMembers.getStringValueArray()).thenReturn(new String[]{
          "uid=user.0,ou=People,dc=silverpeas,dc=org",
          "uid=user.1,ou=People,dc=silverpeas,dc=org",
          "uid=user.2,ou=People,dc=silverpeas,dc=org",
          "uid=user.3,ou=People,dc=silverpeas,dc=org",
          "uid=user.4,ou=People,dc=silverpeas,dc=org"
        });
    when(groupEntry.getAttribute("entryUUID")).thenReturn(uuidAttribute);
    when(groupEntry.getAttribute("uniqueMember")).thenReturn(uniqueMembers);
    when(groupEntry.getDN()).thenReturn("cn=Groupe 1,dc=silverpeas,dc=org");
    
    List<LDAPEntry> result = instance.getTRUEChildGroupsEntry(connectionId, groupEntry);
    assertThat(result, is(not(nullValue())));
    assertThat(result, hasSize(0));
  }

  /**
   * Test of getAllChangedGroups method, of class LDAPGroupAllRoot.
   */
  @Test
  public void testGetAllChangedGroups() throws Exception {
    String extraFilter = "";
    Group[] result = instance.getAllChangedGroups(connectionId, extraFilter);
    assertThat(result, is(not(nullValue())));
    assertThat(result, arrayWithSize(1));
    assertThat(result[0].getSpecificId(), is("a95b39de-ea91-45cb-9af0-890670075d54"));
    assertThat(result[0].getName(), is("Groupe 1"));
    assertThat(result[0].getDescription(), is("Description du premier groupe"));
    assertThat(result[0].getDomainId(), is(nullValue()));
    assertThat(result[0].getId(), is(nullValue()));
    assertThat(result[0].getRule(), is(nullValue()));
    assertThat(result[0].getUserIds(), is(not(nullValue())));
    assertThat(result[0].getUserIds(), arrayWithSize(5));
  }
}
