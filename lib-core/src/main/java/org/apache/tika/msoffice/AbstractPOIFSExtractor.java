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

package org.apache.tika.msoffice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.Ole10NativeException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.tika.detect.ZipContainerDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParser.POIFSDocumentType;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.SAXException;

abstract class AbstractPOIFSExtractor {

  private final EmbeddedDocumentExtractor extractor;

  protected AbstractPOIFSExtractor(ParseContext context) {
    EmbeddedDocumentExtractor ex = context.get(EmbeddedDocumentExtractor.class);

    if (ex == null) {
      this.extractor = new ParsingEmbeddedDocumentExtractor(context);
    } else {
      this.extractor = ex;
    }
  }

  protected void handleEmbeddedResource(TikaInputStream resource, String filename,
      String mediaType, XHTMLContentHandler xhtml, boolean outputHtml)
      throws IOException, SAXException, TikaException {
    try {
      Metadata metadata = new Metadata();
      if (filename != null) {
        metadata.set(Metadata.TIKA_MIME_FILE, filename);
        metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
      }
      if (mediaType != null) {
        metadata.set(Metadata.CONTENT_TYPE, mediaType);
      }

      if (extractor.shouldParseEmbedded(metadata)) {
        extractor.parseEmbedded(resource, xhtml, metadata, outputHtml);
      }
    } finally {
      resource.close();
    }
  }

  /**
   * Handle an office document that's embedded at the POIFS level
   */
  protected void handleEmbededOfficeDoc(
      DirectoryEntry dir, XHTMLContentHandler xhtml)
      throws IOException, SAXException, TikaException {
    // Is it an embedded OLE2 document, or an embedded OOXML document?
    try {
      Entry ooxml = dir.getEntry("Package");

      // It's OOXML
      TikaInputStream stream = TikaInputStream.get(
          new DocumentInputStream((DocumentEntry) ooxml));
      try {
        ZipContainerDetector detector = new ZipContainerDetector();
        MediaType type = detector.detect(stream, new Metadata());
        handleEmbeddedResource(stream, null, type.toString(), xhtml, true);
        return;
      } finally {
        stream.close();
      }
    } catch (FileNotFoundException e) {
      // It's regular OLE2
    }

    // Need to dump the directory out to a new temp file, so
    // it's stand along
    POIFSFileSystem newFS = new POIFSFileSystem();
    copy(dir, newFS.getRoot());

    File tmpFile = File.createTempFile("tika", ".ole2");
    try {
      FileOutputStream out = new FileOutputStream(tmpFile);
      newFS.writeFilesystem(out);
      out.close();

      // What kind of document is it?
      Metadata metadata = new Metadata();
      POIFSDocumentType type = POIFSDocumentType.detectType(dir);

      TikaInputStream embedded;

      if (type == POIFSDocumentType.OLE10_NATIVE) {
        Entry entry = dir.getEntry(Ole10Native.OLE10_NATIVE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(new DocumentInputStream((DocumentEntry) entry), bos);
        byte[] data = bos.toByteArray();

        try {
          Ole10Native ole = new Ole10Native(data, 0);
          byte[] dataBuffer = ole.getDataBuffer();

          metadata.set("resourceName", dir.getName() + '/' + ole.getLabel());

          embedded = TikaInputStream.get(dataBuffer);
        } catch (Ole10NativeException ex) {
          embedded = TikaInputStream.get(data);
        }
      } else {
        metadata.set(Metadata.CONTENT_TYPE, type.getType().toString());
        metadata.set(Metadata.RESOURCE_NAME_KEY, dir.getName() + '.' + type.getExtension());

        embedded = TikaInputStream.get(tmpFile);
      }

      try {
        if (extractor.shouldParseEmbedded(metadata)) {
          extractor.parseEmbedded(embedded, xhtml, metadata, true);
        }
      } finally {
        embedded.close();
      }
    } finally {
      tmpFile.delete();
    }
  }

  protected void copy(DirectoryEntry sourceDir, DirectoryEntry destDir)
      throws IOException {
    for (Entry entry : sourceDir) {
      if (entry instanceof DirectoryEntry) {
        // Need to recurse
        DirectoryEntry newDir = destDir.createDirectory(entry.getName());
        copy((DirectoryEntry) entry, newDir);
      } else {
        // Copy entry
        InputStream contents = new DocumentInputStream((DocumentEntry) entry);
        destDir.createDocument(entry.getName(), contents);
      }
    }
  }
}
