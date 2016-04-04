/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.search.searchEngine;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import org.silverpeas.search.searchEngine.model.DidYouMeanSearcher;
import org.silverpeas.search.searchEngine.model.QueryDescription;

/**
 *
 *
 */
public class TestDidYouMeanSearcher {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    String indexDirectory = ResourceLocator.getGeneralSettingBundle().getString("uploadsIndexPath")
        + File.separatorChar + "kmelia2";
    String base = System.getProperty("basedir");
    File fileDest = new File(indexDirectory);
    try {
      FileUtils.forceDelete(fileDest);
    } catch (IOException ex) {
    }

    File fileSrc =
        new File(base + File.separatorChar + "src" + File.separatorChar + "test"
        + File.separatorChar + "resources" + File.separatorChar + "index" + File.separatorChar
        + "kmelia2");

    try {
      FileUtils.copyDirectory(fileSrc, fileDest);
    } catch (IOException ioEx) {
    }
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.searchEngine.model.DidYouMeanSearcher#suggest(com.stratelia.webactiv.searchEngine.model.QueryDescription)}
   * .
   *
   * @throws ParseException
   * @throws com.stratelia.webactiv.searchEngine.model.ParseException
   */
  @Test
  public final void testSuggest() throws ParseException {
    DidYouMeanSearcher searcher = new DidYouMeanSearcher();
    QueryDescription query = new QueryDescription(
        "\"la pierre coche\"  NOT présentation  jana OR jacq");
    query.setSearchingUser("0");
    query.addSpaceComponentPair(null, "kmelia2");
    //assertEquals("\"tierces couche\" NOT presenter java java", searcher.suggest(query)[0]);

  }
}
