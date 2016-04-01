/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form;

import java.io.PrintWriter;

import org.silverpeas.core.util.URLUtil;

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
        URLUtil.getApplicationURL() +
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
