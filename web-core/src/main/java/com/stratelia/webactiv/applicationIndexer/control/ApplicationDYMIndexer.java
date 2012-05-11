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

package com.stratelia.webactiv.applicationIndexer.control;

import java.io.File;
import java.io.FilenameFilter;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer;

/**
 * Executes a partial or full reindexing of spelling indexes
 */
public class ApplicationDYMIndexer extends AbstractIndexer {

  public ApplicationDYMIndexer() {
    setSilverTraceLevel();
  }

  /**
   * Indexes all spelling indexes
   * @throws Exception whether an exception occurred
   */
  public void indexAll() throws Exception {
    indexAllSpaces();
    indexPersonalComponents();
    indexPdc();
  }

  /**
   * Indexes one component
   * @param spaceId space identifier
   * @param componentId component identifier
   * @throws Exception whether an exception occurred
   */
  @Override
  public void indexComponent(String spaceId, String componentId) throws Exception {
    SilverTrace.info(ApplicationDYMIndexer.class.toString(),
        "ApplicationDYMIndexer.indexComponent()",
        "applicationIndexer.MSG_START_INDEXING_COMPONENT",
        "component = " + componentId);
    try {
      String ComponentIndexPath = FileRepositoryManager.getAbsoluteIndexPath(null, componentId);
      DidYouMeanIndexer.createSpellIndexForAllLanguage("content", ComponentIndexPath);
    } catch (Exception e) {
      SilverTrace.error(ApplicationDYMIndexer.class.toString(),
          "ApplicationDYMIndexer.indexComponent()",
          "applicationIndexer.EX_INDEXING_COMPONENT_FAILED", "component = " + componentId, e);
    }
    SilverTrace.info(ApplicationDYMIndexer.class.toString(),
        "ApplicationDYMIndexer.indexComponent()",
        "applicationIndexer.MSG_END_INDEXING_COMPONENT", "component = "
        + componentId);

  }

  /**
   * create spellchecker indexes of personal component by parsing the file system to retrieve
   * existing indexes
   * @param personalComponent personal component name
   */
  @Override
  public void indexPersonalComponent(String personalComponent) {
    SilverTrace.info(ApplicationDYMIndexer.class.toString(),
        "ApplicationDYMIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_START_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
    try {
      File file = new File(FileRepositoryManager.getIndexUpLoadPath());
      FilenameFilter filter = null;
      if ("agenda".equalsIgnoreCase(personalComponent)) {
        filter = new FileFilterAgenda();
      } else if ("todo".equalsIgnoreCase(personalComponent)) {
        filter = new FileFilterTodo();
      } else {
        SilverTrace.error(ApplicationDYMIndexer.class.toString(),
            "ApplicationDYMIndexer.indexPersonalComponent()",
            "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED",
            "personalComponent = " + personalComponent);
        return;
      }
      String[] paths = file.list(filter);
      for (String personalComponentName : paths) {
        String personalComponentIndexPath = FileRepositoryManager.getAbsoluteIndexPath(null,
            personalComponentName);
        DidYouMeanIndexer.createSpellIndex("content", personalComponentIndexPath);
      }

    } catch (Exception e) {
      SilverTrace.error(ApplicationDYMIndexer.class.toString(),
          "ApplicationDYMIndexer.indexPersonalComponent()",
          "applicationIndexer.EX_INDEXING_PERSONAL_COMPONENT_FAILED",
          "personalComponent = " + personalComponent, e);
    }
    SilverTrace.info(ApplicationDYMIndexer.class.toString(),
        "ApplicationDYMIndexer.indexPersonalComponent()",
        "applicationIndexer.MSG_END_INDEXING_PERSONAL_COMPONENT",
        "personalComponent = " + personalComponent);
  }

  /**
   * creates a spellchecker index for the PDC
   */
  public void indexPdc() {
    setSilverTraceLevel();
    String pdcIndexPath = FileRepositoryManager.getAbsoluteIndexPath(null, "pdc");
    DidYouMeanIndexer.createSpellIndexForAllLanguage("content", pdcIndexPath);
  }
}
