/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.mylinks.dao;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.test.WarBuilder4MyLinks;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class LinkDAOIT {

  private static final String USER_ID_WITH_POSITIONS = "user_1";
  private static final String USER_ID_WITHOUT_POSITION = "user_2";
  private static final Integer UNIQUE_ID = 25;

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String DATASET_XML_SCRIPT = "test-mylinks-dataset.xml";
  
  @Inject
  private LinkDAO linkDao;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4MyLinks.onWarForTestClass(LinkDAOIT.class).build();
  }

  @Test
  public void getAllUserLinks() throws Exception {
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5));
    result = linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
  }

  @Test
  public void insertUserLinkIntoLinksWithoutPositionSet() {
    Transaction.performInNew(() -> {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITHOUT_POSITION);
      link.setPosition(56);
      link.setHasPosition(true);
      return linkDao.createLink(link);
    });
    LinkDetail createdLink = Transaction.performInNew(() -> {
      List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION);
      assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15, (UNIQUE_ID + 1)));
      return linkDao.getLink(String.valueOf(UNIQUE_ID + 1));
    });
    assertThat(createdLink.getLinkId(), is((UNIQUE_ID + 1)));
    assertThat(createdLink.getUserId(), is(USER_ID_WITHOUT_POSITION));
    assertThat(createdLink.getName(), is("new name"));
    assertThat(createdLink.getDescription(), is("new description"));
    assertThat(createdLink.getUrl(), is("new url"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(true));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getPosition(), is(0));
  }

  @Test
  public void insertUserLinkIntoLinksWithPositionSet() {
    Transaction.performInNew(() -> {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITH_POSITIONS);
      link.setPosition(56);
      link.setHasPosition(true);
      return linkDao.createLink(link);
    });
    LinkDetail createdLink = Transaction.performInNew(() -> {
      List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
      assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5, (UNIQUE_ID + 1)));
      return linkDao.getLink(String.valueOf(UNIQUE_ID + 1));
    });
    assertThat(createdLink.getLinkId(), is((UNIQUE_ID + 1)));
    assertThat(createdLink.getName(), is("new name"));
    assertThat(createdLink.getDescription(), is("new description"));
    assertThat(createdLink.getUrl(), is("new url"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(true));
    assertThat(createdLink.getUserId(), is(USER_ID_WITH_POSITIONS));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getPosition(), is(0));
  }

  @Test
  public void updateUserLinkThatHadAlreadyPosition() {
    final LinkDetail linkToUpdate = Transaction.performInNew(() -> {
      LinkDetail link = linkDao.getLink("4");
      assertThat(link.getLinkId(), is(4));
      assertThat(link.getUserId(), is(USER_ID_WITH_POSITIONS));
      assertThat(link.hasPosition(), is(true));
      assertThat(link.getPosition(), is(2));
      link.setPosition(26);
      linkDao.updateLink(link);
      return link;
    });
    final LinkDetail updatedLink = Transaction.performInNew(() -> linkDao.getLink("4"));
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
    assertThat(updatedLink.hasPosition(), is(linkToUpdate.hasPosition()));
    assertThat(updatedLink.getPosition(), is(26));
  }

  @Test
  public void updateUserLinkThatHadNoPosition() {
    final LinkDetail linkToUpdate = Transaction.performInNew(() -> {
      LinkDetail link = linkDao.getLink("14");
      assertThat(link.getLinkId(), is(14));
      assertThat(link.getUserId(), is(USER_ID_WITHOUT_POSITION));
      assertThat(link.hasPosition(), is(false));
      assertThat(link.getPosition(), is(0));
      link.setHasPosition(true);
      link.setPosition(26);
      linkDao.updateLink(link);
      return link;
    });
    LinkDetail updatedLink = Transaction.performInNew(() -> linkDao.getLink("14"));
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
    assertThat(updatedLink.hasPosition(), is(true));
    assertThat(updatedLink.getPosition(), is(26));
  }

  @Test
  public void deleteUserLink() throws Exception {
    getAllUserLinks();
    Transaction.performInNew(() -> {
      linkDao.deleteLink("4");
      return null;
    });
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 5));
    result = Transaction.performInNew(() -> linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION));
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
  }

  /*
  TOOL METHODS
   */

  private List<Integer> extractLinkIds(List<LinkDetail> links) {
    return links.stream().map(LinkDetail::getLinkId).collect(Collectors.toList());
  }
}
