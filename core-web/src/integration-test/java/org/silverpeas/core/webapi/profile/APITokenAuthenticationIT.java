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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * An integration test to validate the old and deprecated V5 authentication mechanism works with the
 * REST web services.
 */
@RunWith(Arquillian.class)
public class APITokenAuthenticationIT extends RESTWebServiceTest {

  private static final String DATA_SET_SCRIPT =
      "/org/silverpeas/core/webapi/profile/create-dataset.sql";

  private static final String USER_ID = "32";

  private UserFull user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(APITokenAuthenticationIT.class)
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
    assertThat(StringUtil.isDefined(user.getToken()), is(true));
  }

  @Test
  public void authenticateAUserWithGoodCredentialsShouldSucceed() {
    Response response = authenticate(user.getToken());
    assertThat(response.getStatusInfo().toEnum(), is(Response.Status.OK));

    UserProfileEntity entity = response.readEntity(UserProfileEntity.class);
    assertThat(entity, notNullValue());
    assertThat(entity.getId(), is(USER_ID));
  }

  @Test
  public void authenticateAUserWithInvalidTokenShouldDontSucceed() {
    Response response = authenticate("prout");
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

  private Response authenticate(final String token) {
    String thePath = AuthenticationResource.PATH;
    WebTarget resource = resource();
    Invocation.Builder resourcePoster = resource.path(thePath).request(MediaType.APPLICATION_JSON);
    return resourcePoster.header("Authorization", encodeToken(token)).post(null);
  }

  private String encodeToken(final String token) {
    return "BEARER " + token;
  }
}
