/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.util.publication.ejb;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.stratelia.webactiv.publication.social.SocialInformationPublication;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationWithStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author ehugonnet
 */
public class PublicationDAOTest extends AbstractTestDao {

  public PublicationDAOTest() {
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    ApplicationContext ctx = new ClassPathXmlApplicationContext("/spring-publication.xml");
    Properties props = new Properties();
    props.load(this.getClass().getClassLoader().getResourceAsStream(
        "jndi.properties"));
    String jndiBaseDir = props.getProperty(Context.PROVIDER_URL).substring(8);
    props = new Properties();
    props.load(this.getClass().getClassLoader().getResourceAsStream(
        "jdbc.properties"));
    String jndiPath = props.getProperty("jndi.name", "");
    File jndiDir = new File(jndiBaseDir + File.separatorChar
        + jndiPath.substring(0, jndiPath.lastIndexOf('/')));
    jndiDir.mkdirs();
    super.setUp();
  }

  /**
   * Test of invalidateLastPublis method, of class PublicationDAO.
   */
  /*
  public void testInvalidateLastPublis() {
  System.out.println("invalidateLastPublis");
  String instanceId = "";
  PublicationDAO.invalidateLastPublis(instanceId);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getNbPubInFatherPKs method, of class PublicationDAO.
   */
  /*
  public void testGetNbPubInFatherPKs() throws Exception {
  System.out.println("getNbPubInFatherPKs");
  Connection con = null;
  Collection fatherPKs = null;
  int expResult = 0;
  int result = PublicationDAO.getNbPubInFatherPKs(con, fatherPKs);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getNbPubByFatherPath method, of class PublicationDAO.
   */
  /*
  public void testGetNbPubByFatherPath() throws Exception {
  System.out.println("getNbPubByFatherPath");
  Connection con = null;
  NodePK fatherPK = null;
  String fatherPath = "";
  int expResult = 0;
  int result = PublicationDAO.getNbPubByFatherPath(con, fatherPK, fatherPath);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getDistribution method, of class PublicationDAO.
   */
  /*
  public void testGetDistribution() throws Exception {
  System.out.println("getDistribution");
  Connection con = getConnection().getConnection();
  String instanceId = "kmelia36";
  String statusSubQuery = "";
  boolean checkVisibility = false;
  Hashtable expResult = null;
  Hashtable result = PublicationDAO.getDistribution(con, instanceId, statusSubQuery,
  checkVisibility);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of insertRow method, of class PublicationDAO.
   */
  public void testInsertRow() throws Exception {
    Connection con = getConnection().getConnection();
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
    PublicationDetail detail = new PublicationDetail(pk, name, description, now.getTime(), beginDate.
        getTime(),
        endDate.getTime(),
        creatorId, importance, version, keywords, contenu);
    detail.setBeginHour(DateUtil.formatTime(beginDate));
    detail.setEndHour(DateUtil.formatTime(endDate));
    PublicationDAO.insertRow(con, detail);
    PublicationDetail result = PublicationDAO.loadRow(con, pk);
    detail.setUpdateDate(now.getTime());
    detail.setUpdaterId(creatorId);
    detail.setInfoId("0");
    assertEquals(detail.getPK(), result.getPK());
    assertEquals(detail.getAuthor(), result.getAuthor());
    assertEquals(detail.getBeginDate(), result.getBeginDate());
    assertEquals(detail.getBeginHour(), result.getBeginHour());
    assertEquals(detail.getContent(), result.getContent());
    assertEquals(detail.getCreationDate(), result.getCreationDate());
    assertEquals(detail.getUpdateDate(), result.getCreationDate());
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

  /**
   * Test of deleteRow method, of class PublicationDAO.
   */
  /*
  public void testDeleteRow() throws Exception {
  System.out.println("deleteRow");
  Connection con = null;
  PublicationPK pk = null;
  PublicationDAO.deleteRow(con, pk);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByPrimaryKey method, of class PublicationDAO.
   */
  public void testSelectByPrimaryKey() throws Exception {
    System.out.println("selectByPrimaryKey");
    Connection con = getConnection().getConnection();
    PublicationPK primaryKey = new PublicationPK("100", "kmelia200");
    PublicationDetail result = PublicationDAO.selectByPrimaryKey(con, primaryKey).pubDetail;
    assertEquals(primaryKey, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of selectByPublicationName method, of class PublicationDAO.
   */
  public void testSelectByPublicationName() throws Exception {
    System.out.println("selectByPublicationName");
    Connection con = getConnection().getConnection();
    String name = "Publication 1";
    PublicationPK primaryKey = new PublicationPK(null, "kmelia200");
    PublicationDetail result =
        PublicationDAO.selectByPublicationName(con, primaryKey, name).pubDetail;
    primaryKey = new PublicationPK("100", "kmelia200");
    assertEquals(primaryKey, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of selectByPublicationNameAndNodeId method, of class PublicationDAO.
   */
  public void testSelectByPublicationNameAndNodeId() throws Exception {
    System.out.println("selectByPublicationNameAndNodeId");
    Connection con = getConnection().getConnection();
    String name = "Publication 1";
    PublicationPK primaryKey = new PublicationPK(null, "kmelia200");
    int nodeId = 110;
    PublicationDetail result = PublicationDAO.selectByPublicationNameAndNodeId(con, primaryKey, name,
        nodeId).pubDetail;
    primaryKey = new PublicationPK("100", "kmelia200");
    assertEquals(primaryKey, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  public void testSelectByFatherPK_Connection_NodePK() throws Exception {
    System.out.println("selectByFatherPK");
    Connection con = getConnection().getConnection();
    ;
    NodePK fatherPK = new NodePK("110", "kmelia200");
    Collection<PublicationDetail> result = PublicationDAO.selectByFatherPK(con, fatherPK);
    assertNotNull(result);
    assertEquals(2, result.size());
    Iterator<PublicationDetail> iter = result.iterator();
    PublicationDetail detail = iter.next();
    PublicationPK primaryKey = new PublicationPK("100", "kmelia200");
    assertEquals(primaryKey, detail.getPK());
    assertEquals("Homer Simpson", detail.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
    assertEquals("00:00", detail.getBeginHour());
    assertEquals("Contenu de la publication 1", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("100", detail.getCreatorId());
    assertEquals("Première publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
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
    assertEquals("Contenu de la publication 2", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("101", detail.getCreatorId());
    assertEquals("2ème publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
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

  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  /*
  public void testSelectByFatherPK_3args_1() throws Exception {
  System.out.println("selectByFatherPK");
  Connection con = null;
  NodePK fatherPK = null;
  boolean filterOnVisibilityPeriod = false;
  Collection expResult = null;
  Collection result = PublicationDAO.selectByFatherPK(con, fatherPK, filterOnVisibilityPeriod);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  /*
  public void testSelectByFatherPK_4args() throws Exception {
  System.out.println("selectByFatherPK");
  Connection con = null;
  NodePK fatherPK = null;
  String sorting = "";
  boolean filterOnVisibilityPeriod = false;
  Collection expResult = null;
  Collection result = PublicationDAO.selectByFatherPK(con, fatherPK, sorting,
  filterOnVisibilityPeriod);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  public void testSelectByFatherPK_5args() throws Exception {
    System.out.println("selectByFatherPK");
    Connection con = getConnection().getConnection();
    NodePK fatherPK = new NodePK("110", "kmelia200");
    String sorting = null;
    boolean filterOnVisibilityPeriod = false;
    String userId = "100";
    Collection<PublicationDetail> result = PublicationDAO.selectByFatherPK(con, fatherPK, sorting,
        filterOnVisibilityPeriod, userId);
    assertNotNull(result);
    assertEquals(1, result.size());
    Iterator<PublicationDetail> iter = result.iterator();
    PublicationDetail detail = iter.next();
    PublicationPK primaryKey = new PublicationPK("100", "kmelia200");
    assertEquals(primaryKey, detail.getPK());
    assertEquals("Homer Simpson", detail.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
    assertEquals("00:00", detail.getBeginHour());
    assertEquals("Contenu de la publication 1", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("100", detail.getCreatorId());
    assertEquals("Première publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
    assertEquals("23:59", detail.getEndHour());
    assertEquals(1, detail.getImportance());
    assertEquals("0", detail.getInfoId());
    assertEquals("kmelia200", detail.getInstanceId());
    assertEquals("test", detail.getKeywords());
    assertEquals("Publication 1", detail.getName());
    assertEquals("Valid", detail.getStatus());
    assertEquals("300", detail.getValidatorId());
    assertEquals("Publication 1", detail.getTitle());

    filterOnVisibilityPeriod = true;
    result = PublicationDAO.selectByFatherPK(con, fatherPK, sorting, filterOnVisibilityPeriod,
        userId);
    assertNotNull(result);
    assertEquals(1, result.size());
    iter = result.iterator();
    detail = iter.next();
    primaryKey = new PublicationPK("100", "kmelia200");
    assertEquals(primaryKey, detail.getPK());
    assertEquals("Homer Simpson", detail.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
    assertEquals("00:00", detail.getBeginHour());
    assertEquals("Contenu de la publication 1", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("100", detail.getCreatorId());
    assertEquals("Première publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
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

  /**
   * Test of selectByFatherPK method, of class PublicationDAO.
   */
  /*
  public void testSelectByFatherPK_3args_2() throws Exception {
  System.out.println("selectByFatherPK");
  Connection con = null;
  NodePK fatherPK = null;
  String sorting = "";
  Collection expResult = null;
  Collection result = PublicationDAO.selectByFatherPK(con, fatherPK, sorting);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectNotInFatherPK method, of class PublicationDAO.
   */
  /*
  public void testSelectNotInFatherPK_Connection_NodePK() throws Exception {
  System.out.println("selectNotInFatherPK");
  Connection con = null;
  NodePK fatherPK = null;
  Collection expResult = null;
  Collection result = PublicationDAO.selectNotInFatherPK(con, fatherPK);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectNotInFatherPK method, of class PublicationDAO.
   */
  /*
  public void testSelectNotInFatherPK_3args() throws Exception {
  System.out.println("selectNotInFatherPK");
  Connection con = null;
  NodePK fatherPK = null;
  String sorting = "";
  Collection expResult = null;
  Collection result = PublicationDAO.selectNotInFatherPK(con, fatherPK, sorting);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByFatherIds method, of class PublicationDAO.
   */
  public void testSelectByFatherIds() throws Exception {
    System.out.println("selectByFatherIds");
    Connection con = getConnection().getConnection();
    List<String> fatherIds = new ArrayList<String>();
    fatherIds.add("110");
    PublicationPK pubPK = new PublicationPK("useless", "kmelia200");
    String sorting = "P.pubName";
    List<String> status = new ArrayList<String>();
    status.add("Valid");
    boolean filterOnVisibilityPeriod = true;
    ArrayList<PublicationDetail> result = PublicationDAO.selectByFatherIds(con, fatherIds, pubPK,
        sorting, status,
        filterOnVisibilityPeriod);
    assertEquals(result.size(), 2);

    // Test on an empty node
    fatherIds.clear();
    fatherIds.add("999");

    result = PublicationDAO.selectByFatherIds(con, fatherIds, pubPK, sorting, status,
        filterOnVisibilityPeriod);
    assertEquals(result.size(), 0);

  }

  /**
   * Test of selectByPublicationPKs method, of class PublicationDAO.
   */
  /*
  public void testSelectByPublicationPKs() throws Exception {
  System.out.println("selectByPublicationPKs");
  Connection con = getConnection().getConnection();
  Collection publicationPKs = null;
  Collection expResult = null;
  Collection result = PublicationDAO.selectByPublicationPKs(con, publicationPKs);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByStatus method, of class PublicationDAO.
   */
  public void testSelectByStatus_3args_1() throws Exception {
    System.out.println("selectByStatus");
    Connection con = getConnection().getConnection();
    PublicationPK pubPK = new PublicationPK("useless", "kmelia200");
    String status = "Valid";
    Collection<PublicationDetail> result = PublicationDAO.selectByStatus(con, pubPK, status);
    assertEquals(result.size(), 2);

    status = "Draft";
    result = PublicationDAO.selectByStatus(con, pubPK, status);
    assertEquals(result.size(), 0);
  }

  /**
   * Test of selectByStatus method, of class PublicationDAO.
   */
  public void testSelectByStatus_3args_2() throws Exception {
    System.out.println("selectByStatus");
    Connection con = getConnection().getConnection();
    List<String> componentIds = new ArrayList<String>();
    componentIds.add("kmelia200");
    componentIds.add("kmelia201");
    String status = "Valid";
    Collection<PublicationDetail> result = PublicationDAO.selectByStatus(con, componentIds, status);
    assertEquals(result.size(), 2);

    status = "Draft";
    result = PublicationDAO.selectByStatus(con, componentIds, status);
    assertEquals(result.size(), 0);

    status = "Valid";
    componentIds.remove("kmelia200");
    result = PublicationDAO.selectByStatus(con, componentIds, status);
    assertEquals(result.size(), 0);
  }

  /**
   * Test of selectPKsByStatus method, of class PublicationDAO.
   */
  /*
  public void testSelectPKsByStatus() throws Exception {
  System.out.println("selectPKsByStatus");
  Connection con = null;
  List componentIds = null;
  String status = "";
  Collection expResult = null;
  Collection result = PublicationDAO.selectPKsByStatus(con, componentIds, status);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectAllPublications method, of class PublicationDAO.
   */
  /*
  public void testSelectAllPublications_Connection_PublicationPK() throws Exception {
  System.out.println("selectAllPublications");
  Connection con = null;
  PublicationPK pubPK = null;
  Collection expResult = null;
  Collection result = PublicationDAO.selectAllPublications(con, pubPK);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectAllPublications method, of class PublicationDAO.
   */
  /*
  public void testSelectAllPublications_3args() throws Exception {
  System.out.println("selectAllPublications");
  Connection con = null;
  PublicationPK pubPK = null;
  String sorting = "";
  Collection expResult = null;
  Collection result = PublicationDAO.selectAllPublications(con, pubPK, sorting);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByBeginDateDescAndStatus method, of class PublicationDAO.
   */
  /*
  public void testSelectByBeginDateDescAndStatus() throws Exception {
  System.out.println("selectByBeginDateDescAndStatus");
  Connection con = null;
  PublicationPK pubPK = null;
  String status = "";
  Collection expResult = null;
  Collection result = PublicationDAO.selectByBeginDateDescAndStatus(con, pubPK, status);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByBeginDateDescAndStatusAndNotLinkedToFatherId method, of class PublicationDAO.
   */
  /*
  public void testSelectByBeginDateDescAndStatusAndNotLinkedToFatherId() throws Exception {
  System.out.println("selectByBeginDateDescAndStatusAndNotLinkedToFatherId");
  Connection con = null;
  PublicationPK pubPK = null;
  String status = "";
  String fatherId = "";
  int fetchSize = 0;
  Collection expResult = null;
  Collection result = PublicationDAO.selectByBeginDateDescAndStatusAndNotLinkedToFatherId(con,
  pubPK, status, fatherId, fetchSize);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of selectByBeginDateDesc method, of class PublicationDAO.
   */
  /*
  public void testSelectByBeginDateDesc() throws Exception {
  System.out.println("selectByBeginDateDesc");
  Connection con = null;
  PublicationPK pubPK = null;
  Collection expResult = null;
  Collection result = PublicationDAO.selectByBeginDateDesc(con, pubPK);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getOrphanPublications method, of class PublicationDAO.
   */
  /*
  public void testGetOrphanPublications() throws Exception {
  System.out.println("getOrphanPublications");
  Connection con = null;
  PublicationPK pubPK = null;
  Collection expResult = null;
  Collection result = PublicationDAO.getOrphanPublications(con, pubPK);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getNotOrphanPublications method, of class PublicationDAO.
   */
  /*
  public void testGetNotOrphanPublications() throws Exception {
  System.out.println("getNotOrphanPublications");
  Connection con = null;
  PublicationPK pubPK = null;
  Collection expResult = null;
  Collection result = PublicationDAO.getNotOrphanPublications(con, pubPK);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of deleteOrphanPublicationsByCreatorId method, of class PublicationDAO.
   */
  /*
  public void testDeleteOrphanPublicationsByCreatorId() throws Exception {
  System.out.println("deleteOrphanPublicationsByCreatorId");
  Connection con = null;
  PublicationPK pubPK = null;
  String creatorId = "";
  PublicationDAO.deleteOrphanPublicationsByCreatorId(con, pubPK, creatorId);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of getUnavailablePublicationsByPublisherId method, of class PublicationDAO.
   */
  /*
  public void testGetUnavailablePublicationsByPublisherId() throws Exception {
  System.out.println("getUnavailablePublicationsByPublisherId");
  Connection con = null;
  PublicationPK pubPK = null;
  String publisherId = "";
  String nodeId = "";
  Collection expResult = null;
  Collection result = PublicationDAO.getUnavailablePublicationsByPublisherId(con, pubPK,
  publisherId, nodeId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of searchByKeywords method, of class PublicationDAO.
   */
  public void testSearchByKeywords() throws Exception {
    Connection con = getConnection().getConnection();
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    PublicationDetail result = PublicationDAO.loadRow(con, pk);
    assertEquals(pk, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of loadRow method, of class PublicationDAO.
   */
  public void testLoadRow() throws Exception {
    Connection con = getConnection().getConnection();
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    PublicationDetail result = PublicationDAO.loadRow(con, pk);
    assertEquals(pk, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of changeInstanceId method, of class PublicationDAO.
   */
  public void testChangeInstanceId() throws Exception {
    Connection con = getConnection().getConnection();
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    PublicationDetail detail = PublicationDAO.loadRow(con, pk);
    assertEquals(pk, detail.getPK());
    assertEquals("Homer Simpson", detail.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
    assertEquals("00:00", detail.getBeginHour());
    assertEquals("Contenu de la publication 1", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("100", detail.getCreatorId());
    assertEquals("Première publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
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
    PublicationDAO.changeInstanceId(con, pk, targetInstance);
    pk = new PublicationPK("100", targetInstance);
    detail = PublicationDAO.loadRow(con, pk);
    assertEquals(pk, detail.getPK());
    assertEquals("Homer Simpson", detail.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
    assertEquals("00:00", detail.getBeginHour());
    assertEquals("Contenu de la publication 1", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("100", detail.getCreatorId());
    assertEquals("Première publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
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

  /**
   * Test of storeRow method, of class PublicationDAO.
   */
  public void testStoreRow() throws Exception {
    Connection con = getConnection().getConnection();
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    PublicationDetail detail = PublicationDAO.loadRow(con, pk);
    assertEquals(pk, detail.getPK());
    assertEquals("Homer Simpson", detail.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(detail.getBeginDate()));
    assertEquals("00:00", detail.getBeginHour());
    assertEquals("Contenu de la publication 1", detail.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(detail.getCreationDate()));
    assertEquals("100", detail.getCreatorId());
    assertEquals("Première publication de test", detail.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(detail.getEndDate()));
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
    detail.setBeginDateAndHour(beginDate.getTime());
    detail.setEndDateAndHour(endDate.getTime());
    detail.setCreatorId(creatorId);
    detail.setImportance(importance);
    detail.setVersion(version);
    detail.setKeywords(keywords);
    detail.setContent(contenu);
    detail.setBeginHour(DateUtil.formatTime(beginDate));
    detail.setEndHour(DateUtil.formatTime(endDate));
    PublicationDAO.storeRow(con, detail);
    PublicationDetail result = PublicationDAO.loadRow(con, pk);
    detail.setUpdateDate(now.getTime());
    detail.setUpdaterId(creatorId);
    detail.setInfoId("0");
    assertEquals(detail.getPK(), result.getPK());
    assertEquals(detail.getAuthor(), result.getAuthor());
    assertEquals(detail.getBeginDate(), result.getBeginDate());
    assertEquals(detail.getBeginHour(), result.getBeginHour());
    assertEquals(detail.getContent(), result.getContent());
    assertEquals(detail.getCreationDate(), result.getCreationDate());
    assertEquals(detail.getUpdateDate(), result.getCreationDate());
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

  /**
   * Test of selectByName method, of class PublicationDAO.
   */
  public void testSelectByName() throws Exception {
    Connection con = getConnection().getConnection();
    String name = "Publication 1";
    PublicationPK primaryKey = new PublicationPK(null, "kmelia200");
    PublicationDetail result = PublicationDAO.selectByName(con, primaryKey, name);
    primaryKey = new PublicationPK("100", "kmelia200");
    assertEquals(primaryKey, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of selectByNameAndNodeId method, of class PublicationDAO.
   */
  public void testSelectByNameAndNodeId() throws Exception {
    Connection con = getConnection().getConnection();
    PublicationPK pubPK = new PublicationPK("100", "kmelia200");
    String name = "Publication 1";
    int nodeId = 110;
    PublicationDetail result = PublicationDAO.selectByNameAndNodeId(con, pubPK, name, nodeId);
    assertEquals(pubPK, result.getPK());
    assertEquals("Homer Simpson", result.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(result.getBeginDate()));
    assertEquals("00:00", result.getBeginHour());
    assertEquals("Contenu de la publication 1", result.getContent());
    assertEquals("2008/11/18", DateUtil.formatDate(result.getCreationDate()));
    assertEquals("100", result.getCreatorId());
    assertEquals("Première publication de test", result.getDescription());
    assertEquals("2020/12/18", DateUtil.formatDate(result.getEndDate()));
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

  /**
   * Test of selectBetweenDate method, of class PublicationDAO.
   */

  /*
  public void testSelectBetweenDate() throws Exception {
  System.out.println("selectBetweenDate");
  Connection con = null;
  String beginDate = "";
  String endDate = "";
  String instanceId = "";
  Collection expResult = null;
  Collection result = PublicationDAO.selectBetweenDate(con, beginDate, endDate, instanceId);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  @Override
  protected String getDatasetFileName() {
    return "test-publication-dao-dataset.xml";
  }

  public void testGetAllPublicationsIDbyUserid() throws Exception {
    Connection con = getConnection().getConnection();
    this.setUp();
    String user100 = "100";//who created  pub1

    String user200 = "200";//who updated pub1 and pub2
    String pub1Id = "100";

    PublicationDetail detail1 = PublicationDAO.loadRow(con, new PublicationPK(pub1Id));

//who created  pub1
    SocialInformationPublication sp1 = new SocialInformationPublication(new PublicationWithStatus(
        (detail1), false));
    assertNotNull("SocialInformationPublication1 must be not null", sp1);
    List<SocialInformation> list100 = new ArrayList<SocialInformation>();
    list100.add(sp1);

    Date begin = DateUtil.parse("2008/11/01");
    Date end = DateUtil.parse("2008/11/30");

    List<SocialInformation> list100DOA = PublicationDAO.getAllPublicationsIDbyUserid(con,
        user100, begin, end);
    assertEquals("Must be equal", list100.get(0), list100DOA.get(0));

//who created pub2
    String user101 = "101";//who created pub2
    String pub2Id = "101";
    PublicationDetail detail2 = PublicationDAO.loadRow(con, new PublicationPK(pub2Id));
    SocialInformationPublication sp2 = new SocialInformationPublication(new PublicationWithStatus(
        (detail2), false));
    assertNotNull("SocialInformationPublication2 must be not null", sp2);

    List<SocialInformation> list101 = new ArrayList<SocialInformation>();
    list101.add(sp2);
    List<SocialInformation> list101DOA = PublicationDAO.getAllPublicationsIDbyUserid(con,
        user101, begin, end);
    assertTrue("Must be equal", list101.get(0).equals(list101DOA.get(0)));

//who updated pub1 and pub2
    begin = DateUtil.parse("2009/11/01");
    end = DateUtil.parse("2009/11/30");
    SocialInformationPublication sp1User200 = new SocialInformationPublication(new PublicationWithStatus(
        (detail1), true));
    assertNotNull("SocialInformationPublication2 must be not null", sp1User200);
    SocialInformationPublication sp2User200 = new SocialInformationPublication(new PublicationWithStatus(
        (detail2), true));
    assertNotNull("SocialInformationPublication2 must be not null", sp2User200);
    List<SocialInformation> list200 = new ArrayList<SocialInformation>();
    list200.add(sp2User200);
    list200.add(sp1User200);
    List<SocialInformation> list200DOA = PublicationDAO.getAllPublicationsIDbyUserid(con,
        user200, begin, end);
    assertEquals("Must be equal", list200.get(0), list200DOA.get(0));
    assertEquals("Must be equal", list200.get(1), list200DOA.get(1));

    // test nbr of elements
    list200DOA = PublicationDAO.getAllPublicationsIDbyUserid(con, user200,
        begin, end);
    assertEquals("Must be equal", list200.get(0), list200DOA.get(0));
    List<String> options = new ArrayList<String>();
    options.add("kmelia200");
    List<String> myContactsIds = new ArrayList<String>();
    myContactsIds.add(user100);
    myContactsIds.add(user200);
    list200DOA = PublicationDAO.getSocialInformationsListOfMyContacts(con, myContactsIds,
        options, begin, end);
    assertNotNull("SocialInformationPublication of my contact must be not null", list200DOA);
    assertTrue(
        "SocialInformationPublication of my contact must be not empty", !list200DOA.isEmpty());
  }
}
