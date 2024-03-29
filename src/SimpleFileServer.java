package mp4;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleFileServer implements Runnable{

  public static int SOCKET_PORT = 0;  
  public static String FILE_TO_SEND = null; 

  public SimpleFileServer(int port)
  {
	  SOCKET_PORT = port;
  }
 

@Override
public void run() {
	// TODO Auto-generated method stub
	 FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    OutputStream os = null;
	    ServerSocket servsock = null;
	    Socket sock = null;
	    try {
	      servsock = new ServerSocket(SOCKET_PORT);
	      while (true) {
	        //System.out.println("Waiting...");
	        try {
	        	//System.out.println("waiting...");
	          sock = servsock.accept();
	          //System.out.println("Accepted connection : " + sock);
	          
	          //get information about the recver!
	          InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
	          BufferedReader reader = new BufferedReader(streamReader);
	          
	          String file = reader.readLine();
	          FILE_TO_SEND = file;
	          
	          
	          // send file
	          File myFile = new File (FILE_TO_SEND);
	          byte [] mybytearray  = new byte [(int)myFile.length()];
	          fis = new FileInputStream(myFile);
	          bis = new BufferedInputStream(fis);
	          bis.read(mybytearray,0,mybytearray.length);
	          os = sock.getOutputStream();
	          //System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
	          os.write(mybytearray,0,mybytearray.length);
	          os.flush();
	          //System.out.println("Done.");
	        }
	        finally {
	          if (bis != null) bis.close();
	          if (os != null) os.close();
	          if (sock!=null) sock.close();
	        }
	      }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally {
	      if (servsock != null)
			try {
				servsock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
}
}