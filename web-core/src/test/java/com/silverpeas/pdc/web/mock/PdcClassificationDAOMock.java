/*
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.silverpeas.pdc.dao.PdcClassificationDAO;
import com.silverpeas.pdc.model.PdcClassification;
import java.util.Collection;
import java.util.List;
import javax.inject.Named;
import org.synyx.hades.domain.Page;
import org.synyx.hades.domain.Pageable;
import org.synyx.hades.domain.Sort;
import org.synyx.hades.domain.Specification;

/**
 * Mock of the PdcClassification service bean
 */
@Named("pdcClassificationDAO")
public class PdcClassificationDAOMock implements PdcClassificationDAO {

  @Override
  public PdcClassification findPredefinedClassificationByComponentInstanceId(String instanceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PdcClassification findPredefinedClassificationByNodeId(String nodeId, String instanceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PdcClassification save(PdcClassification t) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcClassification> save(Collection<? extends PdcClassification> clctn) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PdcClassification saveAndFlush(PdcClassification t) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PdcClassification readByPrimaryKey(Long pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean exists(Long pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcClassification> readAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcClassification> readAll(Sort sort) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Page<PdcClassification> readAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcClassification> readAll(Specification<PdcClassification> s) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Page<PdcClassification> readAll(Specification<PdcClassification> s, Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Long count() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Long count(Specification<PdcClassification> s) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(PdcClassification t) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(Collection<? extends PdcClassification> clctn) {
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
  
  
}
