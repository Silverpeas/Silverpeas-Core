/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.mylinks;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.model.LinkDetailComparator;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.util.Mutable;

import javax.ws.rs.WebApplicationException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang3.reflect.FieldUtils.writeDeclaredField;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.webapi.mylinks.MyLinkEntity.fromLinkDetail;

@EnableSilverTestEnv
class MyLinksResourceTest {

  private static final String CURRENT_USER_ID = "26";
  private static final String PATH_BASE = "/silverpeas/services";
  private static final int NOT_VISIBLE_LINK_ID = 12;

  @TestManagedBean
  private MyLinksResourceURIs uri;
  @TestManagedMock
  private MyLinksWebManager manager;
  @TestManagedMock
  private OrganizationController orgaCtrl;
  @TestedBean
  private MyLinksResource4Test rest;

  private final Mutable<LinkDetail> authorizedLink = Mutable.empty();

  @BeforeEach
  void setup() {
    authorizedLink.set(null);
    when(manager.getAuthorizedLink(anyString())).thenAnswer(i -> {
      if (authorizedLink.isPresent()) {
        return authorizedLink.get();
      }
      throw new WebApplicationException(NOT_FOUND);
    });
  }

  @Test
  void getMyLinksWithoutPositionAndOneNotVisible() {
    List<LinkDetail> links = initLinkPositions(null, null, null, null, null);
    when(manager.getAllLinksOfCurrentUser()).thenReturn(links);
    assertThat(extractLinkIdPositions(links),
        contains(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)));
    List<MyLinkEntity> linkEntities = rest.getMyLinks();
    assertThat(extractLinkEntityUriPositions(linkEntities),
        contains(of(PATH_BASE + "/mylinks/14", -1), of(PATH_BASE + "/mylinks/13", -1),
            of(PATH_BASE + "/mylinks/11", -1), of(PATH_BASE + "/mylinks/10", -1)));

  }

  @Test
  void getMyLinksWithPositionAndOneNotVisible() {
    List<LinkDetail> links = initLinkPositions(4, 0, 2, 3, 1);
    when(manager.getAllLinksOfCurrentUser()).thenReturn(links);
    assertThat(extractLinkIdPositions(links),
        contains(of(11, 0), of(14, 1), of(12, 2), of(13, 3), of(10, 4)));
    List<MyLinkEntity> linkEntities = rest.getMyLinks();
    assertThat(extractLinkEntityUriPositions(linkEntities),
        contains(of(PATH_BASE + "/mylinks/11", 0), of(PATH_BASE + "/mylinks/14", 1),
            of(PATH_BASE + "/mylinks/13", 3), of(PATH_BASE + "/mylinks/10", 4)));
  }

  @Test
  void getMyLinkOfCurrentUserWithoutPosition() {
    LinkDetail link = initLinkPositions((Integer) null).get(0);
    setupGetLinkService(link);
    MyLinkEntity linkEntity = rest.getLink("");
    assertThat(linkEntity.getLinkId(), is(link.getLinkId()));
    assertThat(linkEntity.getURI().toString(), is(PATH_BASE + "/mylinks/10"));
    assertThat(linkEntity.getPosition(), is(-1));
  }

  @Test
  void getMyLinkOfCurrentUserWithPosition() {
    LinkDetail link = initLinkPositions(4).get(0);
    setupGetLinkService(link);
    MyLinkEntity linkEntity = rest.getLink("");
    assertThat(linkEntity.getLinkId(), is(link.getLinkId()));
    assertThat(linkEntity.getURI().toString(), is(PATH_BASE + "/mylinks/10"));
    assertThat(linkEntity.getPosition(), is(4));
  }

  @Test
  void getMyLinkThatDoesNotExists() {
    assertThrows(WebApplicationException.class, () -> rest.getLink(""));
  }

  @Test
  void addLink() {
    setupCreateLinkService();
    final LinkDetail link = createLinkInstanceWithId(7);
    MyLinkEntity linkEntityToAdd = fromLinkDetail(link, null);
    rest.addLink(linkEntityToAdd);
    ArgumentCaptor<MyLinkEntity> argumentCaptor = ArgumentCaptor.forClass(MyLinkEntity.class);
    verify(manager, times(1)).createLink(argumentCaptor.capture());
    verify(manager, times(0)).updateLink(any(MyLinkEntity.class));
    verify(manager, times(0)).deleteLinks(any(String[].class));
    MyLinkEntity createdLink = argumentCaptor.getValue();
    assertThat(createdLink.getUserId(), is(CURRENT_USER_ID));
  }

  @Test
  void updateLink() throws Exception {
    setupGetLinkService(getDummyUserLink());
    setupUpdateLinkService();
    final LinkDetail link = createLinkInstanceWithId(38);
    MyLinkEntity linkEntityToUpdate = fromLinkDetail(link, null);
    writeDeclaredField(linkEntityToUpdate, "name", "name updated", true);
    writeDeclaredField(linkEntityToUpdate, "url", "url updated", true);
    rest.updateLink("38", linkEntityToUpdate);
    ArgumentCaptor<MyLinkEntity> argumentCaptor = ArgumentCaptor.forClass(MyLinkEntity.class);
    verify(manager, times(0)).createLink(any(MyLinkEntity.class));
    verify(manager, times(1)).updateLink(argumentCaptor.capture());
    verify(manager, times(0)).deleteLinks(any(String[].class));
    MyLinkEntity updatedLink = argumentCaptor.getValue();
    assertThat(updatedLink.getUserId(), is(CURRENT_USER_ID));
    assertThat(updatedLink.getName(), is("name updated"));
    assertThat(updatedLink.getUrl(), is("url updated"));
  }

  @Test
  void deleteLink() {
    setupGetLinkService(getDummyUserLink());
    rest.deleteLink("38");
    ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
    verify(manager, times(0)).createLink(any(MyLinkEntity.class));
    verify(manager, times(0)).updateLink(any(MyLinkEntity.class));
    verify(manager, times(1)).deleteLinks(argumentCaptor.capture());
    String[] deletedLinkIds = argumentCaptor.getValue();
    assertThat(deletedLinkIds, arrayContaining("38"));
  }

  @Test
  void saveUserLinksOrderAtMiddleWhenNoPositionBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(11);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(null, null, null, null, null),
        asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(11, 2), of(12, 3), of(10, 4)));
  }

  @Test
  void saveUserLinksOrderAtFirstWhenNoPositionBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(11);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(null, null, null, null, null),
        asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(11, 0), of(14, 1), of(13, 2), of(12, 3), of(10, 4)));
  }

  @Test
  void saveUserLinksOrderAtLastWhenNoPositionBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(4);
    linkPosition.setLinkId(11);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(null, null, null, null, null),
        asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(12, 2), of(10, 3), of(11, 4)));
  }

  @Test
  void saveUserLinksOrderFirstSamePositionWhenNoPositionBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(14);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(null, null, null, null, null),
        asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(12, 2), of(11, 3), of(10, 4)));
  }

  @Test
  void saveUserLinksOrderLastSamePositionWhenNoPositionBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(4);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(null, null, null, null, null),
        asList(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)), linkPosition, 5);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(14, 0), of(13, 1), of(12, 2), of(11, 3), of(10, 4)));
  }

  @Test
  void saveUserLinksOrderAtMiddleWhenPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(10, 2), of(14, 3)));
  }

  @Test
  void saveUserLinksOrderAtFirstWhenPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 4);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(10, 0), of(13, 1), of(11, 2), of(14, 3)));
  }

  @Test
  void saveUserLinksOrderAtLastWhenPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(50);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(12, 3), of(10, 50)));
  }

  @Test
  void saveUserLinksOrderFisrtSamePositionWhenPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(0);
    linkPosition.setLinkId(13);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1)));
  }

  @Test
  void saveUserLinksOrderMiddleSamePositionWhenPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(14);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1)));
  }

  @Test
  void saveUserLinksOrderMiddleSamePositionWhenAllPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(2);
    linkPosition.setLinkId(14);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, 1, 4, 0, 2),
        asList(of(13, 0), of(11, 1), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 0);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)), empty());
  }

  @Test
  void saveUserLinksOrderLastSamePositionWhenPositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(4);
    linkPosition.setLinkId(12);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 4, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4)), linkPosition, 2);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1)));
  }

  @Test
  void saveUserLinksOrderWhenStrangePositionExistingBefore() {
    MyLinkPosition linkPosition = new MyLinkPosition();
    linkPosition.setPosition(3);
    linkPosition.setLinkId(10);
    List<LinkDetail> updatedLinks = performSortOrderSave(asList(3, null, 40, null, 2),
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 40)), linkPosition, 3);
    assertThat(extractLinkIdPositions(LinkDetailComparator.sort(updatedLinks)),
        contains(of(13, 0), of(11, 1), of(12, 4)));
  }

  @Test
  void saveUserLinksOrderBadRequest() {
    List<Integer> positions = asList(3, null, 4, null, 2);
    List<Pair<Integer, Integer>> initialLinkListOrderToVerifyBeforeSorting =
        asList(of(13, 0), of(11, 0), of(14, 2), of(10, 3), of(12, 4));
    assertThrows(WebApplicationException.class,
        () -> performSortOrderSave(positions, initialLinkListOrderToVerifyBeforeSorting, null, -1));
  }

  private List<LinkDetail> performSortOrderSave(List<Integer> positions, List<Pair<Integer, Integer>> initialLinkListOrderToVerifyBeforeSorting,
      MyLinkPosition linkPosition, int nbExpectedUpdateCall) {
    authorizedLink.set(getDummyUserLink());
    List<LinkDetail> links = initLinkPositions(positions.toArray(new Integer[0]));
    when(manager.getAllLinksOfCurrentUser()).thenReturn(links);
    assertThat(extractLinkIdPositions(links),
        contains(initialLinkListOrderToVerifyBeforeSorting.toArray()));
    rest.saveUserLinksOrder(linkPosition);
    ArgumentCaptor<MyLinkEntity> argumentCaptor = ArgumentCaptor.forClass(MyLinkEntity.class);
    verify(manager, times(0)).createLink(any(MyLinkEntity.class));
    verify(manager, times(nbExpectedUpdateCall)).updateLink(argumentCaptor.capture());
    verify(manager, times(0)).deleteLinks(any(String[].class));
    return argumentCaptor.getAllValues().stream().map(MyLinkEntity::toLinkDetail).collect(Collectors.toList());
  }

  /*
  METHOD TOOLS
   */

  private void setupCreateLinkService() {
    when(manager.createLink(any(MyLinkEntity.class))).thenAnswer(
        i -> {
          final LinkDetail link = ((MyLinkEntity) i.getArgument(0)).toLinkDetail();
          link.setUserId(CURRENT_USER_ID);
          return link;
        });
  }

  private void setupGetLinkService(final LinkDetail link) {
    authorizedLink.set(link);
  }

  private void setupUpdateLinkService() {
    when(manager.updateLink(any(MyLinkEntity.class))).thenAnswer(
        i -> {
          final LinkDetail link = ((MyLinkEntity) i.getArgument(0)).toLinkDetail();
          link.setUserId(CURRENT_USER_ID);
          return link;
        });
  }

  private LinkDetail createLinkInstanceWithId(final int i) {
    final LinkDetail link = new LinkDetail();
    link.setLinkId(i);
    link.setName("Name_" + i);
    link.setUrl("URL_" + i);
    link.setUserId(CURRENT_USER_ID);
    return link;
  }

  private LinkDetail getDummyUserLink() {
    LinkDetail link = new LinkDetail();
    link.setUserId(CURRENT_USER_ID);
    return link;
  }

  private List<LinkDetail> initLinkPositions(Integer... positions) {
    List<LinkDetail> links = new ArrayList<>();
    for (Integer position : positions) {
      LinkDetail link = createLinkInstanceWithId(links.size() + 10);
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
    List<Pair<Integer, Integer>> linkIdPositions = new ArrayList<>();
    for (LinkDetail link : links) {
      linkIdPositions.add(of(link.getLinkId(), link.getPosition()));
    }
    return linkIdPositions;
  }

  private List<Pair<String, Integer>> extractLinkEntityUriPositions(List<MyLinkEntity> links) {
    List<Pair<String, Integer>> linkUriPositions = new ArrayList<>();
    for (MyLinkEntity link : links) {
      linkUriPositions.add(of(link.getURI().toString(), link.getPosition()));
    }
    return linkUriPositions;
  }

  private static class MyLinksResource4Test extends MyLinksResource {

    private final User user;

    protected MyLinksResource4Test() {
      this.user = mock(User.class);
      when(user.getId()).thenReturn(CURRENT_USER_ID);
      when(user.getUserPreferences()).thenReturn(
          new UserPreferences(CURRENT_USER_ID, "de", ZoneId.of("Europe/Berlin"), null, null, false,
              false, false, UserMenuDisplay.ALL));
    }

    @Override
    protected User getUser() {
      return user;
    }

    @Override
    protected UserPreferences getUserPreferences() {
      return user.getUserPreferences();
    }

    @Override
    protected OrganizationController getOrganisationController() {
      return super.getOrganisationController();
    }
  }
}