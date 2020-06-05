/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import javax.persistence.Table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
public class ClassAnnotationUtilTest {

  @Test
  public void getClassThatDeclaresTableAnnotationFromAbstractEntity() {
    Class<?> identifiedClass =
        ClassAnnotationUtil.searchClassThatDeclaresAnnotation(Table.class, AbstractEntity.class);
    assertThat(identifiedClass, notNullValue());
    assertThat(identifiedClass.getName(), is(AbstractEntity.class.getName()));
    assertThat(identifiedClass.getAnnotation(Table.class).name(), is("st_abstract_entity"));
  }

  @Test
  public void getClassThatDeclaresTableAnnotationFromConcreteEntity() {
    Class<?> identifiedClass =
        ClassAnnotationUtil.searchClassThatDeclaresAnnotation(Table.class, ConcreteEntity.class);
    assertThat(identifiedClass, notNullValue());
    assertThat(identifiedClass.getName(), is(AbstractEntity.class.getName()));
    assertThat(identifiedClass.getAnnotation(Table.class).name(), is("st_abstract_entity"));
  }

  @Test
  public void getClassThatDeclaresTableAnnotationFromAbstractWikispeed() {
    Class identifiedClass =
        ClassAnnotationUtil.searchClassThatDeclaresAnnotation(Table.class, AbstractWikispeed.class);
    assertThat(identifiedClass, nullValue());
  }

  @Test
  public void getEngineParameterizedTypeFromAbstractHierarchy() {
    Class engineType =
        ClassAnnotationUtil.searchParameterizedTypeFrom(Engine.class, AbstractWikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(ElectricEngine.class.getName()));
  }

  @Test
  public void getEngineParameterizedTypeFromInterfaceHierarchy() {
    Class engineType = ClassAnnotationUtil.searchParameterizedTypeFrom(Engine.class, Wikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(ElectricEngine.class.getName()));
  }

  @Test
  public void getModelParameterizedTypeFromAbstractHierarchy() {
    Class engineType =
        ClassAnnotationUtil.searchParameterizedTypeFrom(Model.class, AbstractWikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(SedanModel.class.getName()));
  }

  @Test
  public void getModelParameterizedTypeFromInterfaceHierarchy() {
    Class engineType = ClassAnnotationUtil.searchParameterizedTypeFrom(Model.class, Wikispeed.class);
    assertThat(engineType, notNullValue());
    assertThat(engineType.getName(), is(SedanModel.class.getName()));
  }
}
