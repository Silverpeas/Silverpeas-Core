package com.stratelia.webactiv.util.answer.model;

import com.silverpeas.util.ForeignPK;
import java.io.Serializable;

public class Answer implements Serializable 
{
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
When this answer is selected, the target is the questionLink
   */
   private String questionLink;
   
   /* @deprecated */
  public Answer(AnswerPK pk, ForeignPK questionPK, String label, int nbPointsPos, int nbPointsNeg, boolean isSolution, String comment, int nbVoters, boolean isOpened, String image) {
    setQuestionPK(questionPK);
    setLabel(label);
    setNbVoters(nbVoters);
    setPK(pk);
    setNbPoints(0);
    setIsSolution(isSolution);
    setComment(comment);
    setIsOpened(isOpened);
    setImage(image);
  }

   public Answer(AnswerPK pk, ForeignPK questionPK, String label, int nbPoints, boolean isSolution, String comment, int nbVoters, boolean isOpened, String image, String questionLink) 
   {
    setQuestionPK(questionPK);
    setLabel(label);
    setNbVoters(nbVoters);
    setPK(pk);
    setNbPoints(nbPoints);
    setIsSolution(isSolution);
    setComment(comment);
    setIsOpened(isOpened);
    setImage(image);
    setQuestionLink(questionLink);
   }
   
   public Answer(AnswerPK pk, ForeignPK questionPK, String label, String comment, int nbVoters, boolean isOpened, String image, String questionLink) 
   {
    setQuestionPK(questionPK);
    setLabel(label);
    setNbVoters(nbVoters);
    setPK(pk);
    setNbPoints(0);
    setIsSolution(false);
    setComment(comment);
    setIsOpened(isOpened);
    setImage(image);
    setQuestionLink(questionLink);
   }
   
   public void setPK(AnswerPK pk) 
   {
    this.pk = pk;
   }
   
   public void setQuestionPK(ForeignPK questionPK) 
   {
    this.questionPK = questionPK;
   }
   
   public void setLabel(String label) 
   {
    this.label = label;
   }
   
   public void setNbVoters(int nbVoters) 
   {
    this.nbVoters = nbVoters;
   }
   
   public void setNbPoints(int nbPoints) 
   {
    this.nbPoints = nbPoints;
   }
   
   public void setIsSolution(boolean isSolution) 
   {
    this.isSolution = isSolution;
   }
   
   public void setComment(String comment) 
   {
    this.comment = comment;
   }
   
   public void setIsOpened(boolean isOpened) 
   {
    this.isOpened = isOpened;
   }
   
   public void setImage(String image) 
   {
    this.image = image;
   }
   
   public void setQuestionLink(String questionLink) 
   {
   	this.questionLink = questionLink;
   }
   
   public AnswerPK getPK() 
   {
    return this.pk;
   }

   public ForeignPK getQuestionPK() 
   {
    return this.questionPK;
   }
   
   public String getLabel() 
   {
    return this.label;
   }
   
   public int getNbVoters() 
   {
    return this.nbVoters;
   }
   
   public int getNbPoints() 
   {
    return this.nbPoints;
   }
   
   public boolean isSolution() 
   {
    return this.isSolution;
   }
   
   public String getComment() 
   {
    return this.comment;
   }
   
   public boolean isOpened() 
   {
    return this.isOpened;
   }
   
   public String getImage() 
   {
    return this.image;
   }
   
   public String getQuestionLink() 
   {
   	return this.questionLink;
   }

   public String toString() {
		StringBuffer result = new StringBuffer("Answer {\n");

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