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
package org.silverpeas.core.webapi.mylinks;

import org.silverpeas.core.mylinks.service.MyLinksService;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.model.LinkDetailComparator;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.i18n.I18NHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.reflect.FieldUtils.writeDeclaredField;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class MyLinksResourceTest {

  private static final String CURRENT_USER_ID = "26";
  private static final String PATH_BASE = "/test";
  private static final int NOT_VISIBLE_LINK_ID = 12;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private MyLinksResource4Test rest;
  private MyLinksService service;
  private OrganizationController orgaCtrl;
  boolean oldI18nActivationValue;

  @Before
  public void setup() throws Exception {
    oldI18nActivationValue = I18NHelper.isI18nContentActivated;
    I18NHelper.isI18nContentActivated = false;

    rest = spy(new MyLinksResource4Test());
    doReturn(new UserDetail()).when(rest).getUserDetail();
    doReturn(new UserPreferences(CURRENT_USER_ID, "de", null, null, false, false, false,
        UserMenuDisplay.ALL)).when(rest).getUserPreferences();
    doReturn(mock(UriInfo.class)).when(rest).getUriInfo();
    doReturn(mock(MyLinksService.class)).when(rest).getMyLinksBm();
    doReturn(mock(OrganizationController.class)).when(rest).getOrganisationController();

    rest.getUserDetail().setId(CURRENT_USER_ID);
    when(rest.getUriInfo().getBaseUri()).thenReturn(URI.create(PATH_BASE));
    service = rest.getMyLinksBm();
    orgaCtrl = rest.getOrganisationController();
  }

  @After
  public void tearDown() {
    I18NHelper.isI18nContentActivated = oldI18nActivationValue;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getMyLinksWithoutPositionAndOneNotVisible() {
    List<LinkDetail> links = initLinkPositions(null, null, null, null, null);
    when(service.getAllLinks(anyString())).thenReturn(links);

    assertThat(extractLinkIdPositions(links),
        contains(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)));

    List<MyLinkEntity> linkEntities = rest.getMyLinks();

    assertThat(extractLinkEntityUriPositions(linkEntities),
        contains(of(PATH_BASE + "/mylinks/14", -1), of(PATH_BASE + "/mylinks/13", -1),
            of(PATH_BASE + "/mylinks/11", -1), of(PATH_BASE + "/mylinks/10", -1)));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void getMyLinksWithPositionAndOneNotVisible() {
    List<LinkDetail> links = initLinkPositions(4, 0, 2, 3, 1);
    when(service.getAllLinks(anyString())).thenReturn(links);

    assertThat(extractLinkIdPositions(links),
        contains(of(11, 0), of(14, 1), of(12, 2), of(13, 3), of(10, 4)));

    List<MyLinkEntity> linkEntities = rest.getMyLinks();

    assertThat(extractLinkEntityUriPositions(linkEntities),
        contains(of(PATH_BASE + "/mylinks/11", 0), of(PATH_BASE + "/mylinks/14", 1),
            of(PATH_BASE + "/mylinks/13", 3), of(PATH_BASE + "/mylinks/10", 4)));
  }

  @Test
  public void getMyLinkOfCurrentUserWithoutPosition() {
    LinkDetail link = initLinkPositions((Integer) null).get(0);
    when(service.getLink(anyString())).thenReturn(link);

    MyLinkEntity linkEntity = rest.getMyLink("");

    assertThat(linkEntity.getLinkId(), is(link.getLinkId()));
    assertThat(linkEntity.getUri().toString(), is(PATH_BASE + "/mylinks/10"));
    assertThat(linkEntity.getPosition(), is(-1));
  }

  @Test
  public void getMyLinkOfCurrentUserWithPosition() {
    LinkDetail link = initLinkPositions(4).get(0);
    when(service.getLink(anyString())).thenReturn(link);

    MyLinkEntity linkEntity = rest.getMyLink("");

    assertThat(linkEntity.getLinkId(), is(link.getLinkId()));
    assertThat(linkEntity.getUri().toString(), is(PATH_BASE + "/mylinks/10"));
    assertThat(linkEntity.getPosition(), is(4));
  }

  @Test(expected = WebApplicationException.class)
  public void getMyLinkThatTheCurrentUserIsNotOwner() {
    LinkDetail link = initLinkPositions(4).get(0);
    link.setUserId(CURRENT_USER_ID + "_OTHER");
    when(service.getLink(anyString())).thenReturn(link);

    rest.getMyLink("");
  }

  @Test(expected = WebApplicationException.class)
  public void getMyLinkThatDoesNotExists() {
    rest.getMyLink("");
  }

  @Test
  public void addLink() {
    doReturn(null).when(rest).getMyLink(anyString());
    MyLinkEntity linkEntityToAdd = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);

    rest.addLink(linkEntityToAdd);

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(service, times(1)).createLink(argumentCaptor.capture());
    verify(service, times(0)).updateLink(any(LinkDetail.class));
    verify(service, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
  }

  @Test
  public void updateLink() throws Exception {
    when(service.getLink(anyString())).thenReturn(getDummyUserLink());
    doReturn(null).when(rest).getMyLink(anyString());
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

    rest.updateLink(linkEntityToUpdate);

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(service, times(0)).createLink(any(LinkDetail.class));
    verify(service, times(1)).updateLink(argumentCaptor.capture());
    verify(service, times(0)).deleteLinks(any(String[].class));
    LinkDetail updatedLink = argumentCaptor.getValue();
    assertThat(updatedLink.getUserId(), is(CURRENT_USER_ID));
    assertThat(updatedLink.getName(), is("name updated"));
    assertThat(updatedLink.getUrl(), is("url updated"));
  }

  @Test(expected = WebApplicationException.class)
  public void updateLinkButUrlIsMissing() throws Exception {
    doReturn(null).when(rest).getMyLink(anyString());
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "", true);

    rest.updateLink(linkEntityToUpdate);
  }

  @Test(expected = WebApplicationException.class)
  public void updateLinkButNameIsMissing() throws Exception {
    doReturn(null).when(rest).getMyLink(anyString());
    MyLinkEntity linkEntityToUpdate = MyLinkEntity.fromLinkDetail(new LinkDetail(), null);
    writeDeclaredField(linkEntityToUpdate, "name", "", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);

    rest.updateLink(linkEntityToUpdate);
  }

  @Test
  public void deleteLink() throws Exception {
    when(service.getLink(anyString())).thenReturn(getDummyUserLink());
    doReturn(null).when(rest).getMyLink(anyString());

    rest.deleteLink("38");

    ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
    verify(service, times(0)).createLink(any(LinkDetail.class));
    verify(service, times(0)).updateLink(any(LinkDetail.class));
    verify(service, times(1)).deleteLinks(argumentCaptor.capture());
    String[] deletedLinkIds = argumentCaptor.getValue();
    assertThat(deletedLinkIds, arrayContaining("38"));
  }

  @Test
  public void addSpaceLink() {
    doReturn(null).when(rest).getMyLink(anyString());
    when(orgaCtrl.isSpaceAvailable("750", CURRENT_USER_ID)).thenReturn(true);
    when(orgaCtrl.getSpaceInstLightById(anyString())).thenAnswer(new Answer<SpaceInstLight>() {
      @Override
      public SpaceInstLight answer(final InvocationOnMock invocation) throws Throwable {
        SpaceInstLight space = new SpaceInstLight();
        space.setLocalId(Integer.valueOf((String) invocation.getArguments()[0]));
        space.setName("new space name");
        space.setDescription("new space description");
        return space;
      }
    });
    when(orgaCtrl.getSpacePath("750")).thenAnswer(new Answer<List<SpaceInst>>() {
      @Override
      public List<SpaceInst> answer(final InvocationOnMock invocation) throws Throwable {
        List<SpaceInst> spacePath = new ArrayList<SpaceInst>();
        for (int spaceId : new int[]{260, 380, 750}) {
          SpaceInst spaceInst = new SpaceInst();
          spaceInst.setLocalId(spaceId);
          spaceInst.setName(spaceId + "_name");
          spacePath.add(spaceInst);
        }
        return spacePath;
      }
    });

    rest.addSpaceLink("750");

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(service, times(1)).createLink(argumentCaptor.capture());
    verify(service, times(0)).updateLink(any(LinkDetail.class));
    verify(service, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
    assertThat(createdLink.getName(), is("260_name > 380_name > 750_name"));
    assertThat(createdLink.getDescription(), is("new space description"));
    assertThat(createdLink.getUrl(), is("/Space/750"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(false));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getPosition(), is(0));
  }

  @Test
  public void addComponentLink() {
    doReturn(null).when(rest).getMyLink(anyString());
    when(orgaCtrl.isComponentAvailable("1050", CURRENT_USER_ID)).thenReturn(true);
    when(orgaCtrl.getComponentInstLight(anyString())).thenAnswer(new Answer<ComponentInstLight>() {
      @Override
      public ComponentInstLight answer(final InvocationOnMock invocation) throws Throwable {
        ComponentInstLight component = new ComponentInstLight();
        component.setLocalId(Integer.valueOf((String) invocation.getArguments()[0]));
        component.setLabel("new component name");
        component.setDescription("new component description");
        return component;
      }
    });
    when(orgaCtrl.getSpacePathToComponent("1050")).thenAnswer(new Answer<List<SpaceInst>>() {
      @Override
      public List<SpaceInst> answer(final InvocationOnMock invocation) throws Throwable {
        List<SpaceInst> spacePath = new ArrayList<SpaceInst>();
        for (int spaceId : new int[]{260, 380, 750}) {
          SpaceInst spaceInst = new SpaceInst();
          spaceInst.setLocalId(spaceId);
          spaceInst.setName(spaceId + "_name");
          spacePath.add(spaceInst);
        }
        return spacePath;
      }
    });

    rest.addAppLink("1050");

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(service, times(1)).createLink(argumentCaptor.capture());
    verify(service, times(0)).updateLink(any(LinkDetail.class));
    verify(service, times(0)).deleteLinks(any(String[].class));
    LinkDetail createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
    assertThat(createdLink.getName(),
        is("260_name > 380_name > 750_name > new component name"));
    assertThat(createdLink.getDescription(), is("new component description"));
    assertThat(createdLink.getUrl(), is("/Component/1050"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(false));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getPosition(), is(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderAtMiddleWhenNoPositionBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(11);
    List<LinkDetail> updatedLinks =
        performSortOrderSave(asList((Integer) null, null, null, null, null),
            asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(11, 2), of(12, 3), of(10, 4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderAtFirstWhenNoPositionBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(11);
    List<LinkDetail> updatedLinks =
        performSortOrderSave(asList((Integer) null, null, null, null, null),
            asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(11, 0), of(14, 1), of(13, 2), of(12, 3), of(10, 4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderAtLastWhenNoPositionBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(4);
    linkPosition.setLinkId(11);
    List<LinkDetail> updatedLinks =
        performSortOrderSave(asList((Integer) null, null, null, null, null),
            asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(12, 2), of(10, 3), of(11, 4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderFirstSamePositionWhenNoPositionBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(14);
    List<LinkDetail> updatedLinks =
        performSortOrderSave(asList((Integer) null, null, null, null, null),
            asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(12, 2), of(11, 3), of(10, 4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderLastSamePositionWhenNoPositionBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(4);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks =
        performSortOrderSave(asList((Integer) null, null, null, null, null),
            asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(12, 2), of(11, 3), of(10, 4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderAtMiddleWhenPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(10, 2), of(14, 3)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderAtFirstWhenPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 4);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(10, 0), of(13, 1), of(11, 2), of(14, 3)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderAtLastWhenPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(50);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(12, 3), of(10, 50)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderFisrtSamePositionWhenPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(13);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderMiddleSamePositionWhenPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(14);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderMiddleSamePositionWhenAllPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(14);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 0);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)), empty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderLastSamePositionWhenPositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(4);
    linkPosition.setLinkId(12);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void saveUserLinksOrderWhenStrangePositionExistingBefore() throws Exception {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(3);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 40, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 40)), linkPosition, 3);

    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1), of(12, 4)));
  }

  @SuppressWarnings("unchecked")
  @Test(expected = WebApplicationException.class)
  public void saveUserLinksOrderBadRequest() throws Exception {
    performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), null, -1);
  }

  @SuppressWarnings("unchecked")
  private List<LinkDetail> performSortOrderSave(List<Integer> positions,
      List<Pair<Integer, Integer>> initialLinkListOrderToVerifyBeforeSorting,
      MyLinkPosition linkPosition, int nbExpectedUpdateCall) {
    when(service.getLink(anyString())).thenReturn(getDummyUserLink());

    List<LinkDetail> links = initLinkPositions(positions.toArray(new Integer[positions.size()]));
    when(service.getAllLinks(anyString())).thenReturn(links);

    assertThat(extractLinkIdPositions(links),
        contains(initialLinkListOrderToVerifyBeforeSorting.toArray()));

    rest.saveUserLinksOrder(linkPosition);

    ArgumentCaptor<LinkDetail> argumentCaptor = ArgumentCaptor.forClass(LinkDetail.class);
    verify(service, times(0)).createLink(any(LinkDetail.class));
    verify(service, times(nbExpectedUpdateCall)).updateLink(argumentCaptor.capture());
    verify(service, times(0)).deleteLinks(any(String[].class));
    return argumentCaptor.getAllValues();
  }

  /*
  METHOD TOOLS
   */

  private LinkDetail getDummyUserLink() {
    LinkDetail link = new LinkDetail();
    link.setUserId(CURRENT_USER_ID);
    return link;
  }

  private List<LinkDetail> initLinkPositions(Integer... positions) {
    List<LinkDetail> links = new ArrayList<LinkDetail>();
    for (Integer position : positions) {
      LinkDetail link = new LinkDetail();
      link.setLinkId(links.size() + 10);
      if (position != null) {
        link.setPosition(position);
        link.setHasPosition(true);
      }
      link.setVisible(link.getLinkId() != NOT_VISIBLE_LINK_ID);
      link.setUserId(CURRENT_USER_ID);
      links.add(link);
    }
    return LinkDetailComparator.sort(links);
  }

  private List<Pair<Integer, Integer>> extractLinkIdPositions(List<LinkDetail> links) {
    List<Pair<Integer, Integer>> linkIdPositions = new ArrayList<Pair<Integer, Integer>>();
    for (LinkDetail link : links) {
      linkIdPositions.add(of(link.getLinkId(), link.getPosition()));
    }
    return linkIdPositions;
  }

  private List<Pair<String, Integer>> extractLinkEntityUriPositions(List<MyLinkEntity> links) {
    List<Pair<String, Integer>> linkUriPositions = new ArrayList<Pair<String, Integer>>();
    for (MyLinkEntity link : links) {
      linkUriPositions.add(of(link.getURI().toString(), link.getPosition()));
    }
    return linkUriPositions;
  }

  private static class MyLinksResource4Test extends MyLinksResource {

    @Override
    protected UserDetail getUserDetail() {
      return super.getUserDetail();
    }

    @Override
    protected UserPreferences getUserPreferences() {
      return super.getUserPreferences();
    }

    @Override
    protected OrganizationController getOrganisationController() {
      return super.getOrganisationController();
    }
  }
}