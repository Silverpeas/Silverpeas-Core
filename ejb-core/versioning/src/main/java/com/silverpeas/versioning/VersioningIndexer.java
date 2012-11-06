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

package com.silverpeas.versioning;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;

import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;

/**
 * @author neysseri
 */
public class VersioningIndexer {

  public VersioningIndexer() {
  }

  public void createIndex(Document documentToIndex, DocumentVersion lastVersion)
      throws RemoteException {
    createIndex(documentToIndex, lastVersion, null, null);
  }

  public void createIndex(Document documentToIndex, DocumentVersion lastVersion,
      Date startOfVisibilityPeriod, Date endOfVisibilityPeriod) throws RemoteException {

    SilverTrace.info("versioning", "VersioningIndexer.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "documentToIndex = "
        + documentToIndex.toString());

    String space = documentToIndex.getPk().getSpace();
    String component = documentToIndex.getPk().getComponentName();
    String fk = documentToIndex.getForeignKey().getId();

    try {
      FullIndexEntry indexEntry = new FullIndexEntry(component, "Versioning"
          + documentToIndex.getPk().getId(), fk);
      indexEntry.setTitle(lastVersion.getLogicalName());
      indexEntry.setPreView(documentToIndex.getName() + " v "
          + lastVersion.getMajorNumber() + "." + lastVersion.getMinorNumber());

      // retrieve user who have upload latest version
      String userId = Integer.toString(lastVersion.getAuthorId());
      indexEntry.setCreationUser(userId);
      indexEntry.setCreationDate(lastVersion.getCreationDate());
      if (startOfVisibilityPeriod != null) {
        indexEntry.setStartDate(DateUtil.date2SQLDate(startOfVisibilityPeriod));
      }
      if (endOfVisibilityPeriod != null) {
        indexEntry.setEndDate(DateUtil.date2SQLDate(endOfVisibilityPeriod));
      }

      String path = createPath(space, component) + File.separator
          + lastVersion.getPhysicalName();

      String encoding = null;
      String format = lastVersion.getMimeType();
      String lang = "fr";

      indexEntry.addFileContent(path, encoding, format, lang);

      if (StringUtil.isDefined(lastVersion.getXmlForm())) {
        updateIndexEntryWithXMLFormContent(lastVersion.getPk(), lastVersion
            .getXmlForm(), indexEntry);
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("versioning", "VersioningIndexer.createIndex()",
          "root.EX_INDEX_FAILED");
    }
  }

  public void removeIndex(Document documentToIndex, DocumentVersion lastVersion)
      throws RemoteException {
    String component = documentToIndex.getPk().getComponentName();
    String fk = documentToIndex.getForeignKey().getId();

    IndexEntryPK indexEntry = new IndexEntryPK(component, "Versioning"
        + documentToIndex.getPk().getId(), fk);

    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  /**
   * to create path to version
   * @return String
   * @exception VersioningRuntimeException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public String createPath(String spaceId, String componentId) {
    String[] ctx = { "Versioning" };
    String path = FileRepositoryManager.getAbsolutePath(componentId, ctx);

    try {
      File d = new File(path);
      if (!d.exists()) {
        FileFolderManager.createFolder(path);
      }
      return path;
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningIndexer.createPath()",
          SilverpeasException.ERROR, "root.EX_CANT_CREATE_FILE", e);
    }
  }

  private static void updateIndexEntryWithXMLFormContent(DocumentVersionPK pk,
      String xmlFormName, FullIndexEntry indexEntry) {
    SilverTrace.info("versioning",
        "VersioningIndexer.updateIndexEntryWithXMLFormContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
    try {
      String objectType = "Versioning";
      PublicationTemplate pub = PublicationTemplateManager.getInstance()
          .getPublicationTemplate(indexEntry.getComponent() + ":" + objectType
          + ":" + xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (Exception e) {
      SilverTrace.error("versioning",
          "VersioningIndexer.updateIndexEntryWithXMLFormContent()", "", e);
    }
  }
}
