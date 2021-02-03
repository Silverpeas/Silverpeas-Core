/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.web.importexport.control;

import org.silverpeas.core.importexport.control.ImportExport;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.WAAttributeValuePair;

import java.util.List;

public class ExportXMLTask extends ExportTask {

  private final List<WAAttributeValuePair> pksToExport;
  private final String language;
  private final NodePK rootPK;
  private final int mode;

  ExportXMLTask(ImportExportSessionController toAwake, List<WAAttributeValuePair> pks,
      String language, NodePK rootPK, int mode) {
    super(toAwake);
    pksToExport = pks;
    this.language = language;
    this.rootPK = rootPK;
    this.mode = mode;
  }

  @Override
  protected void doExport() {
    try {
      final ImportExport importExport = ServiceProvider.getService(ImportExport.class);
      exportReport =
          importExport.processExport(super.toAwake.getUserDetail(), language, pksToExport,
          rootPK, mode);
    } catch (Exception e) {
      errorOccurred = e;
    } finally {
      markAsEnded();
    }
  }
}
