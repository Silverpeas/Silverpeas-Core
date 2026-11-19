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
package org.silverpeas.core.contribution.attachment.repository;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Unit tests on the set up of the converter of documents used by a {@link DocumentRepository}.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class DocumentRepositoryTest {

  @TestedBean
  DocumentRepository repository;

  @Test
  void aDocumentConverterIsSetUpAtTheConstructionOfTheRepository() {
    assertThat(repository.converter, notNullValue());
    assertThat(repository.converter.getClass(), is(DocumentConverter.class));
  }

  @Test
  void aSpecificRepositoryCanSetUpItsOwnDocumentConverter() {
    var specificRepository = new SpecificDocumentRepository();
    specificRepository.setUpDocumentConverter();
    assertThat(specificRepository.converter, instanceOf(SpecificDocumentConverter.class));
    assertThat(specificRepository.converter,
        sameInstance(specificRepository.createdConverter));
  }

  private static class SpecificDocumentRepository extends DocumentRepository {

    private DocumentConverter createdConverter;

    @Override
    DocumentConverter createDocumentConverter(final I18n i18n) {
      createdConverter = new SpecificDocumentConverter(i18n);
      return createdConverter;
    }
  }

  private static class SpecificDocumentConverter extends DocumentConverter {

    SpecificDocumentConverter(final I18n i18n) {
      super(i18n);
    }
  }
}
