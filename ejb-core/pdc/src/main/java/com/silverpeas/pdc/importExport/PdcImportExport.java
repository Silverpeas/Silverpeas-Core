/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.pdc.importExport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Classe gérant la manipulation des axes du pdc pour le module d'importExport.
 * @author sdevolder
 */
public class PdcImportExport {

  // Variables
  PdcBm pdcBm = null;

  // Méthodes
  /**
   * Méthodes créant les liens entre les silverObjectId et les positions définies dans un xml mappé
   * dans la classe PdcPositionsType.
   * @param silverObjectId - id de l'objet à lier au pdc
   * @param componentId - id du composant ...
   * @param positions - object contenant les classes classifyValue contenant les axes du pdc à lier
   * @return false si une des données est incorrecte, true sinon
   * @throws PdcException
   */
  public boolean addPositions(int silverObjectId, String componentId, PdcPositionsType positions)
      throws PdcException {
    boolean result = true;
    List<ClassifyPosition> listPositions = positions.getListClassifyPosition();
    // récupération des positions valides
    List<ClassifyPosition> validPositions = getValidPositions(componentId, listPositions);
    if (listPositions.size() != validPositions.size()) {
      result = false;
    }
    if (validPositions != null) {
      for (ClassifyPosition classifyPos : validPositions) {
        try {
          getPdcBm().addPosition(silverObjectId, classifyPos, componentId);
        } catch (PdcException ex) {
          result = false;
          SilverTrace.error("Pdc", "PdcImportExport.addPositions(int,String,PdcPositionsType)",
              "Pdc.CANNOT_INSERT_VALUE", ex);
        }
      }
    }
    return result;
  }

  public List<ClassifyPosition> getValidPositions(String componentId,
      List<ClassifyPosition> positions) throws PdcException {
    List<ClassifyPosition> validPositions = new ArrayList<ClassifyPosition>();
    // récupération des axes utilisés par le composant
    List<UsedAxis> usedAxis = getPdcBm().getUsedAxisByInstanceId(componentId);
    if (usedAxis != null && !usedAxis.isEmpty()) {
      if (positions != null) {
        for (ClassifyPosition classifyPos : positions) {
          if (isValidPosition(usedAxis, classifyPos)) {
            validPositions.add(classifyPos);
          }
        }
      }
    }
    return validPositions;
  }

  private boolean isValidPosition(List<UsedAxis> usedAxis, ClassifyPosition position) {
    List<ClassifyValue> values = position.getValues();
    boolean valueExist = true;
    for (ClassifyValue value : values) {
      valueExist = isExistingValue(value);
    }
    if (!valueExist) {
      // une des valeurs n'est pas correcte
      return false;
    }
    // toutes les valeurs sont correctes
    // Il faut encore vérifier que le classement est complet
    return isCompletePosition(usedAxis, position);
  }

  private boolean isCompletePosition(List<UsedAxis> usedAxis, ClassifyPosition position) {
    for (UsedAxis axis : usedAxis) {
      if (!isUsedAxisOK(axis, position)) {
        return false;
      }
    }
    return true;
  }

  private boolean isUsedAxisOK(UsedAxis axis, ClassifyPosition position) {
    if (axis.getMandatory() == 1) {
      // l'utilisation de cet axe est obligatoire
      // Est ce qu'il y a une valeur sur cet axe
      String valueId = position.getValueOnAxis(axis.getAxisId());
      if (valueId == null) {
        return false;
      } else {
        return true;
      }
    }
    // le classement sur cet axe est facultatif
    return true;

  }

  private boolean isExistingValue(ClassifyValue value) {
    int axisId = value.getAxisId();
    String path = value.getValue();
    String leafId = extractLeaf(path);

    Value existingValue = null;
    try {
      existingValue = getPdcBm().getValue(Integer.toString(axisId), leafId);

      if (existingValue == null) {
        return false;
      } else {
        // Si la valeur existe, on vérifie que le chemin fournit est correct
        if (!existingValue.getFullPath().equals(path)) {
          return false;
        }
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private String extractLeaf(String path) {
    path = path.substring(0, path.length() - 1);
    String leaf = path.substring(path.lastIndexOf("/") + 1);
    return leaf;
  }

  /**
   * Méthode de récupération des position pdc pour un objet silverpeas donné.
   * @param silverObjectId
   * @param sComponentId
   * @return - liste de ClassifyPosition
   * @throws PdcException
   */
  public List<ClassifyPosition> getPositions(int silverObjectId, String sComponentId)
      throws PdcException {
    List<ClassifyPosition> list = getPdcBm().getPositions(silverObjectId, sComponentId);
    if (list.isEmpty()) {
      return null;
    }
    return list;
  }

  public boolean isClassifyingMandatory(String componentId) throws PdcException {
    return getPdcBm().isClassifyingMandatory(componentId);
  }

  /**
   * Méthodes récupérant la totalité des axes utilisés par les positions de la liste en paramètre
   * @param listClassifyPosition - liste des positions dont on veut les axes
   * @return un objet PdcType contenant les axes recherchés
   * @throws PdcException
   */
  public PdcType getPdc(List<ClassifyPosition> listClassifyPosition) throws PdcException {

    // On construit une liste des axes à exporter
    Set<Integer> set = new HashSet<Integer>();
    for (ClassifyPosition classPos : listClassifyPosition) {
      List<ClassifyValue> listClassVal = classPos.getListClassifyValue();
      for (ClassifyValue classVal : listClassVal) {
        set.add(Integer.valueOf(classVal.getAxisId()));
      }
    }

    // On parcours la liste des axes à exporter
    PdcType pdcType = new PdcType();
    List<AxisType> listAxisType = new ArrayList<AxisType>();
    pdcType.setListAxisType(listAxisType);
    for (Integer axis : set) {
      int axisId = axis.intValue();
      // Récupération de la "value" root de l'axe
      Value valueRoot = getPdcBm().getRoot(Integer.toString(axisId));
      AxisType axisType = new AxisType();
      axisType.setId(axisId);
      axisType.setName(valueRoot.getName());
      axisType.setPath(valueRoot.getFullPath());
      listAxisType.add(axisType);
      // Récupération de la totalité de l'arbre de l'axe avec la méthode
      // récursive getValueTree
      List listPdcValueType = getValueTree(axisId, valueRoot.getPK().getId());
      axisType.setListPdcValueType(listPdcValueType);
    }
    return pdcType;
  }

  /**
   * Méthode récursive utilisée par la méthode getPdc de récupération d'axes.
   * @param axisId - id de l'axe que l'on veut récupéré
   * @param fatherValueId - id de la "value" dont on veut les fils
   * @return - liste des values, fils du value d id fatherValueId, null si le père est une feuille
   * de l'arbre
   * @throws PdcException
   */
  private List<PdcValueType> getValueTree(int axisId, String fatherValueId) throws PdcException {
    List<PdcValueType> listChildrenPdcValue = new ArrayList<PdcValueType>();
    // Récupération des ids des valeurs filles directes du value père
    List<String> listValueId = getPdcBm().getDaughterValues(Integer.toString(axisId), fatherValueId);
    if (listValueId != null) {// L'exception oject non trouvé n'est pas gérée
      // dans la méthode DAO!!!
      for (String valueId : listValueId) {
        // Récupération de l'objet value et remplissage de l'objet de mapping
        Value value = getPdcBm().getValue(Integer.toString(axisId), valueId);
        PdcValueType pdcValueType = new PdcValueType();
        pdcValueType.getPK().setId(valueId);
        pdcValueType.setName(value.getName());
        pdcValueType.setDescription(value.getDescription());
        pdcValueType.setPath(value.getFullPath());
        listChildrenPdcValue.add(pdcValueType);
        // Parcours récursif
        pdcValueType.setListPdcValueType(getValueTree(axisId, pdcValueType.getPK().getId()));
      }
    }

    return listChildrenPdcValue;
  }

  /**
   * @return l'EJB PdcBm
   */
  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = new PdcBmImpl();
    }
    return pdcBm;
  }
}