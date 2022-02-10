/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.MimeTypes.MimeTypeRegistry;

import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.util.MimeTypes.MimeTypeRegistry.ADDITIONAL_PREFIX_CLAUSE;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class MimeTypeRegistryTest {

  private static final Pair<String, String> DEFAULT_MIME_TYPE = Pair.of(".def", "default-mime-type");
  private static final Pair<String, String> A_MIME_TYPE = Pair.of(".amt", "a-mime-type");
  private static final Pair<String, String> ANOTHER_MIME_TYPE = Pair.of(".anmt", "another-mime-type");
  private static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
  private final Set<String> DEFAULTS = CollectionUtil.asSet(DEFAULT_MIME_TYPE.getSecond());

  @Test
  void defaultMimeTypes() {
    final MimeTypeRegistry documentMimeTypes = new MimeTypeRegistry(() -> "", DEFAULTS);
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(false));
  }

  @Test
  void noMimeType() {
    final MimeTypeRegistry documentMimeTypes = new MimeTypeRegistry(() -> "deactivated", DEFAULTS);
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(false));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(false));
  }

  @Test
  void supplierMimeTime() {
    // semi colons separator
    final Mutable<String> list = Mutable.of(extList("{0};{1}", A_MIME_TYPE, ANOTHER_MIME_TYPE));
    final MimeTypeRegistry documentMimeTypes = new MimeTypeRegistry(list::get, DEFAULTS);
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(false));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, ANOTHER_MIME_TYPE), is(true));
    // comma separator
    list.set(extList("{0},{1}", A_MIME_TYPE, ANOTHER_MIME_TYPE));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(false));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, ANOTHER_MIME_TYPE), is(true));
    // mixed separator
    list.set(extList("   .a ,, ;   ; ;,, ,.b;.c    ;,.d  "));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(false));
    assertThat(documentMimeTypes.currentMimeTypes, containsInAnyOrder("a", "b", "c", "d"));
    // space separator (so not a right separator)
    list.set(extList("{0} {1}", A_MIME_TYPE, ANOTHER_MIME_TYPE));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(false));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(false));
    assertThat(mimeTypeIsIn(documentMimeTypes, ANOTHER_MIME_TYPE), is(true));
  }

  @Test
  void supplierMimeTimeAndDefaults() {
    // semi colons separator
    final Mutable<String> list = Mutable.of(extList(ADDITIONAL_PREFIX_CLAUSE + "{0};{1}", A_MIME_TYPE, ANOTHER_MIME_TYPE));
    final MimeTypeRegistry documentMimeTypes = new MimeTypeRegistry(list::get, DEFAULTS);
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, ANOTHER_MIME_TYPE), is(true));
    // comma separator
    list.set(extList(ADDITIONAL_PREFIX_CLAUSE + "{0},{1}", A_MIME_TYPE, ANOTHER_MIME_TYPE));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, ANOTHER_MIME_TYPE), is(true));
    // mixed separator
    list.set(extList(ADDITIONAL_PREFIX_CLAUSE + "   .a ,, ;   ; ;,, ,.b;.c    ;,.d  "));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(true));
    assertThat(documentMimeTypes.currentMimeTypes, containsInAnyOrder(DEFAULT_MIME_TYPE.getSecond(), "a", "b", "c", "d"));
    // mixed separator again
    list.set(extList(" " + ADDITIONAL_PREFIX_CLAUSE + "   .a ,, ;   ; ;,," + ADDITIONAL_PREFIX_CLAUSE + " ,.b;.c    ;,.d  "));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(true));
    assertThat(documentMimeTypes.currentMimeTypes, containsInAnyOrder(DEFAULT_MIME_TYPE.getSecond(), UNKNOWN_MIME_TYPE, "a", "b", "c", "d"));
    // mixed separator again (starts not with the additional clause)
    list.set(extList(" ," + ADDITIONAL_PREFIX_CLAUSE + " ;  .a ,, ;   ; ;,, ,.b;.c    ;,.d  "));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(false));
    assertThat(documentMimeTypes.currentMimeTypes, containsInAnyOrder(UNKNOWN_MIME_TYPE, "a", "b", "c", "d"));
    // space separator (so not a right separator)
    list.set(extList(ADDITIONAL_PREFIX_CLAUSE + "{0} {1}", A_MIME_TYPE, ANOTHER_MIME_TYPE));
    assertThat(mimeTypeIsIn(documentMimeTypes, DEFAULT_MIME_TYPE), is(true));
    assertThat(mimeTypeIsIn(documentMimeTypes, A_MIME_TYPE), is(false));
    assertThat(mimeTypeIsIn(documentMimeTypes, ANOTHER_MIME_TYPE), is(true));
  }

  @SafeVarargs
  private final String extList(final String template, final Pair<String, String>... extensions) {
    return MessageFormat.format(template, Stream.of(extensions).map(Pair::getFirst).map(String::toUpperCase).toArray());
  }

  private boolean mimeTypeIsIn(final MimeTypeRegistry documentMimeTypes,
      final Pair<String, String> defaultMimeType) {
    return documentMimeTypes.contains(defaultMimeType.getSecond());
  }
}