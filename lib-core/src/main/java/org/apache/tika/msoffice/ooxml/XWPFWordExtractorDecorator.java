/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apache.tika.msoffice.ooxml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.model.XWPFCommentsDecorator;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.BodyType;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlink;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.WordExtractor;
import org.apache.tika.parser.microsoft.WordExtractor.TagAndStyle;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.xml.sax.SAXException;

public class XWPFWordExtractorDecorator extends AbstractOOXMLExtractor {
  private XWPFDocument document;
  private XWPFStyles styles;

  public XWPFWordExtractorDecorator(ParseContext context, XWPFWordExtractor extractor) {
    super(context, extractor,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    document = (XWPFDocument) extractor.getDocument();
    styles = document.getStyles();
  }

  /**
   * @see org.apache.poi.xwpf.extractor.XWPFWordExtractor#getText()
   */
  @Override
  protected void buildXHTML(XHTMLContentHandler xhtml)
      throws SAXException, XmlException, IOException {
    XWPFHeaderFooterPolicy hfPolicy = document.getHeaderFooterPolicy();

    // headers
    extractHeaders(xhtml, hfPolicy);

    // process text in the order that it occurs in
    extractIBodyText(document, xhtml);

    // then all document tables
    extractFooters(xhtml, hfPolicy);
  }

  private void extractIBodyText(IBody bodyElement, XHTMLContentHandler xhtml)
      throws SAXException, XmlException, IOException {
    for (IBodyElement element : bodyElement.getBodyElements()) {
      if (element instanceof XWPFParagraph) {
        XWPFParagraph paragraph = (XWPFParagraph) element;
        extractParagraph(paragraph, xhtml);
      }
      if (element instanceof XWPFTable) {
        XWPFTable table = (XWPFTable) element;
        extractTable(table, xhtml);
      }
    }
  }

  private void extractParagraph(XWPFParagraph paragraph, XHTMLContentHandler xhtml)
      throws SAXException, XmlException, IOException {
    // If this paragraph is actually a whole new section, then
    // it could have its own headers and footers
    // Check and handle if so
    XWPFHeaderFooterPolicy headerFooterPolicy = null;
    if (paragraph.getCTP().getPPr() != null) {
      CTSectPr ctSectPr = paragraph.getCTP().getPPr().getSectPr();
      if (ctSectPr != null) {
        headerFooterPolicy =
            new XWPFHeaderFooterPolicy(document, ctSectPr);
        extractHeaders(xhtml, headerFooterPolicy);
      }
    }

    // Is this a paragraph, or a heading?
    String tag = "p";
    String styleClass = null;
    if (paragraph.getStyleID() != null) {
      XWPFStyle style = styles.getStyle(
          paragraph.getStyleID()
          );

      TagAndStyle tas = WordExtractor.buildParagraphTagAndStyle(
          style.getName(), paragraph.getPartType() == BodyType.TABLECELL
          );
      tag = tas.getTag();
      styleClass = tas.getStyleClass();
    }

    if (styleClass == null) {
      xhtml.startElement(tag);
    } else {
      xhtml.startElement(tag, "class", styleClass);
    }

    // Attach bookmarks for the paragraph
    // (In future, we might put them in the right place, for now
    // we just put them in the correct paragraph)
    for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
      xhtml.startElement("a", "name", bookmark.getName());
      xhtml.endElement("a");
    }

    // Do the text
    for (XWPFRun run : paragraph.getRuns()) {
      List<String> tags = new ArrayList<String>();
      if (run instanceof XWPFHyperlinkRun) {
        XWPFHyperlinkRun linkRun = (XWPFHyperlinkRun) run;
        XWPFHyperlink link = linkRun.getHyperlink(document);
        if (link != null && link.getURL() != null) {
          xhtml.startElement("a", "href", link.getURL());
          tags.add("a");
        } else if (linkRun.getAnchor() != null && linkRun.getAnchor().length() > 0) {
          xhtml.startElement("a", "href", "#" + linkRun.getAnchor());
          tags.add("a");
        }
      }
      if (run.isBold()) {
        xhtml.startElement("b");
        tags.add("b");
      }
      if (run.isItalic()) {
        xhtml.startElement("i");
        tags.add("i");
      }

      xhtml.characters(run.toString());

      for (int i = tags.size() - 1; i >= 0; i--) {
        xhtml.endElement(tags.get(i));
      }

      // If we have any pictures, output them
      for (XWPFPicture picture : run.getEmbeddedPictures()) {
        if (paragraph.getDocument() != null) {
          XWPFPictureData data = picture.getPictureData();
          if (data != null) {
            xhtml.startElement("img", "src", "embedded:" + data.getFileName());
            xhtml.endElement("img");
          }
        }
      }
    }

    // Now do any comments for the paragraph
    XWPFCommentsDecorator comments = new XWPFCommentsDecorator(paragraph, null);
    String commentText = comments.getCommentText();
    if (commentText != null && commentText.length() > 0) {
      xhtml.characters(commentText);
    }

    String footnameText = paragraph.getFootnoteText();
    if (footnameText != null && footnameText.length() > 0) {
      xhtml.characters(footnameText + "\n");
    }

    // Finish this paragraph
    xhtml.endElement(tag);

    if (headerFooterPolicy != null) {
      extractFooters(xhtml, headerFooterPolicy);
    }
  }

  private void extractTable(XWPFTable table, XHTMLContentHandler xhtml)
      throws SAXException, XmlException, IOException {
    xhtml.startElement("table");
    xhtml.startElement("tbody");
    for (XWPFTableRow row : table.getRows()) {
      xhtml.startElement("tr");
      for (XWPFTableCell cell : row.getTableCells()) {
        xhtml.startElement("td");
        extractIBodyText(cell, xhtml);
        xhtml.endElement("td");
      }
      xhtml.endElement("tr");
    }
    xhtml.endElement("tbody");
    xhtml.endElement("table");
  }

  private void extractFooters(
      XHTMLContentHandler xhtml, XWPFHeaderFooterPolicy hfPolicy)
      throws SAXException {
    // footers
    if (hfPolicy.getFirstPageFooter() != null) {
      xhtml.element("p", hfPolicy.getFirstPageFooter().getText());
    }
    if (hfPolicy.getEvenPageFooter() != null) {
      xhtml.element("p", hfPolicy.getEvenPageFooter().getText());
    }
    if (hfPolicy.getDefaultFooter() != null) {
      xhtml.element("p", hfPolicy.getDefaultFooter().getText());
    }
  }

  private void extractHeaders(
      XHTMLContentHandler xhtml, XWPFHeaderFooterPolicy hfPolicy)
      throws SAXException {
    if (hfPolicy.getFirstPageHeader() != null) {
      xhtml.element("p", hfPolicy.getFirstPageHeader().getText());
    }
    if (hfPolicy.getEvenPageHeader() != null) {
      xhtml.element("p", hfPolicy.getEvenPageHeader().getText());
    }
    if (hfPolicy.getDefaultHeader() != null) {
      xhtml.element("p", hfPolicy.getDefaultHeader().getText());
    }
  }

  /**
   * Word documents are simple, they only have the one main part
   */
  @Override
  protected List<PackagePart> getMainDocumentParts() {
    List<PackagePart> parts = new ArrayList<PackagePart>();
    parts.add(document.getPackagePart());
    return parts;
  }
}
