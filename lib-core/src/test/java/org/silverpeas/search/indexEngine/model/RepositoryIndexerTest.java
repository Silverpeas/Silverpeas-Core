/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
import org.junit.Test;

import org.silverpeas.search.indexEngine.model.RepositoryIndexer.IndexerAction;
import com.silverpeas.util.PathTestUtil;

/**
 *
 * @author ehugonnet
 */
public class RepositoryIndexerTest {

  private static final RepositoryIndexer instance = new RepositoryIndexer("", "kmelia18");

  public RepositoryIndexerTest() {
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

    instance.pathIndexer(path, creationDate, creatorId, IndexerAction.add);
  }

  /**
   * Test of indexFile method, of class RepositoryIndexer.
   */
  @Test
  public void testIndexTifFile() {
    IndexerAction action = IndexerAction.add;
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
    IndexerAction action = IndexerAction.add;
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
  public void testIndexPsdFile() {
    IndexerAction action = IndexerAction.add;
    String creationDate = "";
    String creatorId = "";
    File file = new File(PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes"
        + File.separatorChar + "large", "xza2_seul.psd");
    instance.indexFile(action, creationDate, creatorId, file);
  }
}
