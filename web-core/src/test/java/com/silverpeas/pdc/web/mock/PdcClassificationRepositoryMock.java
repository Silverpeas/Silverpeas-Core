/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import com.silverpeas.pdc.dao.PdcClassificationRepository;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcClassification;
import static com.silverpeas.pdc.model.PdcClassificationHelper.*;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.web.IdGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Mock of the PdcClassification service bean
 */
@Named("pdcClassificationRepository")
public class PdcClassificationRepositoryMock implements PdcClassificationRepository {

  private Map<Long, PdcClassification> classifications =
          new ConcurrentHashMap<Long, PdcClassification>();
  private IdGenerator generator = null;
  
  public void setIdGenerator(IdGenerator generator) {
    this.generator = generator;
  }
  

  @Override
  public PdcClassification findPredefinedClassificationByComponentInstanceId(String instanceId) {
    PdcClassification foundClassification = null;
    for (PdcClassification pdcClassification : classifications.values()) {
      if (pdcClassification.isPredefinedForTheWholeComponentInstance() && pdcClassification.
              getComponentInstanceId().equals(instanceId)) {
        foundClassification = pdcClassification;
        break;
      }
    }
    return foundClassification;
  }

  @Override
  public PdcClassification findPredefinedClassificationByNodeId(String nodeId, String instanceId) {
    PdcClassification foundClassification = null;
    for (PdcClassification pdcClassification : classifications.values()) {
      if (pdcClassification.isPredefinedForANode() && pdcClassification.getComponentInstanceId().
              equals(instanceId) && pdcClassification.getNodeId().equals(nodeId)) {
        foundClassification = pdcClassification;
        break;
      }
    }
    return foundClassification;
  }

  @Override
  public PdcClassification save(PdcClassification t) {
    PdcClassification classification;
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set violations = validator.validate(t);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    if (isPersisted(t)) {
      classification = findOne(idOf(t));
      if (t != classification) {
        classification.getPositions().clear();
        classification.getPositions().addAll(t.getPositions());
      }
    } else {
      classification = t;
      setClassificationId(classification, IdGenerator.getGenerator().nextClassificationId());
    }
    setPositionsId(classification.getPositions());
    classifications.put(idOf(classification), classification);
    return classification;
  }

  @Override
  public List<PdcClassification> save(Iterable<? extends PdcClassification> clctn) {
    List<PdcClassification> saved = new ArrayList<PdcClassification>();
    for (PdcClassification pdcClassification : clctn) {
      saved.add(save(pdcClassification));
    }
    return saved;
  }

  @Override
  public PdcClassification saveAndFlush(PdcClassification t) {
    return save(t);
  }

  @Override
  public PdcClassification findOne(Long pk) {
    return classifications.get(pk);
  }

  @Override
  public boolean exists(Long pk) {
    return (pk == null? false:classifications.containsKey(pk));
  }

  @Override
  public List<PdcClassification> findAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcClassification> findAll(Sort sort) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Page<PdcClassification> findAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

 
  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

 
  @Override
  public void delete(PdcClassification t) {
    if (isPersisted(t)) {
      classifications.remove(idOf(t));
    }
  }

  @Override
  public void delete(Iterable<? extends PdcClassification> clctn) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void flush() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private void setPositionsId(Set<PdcPosition> positions) {
    for (PdcPosition pdcPosition : positions) {
      if (pdcPosition.getId() == null || Integer.parseInt(pdcPosition.getId()) < 0) {
        pdcPosition.withId(IdGenerator.getGenerator().nextPositionIdAsString());
      }
    }
  }

  @Override
  public List<PdcClassification> findClassificationsByPdcAxisValues(List<PdcAxisValue> values) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteInBatch(Iterable<PdcClassification> itrbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(Long id) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
