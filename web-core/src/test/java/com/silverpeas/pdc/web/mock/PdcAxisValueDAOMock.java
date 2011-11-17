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

import com.silverpeas.pdc.dao.PdcAxisValueDAO;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcAxisValuePk;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Named;
import org.synyx.hades.domain.Page;
import org.synyx.hades.domain.Pageable;
import org.synyx.hades.domain.Sort;
import org.synyx.hades.domain.Specification;

/**
 * Mock the PdcAxisValueDAO for tests.
 */
@Named("pdcAxisValueDAO")
public class PdcAxisValueDAOMock implements PdcAxisValueDAO {

  @Override
  public PdcAxisValue save(PdcAxisValue t) {
    return t;
  }

  @Override
  public List<PdcAxisValue> save(Collection<? extends PdcAxisValue> clctn) {
    return new ArrayList<PdcAxisValue>(clctn);
  }

  @Override
  public PdcAxisValue saveAndFlush(PdcAxisValue t) {
    return t;
  }

  @Override
  public PdcAxisValue readByPrimaryKey(PdcAxisValuePk pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean exists(PdcAxisValuePk pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcAxisValue> readAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcAxisValue> readAll(Sort sort) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Page<PdcAxisValue> readAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcAxisValue> readAll(Specification<PdcAxisValue> s) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Page<PdcAxisValue> readAll(Specification<PdcAxisValue> s, Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Long count() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Long count(Specification<PdcAxisValue> s) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(PdcAxisValue t) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(Collection<? extends PdcAxisValue> clctn) {
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

  @Override
  public List<PdcAxisValue> findByAxisId(Long axisId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
