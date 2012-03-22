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

package org.apache.tika.openoffice;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.xml.AttributeDependantMetadataHandler;
import org.apache.tika.parser.xml.DcXMLParser;
import org.apache.tika.parser.xml.MetadataHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.xpath.CompositeMatcher;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.xml.sax.ContentHandler;

/**
 * Parser for OpenDocument <code>meta.xml</code> files.
 */
public class OpenDocumentMetaParser extends DcXMLParser {

  private static final XPathParser META_XPATH = new XPathParser(
      "meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
  // eg <meta:user-defined meta:name="Info1">Text1</meta:user-defined> becomes custom:Info1=Text1
  public static final String USER_DEFINED_METADATA_NAME_PREFIX = "custom:";

  private static ContentHandler getMeta(
      ContentHandler ch, Metadata md, String name, String element) {
    Matcher matcher = new CompositeMatcher(
        META_XPATH.parse("//meta:" + element),
        META_XPATH.parse("//meta:" + element + "//text()"));
    ContentHandler branch =
        new MatchingContentHandler(new MetadataHandler(md, name), matcher);
    return new TeeContentHandler(ch, branch);
  }

  private static ContentHandler getUserDefined(
      ContentHandler ch, Metadata md) {
    Matcher matcher = new CompositeMatcher(
        META_XPATH.parse("//meta:user-defined/@meta:name"),
        META_XPATH.parse("//meta:user-defined//text()"));
    ContentHandler branch = new MatchingContentHandler(
        new AttributeDependantMetadataHandler(md, "meta:name", USER_DEFINED_METADATA_NAME_PREFIX),
        matcher);
    return new TeeContentHandler(ch, branch);
  }

  private static ContentHandler getStatistic(
      ContentHandler ch, Metadata md, String name, String attribute) {
    Matcher matcher =
        META_XPATH.parse("//meta:document-statistic/@meta:" + attribute);
    ContentHandler branch =
        new MatchingContentHandler(new MetadataHandler(md, name), matcher);
    return new TeeContentHandler(ch, branch);
  }

  protected ContentHandler getContentHandler(ContentHandler ch, Metadata md) {
    // Process the Dublin Core Attributes
    ch = super.getContentHandler(ch, md);
    // Process the OO Meta Attributes
    ch = getMeta(ch, md, Metadata.CREATION_DATE.getName(), "creation-date");
    ch = getMeta(ch, md, Metadata.KEYWORDS, "keyword");
    ch = getMeta(ch, md, Metadata.EDIT_TIME, "editing-duration");
    ch = getMeta(ch, md, "editing-cycles", "editing-cycles");
    ch = getMeta(ch, md, "initial-creator", "initial-creator");
    ch = getMeta(ch, md, "generator", "generator");
    // Process the user defined Meta Attributes
    ch = getUserDefined(ch, md);
    // Process the OO Statistics Attributes
    ch = getStatistic(ch, md, "nbTab", "table-count");
    ch = getStatistic(ch, md, "nbObject", "object-count");
    ch = getStatistic(ch, md, "nbImg", "image-count");
    ch = getStatistic(ch, md, "nbPage", "page-count");
    ch = getStatistic(ch, md, "nbPara", "paragraph-count");
    ch = getStatistic(ch, md, "nbWord", "word-count");
    ch = getStatistic(ch, md, "nbCharacter", "character-count");
    ch = new NSNormalizerContentHandler(ch);
    return ch;
  }

}
