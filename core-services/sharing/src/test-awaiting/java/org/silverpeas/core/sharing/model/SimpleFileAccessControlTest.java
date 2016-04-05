/*
 *  Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.sharing.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.silverpeas.core.sharing.services.JpaSharingTicketService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.jackrabbit.api.JackrabbitRepository;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import org.silverpeas.core.sharing.security.ShareableAttachment;
import org.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;

import org.silverpeas.util.DBUtil;

import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class SimpleFileAccessControlTest {

  private static ReplacementDataSet dataSet;
  private static final String instanceId = "kmelia2";
  private static final ClassPathXmlApplicationContext context =
      new ClassPathXmlApplicationContext(
      "/spring-sharing-datasource.xml", "/spring-sharing-service.xml", "/spring-pure-memory-jcr.xml");
  private static final DataSource dataSource = context.getBean("jpaDataSource", DataSource.class);
  private boolean registred = false;
  private static Repository repository = context.getBean(Repository.class);

  public SimpleFileAccessControlTest() {
  }

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    SimpleMemoryContextFactory.setUpAsInitialContext();
    InputStream in = JpaSharingTicketService.class.getClassLoader().getResourceAsStream(
        "com/silverpeas/sharing/services/sharing_security_dataset.xml");
    try {
      dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(in));
      dataSet.addReplacementObject("[NULL]", null);
      DBUtil.clearTestInstance();
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public Repository getRepository() {
    return repository;
  }

  @Before
  public void generalSetUp() throws Exception {
    /*InitialContext ic = new InitialContext();
    ic.rebind(JNDINames.ATTACHMENT_DATASOURCE, dataSource);*/
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
    if (!registred) {
      Reader reader = null;
      try {
        reader = new InputStreamReader(SimpleFileAccessControlTest.class.getClassLoader().
            getResourceAsStream("silverpeas-jcr.txt"), CharEncoding.UTF_8);
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
    ((JackrabbitRepository) repository).shutdown();
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
    context.close();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
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
    attachment.setAttachment(file);
    attachment.setForeignId("12");
    return AttachmentServiceProvider.getAttachmentService().createAttachment(attachment,
        new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8)));

  }
}
