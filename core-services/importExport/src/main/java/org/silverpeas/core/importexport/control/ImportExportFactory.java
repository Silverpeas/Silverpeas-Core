/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.admin.user.model.UserDetail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.silverpeas.core.util.logging.SilverLogger.*;

/**
 * @author ehugonnet
 */
public class ImportExportFactory {

  public static GEDImportExport createGEDImportExport(UserDetail currentUserDetail,
      String currentComponentId) {
    Class<? extends GEDImportExport> gedImportExportClass;
    try {
      gedImportExportClass = (Class<? extends GEDImportExport>) Class
          .forName("org.silverpeas.components.kmelia.importexport.KmeliaImportExport");
      Constructor gedImportExportConstructor =
          gedImportExportClass.getConstructor(UserDetail.class, String.class);
      return (GEDImportExport) gedImportExportConstructor
          .newInstance(currentUserDetail, currentComponentId);
    } catch (InstantiationException | ClassNotFoundException | SecurityException |
        NoSuchMethodException | InvocationTargetException | IllegalArgumentException |
        IllegalAccessException ex) {
      getLogger(ImportExportFactory.class).error(ex.getMessage(), ex);
    }
    return null;
  }
}
