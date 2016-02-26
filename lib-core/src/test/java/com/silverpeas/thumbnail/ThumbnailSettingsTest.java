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
package com.silverpeas.thumbnail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.test.rule.MockByReflectionRule;

import static com.silverpeas.thumbnail.ThumbnailSettings.APP_PARAM_HEIGHT;
import static com.silverpeas.thumbnail.ThumbnailSettings.APP_PARAM_WIDTH;
import static com.silverpeas.thumbnail.ThumbnailSettings.DEFAULT_SIZE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * BE CAREFUL, this unit test verifies especially that a default width and a default height are
 * returned.<br/>
 * @author Yohann Chastagnier
 */
public class ThumbnailSettingsTest {

  private static final String COMPONENT_INSTANCE_ID = "componentInstanceId";

  private OrganisationController mockedOrganisationController;

  @Rule
  public MockByReflectionRule mockByReflectionRule = new MockByReflectionRule();

  @Before
  public void setup() {
    mockedOrganisationController = mockByReflectionRule
        .mockField(OrganisationControllerFactory.class, OrganisationController.class,
            "instance.organisationController");
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_WIDTH)).thenReturn("");
    // the height about component instance parameters is not handled with the mock in order to
    // verify the behavior when null is returned instead of ""
  }

  @Test
  public void shouldReturnDefaultSizeWhenComponentParametersAndDefaultPropertiesAreNotDefined() {
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(DEFAULT_SIZE));
    assertThat(settings.getHeight(), is(DEFAULT_SIZE));
  }

  @Test
  public void defaultWidthPropertyShouldBeTakenIntoAccountAndAppliedOnHeightToo() {
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, 26, -1);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(26));
  }

  @Test
  public void defaultHeightPropertyShouldBeTakenIntoAccountAndAppliedOnWidthToo() {
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, 38);
    assertThat(settings.getWidth(), is(38));
    assertThat(settings.getHeight(), is(38));
  }

  @Test
  public void defaultSizePropertiesShouldBeTakenIntoAccount() {
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, 26, 38);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(38));
  }

  @Test
  public void defaultComponentInstanceWidthParameterShouldBeTakenIntoAccountAndAppliedOnHeightToo
      () {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_WIDTH)).thenReturn("26");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(26));
  }

  @Test
  public void defaultComponentInstanceHeightParameterShouldBeTakenIntoAccountAndAppliedOnWidthToo
      () {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_HEIGHT)).thenReturn("38");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(38));
    assertThat(settings.getHeight(), is(38));
  }

  @Test
  public void defaultComponentInstanceSizeParametersShouldBeTakenIntoAccount() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_WIDTH)).thenReturn("26");
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_HEIGHT)).thenReturn("38");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(38));
  }

  @Test
  public void
  componentInstanceWidthParameterShouldBeGivenPriorityOverTheDefaultWidthAndHeightOfProperties() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_WIDTH)).thenReturn("26");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, 1000, 1000);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(26));
  }

  @Test
  public void
  componentInstanceHeightParameterShouldBeGivenPriorityOverTheDefaultWidthAndHeightOfProperties() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, APP_PARAM_HEIGHT)).thenReturn("38");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, 1000, 1000);
    assertThat(settings.getWidth(), is(38));
    assertThat(settings.getHeight(), is(38));
  }
}