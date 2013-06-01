/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.viewer.util;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.viewer.SwfToolManager;
import org.silverpeas.viewer.exception.PreviewException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.*;

/**
 * Some centralized tools to use SwfTools API
 * @author Yohann Chastagnier
 */
public class SwfUtil {

  public static final String SWF_DOCUMENT_EXTENSION = "swf";
  public static String PAGE_FILENAME_SEPARATOR = "-";

  private static String OUTPUT_COMMAND = "-o";
  private static String TO_SWF_ENDING_COMMAND = "-f -T 9 -t -s storeallcharacters";

  /**
   * Indicates if Swf utils is activated
   * @return
   */
  public static boolean isActivated() {
    return SwfToolManager.isActivated();
  }

  /**
   * Converts a PDF file into an image file.
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
   * @param fileIn the Swf file
   * @param fileOut the image file
   */
  public static void fromSwfToImage(final File fileIn, final File fileOut) {
    exec(buildSwfToImageCommandLine(fileIn, fileOut));
  }

  /**
   * Converts a PDF file into a SWF file.
   * @param fileIn the pdf file
   * @param fileOut the swf file
   */
  public static void fromPdfToSwf(final File fileIn, final File fileOut) {
    fromPdfToSwf(fileIn, fileOut, false);
  }

  /**
   * Converts a PDF file into a SWF file.
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
    exec(buildPdfToSwfCommandLine(endingCommand, fileIn, outputFile));
  }

  /**
   * Return some document info from a PDF file
   * @param pdfFile
   * @return
   */
  public static DocumentInfo getPdfDocumentInfo(final File pdfFile) {
    return new DocumentInfo().addFromSwfToolsOutput(exec(buildPdfDocumentInfoCommandLine(pdfFile)));
  }

  /**
   * Changes the extension of a file
   * @param fileExtension
   * @return
   */
  private static File changeFileExtension(final File file, final String fileExtension) {
    return new File(
        getFullPath(file.getPath()) + getBaseName(file.getPath()) + '.' + fileExtension);
  }

  /**
   * Centralizing command exececution code
   * @param commandLine
   * @return
   */
  private static List<String> exec(final CommandLine commandLine) {
    SilverTrace.info("util", "SwfUtil.exec", "Command " + commandLine);
    System.out.println(commandLine);
    final List<String> result = new LinkedList<String>();
    final List<String> errors = new LinkedList<String>();
    final Process process;
    try {
      process = Runtime.getRuntime().exec(commandLine.toStrings());
      final Thread errEater = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            errors.addAll(IOUtils.readLines(process.getErrorStream()));
          } catch (final IOException e) {
            throw new PreviewException(e);
          }
        }
      });
      errEater.start();
      final Thread outEater = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            result.addAll(IOUtils.readLines(process.getInputStream()));
          } catch (final IOException e) {
            throw new PreviewException(e);
          }
        }
      });
      outEater.start();
      process.waitFor();
      CollectingLogOutputStream logErrors = new CollectingLogOutputStream(errors);
      int exitStatus = process.exitValue();
      if (exitStatus != 0) {
        throw new RuntimeException(
            "Exit error status : " + exitStatus + " " + logErrors.getMessage());
      }
    } catch (final Exception e) {
      SilverTrace.error("util", "SwfUtil.exec", "Command execution error", e);
      throw new PreviewException(e);
    }
    return result;
  }

  static CommandLine buildPdfToSwfCommandLine(final String endingCommand, File inputFile,
      File outputFile) {
    Map<String, File> files = new HashMap<String, File>();
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
    Map<String, File> files = new HashMap<String, File>();
    files.put("file", file);
    CommandLine commandLine = new CommandLine("pdf2swf");
    commandLine.addArgument("-qq");
    commandLine.addArgument("${file}", false);
    commandLine.addArgument("--info");
    commandLine.setSubstitutionMap(files);
    return commandLine;
  }

  static CommandLine buildSwfToImageCommandLine(File inputFile, File outputFile) {
    Map<String, File> files = new HashMap<String, File>();
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
