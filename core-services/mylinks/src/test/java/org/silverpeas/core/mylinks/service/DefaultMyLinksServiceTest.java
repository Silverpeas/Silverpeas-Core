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
package org.silverpeas.core.mylinks.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.mylinks.dao.LinkDAO;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.persistence.jdbc.ConnectionPool;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.RequesterProvider;
import org.silverpeas.core.test.extention.TestManagedMock;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv
public class DefaultMyLinksServiceTest {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  private DefaultMyLinksService service;
  private LinkDAO dao;

  @RequesterProvider
  public User getCurrentRequester() {
    UserDetail user = new UserDetail();
    user.setId("32");
    return user;
  }

  @BeforeEach
  public void setup(@TestManagedMock ConnectionPool pool) throws Exception {
    service = spy(new DefaultMyLinksService());
    when(pool.getDataSourceConnection()).thenReturn(mock(Connection.class));
    dao = mocker.mockField(LinkDAO.class, LinkDAO.class, "linkDAO");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void createLinkAndVerifyIdIsSet() throws Exception {
    when(dao.createLink(any(Connection.class), any(LinkDetail.class)))
        .thenAnswer((Answer<Integer>) invocation -> 38);

    LinkDetail linkToAdd = new LinkDetail();

    service.createLink(linkToAdd);

    assertThat(linkToAdd.getLinkId(), is(38));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getAllLinksByUserWithoutPositions() throws Exception {
    List<LinkDetail> links = initLinkPositions(null, null, null, null, null);
    when(dao.getAllLinksByUser(any(Connection.class), anyString())).thenReturn(links);

    assertThat(extractLinkIdPositions(links),
        contains(of(10, 0), of(11, 0), of(12, 0), of(13, 0), of(14, 0)));

    links = service.getAllLinksByUser("");

    assertThat(extractLinkIdPositions(links),
        contains(of(14, 0), of(13, 0), of(12, 0), of(11, 0), of(10, 0)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getAllLinksByUserWithPositions() throws Exception {
    List<LinkDetail> links = initLinkPositions(5, 0, 2, 1, 3);
    doReturn(links).when(dao).getAllLinksByUser(any(Connection.class), anyString());

    assertThat(extractLinkIdPositions(links),
        contains(of(10, 5), of(11, 0), of(12, 2), of(13, 1), of(14, 3)));

    links = service.getAllLinksByUser("");

    assertThat(extractLinkIdPositions(links),
        contains(of(11, 0), of(13, 1), of(12, 2), of(14, 3), of(10, 5)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getAllLinksByUserWithAndWithoutPositions() throws Exception {
    List<LinkDetail> links = initLinkPositions(null, 0, null, 1, 3);
    doReturn(links).when(dao).getAllLinksByUser(any(Connection.class), anyString());

    assertThat(extractLinkIdPositions(links),
        contains(of(10, 0), of(11, 0), of(12, 0), of(13, 1), of(14, 3)));

    links = service.getAllLinksByUser("");

    assertThat(extractLinkIdPositions(links),
        contains(of(12, 0), of(10, 0), of(11, 0), of(13, 1), of(14, 3)));
  }

  /*
  METHOD TOOLS
   */

  private List<LinkDetail> initLinkPositions(Integer... positions) {
    List<LinkDetail> links = new ArrayList<LinkDetail>();
    for (Integer position : positions) {
      LinkDetail link = new LinkDetail();
      link.setLinkId(links.size() + 10);
      if (position != null) {
        link.setPosition(position);
        link.setHasPosition(true);
      }
      links.add(link);
    }
    return links;
  }

  private List<Pair<Integer, Integer>> extractLinkIdPositions(List<LinkDetail> links) {
    List<Pair<Integer, Integer>> linkIdPositions = new ArrayList<Pair<Integer, Integer>>();
    for (LinkDetail link : links) {
      linkIdPositions.add(of(link.getLinkId(), link.getPosition()));
    }
    return linkIdPositions;
  }
}
