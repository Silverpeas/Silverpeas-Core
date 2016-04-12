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

package org.silverpeas.core.index.indexing.parser.excelParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.silverpeas.core.index.indexing.parser.PipedParser;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javax.inject.Named;

/**
 * ExcelParser parse an excel file
 * @author $Author: neysseri $
 */
@Named("excelParser")
public class ExcelParser extends PipedParser {
  public ExcelParser() {
  }

  /**
   * Read the text content of a pdf file and store it in out to be ready to be indexed.
   * @param out
   * @param path
   * @param encoding
   * @throws IOException
   */
  @Override
  public void outPutContent(Writer out, String path, String encoding) throws IOException {
    FileInputStream file = new FileInputStream(path);
    try {
      POIFSFileSystem fs = new POIFSFileSystem(file);
      HSSFWorkbook workbook = new HSSFWorkbook(fs);

      HSSFSheet sheet;
      for (int nbSheet = 0; nbSheet < workbook.getNumberOfSheets(); nbSheet++) {
        // extract sheet's name
        out.write(workbook.getSheetName(nbSheet));
        sheet = workbook.getSheetAt(nbSheet);
        Iterator<Row> rows = sheet.rowIterator();
        while (rows.hasNext()) {
          Row row = rows.next();
          Iterator<Cell> cells = row.cellIterator();
          while (cells.hasNext()) {
            Cell cell = cells.next();
            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
              out.write(cell.getStringCellValue());
              out.write(' ');
            }
          }
        }
      }
    } catch (IOException ioe) {
      SilverTrace.error("indexing", "ExcelParser.outPutContent()",
          "indexing.MSG_IO_ERROR_WHILE_READING", path, ioe);
    } finally {
      IOUtils.closeQuietly(file);
    }
  }
}