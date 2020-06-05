/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.index;

import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.index.indexing.model.DidYouMeanIndexer;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.index.tools.FileFilterAgenda;
import org.silverpeas.core.web.index.tools.FileFilterTodo;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Executes a partial or full reindexing of spelling indexes
 */
public class ApplicationDYMIndexer extends AbstractIndexer {

  protected ApplicationDYMIndexer() {
  }

  public static ApplicationDYMIndexer getInstance() {
    return ServiceProvider.getService(ApplicationDYMIndexer.class);
  }

  /**
   * Indexes all spelling indexes
   * @throws Exception whether an exception occurred
   */
  @Override
  protected void indexAllData() {
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
  public void indexComponent(String spaceId, String componentId) {
    try {
      String ComponentIndexPath = IndexFileManager.getAbsoluteIndexPath(componentId);
      DidYouMeanIndexer.createSpellIndexForAllLanguage("content", ComponentIndexPath);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("failure while indexing component with id ''{0}''", new String[]{componentId}, e);
    }
  }

  /**
   * create spellchecker indexes of personal component by parsing the file system to retrieve
   * existing indexes
   * @param personalComponent personal component name
   */
  @Override
  public void indexPersonalComponent(String personalComponent) {
    try {
      File file = new File(
          IndexFileManager.getIndexUpLoadPath());
      FilenameFilter filter;
      if ("agenda".equalsIgnoreCase(personalComponent)) {
        filter = new FileFilterAgenda();
      } else if ("todo".equalsIgnoreCase(personalComponent)) {
        filter = new FileFilterTodo();
      } else {
        SilverLogger.getLogger(this)
            .error("failure while indexing personal component of type ''{0}''", personalComponent);
        return;
      }
      String[] paths = file.list(filter);
      for (String personalComponentName : paths != null ? paths : new String[0]) {
        String personalComponentIndexPath = IndexFileManager
            .getAbsoluteIndexPath(personalComponentName);
        DidYouMeanIndexer.createSpellIndex("content", personalComponentIndexPath);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("failure while indexing personal component of type ''{0}''",
              new String[]{personalComponent}, e);
    }
  }

  /**
   * creates a spellchecker index for the PDC
   */
  public void indexPdc() {
    SilverLogger.getLogger(this).debug("starting indexation of PDC");
    String pdcIndexPath = IndexFileManager.getAbsoluteIndexPath("pdc");
    DidYouMeanIndexer.createSpellIndexForAllLanguage("content", pdcIndexPath);
    SilverLogger.getLogger(this).debug("ending indexation of PDC");
  }
}