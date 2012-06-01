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
package org.silverpeas.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
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

/**
 * JUnit ClassRule to start an embedded OpenDJ server in a JUnit Test. Must be used with the
 * CreateLdapServer annoation
 *
 * @sez org.silverpeas.ldap.CreateLdapServer
 * @author ehugonnet
 */
public class OpenDJRule implements TestRule {

  @Override
  public Statement apply(Statement stmnt, Description d) {
    CreateLdapServer annotation = d.getAnnotation(CreateLdapServer.class);
    if (annotation != null) {
      return statement(stmnt, annotation.serverHome(), annotation.ldifConfig(), annotation.
          ldifFile());
    }
    return stmnt;
  }

  private Statement statement(final Statement stmnt, final String serverHome,
      final String ldifConfigFile, final String ldifFile) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        before();
        try {
          stmnt.evaluate();
        } finally {
          after();
        }
      }

      private void after() {
        stopLdapServer();
      }

      private void before() {
        try {
          loadLdif(ldifFile);
          startLdapServer(serverHome, ldifConfigFile);
        } catch (Exception ex) {
          throw new RuntimeException("Could'nt start LDAP Server", ex);
        }
      }
    };
  }

  /**
   * Start the LDAP server.
   * @param serverHome
   * @param ldifConfigFile
   * @throws InitializationException
   * @throws ConfigException
   * @throws FileNotFoundException
   * @throws DirectoryException
   * @throws URISyntaxException 
   */
  private void startLdapServer(String serverHome, String ldifConfigFile) throws
      InitializationException, ConfigException, FileNotFoundException, DirectoryException,
      URISyntaxException {
    File directoryServerRoot = getServerRoot(serverHome);
    new File(directoryServerRoot, "db").mkdir();
    new File(directoryServerRoot, "locks").mkdir();
    new File(directoryServerRoot, "logs").mkdir();
    DirectoryEnvironmentConfig envConfig = new DirectoryEnvironmentConfig();
    envConfig.setServerRoot(directoryServerRoot);
    envConfig.setConfigFile(getFile(ldifConfigFile));
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

  /**
   * Load a LDIF file into th LDAP server.
   * @param ldifFile
   * @throws InitializationException
   * @throws ConfigException
   * @throws FileNotFoundException
   * @throws DirectoryException
   * @throws URISyntaxException 
   */
  private void loadLdif(String ldifFile) throws InitializationException, ConfigException,
      FileNotFoundException, DirectoryException, URISyntaxException {
    LDIFImportConfig importConfig = new LDIFImportConfig(new FileInputStream(getFile(ldifFile)));
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
  }

  /**
   * Stop the server instance.
   */
  private void stopLdapServer() {
    if (EmbeddedUtils.isRunning()) {
      EmbeddedUtils.stopServer(OpenDJRule.class.getName(), Message.EMPTY);
    }
  }

  /**
   * Contruct a java.io.File to the specified serverHome directory, trying as a full path and if not found as
   * a resource path.
   *
   * @param serverHome
   * @return
   * @throws URISyntaxException
   */
  private File getServerRoot(String serverHome) throws URISyntaxException {
    File file = new File(serverHome);
    if (!file.exists() || !file.isDirectory()) {
      return new File(OpenDJRule.class.getClassLoader().getResource(serverHome).toURI());
    }
    return file;
  }
/**
   * Contruct a java.io.File to the specified file, trying as a full path and if not found as
   * a resource path.
   *
   * @param serverHome
   * @return
   * @throws URISyntaxException
   */
  private File getFile(String fileName) throws URISyntaxException {
    File file = new File(fileName);
    if (!file.exists() || file.isDirectory()) {
      return new File(OpenDJRule.class.getClassLoader().getResource(fileName).toURI());
    }
    return file;
  }
}
