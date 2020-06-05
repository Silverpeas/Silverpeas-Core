/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.mylinks.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.mylinks.MyLinksRuntimeException;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.mylinks.MyLinkEntity;

import static org.apache.commons.lang3.reflect.FieldUtils.writeDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv
public class MyLinksPeasSessionControllerTest {

  private static final String CURRENT_USER_ID = "26";

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();
  private MyLinksPeasSessionController ctrl;
  @TestManagedMock
  private MyLinksService myLinksService;

  @BeforeEach
  public void setup() {
    MainSessionController mainSessionController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentName()).thenReturn("myLinks");
    ctrl =
        spy(new MyLinksPeasSessionController(mainSessionController, context));
    doReturn(new UserDetail()).when(mainSessionController).getCurrentUserDetail();
    mocker.setField(ctrl, mainSessionController, "controller");
    doReturn("Bundle value").when(ctrl).getString(anyString());

    ctrl.getUserDetail().setId(CURRENT_USER_ID);
  }

  @Test
  public void addLink() {
    MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);

    ctrl.createLink(linkEntityToAdd);

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(myLinksService, times(1)).createLink(argumentCaptor.capture());
    verify(myLinksService, times(0)).updateLink(any(LinkDetail.class));
    verify(myLinksService, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
  }

  @Test
  public void updateLink() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

    ctrl.updateLink(linkEntityToUpdate);

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
  public void updateLinkWhichTheCurrentUserIsNotTheOwner() {
    assertThrows(MyLinksRuntimeException.class, () -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      linkDetailForVerification.setUserId("otherUserId");
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
      writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

      ctrl.updateLink(linkEntityToUpdate);
    });
  }

  @Test
  public void updateLinkButUrlIsMissing() {
    assertThrows(MyLinksRuntimeException.class, () -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
      writeDeclaredField(linkEntityToUpdate, "url", "", true);

      ctrl.updateLink(linkEntityToUpdate);
    });
  }

  @Test
  public void updateLinkButNameIsMissing() {
    assertThrows(MyLinksRuntimeException.class, () -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);
      MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
      writeDeclaredField(linkEntityToUpdate, "name", "", true);
      writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

      ctrl.updateLink(linkEntityToUpdate);
    });
  }

  @Test
  public void deleteLink() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);

    ctrl.deleteLinks(new String[]{"38", "26"});

    ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
    verify(myLinksService, times(0)).createLink(any(LinkDetail.class));
    verify(myLinksService, times(0)).updateLink(any(LinkDetail.class));
    verify(myLinksService, times(1)).deleteLinks(argumentCaptor.capture());
    String[] deletedLinkIds = argumentCaptor.getValue();
    assertThat(deletedLinkIds, arrayContaining("38", "26"));
  }

  @Test
  public void deleteLinkWhichTheCurrentUserIsNotTheOwner() {
    assertThrows(MyLinksRuntimeException.class, () -> {
      LinkDetail linkDetailForVerification = getDummyUserLink();
      linkDetailForVerification.setUserId("otherUserId");
      when(myLinksService.getLink(anyString())).thenReturn(linkDetailForVerification);

      ctrl.deleteLinks(new String[]{"38", "26"});
    });
  }

  /*
  METHOD TOOLS
   */

  private LinkDetail getDummyUserLink() {
    LinkDetail link = new LinkDetail();
    link.setUserId(CURRENT_USER_ID);
    return link;
  }
}