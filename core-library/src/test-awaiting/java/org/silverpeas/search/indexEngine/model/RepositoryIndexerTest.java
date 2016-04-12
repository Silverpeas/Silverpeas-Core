/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.search.indexEngine.model;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;

import org.silverpeas.core.contribution.attachment.AttachmentServiceTest;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.util.PathTestUtil;

import org.silverpeas.util.DBUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class RepositoryIndexerTest {

  private static final RepositoryIndexer instance = new RepositoryIndexer("", "kmelia18");
  private static EmbeddedDatabase dataSource;
  private static ClassPathXmlApplicationContext context;
  private static JackrabbitRepository repository;

  public RepositoryIndexerTest() {
  }

  @BeforeClass
  public static void loadSpringContext() throws Exception {
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
    Reader reader = new InputStreamReader(AttachmentServiceTest.class.getClassLoader().
        getResourceAsStream("silverpeas-jcr.txt"), Charsets.UTF_8);
    try {
      SimpleMemoryContextFactory.setUpAsInitialContext();
      context = new ClassPathXmlApplicationContext("/spring-pure-memory-jcr.xml");
      repository = context.getBean("repository", JackrabbitRepository.class);

      BasicDaoFactory.getInstance().setApplicationContext(context);
      SilverpeasRegister.registerNodeTypes(reader);
      System.out.println(" -> node types registered");

      EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
      dataSource = builder.setType(EmbeddedDatabaseType.H2).addScript(
          "classpath:/org/silverpeas/attachment/repository/create-database.sql").build();
      DBUtil.getInstanceForTest(dataSource.getConnection());
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  @AfterClass
  public static void tearAlldown() throws Exception {
    repository.shutdown();
    dataSource.shutdown();
    DBUtil.clearTestInstance();
    context.close();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    dataSource.shutdown();
    DBUtil.clearTestInstance();
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
    FileUtils.deleteQuietly(new File(PathTestUtil.BUILD_PATH + "temp"));
  }

  /**
   * Test of pathIndexer method, of class RepositoryIndexer.
   */
  @Test
  public void testPathIndexer() {
    String path = PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes" + File.separatorChar
        + "large";
    String creationDate = "";
    String creatorId = "";

    instance.pathIndexer(path, creationDate, creatorId, RepositoryIndexer.ADD_ACTION);
  }

  /**
   * Test of indexFile method, of class RepositoryIndexer.
   */
  @Test
  public void testIndexTifFile() {
    String action = RepositoryIndexer.ADD_ACTION;
    String creationDate = "";
    String creatorId = "";
    File file = new File(PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes"
        + File.separatorChar + "large", "fond tableau calque.tif");
    instance.indexFile(action, creationDate, creatorId, file);
  }

  /**
   * Test of indexFile method, of class RepositoryIndexer.
   */
  @Test
  public void testIndexTextFile() {
    String action = RepositoryIndexer.ADD_ACTION;
    String creationDate = "";
    String creatorId = "";
    File file = new File(PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes"
        + File.separatorChar + "large", "silverpeas-jcr.txt");
    instance.indexFile(action, creationDate, creatorId, file);
  }

  /**
   * Test of indexFile method, of class RepositoryIndexer.
   */
  @Test
  public void testIndexFileNotClosingIndex() {
    String action = RepositoryIndexer.ADD_ACTION;
    String creationDate = "";
    String creatorId = "";
    File file = new File(PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes"
        + File.separatorChar + "large", "fond tableau calque.tif");
    instance.indexFile(action, creationDate, creatorId, file);
  }
}
