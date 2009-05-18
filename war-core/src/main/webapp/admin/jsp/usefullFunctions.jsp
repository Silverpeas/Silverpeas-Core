<%@page import="java.util.Hashtable"%>
<%!

void sortSpacesByName(String[] asSpacesIds, String[] asSpacesNames)
{
    String[] asTempIds = new String[asSpacesIds.length];
    String[] asTempNames = new String[asSpacesNames.length];
    String sTemp = "";
    int[] anAlreadyFound = new int[asSpacesNames.length];
    int nStartIndex;

    // Reset the array
    for(int nI=0; nI < anAlreadyFound.length; nI++)
        anAlreadyFound[nI] = -1;

    for(int nI=0; nI < asSpacesNames.length; nI++)
    {
		// Search the nI name position

        // Reset with the first index not already found
        nStartIndex = -1;
        for(int nK=0; nK < anAlreadyFound.length && nStartIndex == -1; nK++)
            if(anAlreadyFound[nK] == -1)
                nStartIndex = nK;

        sTemp = asSpacesNames[nStartIndex];
        for(int nJ = nStartIndex; nJ < asSpacesNames.length; nJ++)
        {
            if(anAlreadyFound[nJ] == -1 && asSpacesNames[nJ].compareToIgnoreCase(sTemp) < 0)
            {
                sTemp = asSpacesNames[nJ];
                nStartIndex = nJ;
            }
        }

        // Put the n element in the temp array
        asTempNames[nI] = asSpacesNames[nStartIndex];
        asTempIds[nI] = asSpacesIds[nStartIndex];
        anAlreadyFound[nStartIndex] = nI;
    }

    // Copy the temporary arrays in the destination
    for(int nI=0; nI < asSpacesNames.length; nI++)
        asSpacesNames[nI] = asTempNames[nI];
    for(int nI=0; nI < asSpacesIds.length; nI++)
        asSpacesIds[nI] = asTempIds[nI];
}

public void put(String key, String champ, String defaut, Hashtable icons)
{
    if (champ==null)
        icons.put(key, defaut);
    else 
        if (champ.length()==0)
            icons.put(key, defaut);
        else
            icons.put(key, champ);
}


%>