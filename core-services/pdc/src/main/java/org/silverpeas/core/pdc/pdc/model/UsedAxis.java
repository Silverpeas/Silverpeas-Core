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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import java.util.List;

/**
 * This class contains headers of axis. And uses the persistence class for the DAO. The user can
 * access to the axis main information.
 * @author Nicolas EYSSERIC
 */
public class UsedAxis extends SilverpeasBean implements java.io.Serializable {

  private static final long serialVersionUID = -4027631654408246315L;

  /**
   * The Job'Peas instance which used this axis
   */
  private String instanceId = null;

  /**
   * The id of the axis used
   */
  private int axisId = -1;

  /**
   * The id of the base value
   */
  private int baseValue = -1;

  /**
   * mandatory use or not
   */
  private int mandatory = -1;

  /**
   * values on this axis is variant or not
   */
  private int variant = -1;

  private String _axisName = null;

  private String _axisType = null;

  private String _baseValueName = null;

  private List _axisValues = null;

  private int _axisRootId = -1;

  private String _invariantValue = null;

  private AxisHeader _axisHeader = null;

  public void _setAxisHeader(AxisHeader header) {
    _axisHeader = header;
  }

  public UsedAxis() {
  }

  public UsedAxis(UsedAxisPK pk, String instanceId, int axisId, int baseValue,
      int mandatory, int variant) {
    setPK(pk);
    this.instanceId = instanceId;
    this.axisId = axisId;
    this.baseValue = baseValue;
    this.mandatory = mandatory;
    this.variant = variant;
  }

  public UsedAxis(int usedAxisId, String instanceId, int axisId, int baseValue,
      int mandatory, int variant) {
    setPK(new UsedAxisPK(usedAxisId));
    this.instanceId = instanceId;
    this.axisId = axisId;
    this.baseValue = baseValue;
    this.mandatory = mandatory;
    this.variant = variant;
  }

  public UsedAxis(String usedAxisId, String instanceId, int axisId,
      int baseValue, int mandatory, int variant) {
    setPK(new UsedAxisPK(usedAxisId));
    this.instanceId = instanceId;
    this.axisId = axisId;
    this.baseValue = baseValue;
    this.mandatory = mandatory;
    this.variant = variant;
  }

  //
  // public methods
  //

  public String getInstanceId() {
    return this.instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int getAxisId() {
    return this.axisId;
  }

  public void setAxisId(int axisId) {
    this.axisId = axisId;
  }

  public int getBaseValue() {
    return this.baseValue;
  }

  public void setBaseValue(int baseValue) {
    this.baseValue = baseValue;
  }

  public int getMandatory() {
    return this.mandatory;
  }

  public void setMandatory(int mandatory) {
    this.mandatory = mandatory;
  }

  public int getVariant() {
    return this.variant;
  }

  public void setVariant(int variant) {
    this.variant = variant;
  }

  public void _setAxisName(String axisName) {
    this._axisName = axisName;
  }

  public String _getAxisName() {
    return this._axisName;
  }

  public String _getAxisName(String language) {
    if (!I18NHelper.isI18nContentActivated || _axisHeader == null)
      return _getAxisName();

    return _axisHeader.getName(language);
  }

  public void _setAxisType(String axisType) {
    this._axisType = axisType;
  }

  public String _getAxisType() {
    return this._axisType;
  }

  public void _setBaseValueName(String baseValueName) {
    this._baseValueName = baseValueName;
  }

  public String _getBaseValueName() {
    return this._baseValueName;
  }

  public void _setAxisValues(List axisValues) {
    this._axisValues = axisValues;
  }

  public List _getAxisValues() {
    return this._axisValues;
  }

  public void _setAxisRootId(int rootId) {
    this._axisRootId = rootId;
  }

  public int _getAxisRootId() {
    return this._axisRootId;
  }

  public void _setInvariantValue(String invariantValue) {
    this._invariantValue = invariantValue;
  }

  public String _getInvariantValue() {
    return this._invariantValue;
  }

  public String _getBaseValuePath() {
    String baseValuePath = "";
    Value value = null;
    String valueId = null;
    for (int a = 0; _getAxisValues() != null && a < _getAxisValues().size(); a++) {
      value = (Value) _getAxisValues().get(a);
      valueId = value.getPK().getId();
      if (valueId.equals(Integer.toString(getBaseValue()))) {
        baseValuePath = value.getPath();
        break;
      }
    }
    return baseValuePath;
  }

  /**
   * Converts the contents of the key into a readable String.
   * @return The string representation of this object
   */
  public String toString() {
    return "(pk = " + getPK() + ", instanceId = " + getInstanceId()
        + ", axisId = " + getAxisId() + ", baseValue = " + getBaseValue()
        + ", mandatory = " + getMandatory() + ", variant = " + getVariant()
        + ")";
  }

  /**
   * determine the connection type to the database
   */
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  /**
   * define the table name
   */
  public String _getTableName() {
    return "SB_Pdc_Utilization";
  }

}