/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */
package org.silverpeas.core.webapi.profile;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.web.rs.SynchronizerTokenService;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.web.test.RESTWebServiceTest;

import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests of the REST authentication flow with native TOTP.
 */
@RunWith(Arquillian.class)
public class AuthenticationTwoFactorIT extends RESTWebServiceTest {

  private static final String PASSWORD = "sasa";

  @Inject
  private TwoFactorAuthenticationService twoFactorAuthenticationService;

  @Inject
  private TotpService totpService;

  private User user;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(AuthenticationTwoFactorIT.class)
        .addRESTWebServiceEnvironment()
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.webapi.profile");
          warBuilder.addPackages(true, "org.silverpeas.core.security.authentication.twofactor");
          warBuilder.addPackages(true, "org.silverpeas.core.security.totp");
          warBuilder.addAsResource(
              "org/silverpeas/authentication/settings/authenticationSettings.properties");
        }).build();
  }

  @Before
  public void prepareUser() {
    user = getSilverpeasEnvironmentTest().createDefaultUser();
    TwoFactorAuthentication current =
        twoFactorAuthenticationService.startEnrollment(user.getId());
    assertThat(current.isPending(), is(true));
    assertThat(twoFactorAuthenticationService.confirmEnrollment(
        user.getId(), totpService.generateCode(current.getSecret())), is(true));
  }

  @Test
  public void authenticationRequiresTwoFactorAndCreatesSessionOnlyAfterValidCode() {
    Invocation.Builder authentication = basicAuthenticationRequest();
    Response challenge;
    try (Response response = authentication.post(Entity.json("{}"))) {
      challenge = response;
      assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
      assertThat(response.readEntity(AuthenticationResource.AuthenticationChallengeEntity.class)
          .getStatus(), is("TWO_FACTOR_REQUIRED"));
      assertThat(response.getHeaderString("X-Silverpeas-Session"), is((String) null));
    }

    NewCookie cookie = challenge.getCookies().get("JSESSIONID");
    // The response is closed above; keep the cookie value for the second request.
    assertThat(cookie, notNullValue());

    try (Response response = resource()
        .path("authentication/two-factor")
        .queryParam("code", "000000")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .cookie(cookie)
        .post(Entity.json("{}"))) {
      assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
      assertThat(response.readEntity(AuthenticationResource.AuthenticationChallengeEntity.class)
          .getStatus(), is("TWO_FACTOR_REQUIRED"));
    }

    String code = totpService.generateCode(
        twoFactorAuthenticationService.getAuthentication(user.getId()).orElseThrow().getSecret());

    try (Response response = resource()
        .path("authentication/two-factor")
        .queryParam("code", code)
        .request(MediaType.APPLICATION_JSON_TYPE)
        .cookie(cookie)
        .post(Entity.json("{}"))) {
      assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
      assertThat(response.getHeaderString("X-Silverpeas-Session"), notNullValue());
      assertThat(response.getHeaderString(SynchronizerTokenService.SESSION_TOKEN_KEY),
          notNullValue());
    }
  }

  @Test
  public void authenticationChallengeCanBeFailedFiveTimes() {
    NewCookie cookie;
    try (Response response = basicAuthenticationRequest().post(Entity.json("{}"))) {
      assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
      cookie = response.getCookies().get("JSESSIONID");
      assertThat(cookie, notNullValue());
    }

    for (int attempt = 1; attempt <= 5; attempt++) {
      try (Response response = resource()
          .path("authentication/two-factor")
          .queryParam("code", "000000")
          .request(MediaType.APPLICATION_JSON_TYPE)
          .cookie(cookie)
          .post(Entity.json("{}"))) {
        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
      }
    }

    try (Response response = resource()
        .path("authentication/two-factor")
        .queryParam("code", totpService.generateCode(
            twoFactorAuthenticationService.getAuthentication(user.getId()).orElseThrow().getSecret()))
        .request(MediaType.APPLICATION_JSON_TYPE)
        .cookie(cookie)
        .post(Entity.json("{}"))) {
      assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }
  }

  private Invocation.Builder basicAuthenticationRequest() {
    String credentials = user.getLogin() + "@domain" + user.getDomainId() + ":" + PASSWORD;
    String encoded = Base64.getEncoder().encodeToString(
        credentials.getBytes(StandardCharsets.UTF_8));
    return resource().path("authentication").request(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Basic " + encoded);
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[0];
  }
}
