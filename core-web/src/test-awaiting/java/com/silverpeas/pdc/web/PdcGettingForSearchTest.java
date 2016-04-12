/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.web.ResourceGettingTest;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.beans.admin.ComponentSearchCriteria;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.service.OrganizationController;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import static com.silverpeas.pdc.web.PdcTestResources.JAVA_PACKAGE;
import static com.silverpeas.pdc.web.PdcTestResources.SPRING_CONTEXT;
import static com.silverpeas.pdc.web.TestConstants.*;
import static com.silverpeas.pdc.web.matchers.PdcEntityMatcher.equalTo;

/**
 * Unit tests on the getting of the PdC filtered by the axis and values that are effectively used in
 * the classification of contents. This kind of filtering of the PdC is used in the advanced search.
 */
public class PdcGettingForSearchTest extends ResourceGettingTest<PdcTestResources> {

  private static final String ANOTHER_COMPONENT_ID = "gallery1";
  private String sessionKey;
  private UserDetail theUser;

  public PdcGettingForSearchTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void setUpUserSessionAndPdC() {
    getTestResources().enableThesaurus();
    mockOrganisationControllerBehaviour();
    theUser = aUser();
    sessionKey = authenticate(theUser);
  }

  @Override
  public void gettingAResourceByAnUnauthorizedUser() {
  }

  @Test
  public void gettingAFilteredPdc() throws Exception {
    PdcEntity pdc = getAt(aResourceURI(), aPdcEntity());
    assertNotNull(pdc);
    PdcEntity expectedPdc = toWebEntity(theExpectedFilteredPdc(),
        withURI(PDC_USED_IN_CLASSIFICATION_URI));
    assertThat(pdc, is(equalTo(expectedPdc)));
  }

  @Test
  public void gettingAFilteredPdcByAGivenComponentInstance() throws Exception {
    PdcEntity pdc = getAt(aResourceURI() + "?componentId=" + COMPONENT_INSTANCE_ID, aPdcEntity());
    assertNotNull(pdc);
    PdcEntity expectedPdc = toWebEntity(theExpectedFilteredPdc(),
        withURI(PDC_USED_IN_CLASSIFICATION_URI + "?componentId=" + COMPONENT_INSTANCE_ID));
    assertThat(pdc, is(equalTo(expectedPdc)));
  }

  @Test
  public void gettingAnEmptyPdcByAGivenComponentInstance() throws Exception {
    PdcEntity pdc = getAt(aResourceURI() + "?componentId=" + ANOTHER_COMPONENT_ID, aPdcEntity());
    assertNotNull(pdc);
    assertThat(pdc.getAxis().isEmpty(), is(true));
  }

  @Test
  public void gettingAFilteredPdcWithSomeSpecifiedAxisValues() throws Exception {
    PdcEntity pdc = getAt(aResourceURI() + "?values=1:/0/1/2,2:/0/1", aPdcEntity());
    assertNotNull(pdc);
    PdcEntity expectedPdc = toWebEntity(theExpectedFilteredPdc(new AxisValueCriterion("1",
        "/0/1/2"), new AxisValueCriterion("2", "/0/1")),
        withURI(PDC_USED_IN_CLASSIFICATION_URI + "?values=1:/0/1/2,2:/0/1"));
    assertThat(pdc, is(equalTo(expectedPdc)));
  }

  @Override
  public String aResourceURI() {
    return USED_PDC_PATH;
  }

  @Override
  public String anUnexistingResourceURI() {
    return UNKNOWN_CONTENT_PDC_PATH;
  }

  @Override
  public <T> T aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PdcEntity.class;
  }

  public Class<PdcEntity> aPdcEntity() {
    return PdcEntity.class;
  }

  public PdcEntity toWebEntity(List<UsedAxis> axis, String uri) throws ThesaurusException {
    return PdcEntity.aPdcEntityWithUsedAxis(axis, FRENCH, URI.create(uri), getTestResources().
        aThesaurusHolderFor(
        theUser));
  }

  protected List<UsedAxis> theExpectedPdcFor(String contentId) throws ContentManagerException,
      PdcException {
    int silverObjectId = getTestResources().getContentManager().getSilverContentId(contentId,
        COMPONENT_INSTANCE_ID);
    return getTestResources().getPdcService().getUsedAxisToClassify(COMPONENT_INSTANCE_ID,
        silverObjectId);
  }

  protected List<UsedAxis> theExpectedPdc() throws PdcException {
    return getTestResources().getPdcService().getUsedAxisByInstanceId(COMPONENT_INSTANCE_ID);
  }

  protected List<UsedAxis> theExpectedFilteredPdc() throws PdcException {
    return getTestResources().getAxisUsedInClassification();
  }

  protected List<UsedAxis> theExpectedFilteredPdc(AxisValueCriterion... criteria) throws
      PdcException {
    List<UsedAxis> axis = new ArrayList<UsedAxis>();
    List<UsedAxis> usedAxis = getTestResources().getAxisUsedInClassification();
    for (UsedAxis aUsedAxis : usedAxis) {
      List<Value> values = new ArrayList<Value>();
      for (AxisValueCriterion aCriterion : criteria) {
        if (aCriterion.getAxisId() == aUsedAxis.getAxisId()) {
          for (Iterator it = aUsedAxis._getAxisValues().iterator(); it.hasNext();) {
            Value aValue = (Value) it.next();
            if (aValue.getFullPath().equals(aCriterion.getValuePath())) {
              values.add(aValue);
            }
          }
        }
      }
      if (!values.isEmpty()) {
        aUsedAxis._setAxisValues(values);
        axis.add(aUsedAxis);
      }
    }
    return axis;
  }

  protected static String withURI(String uri) {
    return uri;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_INSTANCE_ID};
  }

  private void mockOrganisationControllerBehaviour() {
    OrganizationController organisationController = getTestResources().getOrganisationController();
    doAnswer(new Answer<List<String>>() {
      @Override
      public List<String> answer(InvocationOnMock invocation) throws Throwable {
        ComponentSearchCriteria criteria = (ComponentSearchCriteria) invocation.getArguments()[0];
        if (criteria.hasCriterionOnComponentInstance()) {
          return Arrays.asList(new String[]{criteria.getComponentInstanceId()});
        }
        return Arrays.asList(new String[]{COMPONENT_INSTANCE_ID, ANOTHER_COMPONENT_ID, "blog2"});
      }
    }).when(organisationController).getSearchableComponentsByCriteria(
        any(ComponentSearchCriteria.class));
  }
}
