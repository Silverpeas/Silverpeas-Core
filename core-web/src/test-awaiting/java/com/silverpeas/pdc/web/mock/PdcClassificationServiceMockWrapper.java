/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.pdc.web.mock;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import java.util.List;
import java.util.Set;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * It plays the role of a PdcClassificationService instance for the unit tests. It wraps a mock of
 * the true PdcClassificationService class used by the business objects and it delegates to it all
 * the invoked methods. The wrapped mock can be get to register some behaviour expected by the
 * tests. Default behaviours are registered by taking into account the business logic of the
 * original methods. Thoses are savePreDefinedClassification, getPreDefinedClassification, and
 * findAPreDefinedClassification: the classification getter returns NONE_CLASSIFICATION and the
 * saver sets the unique identifier of the classification and sets the behaviour of the getter to
 * return it when asked.
 */
@Named("pdcClassificationService")
public class PdcClassificationServiceMockWrapper implements PdcClassificationService {

  private PdcClassificationService mock;
  private long id = 0;

  public PdcClassificationServiceMockWrapper() {
    mock = mock(PdcClassificationService.class);
    initializeBehaviour();
  }

  public PdcClassificationService getPdcClassificationServiceMock() {
    return mock;
  }

  public static PdcClassification withClassification(PdcClassification classification) {
    return classification;
  }

  @Override
  public PdcClassification savePreDefinedClassification(PdcClassification classification) {
    return mock.savePreDefinedClassification(classification);
  }

  @Override
  public PdcClassification getPreDefinedClassification(String instanceId) {
    return mock.getPreDefinedClassification(instanceId);
  }

  @Override
  public PdcClassification getPreDefinedClassification(String nodeId, String instanceId) {
    if (isDefined(nodeId)) {
      return mock.getPreDefinedClassification(nodeId, instanceId);
    } else {
      return mock.getPreDefinedClassification(instanceId);
    }
  }

  @Override
  public PdcClassification findAPreDefinedClassification(String nodeId, String instanceId) {
    return mock.findAPreDefinedClassification(nodeId, instanceId);
  }

  @Override
  public void deletePreDefinedClassification(String nodeId, String instanceId) {
    mock.deletePreDefinedClassification(nodeId, instanceId);
  }

  @Override
  public void classifyContent(SilverpeasContent content, PdcClassification withClassification)
      throws PdcRuntimeException {
    mock.classifyContent(content, withClassification);
  }

  @Override
  public void axisValuesDeleted(List<PdcAxisValue> deletedValues) {
    mock.axisValuesDeleted(deletedValues);
  }

  @Override
  public void axisDeleted(String axisId) {
    mock.axisDeleted(axisId);
  }

  private void initializeBehaviour() {
    when(mock.findAPreDefinedClassification(anyString(), anyString())).thenReturn(
        PdcClassification.NONE_CLASSIFICATION);
    when(mock.getPreDefinedClassification(anyString())).thenReturn(
        PdcClassification.NONE_CLASSIFICATION);
    when(mock.getPreDefinedClassification(anyString(), anyString())).thenReturn(
        PdcClassification.NONE_CLASSIFICATION);
    when(mock.savePreDefinedClassification(any(PdcClassification.class))).thenAnswer(
        new Answer<PdcClassification>() {
      @Override
      public PdcClassification answer(InvocationOnMock invocation) throws Throwable {
        PdcClassificationService mock = (PdcClassificationService) invocation.getMock();
        PdcClassification classification = (PdcClassification) invocation.getArguments()[0];
        if (classification.getId() != null && classification.isEmpty()
            && findAPreDefinedClassification(classification.getNodeId(), classification.
            getComponentInstanceId()) != PdcClassification.NONE_CLASSIFICATION) {
          when(mock.findAPreDefinedClassification(classification.getNodeId(),
              classification.getComponentInstanceId())).thenReturn(
              PdcClassification.NONE_CLASSIFICATION);
          return PdcClassification.NONE_CLASSIFICATION;
        }
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Set<ConstraintViolation<PdcClassification>> violations = validatorFactory.
            getValidator().validate(classification);
        if (!violations.isEmpty()) {
          throw new ConstraintViolationException("Error", null);
        }
        ReflectionTestUtils.setField(classification, "id", id++);
        when(mock.findAPreDefinedClassification(classification.getNodeId(), classification.
            getComponentInstanceId())).thenReturn(classification);
        if (classification.isPredefinedForANode()) {
          when(mock.getPreDefinedClassification(classification.getNodeId(), classification.
              getComponentInstanceId())).thenReturn(classification);
        } else {
          when(mock.getPreDefinedClassification(classification.getComponentInstanceId())).
              thenReturn(classification);
        }
        return classification;
      }
    });
  }

  @Override
  public void classifyContent(SilverpeasContent content, PdcClassification withClassification,
      boolean alertSubscribers) throws PdcRuntimeException {
    mock.classifyContent(content, withClassification, alertSubscribers);
  }
}
