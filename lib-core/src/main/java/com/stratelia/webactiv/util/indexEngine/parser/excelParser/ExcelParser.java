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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.util.indexEngine.parser.excelParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.PipedParser;

/**
 * ExcelParser parse an excel file
 * @author $Author: neysseri $
 */

public class ExcelParser extends PipedParser {
  public ExcelParser() {
  }

  /**
   * outPutContent read the text content of a pdf file and store it in out to be ready to be indexed
   */
  public void outPutContent(Writer out, String path, String encoding)
      throws IOException {
    POIFSFileSystem fs = null;
    HSSFWorkbook workbook = null;
    FileInputStream file = new FileInputStream(path);
    try {
      fs = new POIFSFileSystem(file);
      workbook = new HSSFWorkbook(fs);

      HSSFSheet sheet = null;
      for (int nbSheet = 0; nbSheet < workbook.getNumberOfSheets(); nbSheet++) {
        // extract sheet's name
        out.write(workbook.getSheetName(nbSheet));
        SilverTrace.debug("indexEngine", "ExcelParser.outputContent",
            "root.MSG_GEN_PARAM_VALUE", "sheetName = "
            + workbook.getSheetName(nbSheet));

        sheet = workbook.getSheetAt(nbSheet);

        Iterator<HSSFRow> rows = sheet.rowIterator();
        HSSFRow row = null;
        while (rows.hasNext()) {
          row = rows.next();
          Iterator<HSSFCell> cells = row.cellIterator();
          HSSFCell cell = null;
          while (cells.hasNext()) {
            cell = cells.next();
            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
              out.write(cell.getStringCellValue());
              out.write(' ');
              SilverTrace.debug("indexEngine", "ExcelParser.outputContent",
                  "root.MSG_GEN_PARAM_VALUE", "cellValue = "
                  + cell.getStringCellValue());
            }
          }
        }
      }
    } catch (IOException ioe) {
      SilverTrace.error("indexEngine", "ExcelParser.outPutContent()",
          "indexEngine.MSG_IO_ERROR_WHILE_READING", path, ioe);
    } finally {
      if (file != null)
        file.close();
    }
  }
}