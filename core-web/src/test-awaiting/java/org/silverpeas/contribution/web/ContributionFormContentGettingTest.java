/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.web;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagementProvider;
import com.silverpeas.web.ResourceGettingTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.contribution.web.ContributionTestResources.JAVA_PACKAGE;
import static org.silverpeas.core.contribution.web.ContributionTestResources.SPRING_CONTEXT;

/**
 * User: Yohann Chastagnier
 * Date: 22/05/13
 */
public class ContributionFormContentGettingTest
    extends ResourceGettingTest<ContributionTestResources> {

  private String sessionKey;
  private SessionInfo sessionInfo;
  private long lastUserAccessTime;

  public ContributionFormContentGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    sessionKey = authenticate(aUser());
    sessionInfo =
        SessionManagementProvider.getFactory().getSessionManagement().getSessionInfo(sessionKey);
    lastUserAccessTime = sessionInfo.getLastAccessTimestamp();
  }

  @Test
  public void getContributionFormContentFormData() {
    final AbstractContentEntity entity = getAt(aResourceURI(), FormEntity.class);
    assertNotNull(entity);
    assertThat(entity.getType(), is("form"));
    assertThat(entity.getURI().toString(), endsWith(aResourceURI() + "/testFormId"));
    assertThat(lastUserAccessTime, not(is(sessionInfo.getLastAccessTimestamp())));
  }

  @Test
  public void getContributionFormContentFormWithSpecifiedFormId() {
    final AbstractContentEntity entity =
        getAt(aResourceURI() + "/specifiedFormId", FormEntity.class);
    assertNotNull(entity);
    assertThat(entity.getType(), is("form"));
    assertThat(entity.getURI().toString(), endsWith(aResourceURI() + "/specifiedFormId"));
    assertThat(lastUserAccessTime, not(is(sessionInfo.getLastAccessTimestamp())));
  }

  @Test
  public void getContributionFormContentFormWithSpecifiedLanguage() {
    final AbstractContentEntity entity = getAt(aResourceURI() + "?lang=en", FormEntity.class);
    assertNotNull(entity);
    assertThat(entity.getType(), is("form"));
    assertThat(entity.getURI().toString(), endsWith(aResourceURI() + "/testFormId"));
    assertThat(lastUserAccessTime, not(is(sessionInfo.getLastAccessTimestamp())));
  }

  @Test
  public void getContributionFormContentFormWithAllSpecified() {
    final AbstractContentEntity entity =
        getAt(aResourceURI() + "/specifiedFormId?lang=en", FormEntity.class);
    assertNotNull(entity);
    assertThat(entity.getType(), is("form"));
    assertThat(entity.getURI().toString(), endsWith(aResourceURI() + "/specifiedFormId"));
    assertThat(lastUserAccessTime, not(is(sessionInfo.getLastAccessTimestamp())));
  }

  @Override
  public String aResourceURI() {
    return aResourceURI("3");
  }

  private String aResourceURI(final String contributionId) {
    return "contribution/" + getExistingComponentInstances()[0] + "/" +
        contributionId + "/content/form";
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI("0");
  }

  @Override
  public Object aResource() {
    return null;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return FormEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"componentName5"};
  }
}
