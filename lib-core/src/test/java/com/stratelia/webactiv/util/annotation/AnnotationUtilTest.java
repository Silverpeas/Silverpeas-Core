/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.util.annotation;

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.ForeignPK;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 22/10/13
 */
public class AnnotationUtilTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testGetAnnotedValues() {
    Map<Class<Annotation>, List<Object>> annotationParameterValues =
        new HashMap<Class<Annotation>, List<Object>>();

    // Prepare data
    annotationParameterValues.put((Class) Language.class, CollectionUtil.asList((Object) "fr"));
    annotationParameterValues.put((Class) SourcePK.class, CollectionUtil
        .asList(new ForeignPK("1", "componentId1"), (Object) new ForeignPK("2", "componentId2")));
    annotationParameterValues.put((Class) TargetPK.class,
        CollectionUtil.asList((Object) new ForeignPK("1", "targetComponentId")));

    // Test
    List<Object> test = AnnotationUtil.getAnnotedValues(annotationParameterValues, Language.class);
    assertThat(test, notNullValue());
    assertThat(test, hasItem("fr"));

    test = AnnotationUtil.getAnnotedValues(annotationParameterValues, SourcePK.class);
    assertThat(test, notNullValue());
    assertThat(test, hasSize(2));

    test = AnnotationUtil.getAnnotedValues(annotationParameterValues, TargetPK.class);
    assertThat(test, notNullValue());
    assertThat(test, hasSize(1));

    test = AnnotationUtil.getAnnotedValues(annotationParameterValues, Inject.class);
    assertThat(test, notNullValue());
    assertThat(test, hasSize(0));
  }
}
