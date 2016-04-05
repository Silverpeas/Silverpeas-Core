/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opends.messages.Message;
import org.opends.server.admin.std.server.BackendCfg;
import org.opends.server.api.Backend;
import org.opends.server.backends.MemoryBackend;
import org.opends.server.config.ConfigException;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.LockFileManager;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.tools.BackendToolUtils;
import org.opends.server.types.DN;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.Entry;
import org.opends.server.types.InitializationException;
import org.opends.server.types.LDIFImportConfig;
import org.opends.server.types.LDIFImportResult;
import org.opends.server.util.EmbeddedUtils;
import org.opends.server.util.StaticUtils;
import org.silverpeas.core.security.authentication.exception.AuthenticationBadCredentialException;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class LDAPDriverTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(LDAPDriverTest.class).addLDAPFeatures()
        .addAdministrationFeatures().addSilverpeasExceptionBases().testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.admin.domain.driver.ldapdriver");
          warBuilder
              .addClasses(AuthenticationBadCredentialException.class, AuthenticationException.class,
                  LdapConfiguration.class, TokenException.class, FieldTemplate.class, Field.class,
                  TokenRuntimeException.class);
          warBuilder.addAsResource("org/silverpeas/domains/domainLDAP.properties");
        }).build();
  }

  private String connectionId;
  private LDAPDriver driver = new LDAPDriver();

  public LDAPDriverTest() {
  }

  @Before
  //@RunAsClient
  public void prepareConnection() throws Exception {
    final String BASE_PATH = getLDAPServerPath();
    final String LDIF_CONFIG_FILE = BASE_PATH + "/opendj/config/config.ldif";
    final String SERVER_HOME = BASE_PATH + "/opendj";
    final String LDIFF_FILE = BASE_PATH + "/opendj/silverpeas-ldap.ldif";
    final String BACKEND_ID = "silverpeas";
    final String BACKEND_DN = "dc=silverpeas,dc=org";

    try {
      startLdapServer(SERVER_HOME, LDIF_CONFIG_FILE);
      loadLdif(LDIFF_FILE, BACKEND_ID, DN.decode(BACKEND_DN));
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException("Could'nt start LDAP Server", ex);
    }
    driver.init(0, "org.silverpeas.domains.domainLDAP", null);
    connectionId = LDAPUtility.openConnection(driver.driverSettings);
  }


  private String getLDAPServerPath() {
    return mavenTargetDirectoryRule.getResourceTestDirFile().getAbsolutePath();
  }


  @After
  //@RunAsClient
  public void closeConnection() throws AdminException {
    try {
      LDAPUtility.closeConnection(connectionId);
    } finally {
      stopLdapServer();
    }
  }

  @Test
  public void getAllUsers() throws Exception {
    List<String> userNames = Arrays.asList(
        new String[]{"Nicolas", "Aaren", "Aarika", "Aaron", "Aartjan", "Abagael", "Abagail",
            "Abahri", "Abbas", "Abbe"});
    UserDetail[] users = driver.getAllUsers();
    assertThat(users, notNullValue());
    assertThat(users, arrayWithSize(10));
    for (UserDetail aUser : users) {
      assertThat(userNames, hasItem(aUser.getFirstName()));
    }
  }

  @Test
  public void getAllGroups() throws Exception {
    Group[] groups = driver.getAllGroups();
    assertThat(groups, notNullValue());
    assertThat(groups, arrayWithSize(3));
    List<String> groupNames = Arrays.asList(new String[]{"Groupe 1", "Groupe 2", "Groupe 3"});
    List<String> groupDescriptions = Arrays.asList(
        new String[]{"Description du premier groupe", "Description du second groupe",
            "Description du troisème groupe"});
    for (Group group : groups) {
      assertThat(groupNames, hasItem(group.getName()));
      assertThat(groupDescriptions, hasItem(group.getDescription()));
    }
  }

  @Test
  public void getAUser() throws Exception {
    UserDetail user = driver.getUser("user.7");
    assertThat(user, notNullValue());
    assertThat(user.getFirstName(), is("Abahri"));
    assertThat(user.getLastName(), is("Abazari"));
  }

  @Test
  public void getAUserFull() throws Exception {
    UserFull user = driver.getUserFull("user.7");
    assertThat(user, notNullValue());
    assertThat(user.getFirstName(), is("Abahri"));
    assertThat(user.getLastName(), is("Abazari"));
    assertThat(user.getValue("city"), is("Hattiesburg"));
  }

  @Test
  public void getAGroup() throws Exception {
    Group group = driver.getGroup("a95b39de-ea91-45cb-9af0-890670075d54");
    assertThat(group, notNullValue());
    assertThat(group.getName(), is("Groupe 1"));
    assertThat(group.getDescription(), is("Description du premier groupe"));
  }

  @Test
  public void updateAUserFull() throws Exception {
    String newCity = "Grenoble";
    UserFull user = driver.getUserFull("user.7");
    assertThat(user.getValue("city"), is("Hattiesburg"));
    user.setValue("city", newCity);
    user.setValue("matricule", "123");
    user.setValue("homePhone", "");
    driver.updateUserFull(user);

    user = driver.getUserFull("user.7");
    // checks an updatable field is well updated
    assertThat(user.getValue("city"), is(newCity));
    // checks a non-updatable field is not updated
    assertThat(user.getValue("matricule"), is("7"));
    // checks reset is ok
    assertThat(user.getValue("homePhone"), isEmptyOrNullString());
  }


  /**
   * Start the LDAP server.
   * @param serverHome
   * @param ldifConfigFile
   * @throws org.opends.server.types.InitializationException
   * @throws org.opends.server.config.ConfigException
   * @throws java.net.URISyntaxException
   */
  private void startLdapServer(String serverHome, String ldifConfigFile)
      throws InitializationException, ConfigException, URISyntaxException {
    File directoryServerRoot = getServerRoot(serverHome);
    new File(directoryServerRoot, "locks").mkdir();
    new File(directoryServerRoot, "logs").mkdir();
    // Start the OpenDS server.
    if (EmbeddedUtils.isRunning()) {
      return;
    } else {
      DirectoryEnvironmentConfig envConfig = new DirectoryEnvironmentConfig();
      envConfig.setServerRoot(directoryServerRoot);
      envConfig.setConfigFile(getFile(ldifConfigFile));
      envConfig.setDisableConnectionHandlers(false);
      envConfig.setMaintainConfigArchive(false);
      EmbeddedUtils.startServer(envConfig);
    }
    // Get an internal, root connection to the OpenDJ instance.
    InternalClientConnection internalConnection = InternalClientConnection.getRootConnection();
    if (internalConnection == null) {
      System.out.println("OpenDJ cannot get internal connection (null)");
      throw new RuntimeException("OpenDJ cannot get internal connection (null)");
    }
    System.out.println("OpenDJ started");
  }

  public Backend initializeTestBackend(boolean createBaseEntry, DN baseDN, String backendId)
      throws DirectoryException, ConfigException, InitializationException {
    MemoryBackend memoryBackend = new MemoryBackend();
    memoryBackend.setBackendID(backendId);
    memoryBackend.setBaseDNs(new DN[]{baseDN});
    memoryBackend.supportsControl("1.2.840.113556.1.4.473");
    memoryBackend.initializeBackend();
    DirectoryServer.registerBackend(memoryBackend);
    memoryBackend.clearMemoryBackend();
    if (createBaseEntry) {
      Entry e = StaticUtils.createEntry(baseDN);
      memoryBackend.addEntry(e, null);
    }
    return memoryBackend;
  }

  /**
   * Load a LDIF file into th LDAP server.
   * @param ldifFile
   * @throws InitializationException
   * @throws ConfigException
   * @throws java.io.FileNotFoundException
   * @throws DirectoryException
   * @throws URISyntaxException
   */
  private void loadLdif(String ldifFile, String backendID, DN baseDN)
      throws InitializationException, ConfigException, FileNotFoundException, DirectoryException,
             URISyntaxException {
    LDIFImportConfig importConfig = new LDIFImportConfig(new FileInputStream(getFile(ldifFile)));
    importConfig.setAppendToExistingData(true);
    importConfig.setReplaceExistingEntries(true);
    importConfig.setCompressed(false);
    importConfig.setEncrypted(false);
    importConfig.setValidateSchema(false);
    importConfig.setSkipDNValidation(false);
    ArrayList<Backend> backendList = new ArrayList<>();
    ArrayList<BackendCfg> entryList = new ArrayList<>();
    ArrayList<List<DN>> dnList = new ArrayList<>();
    BackendToolUtils.getBackends(backendList, entryList, dnList);
    Backend backend = null;
    for (Backend b : backendList) {
      if (backendID.equals(b.getBackendID())) {
        backend = b;
        break;
      }
    }
    if (backend == null) {
      backend = initializeTestBackend(true, baseDN, backendID);
      LDIFImportResult result = backend.importLDIF(importConfig);
      System.out.println("OpenDJ LDIF import result " + result);

    } else {
      String lockFile = LockFileManager.getBackendLockFileName(backend);
      StringBuilder failureReason = new StringBuilder();
      if (!LockFileManager.acquireExclusiveLock(lockFile, failureReason)) {
        throw new RuntimeException(
            "OpenDJ cannot get lock the backend " + backend.getBackendID() + " " + failureReason);
      }
      LDIFImportResult result = backend.importLDIF(importConfig);
      System.out.println("OpenDJ LDIF import result " + result);
      lockFile = LockFileManager.getBackendLockFileName(backend);
      failureReason = new StringBuilder();
      if (!LockFileManager.releaseLock(lockFile, failureReason)) {
        throw new RuntimeException("OpenDJ cannot release the lock the backend " + backend.
            getBackendID() + " " + failureReason);
      }
    }
  }

  /**
   * Stop the server instance.
   */
  private void stopLdapServer() {
    if (EmbeddedUtils.isRunning()) {
      EmbeddedUtils.stopServer(LDAPDriverTest.class.getName(), Message.EMPTY);
      System.out.println("OpenDJ stopped");
    }
  }

  /**
   * Contruct a java.io.File to the specified SERVER_HOME directory, trying as a full path and if
   * not
   * found as a resource path.
   * @param serverHome
   * @return
   * @throws URISyntaxException
   */
  private File getServerRoot(String serverHome) throws URISyntaxException {
    File file = new File(serverHome);
    if (!file.exists() || !file.isDirectory()) {
      return new File(LDAPDriverTest.class.getClassLoader().getResource(serverHome).toURI());
    }
    return file;
  }

  /**
   * Contruct a java.io.File to the specified file, trying as a full path and if not found as a
   * resource path.
   * @param fileName
   * @return
   * @throws URISyntaxException
   */
  private File getFile(String fileName) throws URISyntaxException {
    File file = new File(fileName);
    if (!file.exists() || file.isDirectory()) {
      return new File(LDAPDriverTest.class.getClassLoader().getResource(fileName).toURI());
    }
    return file;
  }

}