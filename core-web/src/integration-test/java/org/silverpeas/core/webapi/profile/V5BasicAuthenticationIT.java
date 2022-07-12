package org.silverpeas.core.webapi.profile;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.web.RESTWebServiceTest;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * An integration test to validate the old and deprecated authentication mechanism in Silverpeas V5
 * works with the REST web services. Currently, 6.2.x (x >= 4) supports in two ways the old Basic
 * authentication in Silverpeas V5:
 * <ul>
 *   <li>the basic authentication string in the correct format as expected by the HTTP
 *   specification (RFC 7617) in which only the user credentials are encoded in base 64,</li>
 *   <li>the basic authentication string as expected by Silverpeas V5 in which all the value
 *   string is encoded in base 64.</li>
 * </ul>
 */
@RunWith(Arquillian.class)
public class V5BasicAuthenticationIT extends RESTWebServiceTest {

  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/profile/create-dataset.sql";

  private static final String USER_ID = "32";

  private UserFull user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(V5BasicAuthenticationIT.class)
        .addRESTWebServiceEnvironment()
        .addStringTemplateFeatures()
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(UserProfileEntity.class, ProfileResourceBaseURIs.class,
              AuthenticationResource.class);
        }).build();
  }

  @Before
  public void checkUserExists() {
    user = OrganizationController.get().getUserFull(USER_ID);
    assertThat(user, notNullValue());
    assertThat(user.getId(), is(USER_ID));
    assertThat(StringUtil.isDefined(user.getPassword()), is(true));
    assertThat(user.getPassword().startsWith("$6$"), is(true));
  }

  @Test
  public void authenticateAUserWithGoodCredentialsShouldSucceed() {
    Response response = authenticate(false, user.getId(), user.getPassword());
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.OK));

    UserProfileEntity entity = response.readEntity(UserProfileEntity.class);
    assertThat(entity, notNullValue());
    assertThat(entity.getId(), is(USER_ID));
  }

  @Test
  public void authenticateAUserWithInvalidUserIdShouldDontSucceed() {
    Response response = authenticate(false, "42", user.getPassword());
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.UNAUTHORIZED));
  }

  @Test
  public void authenticateAUserWithInvalidPasswordShouldDontSucceed() {
    Response response = authenticate(false, user.getLogin(), "prout");
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.UNAUTHORIZED));
  }

  @Test
  public void fullV5AuthenticationOfAUserWithGoodCredentialsShouldSucceed() {
    Response response = authenticate(true, user.getId(), user.getPassword());
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.OK));

    UserProfileEntity entity = response.readEntity(UserProfileEntity.class);
    assertThat(entity, notNullValue());
    assertThat(entity.getId(), is(USER_ID));
  }

  @Test
  public void fullV5AuthenticationOfAUserWithInvalidUserIdShouldDontSucceed() {
    Response response = authenticate(true, "42", user.getPassword());
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.UNAUTHORIZED));
  }

  @Test
  public void fullV5authenticationOfAUserWithInvalidPasswordShouldDontSucceed() {
    Response response = authenticate(true, user.getLogin(), "prout");
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.UNAUTHORIZED));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[0];
  }

  @Override
  protected String getDataSetScript() {
    return DATA_SET_SCRIPT;
  }

  private Response authenticate(boolean fullV5, final String userId, final String password) {
    String thePath = AuthenticationResource.PATH;
    String credentials = userId + ":" + password;
    WebTarget resource = resource();
    String authValue =
        fullV5 ? encodesCredentialsAsInV5(credentials) : encodesCredentials(credentials);
    Invocation.Builder resourcePoster = resource.path(thePath).request(MediaType.APPLICATION_JSON);
    return resourcePoster.header("Authorization", authValue).post(null);
  }

  private String encodesCredentials(final String credentials) {
    return "bAsiC " +
        Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  private String encodesCredentialsAsInV5(final String credentials) {
    return Base64.getEncoder()
        .encodeToString((credentials).getBytes(StandardCharsets.UTF_8));
  }
}
