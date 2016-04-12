/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.questioncontainer.answer.model;

import java.io.Serializable;

import org.silverpeas.core.ForeignPK;

public class Answer implements Serializable {
  private static final long serialVersionUID = 7915608110782813687L;
  private int nbPoints = 0;
  private boolean isSolution = false;
  private boolean isOpened = false;
  private int nbVoters = 0;
  private String image = null;
  private AnswerPK pk = null;
  private ForeignPK questionPK = null;
  private String label = null;
  private String comment = null;

  /**
   * When this answer is selected, the target is the questionLink
   */
  private String questionLink;

  /**
   * @deprecated
   */
  public Answer(AnswerPK pk, ForeignPK questionPK, String label, int nbPointsPos, int nbPointsNeg,
      boolean isSolution, String comment, int nbVoters, boolean isOpened, String image) {
    this.questionPK = questionPK;
    this.label = label;
    this.nbVoters = nbVoters;
    this.pk = pk;
    this.isSolution = isSolution;
    this.comment = comment;
    this.isOpened = isOpened;
    this.image = image;
  }

  public Answer(AnswerPK pk, ForeignPK questionPK, String label, int nbPoints, boolean isSolution,
      String comment, int nbVoters, boolean isOpened, String image, String questionLink) {
    this.questionPK = questionPK;
    this.label = label;
    this.nbVoters = nbVoters;
    this.pk = pk;
    this.isSolution = isSolution;
    this.comment = comment;
    this.isOpened = isOpened;
    this.image = image;
    this.questionLink = questionLink;
    this.nbPoints = nbPoints;
  }

  public Answer(AnswerPK pk, ForeignPK questionPK, String label, String comment, int nbVoters,
      boolean isOpened, String image, String questionLink) {
    this(pk, questionPK, label, 0, false, comment, nbVoters, isOpened, image, questionLink);
  }

  public void setPK(AnswerPK pk) {
    this.pk = pk;
  }

  public void setQuestionPK(ForeignPK questionPK) {
    this.questionPK = questionPK;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setNbVoters(int nbVoters) {
    this.nbVoters = nbVoters;
  }

  public void setNbPoints(int nbPoints) {
    this.nbPoints = nbPoints;
  }

  public void setIsSolution(boolean isSolution) {
    this.isSolution = isSolution;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setIsOpened(boolean isOpened) {
    this.isOpened = isOpened;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public void setQuestionLink(String questionLink) {
    this.questionLink = questionLink;
  }

  public AnswerPK getPK() {
    return this.pk;
  }

  public ForeignPK getQuestionPK() {
    return this.questionPK;
  }

  public String getLabel() {
    return this.label;
  }

  public int getNbVoters() {
    return this.nbVoters;
  }

  public int getNbPoints() {
    return this.nbPoints;
  }

  public boolean isSolution() {
    return this.isSolution;
  }

  public String getComment() {
    return this.comment;
  }

  public boolean isOpened() {
    return this.isOpened;
  }

  public String getImage() {
    return this.image;
  }

  public String getQuestionLink() {
    return this.questionLink;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Answer {\n");

    result.append("  getQuestionPK() = " + getQuestionPK() + "\n");
    result.append("  getLabel() = " + getLabel() + "\n");
    result.append("  getNbVoters() = " + getNbVoters() + "\n");
    result.append("  getPK() = " + getPK() + "\n");
    result.append("  getNbPoints() = " + getNbPoints() + "\n");
    result.append("  isSolution() = " + isSolution() + "\n");
    result.append("  getComment() = " + getComment() + "\n");
    result.append("  isOpened() = " + isOpened() + "\n");
    result.append("  getQuestionLink() = " + getQuestionLink() + "\n");
    result.append("}");
    return result.toString();
  }
}