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
package org.silverpeas.core.io.media.image.thumbnail;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * BE CAREFUL, this unit test verifies especially that a default width and a default height are
 * returned in any case.<br>
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
public class ThumbnailSettingsTest {

  private static final String COMPONENT_INSTANCE_ID = "componentInstanceId";

  @TestManagedMock
  private OrganizationController mockedOrganisationController;

  @BeforeEach
  public void setup() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_WIDTH)).thenReturn("");
    // the height about component instance parameters is not handled with the mock in order to
    // verify the behavior when null is returned instead of ""
  }

  @Test
  public void shouldReturnDefaultSizeWhenComponentParametersAndDefaultPropertiesAreNotDefined() {
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), Matchers.is(ThumbnailSettings.DEFAULT_SIZE));
    assertThat(settings.getHeight(), Matchers.is(ThumbnailSettings.DEFAULT_SIZE));
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
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_WIDTH)).thenReturn("26");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(26));
  }

  @Test
  public void defaultComponentInstanceHeightParameterShouldBeTakenIntoAccountAndAppliedOnWidthToo
      () {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_HEIGHT)).thenReturn("38");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(38));
    assertThat(settings.getHeight(), is(38));
  }

  @Test
  public void defaultComponentInstanceSizeParametersShouldBeTakenIntoAccount() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_WIDTH)).thenReturn("26");
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_HEIGHT)).thenReturn("38");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, -1, -1);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(38));
  }

  @Test
  public void
  componentInstanceWidthParameterShouldBeGivenPriorityOverTheDefaultWidthAndHeightOfProperties() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_WIDTH)).thenReturn("26");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, 1000, 1000);
    assertThat(settings.getWidth(), is(26));
    assertThat(settings.getHeight(), is(26));
  }

  @Test
  public void
  componentInstanceHeightParameterShouldBeGivenPriorityOverTheDefaultWidthAndHeightOfProperties() {
    when(mockedOrganisationController
        .getComponentParameterValue(COMPONENT_INSTANCE_ID, ThumbnailSettings.APP_PARAM_HEIGHT)).thenReturn("38");
    ThumbnailSettings settings = ThumbnailSettings.getInstance(COMPONENT_INSTANCE_ID, 1000, 1000);
    assertThat(settings.getWidth(), is(38));
    assertThat(settings.getHeight(), is(38));
  }
}