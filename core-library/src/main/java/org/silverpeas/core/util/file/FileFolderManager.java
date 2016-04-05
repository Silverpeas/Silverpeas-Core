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
package org.silverpeas.core.util.file;

/**
 *
 * @author  cbonin
 * @version
 */

import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.exception.UtilException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FileFolderManager {

  /**
   * retourne une Collection d'objets File qui representent les repertoires (et seulement les
   * repertoires, pas les fichiers) contenus dans le repertoire passe en parametre
   * @param chemin le chemin du repertoire
   * @return une Collection d'objets File qui representent les repertoires (et seulement les
   * repertoires, pas les fichiers) contenus dans le repertoire passe en parametre
   * @throws UtilException
   */
  public static Collection<File> getAllSubFolder(String chemin) throws UtilException {
    List<File> resultat = new ArrayList<File>();
    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();
      for (File file : list) {
        if (file.isDirectory()) {
          resultat.add(file);
        }
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.getAllSubFolder", "util.EX_NO_CHEMIN_REPOS",
          chemin);
      throw new UtilException("FileFolderManager.getAllSubFolder", "util.EX_NO_CHEMIN_REPOS",
          chemin);
    }
    return resultat;
  }

  /**
   * Returns all the files (and only the files, no directory) inside the given directory.
   * @param chemin
   * @return
   * @throws UtilException
   */
  public static Collection<File> getAllFile(String chemin) throws UtilException {
    List<File> resultat = new ArrayList<File>();
    File directory = new File(chemin);
    if (directory.isDirectory()) {
       resultat = new ArrayList<File>(FileUtils.listFiles(directory, null, false));
       Collections.sort(resultat, new NameFileComparator(IOCase.INSENSITIVE));
    } else {
      SilverTrace.error("util", "FileFolderManager.getAllFile", "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllFile", "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * Returns all the image files (and only the files, no directory) inside the given directory.
   * @param chemin
   * @return
   * @throws UtilException
   */
  public static Collection<File> getAllImages(String chemin) throws UtilException {

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      return FileUtil.listFiles(directory, ImageUtil.IMAGE_EXTENTIONS, false, true);
    } else {
      SilverTrace
          .error("util", "FileFolderManager.getAllImages", "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllImages", "util.EX_NO_CHEMIN_REPOS", chemin);
    }
  }

  /**
   * Retourne une Collection d'objets File qui representent les fichiers du site web contenus dans
   * le repertoire passe en parametre et ses sous repertoires
   * @param chemin le chemin du repertoire du site
   * @return une Collection d'objets File qui representent les fichiers du site web contenus dans le
   * repertoire passe en parametre et ses sous repertoires
   * @throws UtilException
   */
  public static Collection<File> getAllWebPages(String chemin) throws UtilException {
    List<File> resultat = new ArrayList<File>();

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();
      for (File file : list) {
        if (file.isFile()) {
          resultat.add(file);
        } else if (file.isDirectory()) {
          String cheminRep = file.getAbsolutePath();
          Collection<File> fich = getAllWebPages(cheminRep);
          for (File page : fich) {
            resultat.add(page);
          }
        }
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.getAllWebPages",
          "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllWebPages",
          "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * getAllWebPages2 : retourne une Collection d'objets File qui representent les fichiers web (type
   * HTML) contenus dans le repertoire passe en parametre et seulement dans ce repertoire Param =
   * chemin du repertoire du site
   */
  public static Collection<File> getAllWebPages2(String chemin) throws Exception {
    List<File> resultat = new ArrayList<File>();

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();
      for (File file : list) {
        if (file.isFile()) {
          String fichier = file.getName();
          int indexPoint = fichier.lastIndexOf(".");
          String type = fichier.substring(indexPoint + 1);
          if ("htm".equals(type.toLowerCase()) || "html".equals(type.toLowerCase())) {
            resultat.add(file);
          }
        }
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.getAllWebPages2",
          "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllWebPages2",
          "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * creation d'un repertoire
   * @param chemin le chemin du repertoire
   * @throws UtilException
   */
  public static void createFolder(String chemin) throws UtilException {

    File directory = new File(chemin);
    if (directory == null || !directory.exists() || directory.isDirectory()) {
      createFolder(directory);
    }
  }

  public static void createFolder(File directory) throws UtilException {
    try {
      FileUtils.forceMkdir(directory);
    } catch (IOException ioex) {
      SilverTrace.error("util", "FileFolderManager.createFolder",
          "util.EX_REPOSITORY_CREATION", directory.getPath(), ioex);
      throw new UtilException("FileFolderManager.createFolder",
          "util.EX_REPOSITORY_CREATION", directory.getPath(), ioex);
    }
  }

  /**
   * renameFolder : modification du nom d'un repertoire Param = chemin du repertoire
   */
  public static void renameFolder(String cheminRep, String newCheminRep)
      throws UtilException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */

    File directory = new File(cheminRep);

    if (directory.isDirectory()) {
      File newDirectory = new File(newCheminRep);
      if (!directory.renameTo(newDirectory)) {
        SilverTrace.error("util", "FileFolderManager.renameFolder",
            "util.EX_REPOSITORY_RENAME", cheminRep + " en "
            + newCheminRep);
        throw new UtilException("FileFolderManager.renameFolder",
            "util.EX_REPOSITORY_RENAME", cheminRep + " en "
            + newCheminRep);
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.renameFolder",
          "util.EX_NO_CHEMIN_REPOS", cheminRep);
      throw new UtilException("FileFolderManager.renameFolder",
          "util.EX_NO_CHEMIN_REPOS", cheminRep);
    }
  }

  /**
   * Deletes the specified directory recursively and quietly.
   * @param chemin the specified directory
   */
  public static void deleteFolder(String chemin) {
    File directory = new File(chemin);
    FileUtils.deleteQuietly(directory);
  }

  /**
   * Deletes the specified directory recursively.
   * @param chemin the specified directory
   * @param throwException set to false if you want to delete quietly - false otherwise.
   * @throws UtilException
   */
  public static void deleteFolder(String chemin, boolean throwException) throws UtilException {
    File directory = new File(chemin);
    boolean result = FileUtils.deleteQuietly(directory);
    if (!result) {

      if (throwException) {
        throw new UtilException("FileFolderManager.deleteFolder", "util.EX_REPOSITORY_DELETE",
            chemin);
      }
    }
  }

  /**
   * createFile : creation d'un fichier Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   * contenuFichier = code du fichier : "<HTML><TITLE>...."
   */
  public static void createFile(String cheminFichier, String nomFichier,
      String contenuFichier) throws UtilException {
    File directory = new File(cheminFichier);
    if (directory.isDirectory()) {
      try {
        /* Création d'un nouveau fichier sous la bonne arborescence */
        File file = new File(directory, nomFichier);
        FileUtils.writeStringToFile(file, contenuFichier, "UTF-8");
      } catch (IOException e) {
        throw new UtilException("FileFolderManager.createFile",
            "util.EX_CREATE_FILE_ERROR", e);
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.createFile",
          "util.EX_CREATE_FILE_ERROR", cheminFichier);
      throw new UtilException("FileFolderManager.createFile",
          "util.EX_CREATE_FILE_ERROR");
    }
  }

  /**
   * renameFile : modification du nom d'un fichier Param = chemin du fichier
   */
  public static void renameFile(String cheminRep, String name, String newName)
      throws UtilException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */

    File file = new File(cheminRep, name);

    if (file.isFile()) {

      File newFile = new File(cheminRep, newName);
      if (!file.renameTo(newFile)) {
        SilverTrace.error("util", "FileFolderManager.renameFile",
            "util.EX_RENAME_FILE_ERROR", name + " en " + cheminRep + "\\"
            + newName);
        throw new UtilException("FileFolderManager.renameFile",
            "util.EX_RENAME_FILE_ERROR", name + " en " + cheminRep + "\\"
            + newName);
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.renameFile",
          "util.EX_NO_CHEMIN_FINCHER", cheminRep + "\\" + name);
      throw new UtilException("fileFolderManager.renameFile",
          "util.EX_NO_CHEMIN_FINCHER", cheminRep + "\\" + name);
    }
  }

  /**
   * Deletes a file.
   * @Param chemin : path to the file
   */
  public static void deleteFile(String chemin) throws UtilException {
    File directory = new File(chemin);
    boolean result = FileUtils.deleteQuietly(directory);
    if (!result) {
      SilverTrace
          .error("util", "FileFolderManager.deleteFile", "util.EX_DELETE_FILE_ERROR", chemin);
      throw new UtilException("fileFolderManager.deleteFile", "util.EX_DELETE_FILE_ERROR", chemin);
    }
  }

  /**
   * getCode : Récupération du contenu d'un fichier Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   */
  public static String getCode(String cheminFichier, String nomFichier)
      throws UtilException {
    File directory = new File(cheminFichier);
    if (directory.isDirectory()) {
      try {
        File file = new File(directory, nomFichier);
        return FileUtils.readFileToString(file, "UTF-8");
      } catch (IOException e) {
        return null;
      }
    } else {
      SilverTrace.error("util", "FileFolderManager.deleteFile",
          "util.util.EX_WRONG_CHEMLIN_SPEC", cheminFichier);
      throw new UtilException("fileFolderManager.getCode",
          "util.util.EX_WRONG_CHEMLIN_SPEC", cheminFichier);
    }
  }

  private FileFolderManager() {
  }
}
