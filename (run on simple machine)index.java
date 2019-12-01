import java.io.*;
import java.util.*;

public class index {
	public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        String filePath = "example/ws4/ws4";//the original file
        String docIndex = "docIndex.txt";//a file to store the fire full location
        String wordIndex = "wordIndex.txt";//a file to store the word index
        getFileIndex(filePath, docIndex);
        getWordsFrequency(docIndex, wordIndex);
        long end = System.currentTimeMillis();
        System.out.println(end-start);//show the running time
        System.out.println("Work Done!");
    }
    public static void getFileIndex(String filePath, String docIndex) {
    	//though the file path and write the document information into the docIndex
        File file = new File(filePath);//find the file
        File[] fileList = file.listFiles();//list all the file
        BufferedWriter bufw = null;
        try { //put all the file path file in to the docIndex
            bufw = new BufferedWriter(new FileWriter(docIndex));
            for (int x = 0; x < fileList.length; x++) {
                String docPath = fileList[x].getPath();
                String docName = fileList[x].getName();
                bufw.write(docName + "\t" + docPath);//write the name and path in to the file 
                bufw.newLine();
                bufw.flush();//refresh  
            }
        } catch (IOException e) {
            System.out.println("fail to open the file" + e);
        } finally {
            try {
                if (bufw != null)
                    bufw.close();
            } catch (IOException ex) {
                System.out.println("fail to close the file" + ex);
            }
        }
    }
    
    //find all the file by the docIndex, and write the wordIndex
    public static void getWordsFrequency(String docIndex, String wordIndex) throws IOException {
    	
    	        TreeMap<String, TreeMap<String, ArrayList<Integer>>> tmp = new TreeMap<String, TreeMap<String, ArrayList<Integer>>>();
    	        BufferedReader bufr = new BufferedReader(new FileReader(docIndex));
    	        BufferedWriter bufw = new BufferedWriter(new FileWriter(wordIndex));
    	        BufferedReader bufrDoc = null;
    	        String docIDandPath = null;

    	        while ((docIDandPath = bufr.readLine()) != null) {
    	            String[] docInfo = docIDandPath.split("\t");//split the string and get the file name and file path
    	            String docName = docInfo[0];//doc name
    	            String docPath = docInfo[1];//doc path
    	            bufrDoc = new BufferedReader(new FileReader(docPath));
    	            String wordLine = null;
    	            int linenumber = 1;//to count the word appeared line
    	            while ((wordLine = bufrDoc.readLine()) != null)// read line
    	            {
    	                wordLine = wordLine.replaceAll("[^a-zA-Z]", "->");//ignore the non word and change to "->"
    	                String[] words = wordLine.split("->");//get the word
    	                
    	                for (String wordOfDoc : words)
    	                    if (!wordOfDoc.equals("")) {
    	                        wordDeal(wordOfDoc, docName, tmp, linenumber);
    	                    }
    	                linenumber++;
    	            }
    	        }
    	        // write to the index
    	        String wordFreInfo = null;
    	        Set<Map.Entry<String, TreeMap<String, ArrayList<Integer>>>> entrySet = tmp.entrySet();
    	        Iterator<Map.Entry<String, TreeMap<String, ArrayList<Integer>>>> it = entrySet.iterator();
    	        while (it.hasNext()) {
    	            Map.Entry<String, TreeMap<String, ArrayList<Integer>>> em = it.next();
    	            wordFreInfo = em.getKey() + "\t" + em.getValue();
    	            bufw.write(wordFreInfo);
    	            bufw.newLine();
    	            bufw.flush();
    	        }
    	        bufw.close();
    	        bufr.close();
    	        bufrDoc.close();
    	    }

            //deal with the data 
    	    public static void wordDeal(String wordOfDoc, String docID,
    	            TreeMap<String, TreeMap<String, ArrayList<Integer>>> tmp, int linenumber) {
    	    	if (!tmp.containsKey(wordOfDoc)) {
    	            //if the word is first appear, write on a new line
    	            TreeMap<String, ArrayList<Integer>> tmpST = new TreeMap<String, ArrayList<Integer>>();
    	            ArrayList<Integer> list = new ArrayList<Integer>();
    	            list.add(linenumber);
    	            tmpST.put(docID, list);
    	            tmp.put(wordOfDoc, tmpST);
    	        } else {
    	                // the word is not the first appear, write after the word
    	            TreeMap<String, ArrayList<Integer>> tmpST = tmp.get(wordOfDoc);
    	            ArrayList<Integer> list = (ArrayList<Integer>) tmpST.get(docID);
    	            if (list == null) {
    	                list = new ArrayList<Integer>();
    	            }
    	            list.add(linenumber);
    	            tmpST.put(docID, list);//
    	            tmp.put(wordOfDoc, tmpST); //put the newest result on the file
    	        }
    	    }
	
	

}
