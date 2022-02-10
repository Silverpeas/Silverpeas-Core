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

package org.silverpeas.core.notification.user.model;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.ui.DisplayI18NHelper;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author silveryocha
 */
@UnitTest
class NotificationResourceDataTest {

  @Test
  void getNotDefinedResourceDetails() throws Exception {
    final NotificationResourceData data = new NotificationResourceData();
    assertThat(getTechnicalResourceName(data), nullValue());
    assertThat(getTechnicalResourceDescription(data), nullValue());
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), nullValue());
  }

  @Test
  void setResourceNameOnDefaultLanguageDoesNotInduceTransientDetailsInstantiation() throws Exception {
    final NotificationResourceData data = new NotificationResourceData();
    data.setResourceName("resource name");
    assertThat(getTechnicalResourceName(data), is("resource name"));
    assertThat(getTechnicalResourceDescription(data), nullValue());
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), nullValue());
    assertThat(data.getResourceName(), is("resource name"));
    assertThat(getTransientResourceDetails(data), notNullValue());
    data.setCurrentLanguage("otherLanguage");
    assertThat(data.getResourceName(), is("resource name"));
  }

  @Test
  void setResourceNameOnOtherLanguageInducesTransientDetailsInstantiation() throws Exception {
    final NotificationResourceData data = new NotificationResourceData();
    data.setCurrentLanguage("otherLanguage");
    data.setResourceName("resource name");
    assertThat(getTechnicalResourceName(data), isEmptyString());
    assertThat(getTechnicalResourceDescription(data), nullValue());
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), notNullValue());
    assertThat(data.getResourceName(), is("resource name"));
    data.setCurrentLanguage(DisplayI18NHelper.getDefaultLanguage());
    assertThat(data.getResourceName(), is(""));
  }

  @Test
  void setResourceDescriptionOnDefaultLanguageDoesNotInduceTransientDetailsInstantiation() throws Exception {
    final NotificationResourceData data = new NotificationResourceData();
    data.setResourceDescription("resource description");
    assertThat(getTechnicalResourceName(data), nullValue());
    assertThat(getTechnicalResourceDescription(data), is("resource description"));
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), nullValue());
    assertThat(data.getResourceDescription(), is("resource description"));
    assertThat(getTransientResourceDetails(data), notNullValue());
    data.setCurrentLanguage("otherLanguage");
    assertThat(data.getResourceDescription(), is("resource description"));
  }

  @Test
  void setResourceDescriptionOnOtherLanguageInducesTransientDetailsInstantiation() throws Exception {
    final NotificationResourceData data = new NotificationResourceData();
    data.setCurrentLanguage("otherLanguage");
    data.setResourceDescription("resource description");
    assertThat(getTechnicalResourceName(data), nullValue());
    assertThat(getTechnicalResourceDescription(data), nullValue());
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), notNullValue());
    assertThat(data.getResourceDescription(), is("resource description"));
    data.setCurrentLanguage(DisplayI18NHelper.getDefaultLanguage());
    assertThat(data.getResourceDescription(), nullValue());
  }

  @Test
  void setResourceLinkLabelInducesTransientDetailsInstantiationInAnyCase() throws Exception {
    NotificationResourceData data = new NotificationResourceData();
    data.setLinkLabel("link label");
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), notNullValue());
    data = new NotificationResourceData();
    data.setCurrentLanguage("otherLanguage");
    data.setLinkLabel("link label");
    assertThat(getTechnicalResourceDetails(data), nullValue());
    assertThat(getTransientResourceDetails(data), notNullValue());
  }

  private String getTechnicalResourceName(final NotificationResourceData data)
      throws IllegalAccessException {
    return (String) readDeclaredField(data, "resourceName", true);
  }

  private String getTechnicalResourceDescription(final NotificationResourceData data)
      throws IllegalAccessException {
    return (String) readDeclaredField(data, "resourceDescription", true);
  }

  private String getTechnicalResourceDetails(final NotificationResourceData data)
      throws IllegalAccessException {
    return (String) readDeclaredField(data, "details", true);
  }

  private NotificationResourceDataDetails getTransientResourceDetails(
      final NotificationResourceData data) throws IllegalAccessException {
    return (NotificationResourceDataDetails) readDeclaredField(data, "transientDetails", true);
  }
}