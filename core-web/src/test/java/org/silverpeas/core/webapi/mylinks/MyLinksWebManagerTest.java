/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.mylinks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;
import org.silverpeas.core.test.unit.extention.RequesterProvider;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;

import javax.ws.rs.WebApplicationException;
import java.time.ZoneId;

import static org.apache.commons.lang3.reflect.FieldUtils.writeDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class MyLinksWebManagerTest {

  private static final String CURRENT_USER_ID = "26";
  private static final String FR = LocalizationBundleStub.LANGUAGE_FR;

  @RegisterExtension
  static LocalizationBundleStub bundle = new LocalizationBundleStub(
      "org.silverpeas.mylinks.multilang.myLinksBundle", FR);

  @TestManagedMock
  private PersonalizationService personalizationService;
  @TestManagedMock
  private MyLinksService myLinksService;
  @TestedBean
  private MyLinksWebManager manager;

  @RequesterProvider
  public User getCurrentRequester() {
    UserDetail user = new UserDetail();
    user.setId(CURRENT_USER_ID);
    return user;
  }

  @BeforeEach
  void setup() {
    var preferences = new UserPreferences(CURRENT_USER_ID, FR, ZoneId.of("UTC"));
    preferences.setLook("");
    preferences.setPersonalWorkSpaceId("");
    preferences.setDisplay(UserMenuDisplay.DEFAULT);
    preferences.enableThesaurus(false);
    preferences.enableDragAndDrop(false);
    preferences.enableWebdavEdition(false);
    when(personalizationService.getUserSettings(CURRENT_USER_ID)).thenReturn(preferences);
    bundle.put(FR, "myLinks.messageConfirm", "added");
    bundle.put(FR, "myLinks.updateLink.messageConfirm", "updated");
    bundle.put(FR, "myLinks.deleteLinks.messageConfirm", "deleted");
  }

  @Test
  void addLink() throws IllegalAccessException {
    MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToAdd, "name", "name", true);
    writeDeclaredField(linkEntityToAdd, "url", "url", true);
    writeDeclaredField(linkEntityToAdd, "userId", CURRENT_USER_ID, true);
    manager.createLink(linkEntityToAdd);
    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(myLinksService, times(1)).createLink(argumentCaptor.capture());
    verify(myLinksService, times(0)).updateLink(any(LinkDetail.class));
    verify(myLinksService, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
  }

  @Test
  void addLinkButUrlIsMissing() {
    assertThrowsWebApplicationException(() -> {
      MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToAdd, "name", "name", true);
      writeDeclaredField(linkEntityToAdd, "userId", "otherUserId", true);
      manager.createLink(linkEntityToAdd);
    }, "HTTP 400 Bad Request");
  }

  @Test
  void addLinkButNameIsMissing() {
    assertThrowsWebApplicationException(() -> {
      MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToAdd, "url", "url", true);
      writeDeclaredField(linkEntityToAdd, "userId", "otherUserId", true);
      manager.createLink(linkEntityToAdd);
    }, "HTTP 400 Bad Request");
  }

  @Test
  void addLinkWhichTheCurrentUserIsNotTheOwner() throws IllegalAccessException {
    MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToAdd, "name", "name", true);
    writeDeclaredField(linkEntityToAdd, "url", "url", true);
    writeDeclaredField(linkEntityToAdd, "userId", "otherUserId", true);
    manager.createLink(linkEntityToAdd);
    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(myLinksService, times(1)).createLink(argumentCaptor.capture());
    verify(myLinksService, times(0)).updateLink(any(LinkDetail.class));
    verify(myLinksService, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
  }

  @Test
  void updateLink() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);
    manager.updateLink(linkEntityToUpdate);
    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(myLinksService, times(0)).createLink(any(LinkDetail.class));
    verify(myLinksService, times(1)).updateLink(argumentCaptor.capture());
    verify(myLinksService, times(0)).deleteLinks(any(String[].class));
    LinkDetail updatedLink = argumentCaptor.getValue();
    assertThat(updatedLink.getUserId(), is(CURRENT_USER_ID));
    assertThat(updatedLink.getName(), is("name updated"));
    assertThat(updatedLink.getUrl(), is("url updated"));
  }

  @Test
  void updateLinkWhichTheCurrentUserIsNotTheOwner() {
    assertThrowsWebApplicationException(() -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      linkDetailForVerification.setUserId("otherUserId");
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
      writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);
      manager.updateLink(linkEntityToUpdate);
    }, "HTTP 403 Forbidden");
  }

  @Test
  void updateLinkButUrlIsMissing() {
    assertThrowsWebApplicationException(() -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
      writeDeclaredField(linkEntityToUpdate, "url", "", true);
      manager.updateLink(linkEntityToUpdate);
    }, "HTTP 400 Bad Request");
  }

  @Test
  void updateLinkButNameIsMissing() {
    assertThrowsWebApplicationException(() -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToUpdate, "name", "", true);
      writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);
      manager.updateLink(linkEntityToUpdate);
    }, "HTTP 400 Bad Request");
  }

  @Test
  void deleteLink() {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
    manager.deleteLinks(new String[]{"38", "26"});
    ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
    verify(myLinksService, times(0)).createLink(any(LinkDetail.class));
    verify(myLinksService, times(0)).updateLink(any(LinkDetail.class));
    verify(myLinksService, times(1)).deleteLinks(argumentCaptor.capture());
    String[] deletedLinkIds = argumentCaptor.getValue();
    assertThat(deletedLinkIds, arrayContaining("38", "26"));
  }

  @Test
  void deleteLinkWhichTheCurrentUserIsNotTheOwner() {
    assertThrowsWebApplicationException(() -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      linkDetailForVerification.setUserId("otherUserId");
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      manager.deleteLinks(new String[]{"38", "26"});
    }, "HTTP 403 Forbidden");
  }

  /*
  METHOD TOOLS
   */

  private void assertThrowsWebApplicationException(Executable executable, String message) {
    assertThat(assertThrows(WebApplicationException.class, executable).getMessage(), is(message));
  }

  private LinkDetail getDummyUserLink() {
    LinkDetail link = new LinkDetail();
    link.setUserId(CURRENT_USER_ID);
    return link;
  }
}