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
package org.silverpeas.core.util.annotation;

import org.junit.Test;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.ForeignPK;

import javax.inject.Inject;
import javax.persistence.Table;
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
  public void getClassThatDeclaresTableAnnotationFromAbstractEntity() {
    Class<?> identifiedClass =
        AnnotationUtil.searchClassThatDeclaresAnnotation(Table.class, AbstractEntity.class);
    assertThat(identifiedClass, notNullValue());
    assertThat(identifiedClass.getName(), is(AbstractEntity.class.getName()));
    assertThat(identifiedClass.getAnnotation(Table.class).name(), is("st_abstract_entity"));
  }

  @Test
  public void getClassThatDeclaresTableAnnotationFromConcreteEntity() {
    Class<?> identifiedClass =
        AnnotationUtil.searchClassThatDeclaresAnnotation(Table.class, ConcreteEntity.class);
    assertThat(identifiedClass, notNullValue());
    assertThat(identifiedClass.getName(), is(AbstractEntity.class.getName()));
    assertThat(identifiedClass.getAnnotation(Table.class).name(), is("st_abstract_entity"));
  }

  @Test
  public void getClassThatDeclaresTableAnnotationFromAbstractWikispeed() {
    Class identifiedClass =
        AnnotationUtil.searchClassThatDeclaresAnnotation(Table.class, AbstractWikispeed.class);
    assertThat(identifiedClass, nullValue());
  }

  @Test
  public void getEngineParameterizedTypeFromAbstractHierarchy() {
    Class engineType =
        AnnotationUtil.searchParameterizedTypeFrom(Engine.class, AbstractWikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(ElectricEngine.class.getName()));
  }

  @Test
  public void getEngineParameterizedTypeFromInterfaceHierarchy() {
    Class engineType = AnnotationUtil.searchParameterizedTypeFrom(Engine.class, Wikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(ElectricEngine.class.getName()));
  }

  @Test
  public void getModelParameterizedTypeFromAbstractHierarchy() {
    Class engineType =
        AnnotationUtil.searchParameterizedTypeFrom(Model.class, AbstractWikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(SedanModel.class.getName()));
  }

  @Test
  public void getModelParameterizedTypeFromInterfaceHierarchy() {
    Class engineType = AnnotationUtil.searchParameterizedTypeFrom(Model.class, Wikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(SedanModel.class.getName()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetAnnotatedValues() {
    Map<Class<Annotation>, List<Object>> annotationParameterValues = new HashMap<>();

    // Prepare data
    annotationParameterValues.put((Class) Language.class, CollectionUtil.asList((Object) "fr"));
    annotationParameterValues.put((Class) SourcePK.class, CollectionUtil
        .asList(new ForeignPK("1", "componentId1"), (Object) new ForeignPK("2", "componentId2")));
    annotationParameterValues.put((Class) TargetPK.class,
        CollectionUtil.asList((Object) new ForeignPK("1", "targetComponentId")));

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
