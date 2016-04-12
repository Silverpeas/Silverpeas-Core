/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.viewer.util;

import org.apache.commons.exec.CommandLine;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecutionException;
import org.silverpeas.core.viewer.service.JsonPdfToolManager;
import org.silverpeas.core.viewer.service.ViewerException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Some centralization about the use of pdf2json tool.
 * @author Yohann Chastagnier
 */
public class JsonPdfUtil extends ExternalExecution {

  public static final String JSON_DOCUMENT_EXTENSION = "js";

  private static final String PDF_TO_JSON_COMMON_PARAMS = "-enc UTF-8 -compress -split 10";

  /**
   * Indicates if tool that permits to convert PDF into JSon data is activated.
   * @return
   */
  public static boolean isActivated() {
    return JsonPdfToolManager.isActivated();
  }

  /**
   * Converts a PDF file into a SWF file.
   * @param pdfFile the pdf file
   * @param destination the destination file without include some path parts in relation to the
   * mechanism of conversion.
   */
  public static void convert(final File pdfFile, final File destination) {
    File out = new File(destination.getParentFile(),
        destination.getName() + "_%." + JSON_DOCUMENT_EXTENSION);
    try {
      if (exec(buildJsonPdfCommandLine(pdfFile, out)).isEmpty()) {
        throw new ViewerException("pdf2json conversion failed...");
      }
    } catch (ExternalExecutionException e) {
      throw new ViewerException(e);
    }
  }

  static CommandLine buildJsonPdfCommandLine(File inputFile, File outputFile) {
    Map<String, File> files = new HashMap<String, File>(2);
    files.put("inputFile", inputFile);
    files.put("outputFile", outputFile);
    CommandLine commandLine = new CommandLine("pdf2json");
    commandLine.addArgument("${inputFile}", false);
    commandLine.addArguments(PDF_TO_JSON_COMMON_PARAMS, false);
    commandLine.addArgument("${outputFile}", false);
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }
}
