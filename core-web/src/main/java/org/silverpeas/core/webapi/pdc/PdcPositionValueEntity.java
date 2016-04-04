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

package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.pdc.model.PdcAxisValue;
import static org.silverpeas.core.util.StringUtil.isDefined;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.Value;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A PdC position value is a value of a position of a resource content on a given axis of the PdC. A
 * PdC axis can be compound either of terms or of hierarchical semantic trees. An example of a such
 * trees in a geographic axis can be a division of each country into different administrative
 * geographic area; for example France can be a tree splitted into regions -> departments -> towns.
 * So, a PdC position value can be then a single term or a path of terms in a such semantic tree.
 * For example, in a geographic axis, a position value can be a path such as France/Isère/Grenoble.
 * In an international context, the terms require to be translated according to the language of the
 * user that requested the position's value. So, the values in an axis are actually represented by
 * an identifier that is used as a key to get the correct translated term. Thus, in an hierarchical
 * semantic tree, the branch modeled by a value is in fact represented by a path of the term
 * identifiers from which a path of the translated terms can be easily retrieved.
 */
@XmlRootElement
public class PdcPositionValueEntity extends PdcValueEntity {

  private static final long serialVersionUID = -6826039385078009600L;

  @XmlElement(defaultValue="") @NotNull
  private String meaning;

  /**
   * Creates a new PdC position value fom the specified PdC classification value.
   * @param value a value of a PdC classification position.
   * @param inLanguage the language in which the terms of the value should be translated.
   * @return a representation of the PdC position value.
   */
  public static PdcPositionValueEntity fromClassifiyValue(final ClassifyValue value,
      String inLanguage) {
    List<Value> termPath = value.getFullPath();
    Value term = value.getFullPath().get(termPath.size() - 1);
    PdcPositionValueEntity positionValue = new PdcPositionValueEntity(
        withId(value.getValue()),
        inAxis(value.getAxisId()),
        withTranslatedMeaningOf(value, inLanguage));
    String treeId = term.getTreeId();
    if (isDefined(treeId) && Integer.valueOf(treeId) >= 0) {
      positionValue.setTreeId(treeId);
    }
    return positionValue;
  }

  /**
   * Creates a new PdC position value fom the specified value of PdC's axis.
   * @param value a value of a PdC's axis.
   * @param inLanguage the language in which the terms of the value should be translated.
   * @return a representation of the PdC position value.
   */
  static PdcPositionValueEntity fromPdcAxisValue(PdcAxisValue value, String inLanguage) {
    PdcPositionValueEntity positionValue = new PdcPositionValueEntity(
        withId(value.getValuePath() + "/"), // the last '/' for compatibility with the old way
        inAxis(value.getAxisId()),
        withTranslatedMeaningOf(value, inLanguage));
    String treeId = String.valueOf(value.getAxisId());
    positionValue.setTreeId(treeId);
    return positionValue;
  }

  /**
   * Gets a value of a position in the PdC by specifying both the axis to which the value belongs
   * and its identifier.
   * @param axisId the identifier of the axis to which the value belongs.
   * @param valueId the identifier of the value itself.
   * @return a PdC position value.
   */
  public static PdcPositionValueEntity aPositionValue(int axisId, String valueId) {
    return new PdcPositionValueEntity(valueId, axisId, "");
  }

  /**
   * Gets the business classification position's value that is represented by this PdC position
   * value.
   * @return a ClassifyValue instance.
   */
  public ClassifyValue toClassifyValue() {
    ClassifyValue value = new ClassifyValue(getAxisId(), getId());
    return value;
  }

  /**
   * Gets the business value of a PdC's axis this entity represents.
   * @return a PdcAxisValue instance.
   */
  public PdcAxisValue toPdcAxisValue() {
    return PdcAxisValue.aPdcAxisValue(getTermId(), String.valueOf(getAxisId()));
  }

  /**
   * Gets the meaning vehiculed by this value. If the value is a single term, then the meaning is
   * just that translated term. In the case the value is a branch in an hierarchical semantic tree,
   * then the meaning is made up of the path of the translated terms in the branch; then each term
   * in the branch is separated by a slash character.
   * @return the meaning vehiculed by this value.
   */
  public String getMeaning() {
    return meaning;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PdcPositionValueEntity other = (PdcPositionValueEntity) obj;
    if (!super.equals(other)) {
      return false;
    }
    return !((this.meaning == null) ? (other.meaning != null) : !this.meaning.equals(
        other.meaning));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + super.hashCode();
    hash = 59 * hash + (this.meaning != null ? this.meaning.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder synonymArray = new StringBuilder("[");
    for (String synonym : getSynonyms()) {
      synonymArray.append(synonym).append(", ");
    }
    if (synonymArray.length() > 1) {
      synonymArray.replace(synonymArray.length() - 2, synonymArray.length(), "]");
    } else {
      synonymArray.append("]");
    }
    return "PdcPositionValue{id=" + getId() + ", axisId=" + getAxisId() + ", treeId=" +
        getTreeId() + ", meaning=" + getMeaning() +
        ", synonyms=" + synonymArray.toString() + '}';
  }

  private static String withId(String valueId) {
    return valueId;
  }

  private static int inAxis(int axisId) {
    return axisId;
  }

  private static int inAxis(String axisId) {
    return Integer.valueOf(axisId);
  }

  private static String withTranslatedMeaningOf(final ClassifyValue value, String inLanguage) {
    LocalizedClassifyValue localizedValue = LocalizedClassifyValue.decorate(value, inLanguage);
    return localizedValue.getLocalizedPath();
  }

  private static String withTranslatedMeaningOf(PdcAxisValue value, String inLanguage) {
    LocalizedPdcAxisValue localizedValue = LocalizedPdcAxisValue.decorate(value, inLanguage);
    return localizedValue.getLocalizedPath();
  }

  protected PdcPositionValueEntity() {
    super();
  }

  private PdcPositionValueEntity(String id, int axisId, String path) {
    super(id, axisId);
    this.meaning = path;
  }

}
