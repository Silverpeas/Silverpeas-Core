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
 * An integration test to validate the old and deprecated V5 authentication mechanism works with the
 * REST web services.
 */
@RunWith(Arquillian.class)
public class BasicAuthenticationIT extends RESTWebServiceTest {

  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/profile/create-dataset.sql";

  private static final String USER_ID = "32";
  private static final String USER_PASSWORD = "sasa";

  private UserFull user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(BasicAuthenticationIT.class)
        .addRESTWebServiceEnvironment()
        .addStringTemplateFeatures()
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(UserProfileEntity.class, ProfileResourceBaseURIs.class,
              AuthenticationResource.class);
          warBuilder.addAsResource("org/silverpeas/domains");
          warBuilder.addAsResource("org/silverpeas/authentication");
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
    Response response = authenticate(user.getLogin(), user.getDomainId(), USER_PASSWORD);
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.OK));

    UserProfileEntity entity = response.readEntity(UserProfileEntity.class);
    assertThat(entity, notNullValue());
    assertThat(entity.getId(), is(USER_ID));
  }

  @Test
  public void authenticateAUserWithInvalidLoginShouldDontSucceed() {
    Response response = authenticate("prout", user.getDomainId(), USER_PASSWORD);
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.UNAUTHORIZED));
  }

  @Test
  public void authenticateAUserWithInvalidPasswordShouldDontSucceed() {
    Response response = authenticate(user.getLogin(), user.getDomainId(), "prout");
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.UNAUTHORIZED));
  }

  @Test
  public void authenticateAUserWithInvalidDomainIdShouldDontSucceed() {
    Response response = authenticate(user.getLogin(), "42", USER_PASSWORD);
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

  private Response authenticate(final String login, final String domainId, final String password) {
    String thePath = AuthenticationResource.PATH;
    String credentials = login + "@domain" + domainId + ":" + password;
    WebTarget resource = resource();
    Invocation.Builder resourcePoster = resource.path(thePath).request(MediaType.APPLICATION_JSON);
    return resourcePoster.header("Authorization", encodesCredentials(credentials)).post(null);
  }

  private String encodesCredentials(final String credentials) {
    return "Basic " +
        Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }
}
