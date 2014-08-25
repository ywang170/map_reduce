package mp4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
//import java.io.BufferedWriter;
//import java.io.File;
import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
 
public class maple_exe {
	
	private static String inputfile;
    //private static String output;
	
    public static void countLetters(String sourcefile) throws IOException{
    //public static void countLetters(String outputgivenName, String[] sourcefiles) throws IOException{
		int[] freqs = new int[26];
		String[] pairs = new String[26];
        //String outputfilename = "";
        //outputfilename = "./" + outputgivenName;
		//File outputFile = new File(outputfilename);
		//if (!outputFile.exists()) {
		//	outputFile.createNewFile();
		//}
		//FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		//BufferedWriter bw = new BufferedWriter(fw);
		//for(String file:sourcefiles){
		
        BufferedReader in = new BufferedReader(new FileReader(sourcefile));
		//BufferedReader in = new BufferedReader(new FileReader(file));
			
			String line;
			while((line = in.readLine()) != null){
				line = line.toUpperCase();
				for(char ch:line.toCharArray()){
					if(Character.isLetter(ch)){
						freqs[ch - 'A']++;
						pairs[ch - 'A'] = "( " + Character.toString(ch) + " , " + Integer.toString(freqs[ch - 'A']) + " )";
					}
				}
			}
			in.close();
		//}
		
			
		for(String thePair:pairs){
			if(thePair != null)
			{
				BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
				 log.write(thePair + "\n");
				  log.flush();
			}
				//bw.write((thePair.toLowerCase()) + "\n");
		}
		//bw.close();
		
	}
 
	//private static void println(String string) {
		// TODO Auto-generated method stub
		
	//}

	public static void main(String[] args) throws IOException{
        if(args.length < 1)
            return;
        
        //inputfile = new String();
        //inputfile = "1random.txt";
        inputfile = args[0];
        //int numInput = 0;
		//output = args[0];
		//int pos = 1;
        //int count = 1;
        //while(args[count] != null){
        //    count++;
        //    numInput++;
        //}

        //if(numInput > 0){
        //    inputfiles = new String[numInput];
        //}

        //while(args[pos] != null){
        //    inputfiles[pos-1] = args[pos];
        //    pos++;
        
		//inputfiles = new String[3];
		//inputfiles[0] = "1random.txt";
		//inputfiles[1] = "2random.txt";
		//inputfiles[2] = "3random.txt";
	    //}
		countLetters(inputfile);
    }
}
