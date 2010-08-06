package com.silverpeas.form;

import java.io.PrintWriter;

import com.stratelia.silverpeas.peasCore.URLManager;

public class GalleryHelper {

  public static void getJavaScript(String fieldNameFunction, String fieldName, String language,
      PrintWriter out) {
    out.println("var galleryFileWindow=window;");
    out.println("function openGalleryFileManager" + fieldNameFunction + "(){");
    out.println("index = document.getElementById(\"galleryFile_" + fieldName +
        "\").selectedIndex;");
    out.println("var componentId = document.getElementById(\"galleryFile_" + fieldName +
        "\").options[index].value;");
    out.println("if (index != 0){  ");
    out.println("url = \"" +
        URLManager.getApplicationURL() +
        "/gallery/jsp/wysiwygBrowser.jsp?ComponentId=\"+componentId+\"&Language=" +
        language + "&FieldName=" + fieldNameFunction + "\";");
    out.println("windowName = \"GalleryFileWindow\";");
    out.println("width = \"750\";");
    out.println("height = \"580\";");
    out
        .println("windowParams = \"scrollbars=1,directories=0,menubar=0,toolbar=0, alwaysRaised\";");
    out.println("if (!galleryFileWindow.closed && galleryFileWindow.name==windowName)");
    out.println("galleryFileWindow.close();");
    out
        .println("galleryFileWindow = SP_openWindow(url, windowName, width, height, windowParams);");
    out.println("}}");
  }
}
