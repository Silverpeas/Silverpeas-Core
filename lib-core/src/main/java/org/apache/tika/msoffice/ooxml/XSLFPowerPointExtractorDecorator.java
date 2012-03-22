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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFCommonSlideData;
import org.apache.poi.xslf.usermodel.DrawingParagraph;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.xml.sax.SAXException;

public class XSLFPowerPointExtractorDecorator extends AbstractOOXMLExtractor {

  public XSLFPowerPointExtractorDecorator(ParseContext context, XSLFPowerPointExtractor extractor) {
    super(context, extractor,
        "application/vnd.openxmlformats-officedocument.presentationml.presentation");
  }

  /**
   * @see org.apache.poi.xslf.extractor.XSLFPowerPointExtractor#getText()
   */
  @Override
  protected void buildXHTML(XHTMLContentHandler xhtml) throws SAXException,
      XmlException, IOException {
    XSLFSlideShow slideShow = (XSLFSlideShow) extractor.getDocument();
    XMLSlideShow xmlSlideShow = new XMLSlideShow(slideShow);

    XSLFSlide[] slides = xmlSlideShow.getSlides();
    for (XSLFSlide slide : slides) {
      CTSlide rawSlide = slide._getCTSlide();
      CTSlideIdListEntry slideId = slide._getCTSlideId();

      CTNotesSlide notes = xmlSlideShow._getXSLFSlideShow().getNotes(
          slideId);
      CTCommentList comments = xmlSlideShow._getXSLFSlideShow()
          .getSlideComments(slideId);

      xhtml.startElement("div");
      extractShapeContent(slide.getCommonSlideData(), xhtml);

      if (comments != null) {
        for (CTComment comment : comments.getCmArray()) {
          xhtml.element("p", comment.getText());
        }
      }

      if (notes != null) {
        extractShapeContent(new XSLFCommonSlideData(notes.getCSld()), xhtml);
      }
      xhtml.endElement("div");
    }
  }

  private void extractShapeContent(XSLFCommonSlideData data, XHTMLContentHandler xhtml)
      throws SAXException {
    for (DrawingParagraph p : data.getText()) {
      xhtml.element("p", p.getText().toString());
    }
  }

  /**
   * In PowerPoint files, slides have things embedded in them, and slide drawings which have the
   * images
   */
  @Override
  protected List<PackagePart> getMainDocumentParts() throws TikaException {
    List<PackagePart> parts = new ArrayList<PackagePart>();
    XSLFSlideShow document = (XSLFSlideShow) extractor.getDocument();

    for (CTSlideIdListEntry ctSlide : document.getSlideReferences().getSldIdList()) {
      // Add the slide
      PackagePart slidePart;
      try {
        slidePart = document.getSlidePart(ctSlide);
      } catch (IOException e) {
        throw new TikaException("Broken OOXML file", e);
      } catch (XmlException xe) {
        throw new TikaException("Broken OOXML file", xe);
      }
      parts.add(slidePart);

      // If it has drawings, return those too
      try {
        // TODO Improve when we upgrade POI
        // for(PackageRelationship rel :
        // slidePart.getRelationshipsByType(XSLFRelation.VML_DRAWING.getRelation())) {
        for (PackageRelationship rel : slidePart
            .getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing")) {
          if (rel.getTargetMode() == TargetMode.INTERNAL) {
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            parts.add(rel.getPackage().getPart(relName));
          }
        }
      } catch (InvalidFormatException e) {
        throw new TikaException("Broken OOXML file", e);
      }
    }

    return parts;
  }
}
