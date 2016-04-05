/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.web.mylinks.control;

import org.silverpeas.core.mylinks.MyLinksRuntimeException;
import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.webapi.mylinks.MyLinkEntity;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.admin.component.ComponentHelper;
import org.silverpeas.core.i18n.I18NHelper;

import static org.apache.commons.lang.reflect.FieldUtils.writeDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class MyLinksPeasSessionControllerTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  private static final String CURRENT_USER_ID = "26";

  private MyLinksPeasSessionController ctrl;
  private MyLinksService ejb;

  @Before
  public void setup() throws Exception {
    reflectionRule.setField(I18NHelper.class, false, "isI18nContentActivated");
    commonAPI4Test.injectIntoMockedBeanContainer(mock(ComponentHelper.class));

    MainSessionController mainSessionController = mock(MainSessionController.class);
    ctrl =
        spy(new MyLinksPeasSessionController(mainSessionController, mock(ComponentContext.class)));
    doReturn(new UserDetail()).when(mainSessionController).getCurrentUserDetail();
    reflectionRule.setField(ctrl, mainSessionController, "controller");
    doReturn("Bundle value").when(ctrl).getString(anyString());

    ctrl.getUserDetail().setId(CURRENT_USER_ID);
    ejb = commonAPI4Test.injectIntoMockedBeanContainer(mock(MyLinksService.class));
  }

  @Test
  public void addLink() {
    MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);

    ctrl.createLink(linkEntityToAdd);

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(ejb, times(1)).createLink(argumentCaptor.capture());
    verify(ejb, times(0)).updateLink(any(LinkDetail.class));
    verify(ejb, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
  }

  @Test
  public void updateLink() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(ejb.getLink(anyString())).thenReturn(linkDetailForVerification);
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

    ctrl.updateLink(linkEntityToUpdate);

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(ejb, times(0)).createLink(any(LinkDetail.class));
    verify(ejb, times(1)).updateLink(argumentCaptor.capture());
    verify(ejb, times(0)).deleteLinks(any(String[].class));
    LinkDetail updatedLink = argumentCaptor.getValue();
    assertThat(updatedLink.getUserId(), is(CURRENT_USER_ID));
    assertThat(updatedLink.getName(), is("name updated"));
    assertThat(updatedLink.getUrl(), is("url updated"));
  }

  @Test(expected = MyLinksRuntimeException.class)
  public void updateLinkWhichTheCurrentUserIsNotTheOwner() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    linkDetailForVerification.setUserId("otherUserId");
    when(ejb.getLink(anyString())).thenReturn(linkDetailForVerification);
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

    ctrl.updateLink(linkEntityToUpdate);
  }

  @Test(expected = MyLinksRuntimeException.class)
  public void updateLinkButUrlIsMissing() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(ejb.getLink(anyString())).thenReturn(linkDetailForVerification);
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "", true);

    ctrl.updateLink(linkEntityToUpdate);
  }

  @Test(expected = MyLinksRuntimeException.class)
  public void updateLinkButNameIsMissing() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(ejb.getLink(anyString())).thenReturn(linkDetailForVerification);
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

    ctrl.updateLink(linkEntityToUpdate);
  }

  @Test
  public void deleteLink() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    when(ejb.getLink(anyString())).thenReturn(linkDetailForVerification);

    ctrl.deleteLinks(new String[]{"38", "26"});

    ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
    verify(ejb, times(0)).createLink(any(LinkDetail.class));
    verify(ejb, times(0)).updateLink(any(LinkDetail.class));
    verify(ejb, times(1)).deleteLinks(argumentCaptor.capture());
    String[] deletedLinkIds = argumentCaptor.getValue();
    assertThat(deletedLinkIds, arrayContaining("38", "26"));
  }

  @Test(expected = MyLinksRuntimeException.class)
  public void deleteLinkWhichTheCurrentUserIsNotTheOwner() throws Exception {
    LinkDetail linkDetailForVerification = getDummyUserLink();
    linkDetailForVerification.setUserId("otherUserId");
    when(ejb.getLink(anyString())).thenReturn(linkDetailForVerification);

    ctrl.deleteLinks(new String[]{"38", "26"});
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