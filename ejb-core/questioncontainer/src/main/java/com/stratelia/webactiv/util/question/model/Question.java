package com.stratelia.webactiv.util.question.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.webactiv.util.answer.model.Answer;

public class Question implements Serializable
{
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
   private Collection answers = null;
   private Collection questionResults = null;
   private String style = null;
   
   public Question(QuestionPK pk, String fatherId, String label, String description, String clue, String image, String style, int type)
   {
    setPK(pk);
    setFatherId(fatherId);
    setLabel(label);
    setDescription(description);
    setClue(clue);
    setImage(image);
    setStyle(style);
    setType(type);
   }
   
   public Question(QuestionPK pk, String fatherId, String label, String description, String clue, String image, boolean isQCM, int type, boolean open)
   {
    setPK(pk);
    setFatherId(fatherId);
    setLabel(label);
    setDescription(description);
    setClue(clue);
    setImage(image);
    setQCM(isQCM);
    setType(type);
    setOpen(open);
   }

   public Question(QuestionPK pk, String fatherId, String label, String description, String clue, String image, boolean isQCM, int type, boolean open, int cluePenalty, int maxTime, int displayOrder, int nbPointsMin, int nbPointsMax, String style)
   {
    setPK(pk);
    setFatherId(fatherId);
    setLabel(label);
    setDescription(description);
    setClue(clue);
    setImage(image);
    setQCM(isQCM);
    setType(type);
    setOpen(open);
    setCluePenalty(cluePenalty);
    setMaxTime(maxTime);
    setDisplayOrder(displayOrder);
    setNbPointsMin(nbPointsMin);
    setNbPointsMax(nbPointsMax);
    setStyle(style);
   }
   
   public Question(QuestionPK pk, String fatherId, String label, String description, String clue, String image, boolean isQCM, int type, boolean open, int cluePenalty, int maxTime, int displayOrder, int nbPointsMin, int nbPointsMax)
   {
    setPK(pk);
    setFatherId(fatherId);
    setLabel(label);
    setDescription(description);
    setClue(clue);
    setImage(image);
    setQCM(isQCM);
    setType(type);
    setOpen(open);
    setCluePenalty(cluePenalty);
    setMaxTime(maxTime);
    setDisplayOrder(displayOrder);
    setNbPointsMin(nbPointsMin);
    setNbPointsMax(nbPointsMax);
   }

   public Question(QuestionPK pk, String fatherId, String label, String description, String clue, String image, int type, String style, int cluePenalty, int maxTime, int displayOrder, int nbPointsMin, int nbPointsMax)
   {
    setPK(pk);
    setFatherId(fatherId);
    setLabel(label);
    setDescription(description);
    setClue(clue);
    setImage(image);
    //setQCM(isQCM);
    setType(type);
    //setOpen(open);
    setCluePenalty(cluePenalty);
    setMaxTime(maxTime);
    setDisplayOrder(displayOrder);
    setNbPointsMin(nbPointsMin);
    setNbPointsMax(nbPointsMax);
    setStyle(style);
   }
   
   public void setPK(QuestionPK pk)
   {
    this.pk = pk;
   }

   public void setFatherId(String fatherId)
   {
    this.fatherId = fatherId;
   }


   public void setLabel(String label)
   {
    this.label = label;
   }


   public void setDescription(String description)
   {
    this.description = description;
   }


   public void setClue(String clue)
   {
    this.clue = clue;
   }

   public void setImage(String image)
   {
    this.image = image;
   }

   public void setAnswers(Collection answers)
  {
            int nbMaxPoints=0;
            int nbMinPoints=0;
	    Iterator it2 = answers.iterator();
            while (it2.hasNext()) {
                    Answer answer = (Answer) it2.next();
                    if (answer.isSolution())
                      nbMaxPoints+=answer.getNbPoints();
                    else
                      nbMinPoints+=answer.getNbPoints();
            }
            if (getNbPointsMin()>nbMinPoints)
              nbMinPoints=getNbPointsMin();
            if (getNbPointsMax()<nbMaxPoints)
              nbMaxPoints=getNbPointsMax();
            this.setNbPointsMax(nbMaxPoints);
            this.setNbPointsMin(nbMinPoints);
            this.answers = answers;
   }

   public void setQCM(boolean isQCM)
   {
    this.qcm = isQCM;
   }

   public void setType(int type)
   {
    this.type = type;
   }

   public void setOpen(boolean open)
   {
    this.isOpen = open;
   }

   public void setCluePenalty(int cluePenalty)
   {
		this.cluePenalty = cluePenalty;
   }

   public void setMaxTime(int maxTime)
   {
		this.maxTime = maxTime;
   }

   public void setDisplayOrder(int displayOrder)
   {
   		this.displayOrder = displayOrder;
   }

   public void setNbPointsMin(int nbPointsMin)
   {
		this.nbPointsMin = nbPointsMin;
   }

   public void setNbPointsMax(int nbPointsMax)
   {
		this.nbPointsMax = nbPointsMax;
   }

   public void setQuestionResults(Collection results) {
      this.questionResults = results;
  }

   public void setStyle(String style) 
   {
	   this.style = style;
   }
   
   public QuestionPK getPK()
   {
    return this.pk;
   }

   public String getFatherId()
   {
    return this.fatherId;
   }

   public String getLabel()
   {
    return this.label;
   }

   public String getDescription()
   {
    return this.description;
   }

   public String getClue()
   {
    return this.clue;
   }

   public String getImage()
   {
    return this.image;
   }

   public Collection getAnswers()
   {
    return this.answers;
   }

   public Answer getAnswer(String answerId)
   {
    Collection answers = getAnswers();
    Iterator it = answers.iterator();
    Answer answer = null;
    while (it.hasNext())
    {
      answer = (Answer) it.next();
      if (answer.getPK().getId().equals(answerId))
        return answer;
    }
    return answer;
  }

   public int getType()
   {
    return this.type;
   }

   public int getCluePenalty()
   {
		return this.cluePenalty;
   }

   public int getMaxTime()
   {
		return this.maxTime;
   }

   public int getDisplayOrder()
   {
		return this.displayOrder;
   }

   public int getNbPointsMin()
   {
		return this.nbPointsMin;
   }

   public int getNbPointsMax()
   {
		return this.nbPointsMax;
   }

   public float getAverageScore()
   {
   		float averageScore = 0;
   		Iterator iterator = answers.iterator();
		while (iterator.hasNext()) {
			Answer answerDetail = (Answer) iterator.next();
			averageScore+=answerDetail.getNbVoters()*answerDetail.getNbPoints();
		}
		return averageScore;
   }

   public String getStyle()
   {
	   return style;
   }
   
   public boolean isQCM()
   {
    return this.qcm;
   }

   public boolean isOpen()
   {
    return this.isOpen;
   }
  
   public Collection getQuestionResults() {
      return this.questionResults;
   }

   public String toString() {
		StringBuffer result = new StringBuffer("Question {\n");

		result.append("  getPK() = " + getPK() + "\n");
		result.append("  getLabel() = " + getLabel() + "\n");
		result.append("}");
		return result.toString();
   }
}
