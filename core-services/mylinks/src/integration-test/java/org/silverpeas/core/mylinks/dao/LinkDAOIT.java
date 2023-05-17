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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.mylinks.dao;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.test.WarBuilder4MyLinks;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.mylinks.dao.MyLinksDAOITUtil.*;

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
    assertLinkIds(getAllLinkIds(), 1, 2, 3, 4, 5, 11, 12, 13, 14 ,15 ,21, 22 ,23, 24 ,25);
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
    assertLinkCategoryCouple(result);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5));
    result = linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
  }

  @Test
  public void insertUserLinkIntoLinksWithoutPositionSet() throws SQLException {
    Transaction.performInNew(() -> {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITHOUT_POSITION);
      link.setPosition(56);
      link.setHasPosition(true);
      return linkDao.createLink(link);
    });
    final int newLinkId = UNIQUE_ID + 1;
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15, newLinkId));
    LinkDetail createdLink = linkDao.getLink(newLinkId);
    assertThat(createdLink.getLinkId(), is(newLinkId));
    assertThat(createdLink.getUserId(), is(USER_ID_WITHOUT_POSITION));
    assertThat(createdLink.getName(), is("new name"));
    assertThat(createdLink.getDescription(), is("new description"));
    assertThat(createdLink.getUrl(), is("new url"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(true));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getPosition(), is(0));
    assertThat(createdLink.getCategory(), nullValue());
  }

  @Test
  public void insertUserLinkIntoLinksWithCategory() throws SQLException {
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    Transaction.performInNew(() -> {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITHOUT_POSITION);
      link.setPosition(56);
      link.setHasPosition(true);
      link.setCategory(new CategoryDetail());
      link.getCategory().setId(3);
      link.getCategory().setUserId(USER_ID_WITHOUT_POSITION);
      return linkDao.createLink(link);
    });
    final int newLinkId = UNIQUE_ID + 1;
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "3/26", "4/14", "10/21", "13/22", "25/23", "25/24");
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15, newLinkId));
    LinkDetail createdLink =  linkDao.getLink(newLinkId);
    assertThat(createdLink.getLinkId(), is(newLinkId));
    assertThat(createdLink.getUserId(), is(USER_ID_WITHOUT_POSITION));
    assertThat(createdLink.getName(), is("new name"));
    assertThat(createdLink.getDescription(), is("new description"));
    assertThat(createdLink.getUrl(), is("new url"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(true));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getPosition(), is(0));
    assertThat(createdLink.getCategory().getId(), is(3));
  }

  @Test
  public void insertUserLinkIntoLinksWithPositionSet() throws SQLException {
    Transaction.performInNew(() -> {
      LinkDetail link = new LinkDetail("new name", "new description", "new url", true, true);
      link.setUserId(USER_ID_WITH_POSITIONS);
      link.setPosition(56);
      link.setHasPosition(true);
      return linkDao.createLink(link);
    });
    final int newLinkId = UNIQUE_ID + 1;
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5, newLinkId));
    LinkDetail createdLink = linkDao.getLink(newLinkId);
    assertThat(createdLink.getLinkId(), is(newLinkId));
    assertThat(createdLink.getName(), is("new name"));
    assertThat(createdLink.getDescription(), is("new description"));
    assertThat(createdLink.getUrl(), is("new url"));
    assertThat(createdLink.isVisible(), is(true));
    assertThat(createdLink.isPopup(), is(true));
    assertThat(createdLink.getUserId(), is(USER_ID_WITH_POSITIONS));
    assertThat(createdLink.hasPosition(), is(false));
    assertThat(createdLink.getCategory(), nullValue());
  }

  @Test
  public void updateUserLinkThatHadAlreadyPosition() throws SQLException {
    final int testLinkId = 4;
    final LinkDetail linkToUpdate = Transaction.performInNew(() -> {
      LinkDetail link = linkDao.getLink(testLinkId);
      assertThat(link.getLinkId(), is(testLinkId));
      assertThat(link.getUserId(), is(USER_ID_WITH_POSITIONS));
      assertThat(link.hasPosition(), is(true));
      assertThat(link.getPosition(), is(2));
      link.setPosition(26);
      linkDao.updateLink(link);
      return link;
    });
    final LinkDetail updatedLink = linkDao.getLink(testLinkId);
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
    assertThat(updatedLink.hasPosition(), is(linkToUpdate.hasPosition()));
    assertThat(updatedLink.getPosition(), is(26));
  }

  @Test
  public void updateUserLinkWithCategory() throws SQLException {
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    final int testedLinkId = 12;
    final LinkDetail linkToUpdate = Transaction.performInNew(() -> {
      LinkDetail link = linkDao.getLink(testedLinkId);
      assertThat(link.getLinkId(), is(testedLinkId));
      assertThat(link.getUserId(), is(USER_ID_WITHOUT_POSITION));
      assertThat(link.getCategory().getId(), is(3));
      link.getCategory().setId(4);
      linkDao.updateLink(link);
      return link;
    });
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/15", "4/12", "4/14", "10/21", "13/22", "25/23", "25/24");
    final LinkDetail updatedLink = linkDao.getLink(testedLinkId);
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
  }

  @Test
  public void updateUserLinkThatHadNoPosition() throws SQLException {
    final LinkDetail linkToUpdate = Transaction.performInNew(() -> {
      LinkDetail link = linkDao.getLink(14);
      assertThat(link.getLinkId(), is(14));
      assertThat(link.getUserId(), is(USER_ID_WITHOUT_POSITION));
      assertThat(link.hasPosition(), is(false));
      assertThat(link.getPosition(), is(0));
      link.setHasPosition(true);
      link.setPosition(26);
      linkDao.updateLink(link);
      return link;
    });
    LinkDetail updatedLink = linkDao.getLink(14);
    assertThat(updatedLink.getLinkId(), is(linkToUpdate.getLinkId()));
    assertThat(updatedLink.getUserId(), is(linkToUpdate.getUserId()));
    assertThat(updatedLink.hasPosition(), is(true));
    assertThat(updatedLink.getPosition(), is(26));
  }

  @Test
  public void deleteUserLink() throws Exception {
    getAllUserLinks();
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    Transaction.performInNew(() -> {
      linkDao.deleteLink("4");
      return null;
    });
    assertLinkIds(getAllLinkIds(), 1, 2, 3, 5, 11, 12, 13, 14 ,15 ,21, 22 ,23, 24 ,25);
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 5));
    result = Transaction.performInNew(() -> linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION));
    assertThat(extractLinkIds(result), containsInAnyOrder(11, 12, 13, 14, 15));
  }

  @Test
  public void deleteAllUserLinks() throws Exception {
    getAllUserLinks();
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    Transaction.performInNew(() -> {
      linkDao.deleteUserData(USER_ID_WITHOUT_POSITION);
      return null;
    });
    assertLinkIds(getAllLinkIds(), 1, 2, 3, 4, 5, 21, 22 ,23, 24 ,25);
    assertCategoryIds(getAllCategoryIds(), 1, 3, 4, 10, 13, 25);
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "10/21", "13/22", "25/23", "25/24");
    List<LinkDetail> result = linkDao.getAllLinksByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractLinkIds(result), containsInAnyOrder(1, 2, 3, 4, 5));
    result = Transaction.performInNew(() -> linkDao.getAllLinksByUser(USER_ID_WITHOUT_POSITION));
    assertThat(extractLinkIds(result), empty());
  }

  /*
  TOOL METHODS
   */

  private void assertLinkCategoryCouple(final List<LinkDetail> result) throws SQLException {
    final Map<Integer, Integer> couplesByLinkId = getAllOfCouples().stream()
        .map(c -> c.split("/"))
        .collect(toMap(a -> parseInt(a[1]), a -> parseInt(a[0])));
    result.forEach(l -> {
      final Integer catId = couplesByLinkId.get(l.getLinkId());
      if (catId == null) {
        assertThat(l.getCategory(), nullValue());
      } else {
        assertThat(l.getCategory().getId(), is(catId));
      }
    });
  }

  private List<Integer> extractLinkIds(List<LinkDetail> links) {
    return links.stream().map(LinkDetail::getLinkId).collect(Collectors.toList());
  }
}
