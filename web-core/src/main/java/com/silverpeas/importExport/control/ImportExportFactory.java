/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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
package com.silverpeas.importExport.control;

import com.stratelia.webactiv.beans.admin.UserDetail;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ehugonnet
 */
public class ImportExportFactory {

  public static GEDImportExport createGEDImportExport(UserDetail curentUserDetail,
      String currentComponentId) {
    Class<? extends GEDImportExport> gedImportExportClass;
    try {
      gedImportExportClass = (Class<? extends GEDImportExport>) Class.forName("com.silverpeas.kmelia.importexport.KmeliaImportExport");
      Constructor gedImportExportConstructor = gedImportExportClass.getConstructor(UserDetail.class,
          String.class);
      return (GEDImportExport) gedImportExportConstructor.newInstance(curentUserDetail,
          currentComponentId);
    } catch (InstantiationException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvocationTargetException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchMethodException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SecurityException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(ImportExportFactory.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
