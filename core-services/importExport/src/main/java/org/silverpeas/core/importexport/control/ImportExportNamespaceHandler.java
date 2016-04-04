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

/*
 * $Id$ Copyright (c) 2009, Silverpeas
 */
package org.silverpeas.core.importexport.control;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler de contenu XML (poru un parseur SAX), qui permete de récupérer l'URI de spécification du
 * namespace XML utilisé pour l'élément racine.
 */
public class ImportExportNamespaceHandler extends DefaultHandler {

  /** URI désignant pour le namespace XML utilisé. */
  private String nsSpec = null;

  private boolean start = true;

  /**
   * Retourne la valeur de la propriété nsSpec.
   * @return La valeur de la propriété nsSpec.
   */
  public String getNsSpec() {
    return nsSpec;
  }

  @Override
  public void startDocument() throws SAXException {
    this.nsSpec = null;
    this.start = true;
    super.startDocument();
  }

  @Override
  public void startElement(String uri, String localName, String name, Attributes attributes)
      throws SAXException {

    if (this.start) {
      if (uri != null) {
        int numAtts = attributes.getLength();

        for (int i = 0; i < numAtts; i++) {
          String attQName = attributes.getQName(i);

          if (attQName.equals("xmlns") || attQName.startsWith("xmlns:")) {
            String attValue = attributes.getValue(i);

            if (uri.equals(attValue)) {
              this.nsSpec = attValue;
              break;
            }
          }
        }
      }

      this.start = false;
    }

    super.startElement(uri, localName, name, attributes);
  }
}
