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
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.mylinks.test.WarBuilder4MyLinks;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThrows;
import static org.silverpeas.core.mylinks.dao.MyLinksDAOITUtil.assertOfCouples;
import static org.silverpeas.core.mylinks.dao.MyLinksDAOITUtil.getAllOfCouples;

@RunWith(Arquillian.class)
public class LinkCategoryDAOIT {

  private static final String USER_ID_WITH_POSITIONS = "user_3";
  private static final String USER_ID_WITHOUT_POSITION = "user_1";

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String DATASET_XML_SCRIPT = "test-mylinks-dataset.xml";
  
  @Inject
  private LinkCategoryDAO linkCategoryDAO;

  @Inject
  private CategoryDAO categoryDAO;

  @Inject
  private LinkDAO linkDAO;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4MyLinks.onWarForTestClass(LinkCategoryDAOIT.class).build();
  }

  @BeforeEach
  public void verifyData() throws Exception {
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void deleteAllUserOfCouples() throws Exception {
    Transaction.performInNew(() -> {
      linkCategoryDAO.deleteUserData(USER_ID_WITH_POSITIONS);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "3/15", "4/14");
    Transaction.performInNew(() -> {
      linkCategoryDAO.deleteUserData(USER_ID_WITHOUT_POSITION);
      return null;
    });
    assertAllOfCoupleDataAre("3/12", "3/15", "4/14");
  }

  @Test
  public void getAllUserCategoriesByLink() throws Exception {
    Map<Integer, CategoryDetail> categoriesByLinkId =
        linkCategoryDAO.getAllCategoriesByLinkOfUser(USER_ID_WITH_POSITIONS);
    assertOfCouples(extractOfCouples(categoriesByLinkId), "10/21", "13/22", "25/23", "25/24");
    categoriesByLinkId = linkCategoryDAO.getAllCategoriesByLinkOfUser(USER_ID_WITHOUT_POSITION);
    assertOfCouples(extractOfCouples(categoriesByLinkId), "1/2", "1/4");
  }

  @Test
  public void getAllLinkCategoriesByLink() throws Exception {
    Map<Integer, CategoryDetail> categoriesByLinkId =
        linkCategoryDAO.getAllCategoriesByLink(4);
    assertOfCouples(extractOfCouples(categoriesByLinkId), "1/4");
    categoriesByLinkId = linkCategoryDAO.getAllCategoriesByLink(12);
    assertOfCouples(extractOfCouples(categoriesByLinkId), "3/12");
    categoriesByLinkId = linkCategoryDAO.getAllCategoriesByLink(25);
    assertOfCouples(extractOfCouples(categoriesByLinkId));
  }

  @Test
  public void insertNewOfCouple() throws Exception {
    final LinkDetail newLinkOfCouple = createOfCoupleAndAssertCurrentByLinkId(3, 1);
    Transaction.performInNew(() -> {
      linkCategoryDAO.saveByLink(newLinkOfCouple);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/3", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void insertNewOfCoupleWhichUserIdMismatch() throws Exception {
    final LinkDetail newLinkOfCouple = createOfCoupleAndAssertCurrentByLinkId(3, 4);
    assertThrows(Exception.class, () -> Transaction.performInNew(() -> {
      linkCategoryDAO.saveByLink(newLinkOfCouple);
      return null;
    }));
  }

  @Test
  public void saveSameExistingOfCouple() throws Exception {
    final LinkDetail existingCouple = createOfCoupleAndAssertCurrentByLinkId(15, 3, "3/15");
    Transaction.performInNew(() -> {
      linkCategoryDAO.saveByLink(existingCouple);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void changeExistingOfCouple() throws Exception {
    final LinkDetail changeOfCouple = createOfCoupleAndAssertCurrentByLinkId(15, 4, "3/15");
    Transaction.performInNew(() -> {
      linkCategoryDAO.saveByLink(changeOfCouple);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "4/14", "4/15", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void removeOfCoupleThatDoesNotExist() throws Exception {
    final LinkDetail saveOfCoupleThatDoesNotExist = createOfCoupleAndAssertCurrentByLinkId(3, 1);
    saveOfCoupleThatDoesNotExist.setCategory(null);
    Transaction.performInNew(() -> {
      linkCategoryDAO.saveByLink(saveOfCoupleThatDoesNotExist);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void removeExistingCouple() throws Exception {
    final LinkDetail existingOfCouple = createOfCoupleAndAssertCurrentByLinkId(15, 3, "3/15");
    existingOfCouple.setCategory(null);
    Transaction.performInNew(() -> {
      linkCategoryDAO.saveByLink(existingOfCouple);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "4/14", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void deleteOfCouplesOfLink() throws Exception {
    Transaction.performInNew(() -> {
      linkCategoryDAO.deleteByLink(15);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "4/14", "10/21", "13/22", "25/23", "25/24");
  }

  @Test
  public void deleteOfCouplesOfCategory() throws Exception {
    Transaction.performInNew(() -> {
      linkCategoryDAO.deleteByCategory(25);
      return null;
    });
    assertAllOfCoupleDataAre("1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22");
  }

  /*
  TOOL METHODS
   */

  private LinkDetail createOfCoupleAndAssertCurrentByLinkId(final int linkId, final int catId,
      String... currentCouplesOfLinkId) throws SQLException {
    Map<Integer, CategoryDetail> categoriesByLinkId = linkCategoryDAO.getAllCategoriesByLink(linkId);
    assertOfCouples(extractOfCouples(categoriesByLinkId), currentCouplesOfLinkId);
    CategoryDetail category = categoryDAO.getCategory(catId);
    LinkDetail newLinkCouple = linkDAO.getLink(linkId);
    newLinkCouple.setCategory(category);
    return newLinkCouple;
  }

  private List<String> extractOfCouples(final Map<Integer, CategoryDetail> couples) {
    return couples.entrySet()
        .stream()
        .map(e -> e.getValue().getId() + "/" + e.getKey())
        .sorted()
        .collect(Collectors.toList());
  }

  private void assertAllOfCoupleDataAre(final String... couples) throws SQLException {
    assertLinkIdsAre(1, 2, 3, 4, 5, 11, 12, 13, 14, 15, 21, 22, 23, 24, 25);
    assertCategoryIdsAre(1, 3, 4, 10, 13, 25);
    assertThat(getAllOfCouples(), contains(couples));
  }

  private void assertLinkIdsAre(final Integer... ids) throws SQLException {
    assertThat(getAllLinkIds(), contains(ids));
  }

  private void assertCategoryIdsAre(final Integer... ids) throws SQLException {
    assertThat(getAllCategoryIds(), contains(ids));
  }

  private List<Integer> getAllLinkIds() throws SQLException {
    return JdbcSqlQuery.createSelect("linkid")
        .from("SB_MyLinks_Link")
        .orderBy("linkid")
        .execute(r -> r.getInt(1));
  }

  private List<Integer> getAllCategoryIds() throws SQLException {
    return JdbcSqlQuery.createSelect("catid")
        .from("SB_MyLinks_Cat")
        .orderBy("catid")
        .execute(r -> r.getInt(1));
  }
}
