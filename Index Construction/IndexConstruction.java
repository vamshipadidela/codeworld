import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class CSE535Assignment {

	public static HashMap<String, LinkedList<Integer>> indexHashTAAT= new HashMap<String, LinkedList<Integer>>();
	public static HashMap<String, LinkedList<Integer>> indexHashDAAT= new HashMap<String, LinkedList<Integer>>();
	public static String[] queryLine1;
	public static String[] queryLine2;
	public static String outputLogPath; 
	public static int documentsCount = 0;
	public static int comparisionsCount = 0;
	public static long startTime = 0;
	public static long endTime = 0;
	public static void main(String[] args) throws IOException{
		String indexPath = args[0];
		ConstructIndex(indexPath);		
		int K = new Integer(args[2]);
		outputLogPath = args[1]; 
		getTopK(K);
		String queryFile = args[3];
		File indexFile =new File(queryFile);
		BufferedReader indexReader = new BufferedReader(new FileReader(indexFile));
		String line;
		while((line = indexReader.readLine()) != null)
		{
			String[] queryLine1 = line.split("\\s+");
			for(int m=0;m<queryLine1.length;m++)
			{
				getPostings(queryLine1[m]);	
			}
			termAtATimeQueryAnd(queryLine1);
			termAtATimeQueryOr(queryLine1);
			docAtATimeQueryAnd(queryLine1);
			docAtATimeQueryOr(queryLine1);
		}
		indexReader.close();
	}

	private static void ConstructIndex(String indexPath) throws IOException{
		File indexFile =  new File(indexPath);
		BufferedReader indexReader = new BufferedReader(new FileReader(indexFile));
		String line = "";
		String[] indexLine = null;
		while((line = indexReader.readLine()) != null)
		{
		indexLine = line.split("\\\\"); 
		String[] tempPosting = indexLine[2].substring(2,indexLine[2].length()-1).split(",");
	    LinkedList<Integer> DAATlist = new LinkedList<Integer>(); 
	    HashMap<Integer,Integer> tempPostingsHash = new HashMap<Integer,Integer>();
	    for(int i=0;i<tempPosting.length;i++)
	    {
	    	String[] tempDocSplit = tempPosting[i].split("/");
	    	DAATlist.add(new Integer(tempDocSplit[0].replaceAll("\\s","")));
	    	tempPostingsHash.put(new Integer(tempDocSplit[0].replaceAll("\\s+","")),new Integer(tempDocSplit[1]));
	    }
	    Set<Entry<Integer, Integer>> setEntries = tempPostingsHash.entrySet();
	    Comparator<Entry<Integer, Integer>> hashComparator = new Comparator<Entry<Integer,Integer>>() {
	    	public int compare(Entry<Integer, Integer> i, Entry<Integer, Integer> j) { 
	    		Integer x = i.getValue(); 
	    		Integer y = j.getValue();
	    		return y.compareTo(x); 
	    		}
	    	}; 
	    	List<Entry<Integer, Integer>> Entrieslist = new ArrayList<Entry<Integer, Integer>>(setEntries);
	    	Collections.sort(Entrieslist, hashComparator); 
	    	LinkedHashMap<Integer, Integer> sortOnValue = new LinkedHashMap<Integer, Integer>(Entrieslist.size());
	    	for(Entry<Integer, Integer> entry : Entrieslist)
	    	{ 
	    		sortOnValue.put(entry.getKey(), entry.getValue());
	    	}

	    LinkedList<Integer> TAATList = new LinkedList<Integer>(sortOnValue.keySet());
	    indexHashDAAT.put(indexLine[0],DAATlist);
	    indexHashTAAT.put(indexLine[0],TAATList);
		}
		indexReader.close();
	}

	private static void getTopK(int k) throws IOException{
		HashMap<String, LinkedList<Integer>> topHash = new HashMap<String, LinkedList<Integer>>(indexHashDAAT);
		HashMap<String, Integer> ResultMap =new HashMap<String, Integer>(); 
		for (Map.Entry<String, LinkedList<Integer>> entry : topHash.entrySet()) {
		    String key = entry.getKey();
		    LinkedList<Integer> value = entry.getValue();
		    ResultMap.put(key, value.size());
		}
		 Set<Entry<String, Integer>> setEntries = ResultMap.entrySet();
		    Comparator<Entry<String, Integer>> hashComparator = new Comparator<Entry<String,Integer>>() {
		    	public int compare(Entry<String, Integer> i, Entry<String, Integer> j) { 
		    		Integer x = i.getValue(); 
		    		Integer y = j.getValue();
		    		return y.compareTo(x); 
		    		}
		    	}; 
		    	List<Entry<String, Integer>> Entrieslist = new ArrayList<Entry<String, Integer>>(setEntries);
		    	Collections.sort(Entrieslist, hashComparator); 
		    	List<Entry<String, Integer>> EntriesSublist = Entrieslist.subList(0, k);
		    	StringBuilder output = new StringBuilder();
		    	output.append("FUNCTION: getTopK "+k+"\r\n"+"Result:");
		    	for(Entry<String, Integer> entry : EntriesSublist)
		    	{ 
		    		output.append(" "+entry.getKey()+",");
		    	}
		    	output.deleteCharAt(output.length()-1);
		    	output.append("\r\n");
		    	writeToLog(output);
	}	

	private static void getPostings(String queryTerm) throws IOException{
		StringBuilder output = new StringBuilder();
		if(indexHashDAAT.containsKey(queryTerm))
		{
			output.append("FUNCTION: getPostings "+queryTerm+"\r\n"+"Ordered by doc IDs: ");
	    	String list = indexHashDAAT.get(queryTerm).toString();
	    		output.append(list.substring(1,list.length()-1));
	    		output.append("\r\n");
	    		writeToLog(output);
	    		output.setLength(0);
	    		list = "";
	    		output.append("Ordered by TF: ");
	    		list = indexHashTAAT.get(queryTerm).toString();
	    		output.append(list.substring(1, list.length()-1));
	    		output.append("\r\n");
	    		writeToLog(output);
		}
		else
		{
			output.append("FUNCTION: getPostings "+queryTerm+"\r\n"+"term not found"+"\r\n");
		   	writeToLog(output);
		   	return;
		}
	}
	
	private static void termAtATimeQueryAnd(String[] queryLine) throws IOException {
		documentsCount = 0;
		comparisionsCount = 0;
		Map<String, LinkedList<Integer>> temporary = new HashMap<String, LinkedList<Integer>>(indexHashTAAT);
		Map<String, LinkedList<Integer>> tempIndex = new HashMap<String, LinkedList<Integer>>();
		tempIndex.putAll(temporary);
		startTime = System.nanoTime();
		ArrayList<LinkedList<Integer>> postingsArray = new ArrayList<LinkedList<Integer>>();
		StringBuilder output = new StringBuilder();
		output.append("FUNCTION: termAtATimeQueryAnd");
		for(int j=0;j<queryLine.length;j++)
		{
			output.append(" "+queryLine[j]+",");
		}
	    output.deleteCharAt(output.length()-1);
	   	output.append("\r\n");
		for(int i=0;i<queryLine.length;i++)
		{
			if(tempIndex.containsKey(queryLine[i]))
			{
				postingsArray.add(i, tempIndex.get(queryLine[i])); 	
			}						
			else
			{
			   	output.append("terms not found"+"\r\n");
			   	writeToLog(output);
			   	return;
			}
		}
			LinkedList<Integer> result = new LinkedList<Integer>();
			LinkedList<Integer> tempPosting = new LinkedList<Integer>(postingsArray.get(0));
			for(int m=1;m<postingsArray.size();m++)
			{
				result.clear();
				documentsCount = 0;
				for(Integer docId1:tempPosting)
				{
					for(Integer docId2:postingsArray.get(m))
					{
						comparisionsCount++;
						if(docId1.intValue()==docId2.intValue())
						{
							documentsCount++;
							result.add(docId1);
						}	
					}
				}		
				if(!result.isEmpty())
				{
					tempPosting.clear();
					tempPosting =(LinkedList) result.clone();	
				}
				else
					break;
		}	
			
			String list = result.toString();
			endTime =System.nanoTime();
			HashMap<LinkedList<Integer>, Integer> tempMap =new HashMap<LinkedList<Integer>, Integer>(); 
			for (int k=0;k<postingsArray.size();k++) {
			    tempMap.put(postingsArray.get(k), postingsArray.get(k).size());
			}
			 Set<Entry<LinkedList<Integer>, Integer>> setEntries = tempMap.entrySet();
			    Comparator<Entry<LinkedList<Integer>, Integer>> hashComparator = new Comparator<Entry<LinkedList<Integer>,Integer>>() {
			    	public int compare(Entry<LinkedList<Integer>, Integer> i, Entry<LinkedList<Integer>, Integer> j) { 
			    		Integer x = i.getValue(); 
			    		Integer y = j.getValue();
			    		return x.compareTo(y); 
			    		}
			    	}; 
			    	List<Entry<LinkedList<Integer>, Integer>> EntriesSublist = new ArrayList<Entry<LinkedList<Integer>, Integer>>(setEntries);
			    	Collections.sort(EntriesSublist, hashComparator); 
					postingsArray.clear();
					int p=0;
					for(Entry<LinkedList<Integer>, Integer> entry : EntriesSublist)
			    	{ 
						postingsArray.add(p,entry.getKey());
						p++;
			    	}
			    	result.clear();
			    	tempPosting = new LinkedList<Integer>(postingsArray.get(0));
					int optCount =0;
					for(int m=1;m<postingsArray.size();m++)
					{
						result.clear();
						for(Integer docId1:tempPosting)
						{
							for(Integer docId2:postingsArray.get(m))
							{
								optCount++;
								if(docId1.intValue()==docId2.intValue())
								{
									result.add(docId1);
								}	
							}
						}		
						if(!result.isEmpty())
						{
							tempPosting.clear();
							tempPosting =(LinkedList) result.clone();
						}
						else
							break;
				}
					if(postingsArray.size()==1)
					{
						result = (LinkedList) tempPosting.clone();
					}
		    Collections.sort(result); 
			long elapsedTime = endTime - startTime;
			double seconds = (double)elapsedTime / 1000000000.0;
			output.append(documentsCount+" documents are found" + "\r\n");
			output.append(comparisionsCount+" comparisons are made" + "\r\n");
			output.append(String.format("%.9f", seconds)+" seconds are used" + "\r\n");
			output.append(optCount+" comparisons are made with optimization" + "\r\n");
			output.append("Result: ");
			LinkedList<String> resultString = new LinkedList<String>();
			for(int i=0;i<result.size();i++)
			{
				String temp = String.format("%07d", result.get(i));
				resultString.add(temp);
			}
    		list = resultString.toString();
    		output.append(list.substring(1, list.length()-1));
    		output.append("\r\n");
    		writeToLog(output);	
	}
	
	private static void termAtATimeQueryOr(String[] queryLine) throws IOException{
		documentsCount = 0;
		comparisionsCount = 0;
		startTime = System.nanoTime();
		ArrayList<LinkedList<Integer>> postingsArray = new ArrayList<LinkedList<Integer>>();
		StringBuilder output = new StringBuilder();
		output.append("FUNCTION: termAtATimeQueryOr");
		for(int j=0;j<queryLine.length;j++)
		{
			output.append(" "+queryLine[j]+",");
		}
	    output.deleteCharAt(output.length()-1);
	   	output.append("\r\n");
	   	int length =queryLine.length;
		int d=0;
	   	for(int i=0;i<length;i++)
		{
			if(indexHashTAAT.containsKey(queryLine[d]))
			{
				postingsArray.add(i, indexHashTAAT.get(queryLine[d]));
			}
			else
			{
				i--;
				length--;
			}
			d++;
		}
		if(postingsArray.size()==0)
		{
			output.append("terms not found"+"\r\n");
		   	writeToLog(output);
		   	return;	
		}
		LinkedList<Integer> tempPosting = postingsArray.get(0);
		LinkedList<Integer> result = new LinkedList<Integer>(tempPosting);
		documentsCount = tempPosting.size();
		for(int m=1;m<postingsArray.size();m++)
		{
			for(int x=0;x<postingsArray.get(m).size();x++)
			{
				Integer docId2 = postingsArray.get(m).get(x);
				boolean nonrepeating = true;
				for(int y=0;y<result.size();y++)
				{
					Integer docId1 = result.get(y);
					comparisionsCount++;
					if(docId1.intValue()==docId2.intValue())
					{
						nonrepeating=false;
						break;
					}	
				}
				if(nonrepeating)
				{
					result.add(docId2);
					documentsCount++;
				}
			}		
	}		
	String list = result.toString();
	endTime =System.nanoTime();
	ArrayList<LinkedList<Integer>> sortArray = new ArrayList<LinkedList<Integer>>();
	int queryLength = queryLine.length;
	d=0;
	for(int i=0;i<queryLength;i++)
	{
		if(indexHashTAAT.containsKey(queryLine[d]))
		{
			sortArray.add(i, indexHashTAAT.get(queryLine[d])); 	
		}
		else
		{
			i--;
			queryLength--;
		}
		d++;
	}
	HashMap<LinkedList<Integer>, Integer> tempMap =new HashMap<LinkedList<Integer>, Integer>(); 
	for (int k=0;k<sortArray.size();k++) {
	    tempMap.put(sortArray.get(k), sortArray.get(k).size());
	}
	 Set<Entry<LinkedList<Integer>, Integer>> setEntries = tempMap.entrySet();
	    Comparator<Entry<LinkedList<Integer>, Integer>> hashComparator = new Comparator<Entry<LinkedList<Integer>,Integer>>() {
	    	public int compare(Entry<LinkedList<Integer>, Integer> i, Entry<LinkedList<Integer>, Integer> j) { 
	    		Integer x = i.getValue(); 
	    		Integer y = j.getValue();
	    		return y.compareTo(x); 
	    		}
	    	}; 
	    	List<Entry<LinkedList<Integer>, Integer>> EntriesSublist = new ArrayList<Entry<LinkedList<Integer>, Integer>>(setEntries);
	    	Collections.sort(EntriesSublist, hashComparator); 
	    	sortArray.clear();
			int p=0;
			for(Entry<LinkedList<Integer>, Integer> entry : EntriesSublist)
	    	{ 
				sortArray.add(p,entry.getKey());
				p++;
	    	}
	    	result.clear();
	    	int optCount =0;
	    	tempPosting = sortArray.get(0);
	    	LinkedList<Integer> optResult = new LinkedList<Integer>(tempPosting);
			for(int m=1;m<sortArray.size();m++)
			{
				for(int x=0;x<sortArray.get(m).size();x++)
				{
					Integer docId2 = sortArray.get(m).get(x);
					boolean nonrepeating = true;
					for(int y=0;y<optResult.size();y++)
					{
						Integer docId1 = optResult.get(y);
						optCount++;
						if(docId1.intValue()==docId2.intValue())
						{
							nonrepeating=false;
							break;
						}	
					}
					if(nonrepeating)
					{
						optResult.add(docId2);
					}
				}		
		}	
		Collections.sort(optResult);
		LinkedList<String> resultString = new LinkedList<String>();
		for(int i=0;i<optResult.size();i++)
		{
			String temp = String.format("%07d", optResult.get(i));
			resultString.add(temp);
		}
		list = resultString.toString();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;
		output.append(documentsCount+" documents are found" + "\r\n");
		output.append(comparisionsCount+" comparisons are made" + "\r\n");
		output.append(String.format("%.9f", seconds)+" seconds are used" + "\r\n");
		output.append(optCount+" comparisons are made with optimization" + "\r\n");
		output.append("Result: ");
		output.append(list.substring(1, list.length()-1));
		output.append("\r\n");
		writeToLog(output);	
	}
	
	
	private static void docAtATimeQueryAnd(String[] queryLine) throws IOException{
		documentsCount = 0;
		comparisionsCount = 0;
		startTime = System.nanoTime();
		ArrayList<LinkedList<Integer>> postingsArray = new ArrayList<LinkedList<Integer>>();
		StringBuilder output = new StringBuilder();
		output.append("FUNCTION: docAtATimeQueryAnd");
		for(int j=0;j<queryLine.length;j++)
		{
			output.append(" "+queryLine[j]+",");
		}
	    output.deleteCharAt(output.length()-1);
	   	output.append("\r\n");
		for(int i=0;i<queryLine.length;i++)
		{
			if(indexHashDAAT.containsKey(queryLine[i]))
			{
				postingsArray.add(i, indexHashDAAT.get(queryLine[i])); 	
			}						
			else
			{
			   	output.append("terms not found"+"\r\n");
			   	writeToLog(output);
			   	return;
			}
		}
		int startMax = 0;
		int nextMax = 0;
		int startMin = Integer.MAX_VALUE;
		int nextMin = Integer.MAX_VALUE;
		LinkedList<Integer> result = new LinkedList<Integer>();	
		ArrayList<ListIterator<Integer>> iteratorArray = new ArrayList<ListIterator<Integer>>();
		for(int i=0;i<postingsArray.size();i++)
		{
			 iteratorArray.add(i,postingsArray.get(i).listIterator());
		}	
		ArrayList<Integer> start = new ArrayList<Integer>();
		for(int i=0;i<iteratorArray.size();i++)
		{
			start.add(i,iteratorArray.get(i).next().intValue());
			comparisionsCount++;
			if(start.get(i) > startMax)
				startMax = start.get(i);
			comparisionsCount++;
			if(start.get(i) < startMin)
			{
				startMin = start.get(i);
			}
		}
		/*comparisionsCount++;*/
		/*if(startMax==startMin)
		{
			documentsCount++;
			result.add(startMin);
		}*/
		nextMax = startMax;
		ArrayList<Integer> next = new ArrayList<Integer>(start);
		DAATLoop:
			while(ifdone(iteratorArray))
			{
				for(int i=0;i<iteratorArray.size();i++)
				{
					comparisionsCount++;
					while(next.get(i)<nextMax)
					{
						if(iteratorArray.get(i).hasNext())
						{
							next.set(i,iteratorArray.get(i).next().intValue());
						}
						else break DAATLoop;
					}
					comparisionsCount++;
					if(next.get(i) > nextMax)
						nextMax = next.get(i);
				}
				nextMin = Integer.MAX_VALUE;
				for(int m=0;m<next.size();m++)
				{
					comparisionsCount++;
					if(next.get(m) < nextMin)
						nextMin = next.get(m);
				}
				comparisionsCount++;
				if(nextMax==nextMin)
				{
					documentsCount++;
					result.add(nextMin);	
					nextMin = Integer.MAX_VALUE;
					for(int i=0;i<iteratorArray.size();i++)
					{
						if(iteratorArray.get(i).hasNext())
						{
							next.set(i,iteratorArray.get(i).next().intValue());
							if(next.get(i)>nextMax)
								nextMax=next.get(i);
							if(next.get(i)<nextMin)
								nextMin=next.get(i);
						}
						else break DAATLoop;
					}
				}
			}
		Loop:
		for(int i=0;i<iteratorArray.size();i++)
		{
			comparisionsCount++;
			while(next.get(i)<nextMax)
			{
				if(iteratorArray.get(i).hasNext())
				{
					next.set(i,iteratorArray.get(i).next().intValue());
				}
				else break Loop;
			}
			comparisionsCount++;
			if(next.get(i) > nextMax)
				nextMax = next.get(i);
		}
		nextMin = Integer.MAX_VALUE;
		for(int m=0;m<next.size();m++)
		{
			comparisionsCount++;
			if(next.get(m) < nextMin)
				nextMin = next.get(m);
		}
		comparisionsCount++;
		if(nextMax==nextMin)
		{
			documentsCount++;
			result.add(nextMin);
		}
		LinkedList<String> resultString = new LinkedList<String>();
		for(int i=0;i<result.size();i++)
		{
			String temp = String.format("%07d", result.get(i));
			resultString.add(temp);
		}
		String list = resultString.toString();
		endTime =System.nanoTime();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;
		output.append(documentsCount+" documents are found" + "\r\n");
		output.append(comparisionsCount+" comparisons are made" + "\r\n");
		output.append(String.format("%.9f", seconds)+" seconds are used" + "\r\n");
		output.append("Result: ");
		output.append(list.substring(1, list.length()-1));
		output.append("\r\n");
		writeToLog(output);	
	}

	private static boolean ifdone(ArrayList<ListIterator<Integer>> iteratorArray) {
		for(int i=0;i<iteratorArray.size();i++)
		{
			if(!iteratorArray.get(i).hasNext())
			return false;
		}
		return true;
	}
	
	private static void docAtATimeQueryOr(String[] queryLine) throws IOException{
		documentsCount = 0;
		comparisionsCount = 0;
		startTime = System.nanoTime();
		ArrayList<LinkedList<Integer>> postingsArray = new ArrayList<LinkedList<Integer>>();
		StringBuilder output = new StringBuilder();
		output.append("FUNCTION: docAtATimeQueryOr");
		for(int j=0;j<queryLine.length;j++)
		{
			output.append(" "+queryLine[j]+",");
		}
	    output.deleteCharAt(output.length()-1);
	   	output.append("\r\n");
	   	int length = queryLine.length;
		int d = 0;
		for(int i=0;i<length;i++)
		{
			if(indexHashDAAT.containsKey(queryLine[d]))
			{
				postingsArray.add(i, indexHashDAAT.get(queryLine[d])); 	
			}						
			else
			{
			   	i--;
			   	length--;
			}
			d++;
		}
		if(postingsArray.size()==0)
		{
			output.append("terms not found"+"\r\n");
		   	writeToLog(output);
		   	return;	
		}
		ArrayList<ListIterator<Integer>> iteratorArray = new ArrayList<ListIterator<Integer>>();
		for(int i=0;i<postingsArray.size();i++)
		{
			 iteratorArray.add(i,postingsArray.get(i).listIterator());
		}	
		ArrayList<Integer> start = new ArrayList<Integer>();
		int nextMin = Integer.MAX_VALUE;
		int startMin = Integer.MAX_VALUE;
		LinkedList<Integer> result = new LinkedList<Integer>();	
		for(int i=0;i<iteratorArray.size();i++)
		{
			start.add(i,iteratorArray.get(i).next().intValue());
			comparisionsCount++;
			if(start.get(i) < startMin)
			{
				startMin = start.get(i);
			}
		}
		ArrayList<Integer> next = new ArrayList<Integer>(start);
			while(ifAllDone(next))
			{
				documentsCount++;
				result.add(startMin);
				for(int i=0;i<iteratorArray.size();i++)
				{
					comparisionsCount++;
					if(next.get(i)==startMin)
					{
						if(iteratorArray.get(i).hasNext())
						{
							next.set(i,iteratorArray.get(i).next().intValue());
						}
						else 
						{
							next.set(i, Integer.MAX_VALUE);
						}
					}
					comparisionsCount++;
					if(next.get(i) < nextMin)
						nextMin = next.get(i);
				}
				startMin = nextMin;
				nextMin = Integer.MAX_VALUE;
			}
			LinkedList<String> resultString = new LinkedList<String>();
			for(int i=0;i<result.size();i++)
			{
				String temp = String.format("%07d", result.get(i));
				resultString.add(temp);
			}
    		String list = resultString.toString();
		endTime =System.nanoTime();
		long elapsedTime = endTime - startTime;
		double seconds = (double)elapsedTime / 1000000000.0;
		output.append(documentsCount+" documents are found" + "\r\n");
		output.append(comparisionsCount+" comparisons are made" + "\r\n");
		output.append(String.format("%.9f", seconds)+" seconds are used" + "\r\n");
		output.append("Result: ");
		output.append(list.substring(1, list.length()-1));
		output.append("\r\n");
		writeToLog(output);	
	}
	
	private static boolean ifAllDone(ArrayList<Integer> next) {
		for(int i=0;i<next.size();i++)
		{
			if(next.get(i) != Integer.MAX_VALUE)
			return true;
		}
		return false;
	}
	
	private static void writeToLog(StringBuilder output) throws IOException{
		File file = new File(outputLogPath);
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		bufferWriter.write(output.toString());
		bufferWriter.close();
	}
	
}
