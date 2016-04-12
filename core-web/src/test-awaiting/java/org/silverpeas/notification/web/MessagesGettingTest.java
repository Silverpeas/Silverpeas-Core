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
package org.silverpeas.notification.web;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagementProvider;
import com.silverpeas.web.ResourceGettingTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.silverpeas.core.notification.message.MessageContainer;
import org.silverpeas.core.notification.message.MessageManager;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.silverpeas.notification.web.MessageContainerEntityMatcher.matches;
import static org.silverpeas.notification.web.NotificationTestResources.JAVA_PACKAGE;
import static org.silverpeas.notification.web.NotificationTestResources.SPRING_CONTEXT;

/**
 * Tests on the gallery photo getting by the GalleryResource web service.
 * @author Yohann Chastagnier
 */
public class MessagesGettingTest extends ResourceGettingTest<NotificationTestResources> {

  private String sessionKey;
  private SessionInfo sessionInfo;
  private long lastUserAccessTime;
  private String registredKeyOfMessages;
  private MessageContainer expectedMessageContainer;

  public MessagesGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    sessionKey = authenticate(aUser());
    sessionInfo =
        SessionManagementProvider.getFactory().getSessionManagement().getSessionInfo(sessionKey);
    lastUserAccessTime = sessionInfo.getLastAccessTimestamp();
    MessageManager.initialize();
    registredKeyOfMessages = MessageManager.getRegistredKey();
    expectedMessageContainer = MessageManager.getMessageContainer(registredKeyOfMessages);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    MessageManager.destroy();
  }

  @Test
  public void getMessages() {
    MessageManager.addError("error 1");
    MessageManager.addInfo("info 1");
    MessageManager.addSuccess("success 1");
    MessageManager.addError("error 2");
    MessageManager.addError("error 3");
    assertThat(expectedMessageContainer.getMessages(), not(emptyIterable()));
    assertEntity(getAt(aResourceURI(), MessageContainerEntity.class));
    assertThat(lastUserAccessTime, is(sessionInfo.getLastAccessTimestamp()));
  }

  @Override
  public void gettingAnUnexistingResource() {
    MessageContainerEntity entity = getAt(anUnexistingResourceURI(), getWebEntityClass());
    assertNotNull(entity);
    assertThat(entity.getMessages(), emptyIterable());
    assertThat(lastUserAccessTime, is(sessionInfo.getLastAccessTimestamp()));
  }

  @Override
  public void gettingAResourceByANonAuthenticatedUser() {
    assertEntity(resource().path(aResourceURI()).
        accept(MediaType.APPLICATION_JSON).
        get(MessageContainerEntity.class));
  }

  /**
   * Centralization of asserts.
   * @param entity
   */
  private void assertEntity(MessageContainerEntity entity) {
    assertThat(entity, notNullValue());
    // Messages have been consumed ...
    assertThat(MessageManager.getMessageContainer(registredKeyOfMessages), nullValue());
    assertThat(entity, matches(expectedMessageContainer));
  }

  @Ignore
  @Override
  public void gettingAResourceWithAnExpiredSession() {
  }

  @Override
  @Ignore
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Override
  public String aResourceURI() {
    return "messages/" + registredKeyOfMessages;
  }

  @Override
  public String anUnexistingResourceURI() {
    return "messages/dummy";
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
  public Class<MessageContainerEntity> getWebEntityClass() {
    return MessageContainerEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{};
  }
}
