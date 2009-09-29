<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
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