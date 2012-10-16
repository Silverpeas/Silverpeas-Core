/*
 *  Copyright (C) 2000 - 2012 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.sharing.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.sharing.security.ShareableAttachment;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;

import static com.silverpeas.jcrutil.JcrConstants.NT_FOLDER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-sharing-datasource.xml", "/spring-sharing-service.xml",
  "/spring-pure-memory-jcr.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SimpleFileAccessControlTest {

  private static ReplacementDataSet dataSet;
  private static final String instanceId = "kmelia2";

  public SimpleFileAccessControlTest() {
  }

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(JpaSharingTicketService.class.getClassLoader().
        getResourceAsStream("com/silverpeas/sharing/services/sharing_security_dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    DBUtil.clearTestInstance();
  }
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;
  private boolean registred = false;
  @Resource
  private Repository repository;

  public Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  public Repository getRepository() {
    return this.repository;
  }

  @Before
  public void generalSetUp() throws Exception {
    InitialContext context = new InitialContext();
    context.rebind(JNDINames.ATTACHMENT_DATASOURCE, dataSource);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
    if (!registred) {
      Reader reader = null;
      try {
        reader = new InputStreamReader(SimpleFileAccessControlTest.class.getClassLoader().
            getResourceAsStream("silverpeas-jcr.txt"));
        SilverpeasRegister.registerNodeTypes(reader);
      } finally {
        IOUtils.closeQuietly(reader);
      }
      registred = true;
      DBUtil.getInstanceForTest(dataSource.getConnection());
    }
    Session session = null;
    try {
      session = getRepository().login(new SilverpeasSystemCredentials());
      if (!session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().addNode(instanceId, NT_FOLDER);
      }
      session.save();
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @After
  public void cleanRepository() throws RepositoryException {
    Session session = null;
    try {
      session = getRepository().login(new SilverpeasSystemCredentials());
      if (session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().getNode(instanceId).remove();
      }
      session.save();
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @AfterClass
  public static void generalCleanUp() throws Exception {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
  }

  /**
   * Test of isReadable method, of class SimpleFileAccessControl.
   */
  @Test
  public void testIsReadable() {
    String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    SimpleDocument attachment = createFrenchSimpleAttachment();
    ShareableAttachment resource = new ShareableAttachment(token, attachment);
    SimpleFileAccessControl instance = new SimpleFileAccessControl();
    boolean expResult = true;
    boolean result = instance.isReadable(resource);
    assertThat(result, is(expResult));
  }

  @Test
  public void testIsNotReadable() {
    createFrenchSimpleAttachment();
    SimpleDocumentPK pk = new SimpleDocumentPK(null, instanceId);
    pk.setOldSilverpeasId(10);
    SimpleDocument attachment = new SimpleDocument();
    attachment.setPK(pk);
    attachment.setForeignId("15");
    ShareableAttachment resource = new ShareableAttachment(
        "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505", attachment);
    SimpleFileAccessControl instance = new SimpleFileAccessControl();
    boolean expResult = false;
    boolean result = instance.isReadable(resource);
    assertThat(result, is(expResult));
  }

  private SimpleDocument createFrenchSimpleAttachment() {
    String language = "fr";
    String fileName = "test.odp";
    String title = "Mon document de test";
    String description = "Ceci est un document de test";
    String creatorId = "10";
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleAttachment file = new SimpleAttachment(fileName, language, title, description,
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        creatorId, creationDate, null);
    SimpleDocumentPK pk = new SimpleDocumentPK(null, instanceId);
    pk.setOldSilverpeasId(5);
    SimpleDocument attachment = new SimpleDocument();
    attachment.setPK(pk);
    attachment.setFile(file);
    attachment.setForeignId("12");
    return AttachmentServiceFactory.getAttachmentService().createAttachment(attachment,
        new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8)));

  }
}
