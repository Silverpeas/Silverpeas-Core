package com.stratelia.webactiv.searchEngine.model;

public class Test
{
  static public void main(String[] args)
  {
    /*try
    {
		long minFound = Long.MAX_VALUE;
		long minnotFound = Long.MAX_VALUE;
		long maxFound = 0;
		long maxnotFound = 0;
		long moyFound = 0;
		long moynotFound = 0;
		long MIN = 0;
		long MAX = 0;
		long MOY;
		int cptFound = 0;
		int cptnotFound =0;
		int cpt = 0;
		long t;
		long TempFound = 0;
		long TempnotFound = 0;
		long Temp = 0;
		String request = null;
		java.io.InputStream file = new java.io.FileInputStream(args[0]);
		java.io.BufferedReader buffer = new java.io.BufferedReader(new InputStreamReader(file));
		WAIndexSearcher Searcher = new WAIndexSearcher();
		while((request = buffer.readLine())!=null)
		{

			QueryDescription query = new QueryDescription(request);

			query.addSpaceComponentPair("test", "test");

			// the current time in milliseconds before searching this request
			t = System.currentTimeMillis();
			
			MatchingIndexEntry []results = Searcher.search(query);

			// the time in milliseconds, needed to search this request
				long l = System.currentTimeMillis()-t;
	
			if(results.length!=0)
			{	
				cptFound++;
				if(l < minFound) minFound = l;
				if(l > maxFound) maxFound = l;
				TempFound = TempFound + l;
				if (cptFound != 0) moyFound = TempFound/cptFound;
				else moyFound = 0;
			}

			if(results.length == 0)			
			{
				cptnotFound++;
				if(l < minnotFound) minnotFound = l;
				if(l > maxnotFound) maxnotFound = l;
				TempnotFound = TempnotFound + l;
				if(cptnotFound != 0) moynotFound = TempnotFound/cptnotFound;
				else moynotFound = 0;
			}
			cpt++;
			Temp = Temp + l;
		}
		if (cpt != 0) MOY = Temp/cpt;
		else MOY = 0;
		MIN = Math.min(minFound, minnotFound);
		MAX = Math.max(maxFound, maxnotFound);
			
	}
	catch (Exception e)
	{
		 System.err.println("search failed : " +e.getMessage());
	}
  */
  }
}
