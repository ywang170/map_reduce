package mp4;

import java.io.IOException;

public class juice_exe {

	
	 public static void main(String argv[]) throws IOException
	 {
		 String key = null;
		 int value = 0;
		 
		 for(int i = 0; i < argv.length; i++)
		 {
			 if(argv[i] == null)
				 continue;
			 String[] parts = argv[i].split(",");
			 key = parts[0];//.substring(1, parts[0].length());
			 value += Integer.parseInt(parts[1]/*.substring(0, parts[1].length() - 1)*/);
		 }
		 
		 key = "(" + key + ", " + value + ")";
		 System.out.println(key);
	 }
}
