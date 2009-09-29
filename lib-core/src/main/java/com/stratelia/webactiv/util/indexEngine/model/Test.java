package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;

public class Test {
  static public void main(String[] args) {
    try {
      File file = new File(args[0]);

      IndexManager indexEngine = new IndexManager();

      indexDirectory(indexEngine, file, 0);

      indexEngine.optimize();

    } catch (Exception e) {
      System.err.println("Can't add an index entry " + e.getMessage());
    }
  }

  /**
   * index a directory where each file is associated to a document
   */

  public static void indexDirectory(IndexManager indexEngine, File file, int cp)
      throws Exception {
    String format;
    String encoding = "US-ASCII";
    if (file.isDirectory()) {
      String[] files = file.list();
      for (int i = 0; i < files.length; i++)
        indexDirectory(indexEngine, new File(file, files[i]), cpt);
    } else {
      FullIndexEntry indexEntry = new FullIndexEntry("test", "none", "none "
          + cp);
      format = getFormat(file);
      indexEntry.setTitle(file.getName());
      indexEntry
          .setPreView("...ceci est le test ou le meilleur est  ce test...");
      indexEntry
          .setKeyWords("... aller la exemple le simple de marche java les publication test...");
      indexEntry.setCreationUser("... momo ...");
      indexEntry.setCreationDate("...05/06/2000...");
      indexEntry.addTextContent("...être téléchargé exténué ...");
      indexEntry.addFileContent(file.getPath(), encoding, format, "none");
      indexEngine.addIndexEntry(indexEntry);
      cpt++;
    }
  }

  /**
   * return the format of a file
   */

  public static String getFormat(File file) throws Exception {
    String Temp = file.getName();
    if ((Temp.endsWith("pdf")) || (Temp.endsWith("PDF")))
      return "application/pdf";
    if ((Temp.endsWith("html")) || (Temp.endsWith("HTML")))
      return "text/html";
    if ((Temp.endsWith("doc")) || (Temp.endsWith("DOC")))
      return "application/msword";
    if ((Temp.endsWith("txt")) || (Temp.endsWith("TXT")))
      return "text/plain";
    if ((Temp.endsWith("xls")) || (Temp.endsWith("XLS")))
      return "application/msexcel";
    if ((Temp.endsWith("ppt")) || (Temp.endsWith("PPT")))
      return "application/mspowerpoint";
    return Temp;
  }

  // The number of a document added
  private static int cpt = 0;

}
