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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * @author sdevolder
 */
public class TempDirectoryManager {

  /**
   * Méthode purgeant le dossier Temporaire de silverpeas pour une limite
   * ramenée au standard de 2 jours
   */
  public static void purgeTempDir() {
    purgeTempDir(2);
  }

  /**
   * Méthode récursive purgeant le dossier Temporaire de silverpeas de tous
   * les fichiers datés de plus de nbJours. Tous les répertoires vides à
   * l'issue de cette purge sont également effacés
   * 
   * @param nbJour
   *          - nombre de jours limite pour la conservation d'un fichier
   */
  public static void purgeTempDir(int nbJour) {

    // Transformation de temps de conservation de fichier en millisecondes
    long ecartTemps = nbJour * 24 * 3600 * 1000;
    // Récupération du dossier Temp
    String pathTempDir = FileRepositoryManager.getTemporaryPath();
    File dir = new File(pathTempDir);
    // Parcours du dossier Temp
    String[] listContenuStringPath = dir.list();
    List listcontenuPath = convertListStringToListFile(listContenuStringPath,
        dir.getPath());
    Iterator itListcontenuPath = listcontenuPath.iterator();
    while (itListcontenuPath.hasNext()) {
      File file = (File) itListcontenuPath.next();
      if (file.isFile()) {
        // Test sur la condition de suppression du fichier
        Date now = new Date();
        if (now.getTime() - file.lastModified() > ecartTemps)
          file.delete();
      } else {// file est un dossier
        purgeTempDir(file, ecartTemps);
      }
    }
  }

  /**
   * Méthode récursive privée utilisée par public void purgeTempDir(int
   * nbJour) purgeant le dossier Temporaire de silverpeas de tous les fichiers
   * datés. de plus de nbJours. Tous les répertoires vides à l'issue de cette
   * purge sont également effacés.
   * 
   * @param dir
   *          - sous dossier du répertoire Temporaire à purger
   * @param ecartTemps
   *          - Durée de conservation des fichiers en millisecondes
   */
  private static void purgeTempDir(File dir, long ecartTemps) {
    // Parcours du dossier dir
    String[] listContenuStringPath = dir.list();
    List listcontenuPath = convertListStringToListFile(listContenuStringPath,
        dir.getPath());
    if (listcontenuPath != null) {
      Iterator itListcontenuPath = listcontenuPath.iterator();
      while (itListcontenuPath.hasNext()) {
        File file = (File) itListcontenuPath.next();
        if (file.isFile()) {
          // Test sur la condition de suppression du fichier
          Date now = new Date();
          if (now.getTime() - file.lastModified() > ecartTemps)
            file.delete();
        } else {// file est un dossier
          purgeTempDir(file, ecartTemps);
        }
      }
    }
    // Si le dossier est maintenant vide on l'efface aussi
    if (dir != null && dir.list() != null && dir.list().length == 0)
      dir.delete();
  }

  /**
   * Transforme la table des chaines de caractères de nom de fichier en une
   * liste de fichiers pour le chemin passé en paramètre
   * 
   * @param listFileName
   *          - table des nom de fichier sous forme de chaine de caractères.
   * @param path
   *          - chemin des fichiers contenu dans les chaines de caractères.
   * @return renvoie une liste d'objets File pour les noms de fichiers passés
   *         en paramètres
   */
  private static List convertListStringToListFile(String[] listFileName,
      String path) {

    List listFile = new ArrayList();

    if (listFileName == null)
      return null;

    for (int i = 0; i < listFileName.length; i++) {
      listFile.add(new File(path + File.separator + listFileName[i]));
    }
    return listFile;
  }
}
