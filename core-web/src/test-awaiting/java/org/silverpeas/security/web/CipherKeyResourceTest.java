package org.silverpeas.security.web;

import com.silverpeas.web.RESTWebServiceTest;
import com.silverpeas.web.WebResourceTesting;
import com.stratelia.webactiv.beans.admin.Admin;
import org.silverpeas.util.FileRepositoryManager;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.silverpeas.security.web.CryptoTestResources.*;

/**
 * Unit tests on the services provided by the CipherKeyResource REST-based web resource.
 */
public class CipherKeyResourceTest extends RESTWebServiceTest<CryptoTestResources> {

  private String sessionId;

  public CipherKeyResourceTest() {
    super(CRYPTO_WEB_PACKAGE, SPRING_CONTEXT);
  }

  @BeforeClass
  public static void createSecurityDirectoryAndSetupJCEProviders() throws IOException {
    String securityPath = FileRepositoryManager.getSecurityDirPath();
    File securityDir = new File(securityPath);
    if (!securityDir.exists()) {
      FileUtils.forceMkdir(securityDir);
    }
    securityDir.setWritable(true);
    securityDir.setExecutable(true);
    securityDir.setReadable(true);
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      Runtime.getRuntime().exec("attrib +H " + securityPath);
    }

    Security.addProvider(new BouncyCastleProvider());
  }

  @AfterClass
  public static void deleteSecurityDirectory() throws IOException {
    String securityPath = FileRepositoryManager.getSecurityDirPath();
    File securityDir = new File(securityPath);
    if (securityDir.exists()) {
      File keyFile = new File(ACTUAL_KEY_FILE_PATH);
      if (keyFile.exists()) {
        keyFile.setWritable(true);
        FileUtils.forceDelete(keyFile);
      }
      keyFile = new File(OLD_KEY_FILE_PATH);
      if (keyFile.exists()) {
        keyFile.setWritable(true);
        FileUtils.forceDelete(keyFile);
      }
      FileUtils.forceDelete(securityDir);
    }
  }

  @Before
  public void prepareTheUser() throws Exception {
    sessionId = authenticate(aUser());
    assertTheKeyFileExists(false);
    assertTheKeyOldFileExists(false);
  }

  @After
  public void deleteKeyFile() throws Exception {
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
      FileUtils.forceDelete(keyFile);
    }
    keyFile = new File(OLD_KEY_FILE_PATH);
    if (keyFile.exists()) {
      keyFile.setWritable(true);
      FileUtils.forceDelete(keyFile);
    }
  }

  @Test
  public void setTheCipherKeyByANonAuthenticatedUser() {
    ClientResponse response = put(aCipherKeyInHexa(), withAsSessionKey(null));
    int unauthorized = ClientResponse.Status.UNAUTHORIZED.getStatusCode();
    assertThat(response.getStatus(), is(unauthorized));
  }

  @Test
  public void setTheCipherKeyWithinADeprecatedSession() {
    ClientResponse response = put(aCipherKeyInHexa(), withAsSessionKey(UUID.randomUUID().toString()));
    int unauthorized = ClientResponse.Status.UNAUTHORIZED.getStatusCode();
    assertThat(response.getStatus(), is(unauthorized));
  }

  @Test
  public void setTheCipherKeyByANonAuthorizedUser() {
    denieAuthorizationToUsers();
    ClientResponse response = put(aCipherKeyInHexa(), withAsSessionKey(sessionId));
    int forbidden = ClientResponse.Status.FORBIDDEN.getStatusCode();
    assertThat(response.getStatus(), is(forbidden));
  }

  @Test
  public void setCipherKey() throws Exception {
    ClientResponse response = put(aCipherKeyInHexa(), withAsSessionKey(sessionId));
    int ok = ClientResponse.Status.OK.getStatusCode();
    assertThat(response.getStatus(), is(ok));
    assertTheKeyFileExists(true);
    assertTheKeyOldFileExists(false);
  }

  @Test
  public void updateCipherKey() throws Exception {
    generateCipherKeyFile();
    ClientResponse response = put(aCipherKeyInHexa(), withAsSessionKey(sessionId));
    int ok = ClientResponse.Status.OK.getStatusCode();
    assertThat(response.getStatus(), is(ok));
    assertTheKeyFileExists(true);
    assertTheKeyOldFileExists(true);
  }

  @Test
  public void setNonHexadecimalCipherKey() throws Exception {
    String invalidKey = aCipherKeyInHexa();
    invalidKey = invalidKey.replace('f', 'z');
    ClientResponse response = put(invalidKey, withAsSessionKey(sessionId));
    int badRequest = ClientResponse.Status.BAD_REQUEST.getStatusCode();
    assertThat(response.getStatus(), is(badRequest));
    assertThat(response.hasEntity(), is(true));
    String cause = messages.getString(CipherKeyResource.INVALID_CIPHER_KEY);
    assertThat(response.getEntity(String.class), containsString(cause));
  }

  @Test
  public void setTooShortCipherKey() throws Exception {
    String invalidKey = aCipherKeyInHexa();
    invalidKey = invalidKey.substring(0, 32);
    ClientResponse response = put(invalidKey, withAsSessionKey(sessionId));
    int badRequest = ClientResponse.Status.BAD_REQUEST.getStatusCode();
    assertThat(response.getStatus(), is(badRequest));
    assertThat(response.hasEntity(), is(true));
    String cause = messages.getString(CipherKeyResource.INVALID_CIPHER_KEY);
    assertThat(response.getEntity(String.class), containsString(cause));
  }

  @Test
  public void setTooLongCipherKey() throws Exception {
    String invalidKey = aCipherKeyInHexa();
    invalidKey = invalidKey + "ef05";
    ClientResponse response = put(invalidKey, withAsSessionKey(sessionId));
    int badRequest = ClientResponse.Status.BAD_REQUEST.getStatusCode();
    assertThat(response.getStatus(), is(badRequest));
    assertThat(response.hasEntity(), is(true));
    String cause = messages.getString(CipherKeyResource.INVALID_CIPHER_KEY);
    assertThat(response.getEntity(String.class), containsString(cause));
  }

  /**
   * Gets the component instances to take into account in tests. Theses component instances will be
   * considered as existing. Others than thoses will be rejected with an HTTP error 404 (NOT FOUND).
   *
   * @return an array with the identifier of the component instances to take into account in tests.
   * The array cannot be null but it can be empty.
   */
  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{Admin.ADMIN_COMPONENT_ID};
  }

  private static String withAsSessionKey(String sessionKey) {
    return sessionKey;
  }

  private String aCipherKeyInHexa() {
    return "53e6f05b1483ed6e28e51d8e540cd92ba7f055ef835b788f40f2395ceff945cf";
  }

  private ClientResponse put(final String key, String withSessionKey) {
    WebResource resource = resource();
    WebResource.Builder resourceUpdate = resource.
        path(CipherKeyResource.WEB_PATH).
        accept(MediaType.TEXT_PLAIN).
        type(MediaType.TEXT_PLAIN);
    if (isDefined(withSessionKey)) {
      resourceUpdate = resourceUpdate.header(WebResourceTesting.HTTP_SESSIONKEY, withSessionKey);
    }
    return resourceUpdate.put(ClientResponse.class, key);
  }

  private void assertTheKeyFileExists(boolean yes) {
    File keyFile = new File(ACTUAL_KEY_FILE_PATH);
    assertThat(keyFile.exists(), is(yes));
  }

  private void assertTheKeyOldFileExists(boolean yes) {
    File keyFile = new File(OLD_KEY_FILE_PATH);
    assertThat(keyFile.exists(), is(yes));
  }
}
