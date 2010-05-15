/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * Created on 22 févr. 2005
 */
package com.silverpeas.importExport.control;

import java.io.File;
import java.io.IOException;

import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;

/**
 * @author sdevolder
 */
public class TempDirectoryManager {

  /**
   * Méthode purgeant le dossier Temporaire de silverpeas pour une limite ramenée au standard de 2
   * jours
   */
  public static void purgeTempDir() throws IOException {
    purgeTempDir(2);
  }

  /**
   * Méthode récursive purgeant le dossier Temporaire de silverpeas de tous les fichiers datés de
   * plus de nbJours. Tous les répertoires vides à l'issue de cette purge sont également effacés
   * @param nbJour - nombre de jours limite pour la conservation d'un fichier
   */
  public static void purgeTempDir(int nbJour) throws IOException {

    // Transformation de temps de conservation de fichier en millisecondes
    long age = System.currentTimeMillis() - (nbJour * 24 * 3600 * 1000);
    // Récupération du dossier Temp
    String pathTempDir = FileRepositoryManager.getTemporaryPath();
    File dir = new File(pathTempDir);
    if (dir.exists()) {
      File[] files = dir.listFiles((FileFilter) new AgeFileFilter(age));
      for (File file : files) {
        if (file.isFile()) {
          FileUtils.forceDelete(file);
        }
      }
    }
  }
}