package mp4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class maple_test {

	
	public static void main(String[] args) throws IOException{
		
		for(int i= 0; i < 5; i++)
		{
		File a = new File("hahaha");
		a.createNewFile();
		 PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(a, true)));
		    out.println("the text");
		    out.close();
		}
	}
}
