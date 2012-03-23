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

import com.google.common.collect.Lists;
import com.silverpeas.pdc.dao.PdcAxisValueRepository;
import com.silverpeas.pdc.model.PdcAxisValue;
import com.silverpeas.pdc.model.PdcAxisValuePk;
import java.util.List;
import javax.inject.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


/**
 * Mock the PdcAxisValueDAO for tests.
 */
@Named("pdcAxisValueRepository")
public class PdcAxisValueRepositoryMock implements PdcAxisValueRepository {

  @Override
  public PdcAxisValue save(PdcAxisValue t) {
    return t;
  }

  @Override
  public List<PdcAxisValue> save(Iterable<? extends PdcAxisValue> clctn) {
    return Lists.newArrayList(clctn);
  }

  @Override
  public PdcAxisValue saveAndFlush(PdcAxisValue t) {
    return t;
  }

  @Override
  public PdcAxisValue findOne(PdcAxisValuePk pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean exists(PdcAxisValuePk pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcAxisValue> findAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<PdcAxisValue> findAll(Sort sort) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Page<PdcAxisValue> findAll(Pageable pgbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet.");
  }


  @Override
  public void delete(PdcAxisValue t) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(Iterable<? extends PdcAxisValue> clctn) {
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

  @Override
  public void deleteInBatch(Iterable<PdcAxisValue> itrbl) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete(PdcAxisValuePk id) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
