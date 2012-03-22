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
import java.util.List;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Base class for all Tika OOXML extractors. Tika extractors decorate POI extractors so that the
 * parsed content of documents is returned as a sequence of XHTML SAX events. Subclasses must
 * implement the buildXHTML method {@link #buildXHTML(XHTMLContentHandler)} that populates the
 * {@link XHTMLContentHandler} object received as parameter.
 */
public abstract class AbstractOOXMLExtractor implements OOXMLExtractor {
  static final String RELATION_AUDIO =
      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/audio";
  static final String RELATION_IMAGE =
      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
  static final String RELATION_OLE_OBJECT =
      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/oleObject";
  static final String RELATION_PACKAGE =
      "http://schemas.openxmlformats.org/officeDocument/2006/relationships/package";

  protected POIXMLTextExtractor extractor;

  private final EmbeddedDocumentExtractor embeddedExtractor;

  private final String type;

  public AbstractOOXMLExtractor(ParseContext context, POIXMLTextExtractor extractor, String type) {
    this.extractor = extractor;
    this.type = type;

    EmbeddedDocumentExtractor ex = context.get(EmbeddedDocumentExtractor.class);

    if (ex == null) {
      embeddedExtractor = new ParsingEmbeddedDocumentExtractor(context);
    } else {
      embeddedExtractor = ex;
    }

  }

  /**
   * @see org.apache.tika.parser.microsoft.ooxml.OOXMLExtractor#getDocument()
   */
  public POIXMLDocument getDocument() {
    return extractor.getDocument();
  }

  /**
   * @see org.apache.tika.parser.microsoft.ooxml.OOXMLExtractor#getMetadataExtractor()
   */
  public MetadataExtractor getMetadataExtractor() {
    return new MetadataExtractor(extractor, type);
  }

  /**
   * @see org.apache.tika.parser.microsoft.ooxml.OOXMLExtractor#getXHTML(org.xml.sax.ContentHandler,
   * org.apache.tika.metadata.Metadata)
   */
  public void getXHTML(ContentHandler handler, Metadata metadata, ParseContext context)
      throws SAXException, XmlException, IOException, TikaException {
    XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
    xhtml.startDocument();
    buildXHTML(xhtml);
    xhtml.endDocument();

    // Now do any embedded parts
    List<PackagePart> mainParts = getMainDocumentParts();
    for (PackagePart part : mainParts) {
      PackageRelationshipCollection rels;
      try {
        rels = part.getRelationships();
      } catch (InvalidFormatException e) {
        throw new TikaException("Corrupt OOXML file", e);
      }

      for (PackageRelationship rel : rels) {
        // Is it an embedded type (not part of the document)
        if (rel.getRelationshipType().equals(RELATION_AUDIO) ||
            rel.getRelationshipType().equals(RELATION_IMAGE) ||
            rel.getRelationshipType().equals(RELATION_OLE_OBJECT) ||
            rel.getRelationshipType().equals(RELATION_PACKAGE)) {
          if (rel.getTargetMode() == TargetMode.INTERNAL) {
            PackagePartName relName;
            try {
              relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            } catch (InvalidFormatException e) {
              throw new TikaException("Broken OOXML file", e);
            }
            PackagePart relPart = rel.getPackage().getPart(relName);
            handleEmbedded(rel, relPart, handler, context);
          }
        }
      }
    }
  }

  /**
   * Handles an embedded resource in the file
   */
  protected void handleEmbedded(PackageRelationship rel, PackagePart part,
      ContentHandler handler, ParseContext context)
      throws SAXException, XmlException, IOException, TikaException {
    // Get the name
    String name = rel.getTargetURI().toString();
    if (name.indexOf('/') > -1) {
      name = name.substring(name.lastIndexOf('/') + 1);
    }

    // Get the content type
    String type = part.getContentType();

    // Call the recursing handler
    Metadata metadata = new Metadata();
    metadata.set(Metadata.RESOURCE_NAME_KEY, name);
    metadata.set(Metadata.CONTENT_TYPE, type);

    if (embeddedExtractor.shouldParseEmbedded(metadata)) {
      embeddedExtractor.parseEmbedded(
          TikaInputStream.get(part.getInputStream()),
          new EmbeddedContentHandler(handler),
          metadata, false);
    }
  }

  /**
   * Populates the {@link XHTMLContentHandler} object received as parameter.
   */
  protected abstract void buildXHTML(XHTMLContentHandler xhtml)
      throws SAXException, XmlException, IOException;

  /**
   * Return a list of the main parts of the document, used when searching for embedded resources.
   * This should be all the parts of the document that end up with things embedded into them.
   */
  protected abstract List<PackagePart> getMainDocumentParts()
      throws TikaException;
}
