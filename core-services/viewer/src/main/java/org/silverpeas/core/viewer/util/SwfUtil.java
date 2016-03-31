/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecutionException;
import org.silverpeas.core.viewer.service.SwfToolManager;
import org.silverpeas.core.viewer.service.ViewerException;

import org.silverpeas.core.util.StringUtil;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import static org.apache.commons.io.FilenameUtils.*;

/**
 * Some centralized tools to use SwfTools API
 *
 * @author Yohann Chastagnier
 */
public class SwfUtil extends ExternalExecution {

  public static final String SWF_DOCUMENT_EXTENSION = "swf";
  public static final String PAGE_FILENAME_SEPARATOR = "-";

  private static final String OUTPUT_COMMAND = "-o";
  private static final String TO_SWF_ENDING_COMMAND = "-f -T 9 -t -s storeallcharacters";

  /**
   * Indicates if pdf2swf tool is activated
   * @return
   */
  public static boolean isPdfToSwfActivated() {
    return SwfToolManager.isActivated();
  }

  /**
   * Indicates if swfrender0 tool is activated
   * @return
   */
  public static boolean isPdfToImageActivated() {
    return isPdfToSwfActivated() && SwfToolManager.isSwfRenderActivated();
  }

  /**
   * Converts a PDF file into an image file.
   *
   * @param fileIn the pdf file
   * @param fileOut the image file
   */
  public static void fromPdfToImage(final File fileIn, final File fileOut) {
    // First Step : converting first page of PDF file into a SWF file
    final File swfFile = changeFileExtension(fileOut, SWF_DOCUMENT_EXTENSION);
    fromPdfToSwf(fileIn, swfFile, false, "-p 1-1");
    // Secong Step : converting SFW file into image file
    fromSwfToImage(swfFile, fileOut);
    FileUtils.deleteQuietly(swfFile);
  }

  /**
   * Converts a SWF file into an image file.
   *
   * @param fileIn the Swf file
   * @param fileOut the image file
   */
  private static void fromSwfToImage(final File fileIn, final File fileOut) {
    try {
      exec(buildSwfToImageCommandLine(fileIn, fileOut));
    } catch (ExternalExecutionException e) {
      throw new ViewerException(e);
    }
  }

  /**
   * Converts a PDF file into a SWF file.
   *
   * @param fileIn the pdf file
   * @param fileOut the swf file
   */
  public static void fromPdfToSwf(final File fileIn, final File fileOut) {
    fromPdfToSwf(fileIn, fileOut, false);
  }

  /**
   * Converts a PDF file into a SWF file.
   *
   * @param fileIn the pdf file
   * @param fileOut the swf file
   * @param oneFilePerPage if true it activates one swf file per page
   */
  public static void fromPdfToSwf(final File fileIn, final File fileOut,
      final boolean oneFilePerPage) {
    fromPdfToSwf(fileIn, fileOut, oneFilePerPage, null);
  }

  /**
   * Converts a PDF file into a SWF file.
   *
   * @param fileIn the pdf file
   * @param fileOut the swf file
   * @param oneFilePerPage if true it activates one swf file per page
   * @param endingCommand
   */
  public static void fromPdfToSwf(final File fileIn, final File fileOut,
      final boolean oneFilePerPage, final String endingCommand) {
    File outputFile = fileOut;
    if (oneFilePerPage) {
      final StringBuilder onePageFile = new StringBuilder(512);
      onePageFile.append(getFullPath(fileOut.getPath()));
      onePageFile.append(getBaseName(fileOut.getPath()));
      onePageFile.append(PAGE_FILENAME_SEPARATOR);
      onePageFile.append("%.");
      onePageFile.append(getExtension(fileOut.getPath()));
      outputFile = new File(onePageFile.toString());
    }
    try {
      exec(buildPdfToSwfCommandLine(endingCommand, fileIn, outputFile));
    } catch (ExternalExecutionException e) {
      throw new ViewerException(e);
    }
  }

  /**
   * Return some document info from a PDF file
   *
   * @param pdfFile
   * @return
   */
  public static DocumentInfo getPdfDocumentInfo(final File pdfFile) {
    List<String> execResult;
    try {
      execResult = exec(buildPdfDocumentInfoCommandLine(pdfFile));
    } catch (ExternalExecutionException e) {
      throw new ExternalExecutionException(e);
    }
    return new DocumentInfo().addFromSwfToolsOutput(execResult);
  }

  /**
   * Changes the extension of a file
   *
   * @param fileExtension
   * @return
   */
  private static File changeFileExtension(final File file, final String fileExtension) {
    return new File(
        getFullPath(file.getPath()) + getBaseName(file.getPath()) + '.' + fileExtension);
  }

  static CommandLine buildPdfToSwfCommandLine(final String endingCommand, File inputFile,
      File outputFile) {
    Map<String, File> files = new HashMap<>(2);
    files.put("inputFile", inputFile);
    files.put("outputFile", outputFile);
    CommandLine commandLine = new CommandLine("pdf2swf");
    commandLine.addArgument("${inputFile}", false);
    commandLine.addArgument(OUTPUT_COMMAND);
    commandLine.addArgument("${outputFile}", false);
    commandLine.addArguments(TO_SWF_ENDING_COMMAND, false);
    if (StringUtil.isDefined(endingCommand)) {
      commandLine.addArguments(endingCommand, false);
    }
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }

  static CommandLine buildPdfDocumentInfoCommandLine(File file) {
    Map<String, File> files = new HashMap<>(1);
    files.put("file", file);
    CommandLine commandLine = new CommandLine("pdf2swf");
    commandLine.addArgument("-qq");
    commandLine.addArgument("${file}", false);
    commandLine.addArgument("--info");
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }

  static CommandLine buildSwfToImageCommandLine(File inputFile, File outputFile) {
    Map<String, File> files = new HashMap<>(2);
    files.put("inputFile", inputFile);
    files.put("outputFile", outputFile);
    CommandLine commandLine = new CommandLine("swfrender");
    commandLine.addArgument("${inputFile}", false);
    commandLine.addArgument(OUTPUT_COMMAND);
    commandLine.addArgument("${outputFile}", false);
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }
}
