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
 * fileFolderManager.java
 *
 * Created on 17 janvier 2001, 14:38
 */

package com.stratelia.webactiv.util.fileFolder;

/**
 *
 * @author  cbonin
 * @version
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.UtilException;
import org.apache.commons.io.FileUtils;

public class FileFolderManager {

  /**
   * getAllSubFolder : retourne une Collection d'objets File qui representent les repertoires (et
   * seulement les repertoires, pas les fichiers) contenus dans le repertoire passe en parametre
   * Param = chemin du repertoire
   */
  public static Collection getAllSubFolder(String chemin) throws UtilException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite */

    ArrayList resultat = new ArrayList();
    int i = 0;

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();

      while (list != null && i < list.length) {
        if (list[i].isDirectory())
          resultat.add(list[i]);
        i++;
      }
    }

    else {
      SilverTrace.error("util", "FileFolderManager.getAllSubFolder",
          "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllSubFolder",
          "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * getAllFile : retourne une Collection d'objets File qui representent les fichiers (et seulement
   * les fichiers, pas les repertoires) contenus dans le repertoire passe en parametre Param =
   * chemin du repertoire
   */
  public static Collection getAllFile(String chemin) throws UtilException {
    ArrayList resultat = new ArrayList();
    int i = 0;

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();

      while (list != null && i < list.length) {
        if (list[i].isFile())
          resultat.add(list[i]);
        i++;
      }
    }

    else {
      SilverTrace.error("util", "FileFolderManager.getAllFile",
          "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllFile",
          "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * getAllImages : retourne une Collection d'objets File qui representent les fichiers images (type
   * GIF ou JPEG) contenus dans le repertoire passe en parametre Param = chemin du repertoire
   */
  public static Collection getAllImages(String chemin) throws UtilException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep */

    ArrayList resultat = new ArrayList();
    int i = 0;

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();

      while (list != null && i < list.length) {
        if (list[i].isFile()) {
          String fichier = list[i].getName();
          int indexPoint = fichier.lastIndexOf(".");
          String type = fichier.substring(indexPoint + 1);
          if (type.equals("gif") || type.equals("GIF") || type.equals("jpg")
              || type.equals("JPG") || type.equals("png") || type.equals("PNG")
              || type.equals("bmp") || type.equals("BMP") || type.equals("pcd")
              || type.equals("PCD") || type.equals("tga") || type.equals("TGA")
              || type.equals("tif") || type.equals("TIF"))
            resultat.add(list[i]);
        } else if (list[i].isDirectory()) {
          String cheminRep = list[i].getAbsolutePath();
          Collection fich = getAllImages(cheminRep);
          Iterator j = fich.iterator();
          while (j.hasNext()) {
            resultat.add((File) j.next());
          }
        }
        i++;
      }
    }

    else {
      SilverTrace.error("util", "FileFolderManager.getAllImages",
          "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllImages",
          "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * getAllWebPages : retourne une Collection d'objets File qui representent les fichiers du site
   * web contenus dans le repertoire passe en parametre et ses sous repertoires Param = chemin du
   * repertoire du site
   */
  public static Collection getAllWebPages(String chemin) throws UtilException {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep */

    ArrayList resultat = new ArrayList();
    int i = 0;

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();

      while (list != null && i < list.length) {
        if (list[i].isFile()) {
          /*
           * NEWD CBO 22/06/2007 String fichier = list[i].getName(); int indexPoint =
           * fichier.lastIndexOf("."); String type = fichier.substring(indexPoint + 1); if
           * (type.equals("htm") || type.equals("HTM") || type.equals("html") ||
           * type.equals("HTML")) NEWF CBO
           */
          resultat.add(list[i]);
        } else if (list[i].isDirectory()) {
          String cheminRep = list[i].getAbsolutePath();
          Collection fich = getAllWebPages(cheminRep);
          Iterator j = fich.iterator();
          while (j.hasNext()) {
            resultat.add((File) j.next());
          }
        }
        i++;
      }
    }

    else {
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
  public static Collection getAllWebPages2(String chemin) throws Exception {
    /* ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep */

    ArrayList resultat = new ArrayList();
    int i = 0;

    File directory = new File(chemin);
    if (directory.isDirectory()) {
      File[] list = directory.listFiles();

      while (list != null && i < list.length) {
        if (list[i].isFile()) {
          String fichier = list[i].getName();
          int indexPoint = fichier.lastIndexOf(".");
          String type = fichier.substring(indexPoint + 1);
          if ("htm".equals(type.toLowerCase())
              || "html".equals(type.toLowerCase()))
            resultat.add(list[i]);
        }
        i++;
      }
    }

    else {
      SilverTrace.error("util", "FileFolderManager.getAllWebPages2",
          "util.EX_NO_CHEMIN_REPOS", chemin);
      throw new UtilException("FileFolderManager.getAllWebPages2",
          "util.EX_NO_CHEMIN_REPOS", chemin);
    }
    return resultat;
  }

  /**
   * createFolder : creation d'un repertoire Param = chemin du repertoire
   */
  public static void createFolder(String chemin) throws UtilException {
    SilverTrace.info("util", "FileFolderManager.createFolder",
        "root.MSG_GEN_PARAM_VALUE", "chemin=" + chemin);
    File directory = new File(chemin);
    if (directory != null && directory.exists() && directory.isDirectory()) {
    } else {
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
            "util.EX_REPOSITORY_RENAME", cheminRep.toString() + " en "
            + newCheminRep.toString());
        throw new UtilException("FileFolderManager.renameFolder",
            "util.EX_REPOSITORY_RENAME", cheminRep.toString() + " en "
            + newCheminRep.toString());
      }
    }

    else {
      SilverTrace.error("util", "FileFolderManager.renameFolder",
          "util.EX_NO_CHEMIN_REPOS", cheminRep);
      throw new UtilException("FileFolderManager.renameFolder",
          "util.EX_NO_CHEMIN_REPOS", cheminRep);
    }
  }

  /**
   * deleteFolder : destruction d'un repertoire et de tout ce qu'il contient (fichiers et
   * ss-repertoires) Param = chemin du repertoire
   */
  public static void deleteFolder(String chemin) throws Exception {
    deleteFolder(chemin, false);
  }

  /**
   * deleteFolder : destruction d'un repertoire et de tout ce qu'il contient (fichiers et
   * ss-repertoires) Param = chemin du repertoire
   */
  public static void deleteFolder(String chemin, boolean throwException)
      throws UtilException {
    /*
     * ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder
     */

    File directory = new File(chemin);

    /* recupere la liste des fichiers et directory du chemin */
    File[] dirFiles = directory.listFiles();
    for (int i = 0; dirFiles != null && i < dirFiles.length; i++) {
      delDir(dirFiles[i]);
    }
    boolean result = directory.delete();
    if (!result) {
      SilverTrace.info("util", "FileFolderManager.deleteFolder",
          "util.EX_REPOSITORY_DELETE", chemin);
      if (throwException)
        throw new UtilException("FileFolderManager.deleteFolder",
            "util.EX_REPOSITORY_DELETE", chemin);
    }
  }

  /**
   * delDir : procedure privee recursive
   */
  private static void delDir(File dir) {
    if (dir.isDirectory()) {
      File[] dirFiles = dir.listFiles();
      for (int i = 0; dirFiles != null && i < dirFiles.length; i++) {
        delDir(dirFiles[i]);
      }
    }
    dir.delete();
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

        /* ecriture du contenu du fichier */
        /* si le fichier etait deja existant : ecrasement du contenu */
        FileWriter file_write = new FileWriter(file);
        BufferedWriter flux_out = new BufferedWriter(file_write);
        flux_out.write(contenuFichier);
        flux_out.close();
        file_write.close();
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
    }

    else {
      SilverTrace.error("util", "FileFolderManager.renameFile",
          "util.EX_NO_CHEMIN_FINCHER", cheminRep + "\\" + name);
      throw new UtilException("fileFolderManager.renameFile",
          "util.EX_NO_CHEMIN_FINCHER", cheminRep + "\\" + name);
    }
  }

  /**
   * deleteFile : destruction d'un fichier Param = chemin du fichier
   */
  public static void deleteFile(String chemin) throws UtilException {
    /*
     * ex chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder \\File.html
     */

    File file = new File(chemin);
    boolean result = file.delete();

    if (!result) {
      SilverTrace.error("util", "FileFolderManager.deleteFile",
          "util.EX_DELETE_FILE_ERROR", chemin);
      throw new UtilException("fileFolderManager.deleteFile",
          "util.EX_DELETE_FILE_ERROR", chemin);
    }
  }

  /**
   * getCode : Récupération du contenu d'un fichier Param = cheminFichier =
   * c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\rep1\\rep2 nomFichier = index.html
   */
  public static String getCode(String cheminFichier, String nomFichier)
      throws UtilException {
    /* res = contenu du fichier : "<HTML> ..." */
    String ligne;
    String res = "";

    File directory = new File(cheminFichier);
    if (directory.isDirectory()) {
      try {
        File file = new File(directory, nomFichier);

        /* lecture du contenu du fichier */
        FileReader file_read = new FileReader(file);
        BufferedReader flux_in = new BufferedReader(file_read);

        while ((ligne = flux_in.readLine()) != null) {
          res = res + ligne;
          // Debug.debug(700, "util fileFolderManager", "getCode : res = "+res,
          // null, null);
        }
        flux_in.close();
        // file_read.close();
      } catch (IOException e) {
        SilverTrace.debug("util", "FileFolderManager.getCode",
            "result = null, fichier absent", e);
        return null;
      }
    }

    else {
      SilverTrace.error("util", "FileFolderManager.deleteFile",
          "util.util.EX_WRONG_CHEMLIN_SPEC", cheminFichier);
      throw new UtilException("fileFolderManager.getCode",
          "util.util.EX_WRONG_CHEMLIN_SPEC", cheminFichier);
    }
    return res;
  }
}