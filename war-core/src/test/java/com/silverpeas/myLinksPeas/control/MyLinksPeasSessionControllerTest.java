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
package com.silverpeas.myLinksPeas.control;

import com.silverpeas.myLinks.MyLinksRuntimeException;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.model.LinkDetail;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.mylinks.web.MyLinkEntity;

import static org.apache.commons.lang.reflect.FieldUtils.writeDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class MyLinksPeasSessionControllerTest {

  private static final String CURRENT_USER_ID = "26";

  private MyLinksPeasSessionController ctrl;
  private MyLinksBm ejb;
  boolean oldI18nActivationValue;

  @Before
  public void setup() throws Exception {
    oldI18nActivationValue = I18NHelper.isI18nContentActivated;
    I18NHelper.isI18nContentActivated = false;

    MainSessionController mainSessionController = mock(MainSessionController.class);
    ctrl =
        spy(new MyLinksPeasSessionController(mainSessionController, mock(ComponentContext.class)));
    doReturn(new UserDetail()).when(mainSessionController).getCurrentUserDetail();
    FieldUtils.writeField(ctrl, "controller", mainSessionController, true);
    doReturn("Bundle value").when(ctrl).getString(anyString());
    doReturn(mock(MyLinksBm.class)).when(ctrl).getMyLinksBm();

    ctrl.getUserDetail().setId(CURRENT_USER_ID);
    ejb = ctrl.getMyLinksBm();
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