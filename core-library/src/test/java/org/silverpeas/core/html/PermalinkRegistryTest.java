/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.html;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestedBean;
import org.silverpeas.core.util.URLUtil.Permalink;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class PermalinkRegistryTest {

  private static final List<String> PERMALINK_PARTS = Stream.of(Permalink.values())
      .map(p -> p.getURLPrefix().replace("/", "")).collect(Collectors.toList());

  private static final String CONTRIBUTION_PERMALINK_PART = "Contribution";
  private static final String OTHER_PERMALINK_PART = "Other";

  @TestedBean
  private PermalinkRegistry permalinkRegistry;

  @Test
  void limitCases() {
    permalinkRegistry.addUrlPart(null);
    permalinkRegistry.addUrlPart("");
    assertThat(permalinkRegistry.isCompliant(null), is(false));
    assertThat(permalinkRegistry.isCompliant(""), is(false));
    assertThat(permalinkRegistry.isCompliant("toto"), is(false));
    final String publicationPart = PERMALINK_PARTS.get(0);
    assertThat(publicationPart, is("Publication"));
    assertPermalinkPart(publicationPart, true);
    assertPermalinkPart(publicationPart.toLowerCase(), false);
  }

  @Test
  void isCompliantByDefault() {
    for (final String permalinkPart : PERMALINK_PARTS) {
      assertPermalinkPart(permalinkPart, true);
    }
    assertPermalinkPart(CONTRIBUTION_PERMALINK_PART, false);
  }

  @Test
  void isCompliant() {
    permalinkRegistry.addUrlPart(CONTRIBUTION_PERMALINK_PART);
    permalinkRegistry.addUrlPart("//" + OTHER_PERMALINK_PART + "/");
    for (final String permalinkPart : PERMALINK_PARTS) {
      assertPermalinkPart(permalinkPart, true);
    }
    assertPermalinkPart(CONTRIBUTION_PERMALINK_PART, true);
    assertPermalinkPart(OTHER_PERMALINK_PART, true);
  }

  private void assertPermalinkPart(final String permalinkPart, final boolean isCompliant) {
    assertThat(permalinkRegistry.isCompliant(permalinkPart), is(false));
    assertThat(permalinkRegistry.isCompliant("/" + permalinkPart), is(false));
    assertThat(permalinkRegistry.isCompliant(permalinkPart + "/"), is(false));
    assertThat(permalinkRegistry.isCompliant("/" + permalinkPart + "/"), is(isCompliant));
  }
}