// Author : Norbert CHAIX
// Date : 2002/02/18

package com.stratelia.silverpeas.classifyEngine;


class ClassifyManager
{
    public static void main(String[] args)
    {
       /* try
        {*/
						/*ClassifyEngine ce = new ClassifyEngine();
						ArrayList alValues = new ArrayList();

						// Register /unregister Axis
						for(int nI=0; nI < 20; nI++)
								ce.registerAxis(nI);
						ce.unregisterAxis(9);
						ce.unregisterAxis(15);
						ce.registerAxis(21);
						ce.registerAxis(22);
						ce.unregisterAxis(3);
						ce.registerAxis(23);*/
								
						
						// Classify
						/*alValues.add(new Value(0, "/0"));
						alValues.add(new Value(1, "/1"));
						alValues.add(new Value(2, "/2"));
						Position p = new Position(alValues);
						ce.classifySilverObject(null, 0, p);*/

						// Update
						/*alValues = new ArrayList();
						alValues.add(new Value(0, "/0/1/"));
						alValues.add(new Value(1, "/0/12/"));
						p.setValues(alValues);
						p.setPositionId(10);
						ce.updateSilverObjectPosition(null, p);*/

						// Unclassify
						/*alValues = new ArrayList();
						alValues.add(new Value(0, "/0/1/"));
						alValues.add(new Value(1, "/0/1/"));
						Position p = new Position(alValues);
						ce.unclassifySilverObjectByPosition(null, 5, p);*/
						
						// UnclassifyBis
						//ce.unclassifySilverObjectByPositionId(null, 12);

						// Search
						/*ArrayList alCriterias = new ArrayList();
						alCriterias.add(new Criteria(0, "/0/1/2/"));
						alCriterias.add(new Criteria(1, "/0/1/"));
						List alSilverObject = ce.findSilverOjectByCriterias(alCriterias);
						for(int nI=0; nI < alSilverObject.size(); nI++)
								System.out.println("SilverObject: " + ((Integer) alSilverObject.get(nI)).intValue());*/

						// Search bis
						/*List alPositions = ce.findPositionsBySilverOjectId(1);
						for(int nI=0; nI < alPositions.size(); nI++)
								System.out.println("Position PositionId: " + ((Position)alPositions.get(nI)).getPositionId());*/

						// Remove all axis Values
						//ce.removeAllPositionValuesOnAxis(0);

						// Replace values
						/*Value oldValue = new Value(0, "/146/");
						Value newValue = new Value(0, null);
						ce.replaceValuesOnAxis(oldValue, newValue);*/
       /* }
        catch (Exception e)
        {
						System.out.println(e);
        }*/
    }
    
    public ClassifyManager()
    {
    }
}
