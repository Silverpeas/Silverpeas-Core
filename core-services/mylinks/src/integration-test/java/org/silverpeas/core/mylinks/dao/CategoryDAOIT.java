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
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.test.WarBuilder4MyLinks;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.mylinks.dao.MyLinksDAOITUtil.assertOfCouples;
import static org.silverpeas.core.mylinks.dao.MyLinksDAOITUtil.getAllOfCouples;

@RunWith(Arquillian.class)
public class CategoryDAOIT {

  private static final String USER_ID_WITH_POSITIONS = "user_3";
  private static final String USER_ID_WITHOUT_POSITION = "user_1";
  private static final Integer UNIQUE_ID = 25;

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String DATASET_XML_SCRIPT = "test-mylinks-dataset.xml";
  
  @Inject
  private CategoryDAO categoryDao;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4MyLinks.onWarForTestClass(CategoryDAOIT.class).build();
  }

  @Test
  public void getAllUserCategories() throws Exception {
    List<CategoryDetail> result = categoryDao.getAllCategoriesByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractIds(result), containsInAnyOrder(10, 13, 25));
    result = categoryDao.getAllCategoriesByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractIds(result), containsInAnyOrder(1));
  }

  @Test
  public void getSomeCategories() throws Exception {
    final List<CategoryDetail> result = categoryDao.getCategories(List.of(10, 13, 25, 1561561));
    assertThat(extractIds(result), containsInAnyOrder(10, 13, 25));
  }

  @Test
  public void insertUserCategoryIntoCategoriesWithoutPositionSet() {
    Transaction.performInNew(() -> {
      CategoryDetail category = new CategoryDetail();
      category.setName("new name");
      category.setDescription("new description");
      category.setUserId(USER_ID_WITHOUT_POSITION);
      category.setPosition(56);
      category.setHasPosition(true);
      return categoryDao.create(category);
    });
    CategoryDetail createdCategory = Transaction.performInNew(() -> {
      List<CategoryDetail> result = categoryDao.getAllCategoriesByUser(USER_ID_WITHOUT_POSITION);
      assertThat(extractIds(result), containsInAnyOrder(1, (UNIQUE_ID + 1)));
      return categoryDao.getCategory(UNIQUE_ID + 1);
    });
    assertThat(createdCategory.getId(), is(UNIQUE_ID + 1));
    assertThat(createdCategory.getUserId(), is(USER_ID_WITHOUT_POSITION));
    assertThat(createdCategory.getName(), is("new name"));
    assertThat(createdCategory.getDescription(), is("new description"));
    assertThat(createdCategory.hasPosition(), is(false));
    assertThat(createdCategory.getPosition(), is(0));
  }

  @Test
  public void insertUserCategoryIntoCategoriesWithPositionSet() {
    Transaction.performInNew(() -> {
      CategoryDetail category = new CategoryDetail();
      category.setName("new name");
      category.setDescription("new description");
      category.setUserId(USER_ID_WITH_POSITIONS);
      category.setPosition(56);
      category.setHasPosition(true);
      return categoryDao.create(category);
    });
    CategoryDetail createdCategory = Transaction.performInNew(() -> {
      List<CategoryDetail> result = categoryDao.getAllCategoriesByUser(USER_ID_WITH_POSITIONS);
      assertThat(extractIds(result), containsInAnyOrder(10, 13, 25, (UNIQUE_ID + 1)));
      return categoryDao.getCategory(UNIQUE_ID + 1);
    });
    assertThat(createdCategory.getId(), is((UNIQUE_ID + 1)));
    assertThat(createdCategory.getName(), is("new name"));
    assertThat(createdCategory.getDescription(), is("new description"));
    assertThat(createdCategory.getUserId(), is(USER_ID_WITH_POSITIONS));
    assertThat(createdCategory.hasPosition(), is(false));
    assertThat(createdCategory.getPosition(), is(0));
  }

  @Test
  public void updateUserCategoryThatHadAlreadyPosition() {
    final CategoryDetail categoryToUpdate = Transaction.performInNew(() -> {
      CategoryDetail category = categoryDao.getCategory(25);
      assertThat(category.getId(), is(25));
      assertThat(category.getUserId(), is(USER_ID_WITH_POSITIONS));
      assertThat(category.hasPosition(), is(true));
      assertThat(category.getPosition(), is(2));
      category.setPosition(26);
      categoryDao.update(category);
      return category;
    });
    final CategoryDetail updatedCategory = Transaction.performInNew(() -> categoryDao.getCategory(25));
    assertThat(updatedCategory.getId(), is(categoryToUpdate.getId()));
    assertThat(updatedCategory.getUserId(), is(categoryToUpdate.getUserId()));
    assertThat(updatedCategory.hasPosition(), is(categoryToUpdate.hasPosition()));
    assertThat(updatedCategory.getPosition(), is(26));
  }

  @Test
  public void updateUserCategoryThatHadNoPosition() {
    final CategoryDetail categoryToUpdate = Transaction.performInNew(() -> {
      CategoryDetail category = categoryDao.getCategory(1);
      assertThat(category.getId(), is(1));
      assertThat(category.getUserId(), is(USER_ID_WITHOUT_POSITION));
      assertThat(category.hasPosition(), is(false));
      assertThat(category.getPosition(), is(0));
      category.setHasPosition(true);
      category.setPosition(26);
      categoryDao.update(category);
      return category;
    });
    CategoryDetail updatedCategory = Transaction.performInNew(() -> categoryDao.getCategory(1));
    assertThat(updatedCategory.getId(), is(categoryToUpdate.getId()));
    assertThat(updatedCategory.getUserId(), is(categoryToUpdate.getUserId()));
    assertThat(updatedCategory.hasPosition(), is(true));
    assertThat(updatedCategory.getPosition(), is(26));
  }

  @Test
  public void deleteUserCategory() throws Exception {
    getAllUserCategories();
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    Transaction.performInNew(() -> {
      categoryDao.deleteCategory(10);
      return null;
    });
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "13/22", "25/23", "25/24");
    List<CategoryDetail> result = categoryDao.getAllCategoriesByUser(USER_ID_WITH_POSITIONS);
    assertThat(extractIds(result), containsInAnyOrder(13, 25));
    result = categoryDao.getAllCategoriesByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractIds(result), containsInAnyOrder(1));
  }

  @Test
  public void deleteAllUserCategories() throws Exception {
    getAllUserCategories();
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14", "10/21", "13/22", "25/23", "25/24");
    Transaction.performInNew(() -> {
      categoryDao.deleteUserData(USER_ID_WITH_POSITIONS);
      return null;
    });
    assertOfCouples(getAllOfCouples(),
        "1/2", "1/4", "3/12", "3/15", "4/14");
    List<CategoryDetail> result = categoryDao.getAllCategoriesByUser(USER_ID_WITH_POSITIONS);
    assertThat(result, empty());
    result = categoryDao.getAllCategoriesByUser(USER_ID_WITHOUT_POSITION);
    assertThat(extractIds(result), containsInAnyOrder(1));
  }

  /*
  TOOL METHODS
   */

  private List<Integer> extractIds(List<CategoryDetail> categories) {
    return categories.stream().map(CategoryDetail::getId).collect(Collectors.toList());
  }
}
