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

package org.silverpeas.core.questioncontainer.question.model;

import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;

import java.io.Serializable;
import java.util.Collection;

public class Question implements Serializable {
  private static final long serialVersionUID = 3495698479955515991L;
  private boolean qcm = false;
  private int type = 0;
  private boolean isOpen = false;
  private int cluePenalty = 0;
  private int maxTime = 0;
  private int displayOrder = 0;
  private int nbPointsMin = 0;
  private int nbPointsMax = 0;
  private QuestionPK pk = null;
  private String fatherId = null;
  private String label = null;
  private String description = null;
  private String clue = null;
  private String image = null;
  private Collection<Answer> answers = null;
  private Collection<QuestionResult> questionResults = null;
  private String style = null;

  /**
   * Question constructor
   * @param pk
   * @param fatherId
   * @param label
   * @param description
   * @param clue
   * @param image
   * @param style
   * @param type
   */
  public Question(QuestionPK pk, String fatherId, String label, String description, String clue,
      String image, String style, int type) {
    super();
    this.pk = pk;
    this.fatherId = fatherId;
    this.label = label;
    this.description = description;
    this.clue = clue;
    this.image = image;
    this.style = style;
    this.type = type;
  }

  /**
   * @param pk
   * @param fatherId
   * @param label
   * @param description
   * @param clue
   * @param image
   * @param isQCM
   * @param type
   * @param open
   */
  public Question(QuestionPK pk, String fatherId, String label, String description, String clue,
      String image, boolean isQCM, int type, boolean open) {
    this(pk, fatherId, label, description, clue, image, null, type);
    this.qcm = isQCM;
    this.isOpen = open;
  }

  /**
   * @param pk
   * @param fatherId
   * @param label
   * @param description
   * @param clue
   * @param image
   * @param type
   * @param style
   * @param cluePenalty
   * @param maxTime
   * @param displayOrder
   * @param nbPointsMin
   * @param nbPointsMax
   */
  public Question(QuestionPK pk, String fatherId, String label, String description, String clue,
      String image, int type, String style, int cluePenalty, int maxTime, int displayOrder,
      int nbPointsMin, int nbPointsMax) {
    this(pk, fatherId, label, description, clue, image, style, type);
    this.cluePenalty = cluePenalty;
    this.maxTime = maxTime;
    this.displayOrder = displayOrder;
    this.nbPointsMin = nbPointsMin;
    this.nbPointsMax = nbPointsMax;
  }

  public Question(QuestionPK pk, String fatherId, String label, String description, String clue,
      String image, boolean isQCM, int type, boolean open, int cluePenalty, int maxTime,
      int displayOrder, int nbPointsMin, int nbPointsMax) {
    this(pk, fatherId, label, description, clue, image, isQCM, type, open);
    this.cluePenalty = cluePenalty;
    this.maxTime = maxTime;
    this.displayOrder = displayOrder;
    this.nbPointsMin = nbPointsMin;
    this.nbPointsMax = nbPointsMax;
  }

  public Question(QuestionPK pk, String fatherId, String label, String description, String clue,
      String image, boolean isQCM, int type, boolean open, int cluePenalty, int maxTime,
      int displayOrder, int nbPointsMin, int nbPointsMax, String style) {
    this(pk, fatherId, label, description, clue, image, isQCM, type, open, cluePenalty, maxTime,
        displayOrder, nbPointsMin, nbPointsMax);
    this.style = style;
  }

  public void setPK(QuestionPK pk) {
    this.pk = pk;
  }

  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setClue(String clue) {
    this.clue = clue;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public void setAnswers(Collection<Answer> answers) {
    int nbMaxPoints = 0;
    int nbMinPoints = 0;
    for (Answer answer1 : answers) {
      if (answer1.isSolution()) {
        nbMaxPoints += answer1.getNbPoints();
      } else {
        nbMinPoints += answer1.getNbPoints();
      }
    }
    if (getNbPointsMin() > nbMinPoints) {
      nbMinPoints = getNbPointsMin();
    }
    if (getNbPointsMax() < nbMaxPoints) {
      nbMaxPoints = getNbPointsMax();
    }
    this.setNbPointsMax(nbMaxPoints);
    this.setNbPointsMin(nbMinPoints);
    this.answers = answers;
  }

  public void setQCM(boolean isQCM) {
    this.qcm = isQCM;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setOpen(boolean open) {
    this.isOpen = open;
  }

  public void setCluePenalty(int cluePenalty) {
    this.cluePenalty = cluePenalty;
  }

  public void setMaxTime(int maxTime) {
    this.maxTime = maxTime;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  public void setNbPointsMin(int nbPointsMin) {
    this.nbPointsMin = nbPointsMin;
  }

  public void setNbPointsMax(int nbPointsMax) {
    this.nbPointsMax = nbPointsMax;
  }

  public void setQuestionResults(Collection<QuestionResult> results) {
    this.questionResults = results;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public QuestionPK getPK() {
    return this.pk;
  }

  public String getFatherId() {
    return this.fatherId;
  }

  public String getLabel() {
    return this.label;
  }

  public String getDescription() {
    return this.description;
  }

  public String getClue() {
    return this.clue;
  }

  public String getImage() {
    return this.image;
  }

  public Collection<Answer> getAnswers() {
    return this.answers;
  }

  public Answer getAnswer(String answerId) {
    Collection<Answer> answers = getAnswers();
    for (final Answer answer : answers) {
      if (answer.getPK().getId().equals(answerId)) {
        return answer;
      }
    }
    return null;
  }

  public int getType() {
    return this.type;
  }

  public int getCluePenalty() {
    return this.cluePenalty;
  }

  public int getMaxTime() {
    return this.maxTime;
  }

  public int getDisplayOrder() {
    return this.displayOrder;
  }

  public int getNbPointsMin() {
    return this.nbPointsMin;
  }

  public int getNbPointsMax() {
    return this.nbPointsMax;
  }

  public float getAverageScore() {
    float averageScore = 0;
    for (Answer answer : answers) {
      averageScore += answer.getNbVoters() * answer.getNbPoints();
    }
    return averageScore;
  }

  public String getStyle() {
    return style;
  }

  public boolean isQCM() {
    return this.qcm;
  }

  public boolean isOpen() {
    return this.isOpen;
  }

  public Collection<QuestionResult> getQuestionResults() {
    return this.questionResults;
  }

  public boolean isOpenStyle() {
    return "open".equals(getStyle());
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("Question {\n");
    result.append("  getPK() = ").append(getPK()).append("\n");
    result.append("  getLabel() = ").append(getLabel()).append("\n");
    result.append("}");
    return result.toString();
  }
}
