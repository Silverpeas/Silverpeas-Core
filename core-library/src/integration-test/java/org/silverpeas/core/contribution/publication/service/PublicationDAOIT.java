/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.publication.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.model.PublicationWithStatus;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.DateUtil;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.*;

import static junit.framework.TestCase.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.test.integration.rule.DbSetupRule.getSafeConnection;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class PublicationDAOIT {

  private static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  private static final String DATASET_SQL_SCRIPT = "test-publication-dao-dataset.sql";

  @Inject
  private PublicationDAO publicationDAO;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SQL_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Publication.onWarForTestClass(PublicationDAOIT.class)
        .build();
  }

  /**
   * Test of insertRow method, of class PublicationDAO.
   */
  @Test
  public void testInsertRow() throws Exception {
    try (Connection con = getSafeConnection()) {
      PublicationPK pk = new PublicationPK("500", "kmelia36");
      Calendar now = Calendar.getInstance();
      now.set(Calendar.SECOND, 0);
      now.set(Calendar.MILLISECOND, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.HOUR_OF_DAY, 0);
      Calendar beginDate = RandomGenerator.getCalendarAfter(now);
      Calendar endDate = RandomGenerator.getCalendarAfter(beginDate);
      String name = RandomGenerator.getRandomString();
      String description = RandomGenerator.getRandomString();
      String creatorId = "" + RandomGenerator.getRandomInt();
      int importance = RandomGenerator.getRandomInt(5);
      String version = RandomGenerator.getRandomString();
      String contenu = RandomGenerator.getRandomString();
      StringBuilder buffer = new StringBuilder();
      int nbKeywords = RandomGenerator.getRandomInt(5) + 2;
      for (int i = 0; i < nbKeywords; i++) {
        buffer.append(RandomGenerator.getRandomString());
        if (i < (nbKeywords - 1)) {
          buffer.append(' ');
        }
      }
      String keywords = buffer.toString();
      PublicationDetail detail = PublicationDetail.builder()
          .setPk(pk)
          .setNameAndDescription(name, description)
          .created(now.getTime(), creatorId)
          .setBeginDateTime(beginDate.getTime(), DateUtil.formatTime(beginDate))
          .setEndDateTime(endDate.getTime(), DateUtil.formatTime(endDate))
          .setImportance(importance)
          .setVersion(version)
          .setKeywords(keywords)
          .setContentPagePath(contenu)
          .build();
      publicationDAO.insertRow(con, detail);

      PublicationDetail result = publicationDAO.loadRow(con, pk);
      detail.setUpdateDate(now.getTime());
      detail.setUpdaterId(creatorId);
      detail.setInfoId("0");
      assertEquals(detail.getPK(), result.getPK());
      assertEquals(detail.getAuthor(), result.getAuthor());
      assertEquals(detail.getBeginDate(), result.getBeginDate());
      assertEquals(detail.getBeginHour(), result.getBeginHour());
      assertEquals(detail.getContentPagePath(), result.getContentPagePath());
      assertEquals(detail.getCreationDate(), result.getCreationDate());
      assertEquals(detail.getLastUpdateDate(), result.getCreationDate());
      assertEquals(detail.getCreatorId(), result.getCreatorId());
      assertEquals(detail.getDescription(), result.getDescription());
      assertEquals(detail.getEndDate(), result.getEndDate());
      assertEquals(detail.getEndHour(), result.getEndHour());
      assertEquals(detail.getImportance(), result.getImportance());
      assertEquals(detail.getInfoId(), result.getInfoId());
      assertEquals(detail.getInstanceId(), result.getInstanceId());
      assertEquals(detail.getKeywords(), result.getKeywords());
      assertEquals(detail.getName(), result.getName());
      assertEquals(detail.getStatus(), result.getStatus());
      assertEquals(detail.getTitle(), result.getTitle());
    }
  }

  /**
   * Test of selectByPrimaryKey method, of class PublicationDAO.
   */
  @Test
  public void testSelectByPrimaryKey() throws Exception {
    try (Connection con = getSafeConnection()) {
      PublicationPK primaryKey = new PublicationPK("100", "kmelia200");
      PublicationDetail result = publicationDAO.selectByPrimaryKey(con, primaryKey);
      assertThat(result, is(notNullValue()));
      assertEquals(primaryKey, result.getPK());
      assertEquals("Homer Simpson", result.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
      assertEquals("00:00", result.getBeginHour());
      assertEquals("Contenu de la publication 1", result.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
      assertEquals("100", result.getCreatorId());
      assertEquals("Première publication de test", result.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(result.getEndDate()));
      assertEquals("23:59", result.getEndHour());
      assertEquals(1, result.getImportance());
      assertEquals("0", result.getInfoId());
      assertEquals("kmelia200", result.getInstanceId());
      assertEquals("test", result.getKeywords());
      assertEquals("Publication 1", result.getName());
      assertEquals("Valid", result.getStatus());
      assertEquals("300", result.getValidatorId());
      assertEquals("Publication 1", result.getTitle());
    }
  }

  /**
   * Test of selectByPublicationName method, of class PublicationDAO.
   */
  @Test
  public void testSelectByPublicationName() throws Exception {
    try (Connection con = getSafeConnection()) {
      String name = "Publication 1";
      PublicationPK primaryKey = new PublicationPK(null, "kmelia200");
      PublicationDetail result = publicationDAO.selectByPublicationName(con, primaryKey, name);
      primaryKey = new PublicationPK("100", "kmelia200");
      assertEquals(primaryKey, result.getPK());
      assertEquals("Homer Simpson", result.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
      assertEquals("00:00", result.getBeginHour());
      assertEquals("Contenu de la publication 1", result.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
      assertEquals("100", result.getCreatorId());
      assertEquals("Première publication de test", result.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(result.getEndDate()));
      assertEquals("23:59", result.getEndHour());
      assertEquals(1, result.getImportance());
      assertEquals("0", result.getInfoId());
      assertEquals("kmelia200", result.getInstanceId());
      assertEquals("test", result.getKeywords());
      assertEquals("Publication 1", result.getName());
      assertEquals("Valid", result.getStatus());
      assertEquals("300", result.getValidatorId());
      assertEquals("Publication 1", result.getTitle());
    }
  }

  /**
   * Test of selectByPublicationNameAndNodeId method, of class PublicationDAO.
   */
  @Test
  public void testSelectByPublicationNameAndNodeId() throws Exception {
    try (Connection con = getSafeConnection()) {
      String name = "Publication 1";
      PublicationPK primaryKey = new PublicationPK(null, "kmelia200");
      int nodeId = 110;
      PublicationDetail result = publicationDAO.
          selectByPublicationNameAndNodeId(con, primaryKey, name, nodeId);
      primaryKey = new PublicationPK("100", "kmelia200");
      assertEquals(primaryKey, result.getPK());
      assertEquals("Homer Simpson", result.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
      assertEquals("00:00", result.getBeginHour());
      assertEquals("Contenu de la publication 1", result.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
      assertEquals("100", result.getCreatorId());
      assertEquals("Première publication de test", result.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(result.getEndDate()));
      assertEquals("23:59", result.getEndHour());
      assertEquals(1, result.getImportance());
      assertEquals("0", result.getInfoId());
      assertEquals("kmelia200", result.getInstanceId());
      assertEquals("test", result.getKeywords());
      assertEquals("Publication 1", result.getName());
      assertEquals("Valid", result.getStatus());
      assertEquals("300", result.getValidatorId());
      assertEquals("Publication 1", result.getTitle());
    }
  }

  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  @Test
  public void testSelectByFatherPK_Connection_NodePK() throws Exception {
    try (Connection con = getSafeConnection()) {
      NodePK fatherPK = new NodePK("110", "kmelia200");
      Collection<PublicationDetail> result = publicationDAO.selectByFatherPK(con, fatherPK);
      assertNotNull(result);
      assertEquals(2, result.size());
      Iterator<PublicationDetail> iter = result.iterator();
      PublicationDetail detail = iter.next();
      PublicationPK primaryKey = new PublicationPK("100", "kmelia200");
      assertEquals(primaryKey, detail.getPK());
      assertEquals("Homer Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("00:00", detail.getBeginHour());
      assertEquals("Contenu de la publication 1", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("100", detail.getCreatorId());
      assertEquals("Première publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("23:59", detail.getEndHour());
      assertEquals(1, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals("kmelia200", detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 1", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 1", detail.getTitle());

      detail = iter.next();
      primaryKey = new PublicationPK("101", "kmelia200");
      assertEquals(primaryKey, detail.getPK());
      assertEquals("Bart Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("01:10", detail.getBeginHour());
      assertEquals("Contenu de la publication 2", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("101", detail.getCreatorId());
      assertEquals("2ème publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("20:35", detail.getEndHour());
      assertEquals(5, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals("kmelia200", detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 2", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 2", detail.getTitle());
    }
  }

  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  @Test
  public void testSelectByFatherPK_5args() throws Exception {
    try (Connection con = getSafeConnection()) {
      NodePK fatherPK = new NodePK("110", "kmelia200");
      String userId = "100";
      Collection<PublicationDetail> result =
          publicationDAO.selectByFatherPK(con, fatherPK, null, false, userId);
      assertNotNull(result);
      assertEquals(1, result.size());
      Iterator<PublicationDetail> iter = result.iterator();
      PublicationDetail detail = iter.next();
      PublicationPK primaryKey = new PublicationPK("100", "kmelia200");
      assertEquals(primaryKey, detail.getPK());
      assertEquals("Homer Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("00:00", detail.getBeginHour());
      assertEquals("Contenu de la publication 1", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("100", detail.getCreatorId());
      assertEquals("Première publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("23:59", detail.getEndHour());
      assertEquals(1, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals("kmelia200", detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 1", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 1", detail.getTitle());

      result = publicationDAO.selectByFatherPK(con, fatherPK, null, true, userId);
      assertNotNull(result);
      assertEquals(1, result.size());
      iter = result.iterator();
      detail = iter.next();
      primaryKey = new PublicationPK("100", "kmelia200");
      assertEquals(primaryKey, detail.getPK());
      assertEquals("Homer Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("00:00", detail.getBeginHour());
      assertEquals("Contenu de la publication 1", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("100", detail.getCreatorId());
      assertEquals("Première publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("23:59", detail.getEndHour());
      assertEquals(1, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals("kmelia200", detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 1", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 1", detail.getTitle());
    }
  }

  /**
   * Test of selectByFatherIds method, of class PublicationDAO.
   */
  @Test
  public void testSelectByFatherIds() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> fatherIds = new ArrayList<>();
      fatherIds.add("110");
      String sorting = "P.pubName";
      List<String> status = new ArrayList<>();
      status.add("Valid");
      boolean filterOnVisibilityPeriod = true;
      List<PublicationDetail> result = publicationDAO.selectByFatherIds(con, fatherIds, "kmelia200",
          sorting, status,
          filterOnVisibilityPeriod);
      assertThat(result.size(), is(2));

      // Test on an empty node
      fatherIds.clear();
      fatherIds.add("999");

      result = publicationDAO.selectByFatherIds(con, fatherIds, "kmelia200", sorting, status,
          filterOnVisibilityPeriod);
      assertThat(result.size(), is(0));
    }
  }

  /**
   * Test of selectByStatus method, of class PublicationDAO.
   */
  @Test
  public void testSelectByStatus_3args_1() throws Exception {
    try (Connection con = getSafeConnection()) {
      String status = "Valid";
      Collection<PublicationDetail> result = publicationDAO.selectPublicationsByCriteria(con,
          PublicationCriteria
              .excludingTrashNodeOnComponentInstanceIds("kmelia200")
              .ofStatus(status));
      assertThat(result.size(), is(2));

      status = "Draft";
      result = publicationDAO.selectPublicationsByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds("kmelia200")
          .ofStatus(status));
      assertThat(result.size(), is(0));
    }
  }

  /**
   * Test of selectByStatus method, of class PublicationDAO.
   */
  @Test
  public void testSelectByStatus_3args_2() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = new ArrayList<>();
      componentIds.add("kmelia200");
      componentIds.add("kmelia201");
      String status = "Valid";
      Collection<PublicationDetail> result = publicationDAO.selectPublicationsByCriteria(con,
          PublicationCriteria
              .excludingTrashNodeOnComponentInstanceIds(componentIds)
              .ofStatus(status));
      assertThat(result.size(), is(2));

      status = "Draft";
      result = publicationDAO.selectPublicationsByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(status));
      assertThat(result.size(), is(0));

      status = "Valid";
      componentIds.remove("kmelia200");
      result = publicationDAO.selectPublicationsByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(status));
      assertThat(result.size(), is(0));
    }
  }

  /**
   * Test of loadRow method, of class PublicationDAO.
   */
  @Test
  public void testLoadRow() throws Exception {
    try (Connection con = getSafeConnection()) {
      PublicationPK pk = new PublicationPK("100", "kmelia200");
      PublicationDetail result = publicationDAO.loadRow(con, pk);
      assertEquals(pk, result.getPK());
      assertEquals("Homer Simpson", result.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
      assertEquals("00:00", result.getBeginHour());
      assertEquals("Contenu de la publication 1", result.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
      assertEquals("100", result.getCreatorId());
      assertEquals("Première publication de test", result.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(result.getEndDate()));
      assertEquals("23:59", result.getEndHour());
      assertEquals(1, result.getImportance());
      assertEquals("0", result.getInfoId());
      assertEquals("kmelia200", result.getInstanceId());
      assertEquals("test", result.getKeywords());
      assertEquals("Publication 1", result.getName());
      assertEquals("Valid", result.getStatus());
      assertEquals("300", result.getValidatorId());
      assertEquals("Publication 1", result.getTitle());
    }
  }

  /**
   * Test of changeInstanceId method, of class PublicationDAO.
   */
  @Test
  public void testChangeInstanceId() throws Exception {
    try (Connection con = getSafeConnection()) {
      PublicationPK pk = new PublicationPK("100", "kmelia200");
      PublicationDetail detail = publicationDAO.loadRow(con, pk);
      assertEquals(pk, detail.getPK());
      assertEquals("Homer Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("00:00", detail.getBeginHour());
      assertEquals("Contenu de la publication 1", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("100", detail.getCreatorId());
      assertEquals("Première publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("23:59", detail.getEndHour());
      assertEquals(1, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals("kmelia200", detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 1", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 1", detail.getTitle());
      String targetInstance = "kmelia" + RandomGenerator.getRandomInt(600);
      publicationDAO.changeInstanceId(con, pk, targetInstance);
      pk = new PublicationPK("100", targetInstance);
      detail = publicationDAO.loadRow(con, pk);
      assertEquals(pk, detail.getPK());
      assertEquals("Homer Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("00:00", detail.getBeginHour());
      assertEquals("Contenu de la publication 1", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("100", detail.getCreatorId());
      assertEquals("Première publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("23:59", detail.getEndHour());
      assertEquals(1, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals(targetInstance, detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 1", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 1", detail.getTitle());
    }
  }

  /**
   * Test of storeRow method, of class PublicationDAO.
   */
  @Test
  public void testStoreRow() throws Exception {
    try (Connection con = getSafeConnection()) {
      PublicationPK pk = new PublicationPK("100", "kmelia200");
      PublicationDetail detail = publicationDAO.loadRow(con, pk);
      assertEquals(pk, detail.getPK());
      assertEquals("Homer Simpson", detail.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
      assertEquals("00:00", detail.getBeginHour());
      assertEquals("Contenu de la publication 1", detail.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
      assertEquals("100", detail.getCreatorId());
      assertEquals("Première publication de test", detail.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(detail.getEndDate()));
      assertEquals("23:59", detail.getEndHour());
      assertEquals(1, detail.getImportance());
      assertEquals("0", detail.getInfoId());
      assertEquals("kmelia200", detail.getInstanceId());
      assertEquals("test", detail.getKeywords());
      assertEquals("Publication 1", detail.getName());
      assertEquals("Valid", detail.getStatus());
      assertEquals("300", detail.getValidatorId());
      assertEquals("Publication 1", detail.getTitle());
      Calendar now = Calendar.getInstance();
      now.set(Calendar.SECOND, 0);
      now.set(Calendar.MILLISECOND, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.HOUR_OF_DAY, 0);
      Calendar beginDate = RandomGenerator.getCalendarAfter(now);
      Calendar endDate = RandomGenerator.getCalendarAfter(beginDate);
      String name = RandomGenerator.getRandomString();
      String description = RandomGenerator.getRandomString();
      String creatorId = "" + RandomGenerator.getRandomInt();
      int importance = RandomGenerator.getRandomInt(5);
      String version = RandomGenerator.getRandomString();
      String contenu = RandomGenerator.getRandomString();
      StringBuilder buffer = new StringBuilder();
      int nbKeywords = RandomGenerator.getRandomInt(5) + 2;
      for (int i = 0; i < nbKeywords; i++) {
        buffer.append(RandomGenerator.getRandomString());
        if (i < (nbKeywords - 1)) {
          buffer.append(' ');
        }
      }
      String keywords = buffer.toString();
      detail.setName(name);
      detail.setDescription(description);
      detail.setCreationDate(now.getTime());
      detail.setBeginDate(beginDate.getTime());
      detail.setEndDate(endDate.getTime());
      detail.setCreatorId(creatorId);
      detail.setImportance(importance);
      detail.setVersion(version);
      detail.setKeywords(keywords);
      detail.setContentPagePath(contenu);
      detail.setBeginHour(DateUtil.formatTime(beginDate));
      detail.setEndHour(DateUtil.formatTime(endDate));
      publicationDAO.storeRow(con, detail);
      PublicationDetail result = publicationDAO.loadRow(con, pk);
      detail.setUpdateDate(now.getTime());
      detail.setUpdaterId(creatorId);
      detail.setInfoId("0");
      assertEquals(detail.getPK(), result.getPK());
      assertEquals(detail.getAuthor(), result.getAuthor());
      assertEquals(detail.getBeginDate(), result.getBeginDate());
      assertEquals(detail.getBeginHour(), result.getBeginHour());
      assertEquals(detail.getContentPagePath(), result.getContentPagePath());
      assertEquals(detail.getCreationDate(), result.getCreationDate());
      assertEquals(detail.getLastUpdateDate(), result.getCreationDate());
      assertEquals(detail.getCreatorId(), result.getCreatorId());
      assertEquals(detail.getDescription(), result.getDescription());
      assertEquals(detail.getEndDate(), result.getEndDate());
      assertEquals(detail.getEndHour(), result.getEndHour());
      assertEquals(detail.getImportance(), result.getImportance());
      assertEquals(detail.getInfoId(), result.getInfoId());
      assertEquals(detail.getInstanceId(), result.getInstanceId());
      assertEquals(detail.getKeywords(), result.getKeywords());
      assertEquals(detail.getName(), result.getName());
      assertEquals(detail.getStatus(), result.getStatus());
      assertEquals(detail.getTitle(), result.getTitle());
    }
  }

  /**
   * Test of selectByName method, of class PublicationDAO.
   */
  @Test
  public void testSelectByName() throws Exception {
    try (Connection con = getSafeConnection()) {
      String name = "Publication 1";
      PublicationPK primaryKey = new PublicationPK(null, "kmelia200");
      PublicationDetail result = publicationDAO.selectByName(con, primaryKey, name);
      primaryKey = new PublicationPK("100", "kmelia200");
      assertEquals(primaryKey, result.getPK());
      assertEquals("Homer Simpson", result.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
      assertEquals("00:00", result.getBeginHour());
      assertEquals("Contenu de la publication 1", result.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
      assertEquals("100", result.getCreatorId());
      assertEquals("Première publication de test", result.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(result.getEndDate()));
      assertEquals("23:59", result.getEndHour());
      assertEquals(1, result.getImportance());
      assertEquals("0", result.getInfoId());
      assertEquals("kmelia200", result.getInstanceId());
      assertEquals("test", result.getKeywords());
      assertEquals("Publication 1", result.getName());
      assertEquals("Valid", result.getStatus());
      assertEquals("300", result.getValidatorId());
      assertEquals("Publication 1", result.getTitle());
    }
  }

  /**
   * Test of selectByNameAndNodeId method, of class PublicationDAO.
   */
  @Test
  public void testSelectByNameAndNodeId() throws Exception {
    try (Connection con = getSafeConnection()) {
      PublicationPK pubPK = new PublicationPK("100", "kmelia200");
      String name = "Publication 1";
      int nodeId = 110;
      PublicationDetail result = publicationDAO.selectByNameAndNodeId(con, pubPK, name, nodeId);
      assertEquals(pubPK, result.getPK());
      assertEquals("Homer Simpson", result.getAuthor());
      assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
      assertEquals("00:00", result.getBeginHour());
      assertEquals("Contenu de la publication 1", result.getContentPagePath());
      assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
      assertEquals("100", result.getCreatorId());
      assertEquals("Première publication de test", result.getDescription());
      assertEquals("2120/12/18", DateUtil.formatDate(result.getEndDate()));
      assertEquals("23:59", result.getEndHour());
      assertEquals(1, result.getImportance());
      assertEquals("0", result.getInfoId());
      assertEquals("kmelia200", result.getInstanceId());
      assertEquals("test", result.getKeywords());
      assertEquals("Publication 1", result.getName());
      assertEquals("Valid", result.getStatus());
      assertEquals("300", result.getValidatorId());
      assertEquals("Publication 1", result.getTitle());
    }
  }

  @Test
  public void testGetAllPublicationsIDbyUserid() throws Exception {
    try (Connection con = getSafeConnection()) {

      String user100 = "100";//who created  pub1

      String user200 = "200";//who updated pub1 and pub2
      String pub1Id = "100";

      PublicationDetail detail1 = publicationDAO.loadRow(con, new PublicationPK(pub1Id));

//who created  pub1
      SocialInformationPublication sp1 = new SocialInformationPublication(new PublicationWithStatus(
          (detail1), false));
      assertNotNull("SocialInformationPublication1 must be not null", sp1);
      List<SocialInformation> list100 = new ArrayList<>();
      list100.add(sp1);

      Date begin = DateUtil.parse("2008/11/01");
      Date end = DateUtil.parse("2008/11/30");

      List<SocialInformationPublication> list100DOA =
          publicationDAO.getAllPublicationsIDbyUserid(con,
              user100, begin, end);
      assertEquals("Must be equal", list100.get(0), list100DOA.get(0));

//who created pub2
      String user101 = "101";//who created pub2
      String pub2Id = "101";
      PublicationDetail detail2 = publicationDAO.loadRow(con, new PublicationPK(pub2Id));
      SocialInformationPublication sp2 = new SocialInformationPublication(new PublicationWithStatus(
          (detail2), false));
      assertNotNull("SocialInformationPublication2 must be not null", sp2);

      List<SocialInformation> list101 = new ArrayList<>();
      list101.add(sp2);
      List<SocialInformationPublication> list101DOA =
          publicationDAO.getAllPublicationsIDbyUserid(con,
              user101, begin, end);
      assertThat(list101.get(0), is(list101DOA.get(0)));

      //who updated pub1 and pub2
      begin = DateUtil.parse("2009/11/01");
      end = DateUtil.parse("2009/11/30");
      SocialInformationPublication sp1User200 = new SocialInformationPublication(
          new PublicationWithStatus(
              (detail1), true));
      assertNotNull("SocialInformationPublication2 must be not null", sp1User200);
      SocialInformationPublication sp2User200 = new SocialInformationPublication(
          new PublicationWithStatus(
              (detail2), true));
      assertNotNull("SocialInformationPublication2 must be not null", sp2User200);
      List<SocialInformation> list200 = new ArrayList<>();
      list200.add(sp2User200);
      list200.add(sp1User200);
      List<? extends SocialInformation> list200DOA =
          publicationDAO.getAllPublicationsIDbyUserid(con, user200, begin, end);
      assertEquals("Must be equal", list200.get(0), list200DOA.get(0));
      assertEquals("Must be equal", list200.get(1), list200DOA.get(1));

      // test nbr of elements
      list200DOA = publicationDAO.getAllPublicationsIDbyUserid(con, user200,
          begin, end);
      assertEquals("Must be equal", list200.get(0), list200DOA.get(0));
      List<String> options = new ArrayList<>();
      options.add("kmelia200");
      List<String> myContactsIds = new ArrayList<>();
      myContactsIds.add(user100);
      myContactsIds.add(user200);
      list200DOA = publicationDAO.getSocialInformationsListOfMyContacts(con, myContactsIds,
          options, begin, end);
      assertNotNull("SocialInformationPublication of my contact must be not null", list200DOA);
      assertThat(list200DOA.isEmpty(), is(false));
    }
  }
}
