/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.attachment;

import org.junit.jupiter.api.Test;

import java.text.Normalizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the decoding of the name of a file uploaded to create or to update a document.
 * The file name must be stored as it is: escaping it is the job of the rendering.
 *
 * @author mmoquillon
 */
class AbstractSimpleDocumentResourceTest {

  @Test
  void anApostropheInTheFilenameIsKept() {
    String filename = AbstractSimpleDocumentResource.decodeFilename("élément à l'offre.pdf");
    assertThat(filename, is("élément à l'offre.pdf"));
  }

  @Test
  void htmlSpecialCharsInTheFilenameAreNotEncoded() {
    String filename = AbstractSimpleDocumentResource.decodeFilename("Tom & Jerry \"<v2>\".pdf");
    assertThat(filename, is("Tom & Jerry \"<v2>\".pdf"));
  }

  @Test
  void anUrlEncodedFilenameIsDecoded() {
    String filename = AbstractSimpleDocumentResource.decodeFilename("l%27offre%20%C3%A9t%C3%A9.pdf");
    assertThat(filename, is("l'offre été.pdf"));
  }

  @Test
  void aFilenameWithDecomposedCharsIsNormalized() {
    String decomposed = Normalizer.normalize("élément.pdf", Normalizer.Form.NFD);
    String filename = AbstractSimpleDocumentResource.decodeFilename(decomposed);
    assertThat(filename, is("élément.pdf"));
  }
}
