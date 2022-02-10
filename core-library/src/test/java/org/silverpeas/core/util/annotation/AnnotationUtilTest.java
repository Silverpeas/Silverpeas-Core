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
package org.silverpeas.core.util.annotation;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.util.CollectionUtil;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier Date: 22/10/13
 */
@UnitTest
class AnnotationUtilTest {

  @Test
  void testGetAnnotatedValues() {
    Map<Class<? extends Annotation>, List<Object>> annotationParameterValues = new HashMap<>();

    // Prepare data
    annotationParameterValues.put(Language.class, CollectionUtil.asList("fr"));
    annotationParameterValues.put(SourcePK.class,
        CollectionUtil.asList(new ResourceReference("1", "componentId1"),
            new ResourceReference("2", "componentId2")));
    annotationParameterValues.put(TargetPK.class,
        CollectionUtil.asList(new ResourceReference("1", "targetComponentId")));

    // Test
    List<Object> test =
        AnnotationUtil.getAnnotatedValues(annotationParameterValues, Language.class);
    assertThat(test, notNullValue());
    assertThat(test, hasItem("fr"));

    test = AnnotationUtil.getAnnotatedValues(annotationParameterValues, SourcePK.class);
    assertThat(test, notNullValue());
    assertThat(test, hasSize(2));

    test = AnnotationUtil.getAnnotatedValues(annotationParameterValues, TargetPK.class);
    assertThat(test, notNullValue());
    assertThat(test, hasSize(1));

    test = AnnotationUtil.getAnnotatedValues(annotationParameterValues, Inject.class);
    assertThat(test, notNullValue());
    assertThat(test, hasSize(0));
  }
}
