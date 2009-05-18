/*
 * Created on 16 févr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.silverpeas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * Classe contenant des méthodes statiques de gestion des fichiers zip
 * 
 * @author sdevolder
 */
public class ZipManager {

  /**
   * Méthode nécéssaire car l api ne gère pas les accents. Elle est publique car
   * utilisée dans les classes utilisant ZipManager.
   * 
   * @param text
   * @return
   */
  public static String transformStringToAsciiString(String text) {
    String newText = text.replace('é', 'e');
    newText = newText.replace('è', 'e');
    newText = newText.replace('ë', 'e');
    newText = newText.replace('ê', 'e');
    newText = newText.replace('ö', 'o');
    newText = newText.replace('ô', 'o');
    newText = newText.replace('õ', 'o');
    newText = newText.replace('ò', 'o');
    newText = newText.replace('ï', 'i');
    newText = newText.replace('î', 'i');
    newText = newText.replace('ì', 'i');
    newText = newText.replace('ñ', 'n');
    newText = newText.replace('ü', 'u');
    newText = newText.replace('û', 'u');
    newText = newText.replace('ù', 'u');
    newText = newText.replace('ç', 'c');
    newText = newText.replace('à', 'a');
    newText = newText.replace('ä', 'a');
    newText = newText.replace('ã', 'a');
    newText = newText.replace('â', 'a');
    newText = newText.replace('°', '_');
    return newText;
  }

  /**
   * Méthode compressant au format zip un fichier ou un dossier de façon
   * récursive au format zip
   * 
   * @param filename
   *          - fichier ou dossier à compresser
   * @param outfilename
   *          - fichier zip à creer
   * @return la taille du fichier zip généré en octets
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static long compressPathToZip(String filename, String outfilename)
      throws FileNotFoundException, IOException {
    ZipOutputStream zos = null;
    File file = new File(filename);
    try {
      // Création du flux de sortie du fichier zip
      FileOutputStream os = new FileOutputStream(outfilename);
      // création du flux zip
      zos = new ZipOutputStream(os);
      // zos.setEncoding("Cp437");

      if (file.isFile() || file.list().length == 0) // cas fichier ou cas
                                                    // dossier vide
      {
        byte[] buf = new byte[4096]; // read buffer
        // Création du flux d'entrée du fichier à compresser
        FileInputStream is = new FileInputStream(filename);
        // Création d'un champs ZipEntry pour le fichier à compresser dans le
        // fichier zip à creer
        //
        zos.putNextEntry(new ZipEntry(filename));
        // Lecture du fichier et écriture dans le fichier zip
        int len = 0;
        while ((len = is.read(buf)) > 0) {
          zos.write(buf, 0, len);
        }
        zos.closeEntry();
        is.close();
      } else {// file.isDirectory() == true
        // Récupération du contenu du dossier
        // le cas dossier vide est traité plus haut
        String[] listContenuStringPath = file.list();
        List<File> listcontenuPath = ZipManager.convertListStringToListFile(
            listContenuStringPath, file.getPath());
        for (File file1 : listcontenuPath) {
          // enlever les accents sur le nom des file
          compressPathToZip(file1.getPath(), file.getName() + File.separator
              + file1.getName(), outfilename, zos);
        }
      }
      zos.close();
      // Si je ne ferme pas le stream, je ne peux pas avoir la taille du
      // fichier.
      File fileZip = new File(outfilename);
      return fileZip.length();
    } finally {
      try {
        zos.close();
      } catch (Exception ex) {
      }
    }
  }

  /**
   * Méthode récursive utilisée par compress_path pour la création de fichiers
   * zip
   * 
   * @param filename
   *          - path du fichier ou du sous répertoire à compresser
   * @param fileToCreate
   *          - path de l'éventuel fichier tel qu on le retrouvera dans le zip
   * @param outfilename
   *          - fichier de sortie zip passée par la méthode publique
   * @param zos
   *          - ZipOutputStream passée par la méthode publique
   * @throws FileNotFoundException
   * @throws IOException
   */
  private static void compressPathToZip(String filename, String fileToCreate,
      String outfilename, ZipOutputStream zos) throws FileNotFoundException,
      IOException {
    File file = new File(filename);
    // if ((file.isFile()) || (file.list().length == 0)) //cas fichier ou cas
    // dossier vide: ! zip ne crée les dossiers vides
    if (file.isFile()) // cas fichier
    {
      byte[] buf = new byte[4096]; // read buffer
      // Création du flux d'entrée du fichier à compresser
      FileInputStream is = new FileInputStream(filename);
      // Création d'un champs ZipEntry pour le fichier à compresser dans le
      // fichier zip à creer
      // Utilisation de FileServerUtils.replaceAccentChars car l api ne gère pas
      // les accents
      String name = transformStringToAsciiString(fileToCreate);
      // zos.putNextEntry(new ZipEntry(fileToCreate));
      zos.putNextEntry(new ZipEntry(name));
      // Lecture du fichier et écriture dans le fichier zip
      int len = 0;
      while ((len = is.read(buf)) > 0) {
        zos.write(buf, 0, len);
      }
      zos.closeEntry();
      is.close();
    } else {// file.isDirectory() == true
      // Récupération du contenu du dossier
      // le cas dossier vide est traité plus haut
      String[] listContenuStringPath = file.list();
      List<File> listcontenuPath = ZipManager.convertListStringToListFile(
          listContenuStringPath, file.getPath());
      for (File file1 : listcontenuPath) {
        compressPathToZip(file1.getPath(), fileToCreate
            + /* file.getName()+ */File.separator + file1.getName(),
            outfilename, zos);
      }
    }
  }

  /**
   * Transforme la table des chaines de caractères de nom de fichier en une
   * liste de fichiers pour le chemin passé en paramètre
   * 
   * @param listFileName
   *          - table des nom de fichier sous forme de chaine de caractères.
   * @param path
   *          - chemin des fichiers contenu dans les chaines de caractères.
   * @return renvoie une liste d'objets File pour les noms de fichiers passés en
   *         paramètres
   */
  private static List<File> convertListStringToListFile(String[] listFileName,
      String path) {
    List<File> listFile = new ArrayList<File>();
    if (listFileName == null) {
      return null;
    }
    for (String fileName : listFileName) {
      listFile.add(new File(path + File.separator + fileName));
    }
    return listFile;
  }

  /**
   * Méthode permettant la création et l'organisation d'un fichier zip en lui
   * passant directement un flux d'entrée TODO: A TESTER!!
   * 
   * @param inputStream
   *          - flux de données à enregistrer dans le zip
   * @param filePathNameToCreate
   *          - chemin et nom du fichier porté par les données du flux dans le
   *          zip
   * @param outfilename
   *          - chemin et nom du fichier zip à creer ou compléter
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void compressStreamToZip(InputStream inputStream,
      String filePathNameToCreate, String outfilename)
      throws FileNotFoundException, IOException {

    FileOutputStream os = null;
    ZipOutputStream zos = null;
    try {
      byte[] buf = new byte[4096]; // read buffer

      // Création du flux de sortie du fichier zip
      os = new FileOutputStream(outfilename);
      // création du flux zip
      zos = new ZipOutputStream(os);

      // Création d'un champs ZipEntry pour le fichier à compresser dans le
      // fichier zip à creer
      zos.putNextEntry(new ZipEntry(filePathNameToCreate));

      // Lecture du fichier et écriture dans le fichier zip
      int len = 0;
      while ((len = inputStream.read(buf)) > 0) {
        zos.write(buf, 0, len);
      }
      zos.closeEntry();

      // inputStream.close();//Ce nest pas dans cette méthode que l'on doit
      // fermer le flux d'entrée
      zos.close();
    } finally {
      try {
        zos.close();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Do the work.
   * 
   * @exception IOException
   *              Thrown in unrecoverable error.
   */
  public static void extract(File source, File dest) throws UtilException {
    if (source == null) {
      throw new UtilException("Expand.execute()", SilverpeasException.ERROR,
          "util.EXE_SOURCE_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
    }
    if (dest == null) {
      throw new UtilException("Expand.execute()", SilverpeasException.ERROR,
          "util.EXE_DESTINATION_FILE_ATTRIBUTE_MUST_BE_SPECIFIED");
    }
    extractFile(source, dest);
  }

  /**
   * Extracting a zip format file
   * 
   * @param File
   *          - File to extract
   * @param File
   *          - Destination path
   * @return nothing
   */
  @SuppressWarnings("unchecked")
  private static void extractFile(File srcF, File dir) {
    ZipFile zf = null;
    try {
      zf = new ZipFile(srcF, "cp437");
      // zf = new ZipFile(srcF);
      ZipEntry ze = null;

      Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.getEntries();
      while (entries.hasMoreElements()) {
        ze = (ZipEntry) entries.nextElement();
        File f = new File(dir, Project.translatePath(ze.getName()));
        try {
          // create intermediary directories - sometimes zip don't add them
          File dirF = new File(f.getParent());
          dirF.mkdirs();
          if (ze.isDirectory()) {
            f.mkdirs();
          } else {
            byte[] buffer = new byte[1024];
            int length = 0;
            InputStream zis = zf.getInputStream(ze);
            FileOutputStream fos = new FileOutputStream(f);
            while ((length = zis.read(buffer)) >= 0) {
              fos.write(buffer, 0, length);
            }
            fos.close();
          }
        } catch (FileNotFoundException ex) {
          SilverTrace.warn("util", "ZipManager.extractFile()",
              "root.EX_FILE_NOT_FOUND", "file = " + f.getPath(), ex);
        }
      }
    } catch (IOException ioe) {
      SilverTrace.warn("util", "ZipManager.extractFile()",
          "util.EXE_ERROR_WHILE_EXTRACTING_FILE", "sourceFile = "
              + srcF.getPath(), ioe);
    } finally {
      if (zf != null) {
        try {
          zf.close();
        } catch (IOException e) {
          SilverTrace.warn("util", "ZipManager.expandFile()",
              "util.EXE_ERROR_WHILE_CLOSING_ZIPINPUTSTREAM", null, e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static int getNbFiles(File srcF) {
    ZipFile zf = null;
    int nbFiles = 0;
    try {
      zf = new ZipFile(srcF, "cp437");
      // zf = new ZipFile(srcF);
      ZipEntry ze = null;

      Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.getEntries();
      while (entries.hasMoreElements()) {
        ze = (ZipEntry) entries.nextElement();
        if (!ze.isDirectory())
          nbFiles++;
      }
    } catch (IOException ioe) {
      SilverTrace.warn("util", "ZipManager.getNbFiles()",
          "util.EXE_ERROR_WHILE_COUNTING_FILE", "sourceFile = "
              + srcF.getPath(), ioe);
    } finally {
      if (zf != null) {
        try {
          zf.close();
        } catch (IOException e) {
          SilverTrace.warn("util", "ZipManager.getNbFiles()",
              "util.EXE_ERROR_WHILE_CLOSING_ZIPINPUTSTREAM", null, e);
        }
      }
    }
    return nbFiles;
  }
}
