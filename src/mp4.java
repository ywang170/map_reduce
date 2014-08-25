package mp4;


import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class mp4 {

	
	 static DatagramSocket sendSock;//socket used to send message
	 static DatagramSocket recvSock;//recv message
	 static int fileSendSock;//sock number used to send files
	 static int myPort;//my port
	 static int masterPort;//port number of master
	 static InetAddress myIP;//my IP
	 static InetAddress masterIP;//the IP of master
	 static int myheartBeats = 1000;
	 static a_server master = null;
	 static a_server[] servers;
	 static int maxServerNum = 5;//max number of servers allowed.******************** This is changable
	 static long myPreTime = 0;
	 static boolean systemBusy = true;//you cant input order when system is busy
	 static Logger logger1;
	 static int myNum = 0;//my machine number
	 static int serversWeHave  = 1;
	 static String[] myContent = null;
	 static int maxItemNum = 32;//max number of items in the content*****************this is changable!
	 static boolean working = false;//is working on one task and can't accept more
	 static int taskNum = 0;//number of tasks
	 static String mapleOut;
	 static long preTime = new Date().getTime();
	 
	 public static class a_server{
			int port;//the port number
			InetAddress IPAd;//IP address
			long preTime;//last time it is renewed
			int heartBeats;//heartbeat number right now
			boolean alive;//does it still alive
			int num;
			int tasks;
			String[] content;
			
			public a_server()
			{
				port = 0;
				IPAd = null;
				preTime = 0;
				heartBeats = 1000;
				alive = false;
				num = -1;
				content = null;
				tasks  = 0;
			}
		}
	 
	 public static void main(String argv[]) throws IOException
		{
			//log test
			{
				PropertyConfigurator.configure("log4j.properties");
				   
		        logger1 = Logger.getLogger(mp4.class);

		        logger1.setLevel(Level.DEBUG);//
			}
			
			System.out.println("Welcome to my System!");

			servers = new a_server[maxServerNum];
			//declare place for servers
			for(int i = 0; i < maxServerNum; i++)
			{
				servers[i] = new a_server();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Please type in your port number:");//type in your port number
			String s = in.readLine();
			myPort = Integer.parseInt(s);
			System.out.println("Are you the Master? y/n");//check if you will be the holder of the whole system
			s = in.readLine();
			if(s.equals("y"))
				System.out.println("Your machine number is 0, network started!");
			else
			{
				System.out.println("Please type in your machine number: *0 is not accepted!");//holder's port
				s = in.readLine();
				if(s.equals("0"))
				{
					System.out.println("0 is not accepted!!!");
					return;
				}
				myNum  = Integer.parseInt(s);
				System.out.println("please type in master's IP address:");//holder's ip
				s = in.readLine();
				masterIP = InetAddress.getByName(s);
				System.out.println("Please type in master's port number:");//holder's port
				s = in.readLine();
				masterPort = Integer.parseInt(s);
				
			}
			myIP = InetAddress.getLocalHost();
			recvSock = new DatagramSocket(myPort);//declare receive sock
			sendSock = new DatagramSocket();//declare send sock
			if(myNum == 0)
			{
				masterIP = myIP;
				masterPort = myPort;
			}
			else
				master = new a_server();
			
			
			updateContent();
			
			Thread listener = new Thread(new listen());
			listener.start();
			
			Thread checker = new Thread(new check());
			checker.start();
			
			Thread heartBeatSender = new Thread(new sendHeart());
			heartBeatSender.start();
			
			if(myNum != 0)//add the master to list
			{
				servers[0].port = masterPort;
				servers[0].IPAd = masterIP;
				servers[0].num = 0;
				servers[0].heartBeats = 999;
				servers[0].preTime = new Date().getTime();
				servers[0].alive = true;
				serversWeHave++;
			}
			
			fileSendSock = myNum + 1994;//**this is changable
			Thread sender = new Thread(new SimpleFileServer(fileSendSock));
			sender.start();
			
			if(myNum == 0)
			{
				System.out.println("ready to go!!!_________________________________________________________");
				systemBusy = false;
			}
			
			
			in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("______________________________________________________________________");
			System.out.println("Please give command:");
			while(true)
			{
				
				String message = null;
				try {
					message = in.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(systemBusy)
				{
					System.out.println("system is currently busy...please try later!");
					continue;
				}
				if(working)
				{
					System.out.println("system is currently executing another command...please try later!");
					continue;
				}
				String[] messages = message.split(" ");
				if(messages[0].equals("maple"))
				{
					working = true;//set condition to working///////////////////////////////////////////////////////////////////
					//and then send the command directly to master
					logger1.info("new maple command recieved: "+ message);
					message =message + " end " + myNum + " end";
					mapleOut = messages[2];
					
					if(myNum == 0)
					{
						Thread mapler = new Thread(new maple(message));
						mapler.start();
						continue;
					}
					byte[] message_b  = message.getBytes();//the message we want to send
					DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, masterIP, masterPort);
						try {
							sendSock.send(sendData);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
				}
				
				if(messages[0].equals("juice"))
				{
					working = true;
					logger1.info("new juice command recieved: "+ message);
					working = true;
					message =message + " end " + myNum + " end";
					mapleOut = messages[2];
					
					if(myNum == 0)
					{
						Thread mapler = new Thread(new juice(message));
						mapler.start();
						continue;
					}
					byte[] message_b  = message.getBytes();//the message we want to send
					DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, masterIP, masterPort);
						try {
							sendSock.send(sendData);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
				}
				
				System.out.println("illegal command!");
				System.out.println("______________________________________________________________________");
				System.out.println("Please give command:");
				continue;
				
			}
			//Then main Thread is used for working!
		}
	 //***********************************************************************************************************************
	 public static void updateContent()//get the content of the machine itself, and write it to myContent
	 {
		 Runtime r = Runtime.getRuntime();
		 Process p = null;
		 try {
			p = r.exec("ls");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			myContent = new String[maxItemNum];
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
			String a;
			int i = 0;
			try {
				while((a = stdInput.readLine()) != null )
				{
					myContent[i] = a;
					i++;
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	 //***********************************************************************************************************************
	 public static void updateToMaster()//update the content to master, a new server won't show "ready to go" until this step is done
	 {
		 String message = "content " + myNum + " ";
		 for(int i = 0; i < maxItemNum; i++)
		 {
			 if(myContent[i] == null)
				 break;
			 message += myContent[i];
			 message += " ";
		 }
		 message += "end";
		 byte[] message_b  = message.getBytes();//the message we want to send
		 DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, masterIP, masterPort);
			try {
				sendSock.send(sendData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("ready to go!!!_________________________________________________________");
		systemBusy = false;
	 }
	 //***********************************************************************************************************************
	 public static void sendInfo(int port, InetAddress IP)//send information of other members to the new one
	 {
		
			String message  = null;
			byte[] message_b;
			DatagramPacket sendData;
			for(int i = 0; i < maxServerNum; i++)
			{
				if(servers[i].alive)
				{
					message = "joining " + servers[i].num + " " + servers[i].port + " " + servers[i].IPAd + " end";
					message_b  = message.getBytes();//the message we want to send
					sendData = new DatagramPacket(message_b, 0, message_b.length, IP, port);
					try {
						sendSock.send(sendData);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			message = "report end";//tell the machine to report content
			message_b  = message.getBytes();//the message we want to send
			sendData = new DatagramPacket(message_b, 0, message_b.length, IP, port);
			try {
				sendSock.send(sendData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	 }
	 //***********************************************************************************************************************
	 public static class listen implements Runnable//listen to others' message
	 {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
				//receive message, if the message is from sb we know, then update its information, or we add it to our list
				while(true)
				{
					DatagramPacket recvData = new DatagramPacket(new byte[512], 512);
						try {
							recvSock.receive(recvData);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			
					byte[] 
					byte_t = recvData.getData();//get the byte read
					String words = new String(byte_t);
					String[] wordParts = words.split(" ");
					//if blablabla wordsParts[0].equals blablabla!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					if(wordParts[0].equals("joining"))
					{
						for(int i = 0; i < maxServerNum; i++)
						{
							if(!servers[i].alive)
							{
								
								servers[i].num = Integer.parseInt(wordParts[1]);
								if(servers[i].num == myNum)
									break;
								serversWeHave++;
								servers[i].port = Integer.parseInt(wordParts[2]);
								try {
									servers[i].IPAd = InetAddress.getByName(wordParts[3].substring(1, wordParts[3].length()));
								} catch (UnknownHostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								servers[i].heartBeats = 999;
								servers[i].preTime = new Date().getTime();
								servers[i].alive = true;
								break;
							}
						}
						continue;
					}
					if(wordParts[0].equals("report"))
					{
						updateToMaster();
						continue;
					}
					if(wordParts[0].equals("maple"))
					{
						if(working || systemBusy)//tell the machine system is busy
						{
							int port = recvData.getPort();
							InetAddress IP =recvData.getAddress();
							String message = "busy";
							byte[] message_b  = message.getBytes();//the message we want to send
							 DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, IP, port);
								try {
									sendSock.send(sendData);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
						else
						{
							logger1.info("new maple recieved: " + words);
							mapleOut = wordParts[2];
							Thread mapler = new Thread(new maple(words));
							mapler.start();
						}
						continue;
							
					}
					if(wordParts[0].equals("juice"))
					{
						if(working || systemBusy)//tell the machine system is busy
						{
							int port = recvData.getPort();
							InetAddress IP =recvData.getAddress();
							String message = "busy";
							byte[] message_b  = message.getBytes();//the message we want to send
							 DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, IP, port);
								try {
									sendSock.send(sendData);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
						else
						{
							logger1.info("new jucie recieved: " + words);
							mapleOut = wordParts[2];
							Thread mapler = new Thread(new juice(words));
							mapler.start();
						}
						continue;
							
					}
					if(wordParts[0].equals("mapleTask"))
					{
						Thread mapleTasker = new Thread(new mapleTask(words));
						mapleTasker.start();
						
						continue;
					}
					if(wordParts[0].equals("mapleDone"))
					{
						//System.out.println("maple task done for: " + wordParts[2]);
						Thread recver = new Thread(new recvResult(recvData.getAddress() +" "  + words));
						recver.start();
						continue;
					}
					if(wordParts[0].equals("juiceDone"))//download finished file from master
					{
						Thread downloader = new Thread(new download(words));
						downloader.start();
						continue;
					}
					if(wordParts[0].equals("busy"))
					{
						System.out.println("system is currently busy...please try later!");
						continue;
					}
					if(wordParts[0].equals("free"))
					{
						working = false;
						taskNum = 0;
						continue;
					}
					if(wordParts[0].equals("content"))
					{
						//System.out.println("received content of machine " + wordParts[1] + ": ");
						for(int i = 0; i < maxServerNum; i++)
						{
							if(servers[i].num == Integer.parseInt(wordParts[1]))
							{
								servers[i].content = new String[wordParts.length - 3];
								for(int j = 2; j <  wordParts.length - 1; j++)
								{
									servers[i].content[j - 2] = wordParts[j];
									System.out.print(wordParts[j] + " ");
								}
								break;
							}
						}
						System.out.println("______________________________________________________________________");
						System.out.println("Please give command:");
						continue;
					}
					//else it is a heartbeats
					{
						
						int port = Integer.parseInt(wordParts[2]);
						InetAddress IP = recvData.getAddress();
						boolean found = false;
						for(int i = 0; i < maxServerNum; i++)
						{
							if(servers[i].alive && servers[i].port == port && servers[i].IPAd.equals(IP))
							{
								int newBeats = Integer.parseInt(wordParts[0]);
								if(servers[i].heartBeats < newBeats)
								{
									servers[i].heartBeats = newBeats;
									servers[i].preTime = new Date().getTime();
								}
								found = true;
								break;
							}
						}
						if(!found)
						{
							int i = 0;
							for(i = 0; i < maxServerNum; i++)
							{
								if(servers[i].alive == false)
									break;
							}
							servers[i].port = port;
							servers[i].IPAd = IP;
							servers[i].num = Integer.parseInt(wordParts[1]);
							servers[i].heartBeats = Integer.parseInt(wordParts[0]);
							servers[i].preTime = new Date().getTime();
							servers[i].alive = true;
							serversWeHave++;
							logger1.info("Adding a new server: " + servers[i].num + " , servers we have: " + serversWeHave);
							if(myNum == 0)
								sendInfo(port, IP);
							
							
						}
						continue;
					}
				}
		}
		 
	 }
	 //***********************************************************************************************************************
	 public static class check implements Runnable//check the condition of heartbeats of each member
	 {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true)
			{
				for(int i = 0; i < maxServerNum; i++)
				{
					if(servers[i].alive)
					{
						if((new Date().getTime() - servers[i].preTime) > 5000 )
						{
							serversWeHave--;
							logger1.info("machine " + servers[i].num + " down! servers we have: " + serversWeHave);
							servers[i].alive = false;
							if(myNum == 0)
							{
								for(int j = 0; j < servers[i].content.length; j++)
								{
									subTask();
								}
							}
							System.out.println("______________________________________________________________________");
							System.out.println("Please give command:");
						}
					}
				}
			}
		}
		 
	 }
	 //***********************************************************************************************************************
	 public static class sendHeart implements Runnable//send heatbeats
	 {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true)
			{
				if((new Date().getTime() - myPreTime) > 1000 )
				{
					myPreTime = new Date().getTime();
					myheartBeats++;
					String message = Integer.toString(myheartBeats) + " " + myNum + " " + myPort + " end";
					byte[] message_b  = message.getBytes();//the message we want to send
					for(int i = 0; i < maxServerNum; i++)
					{
						if(servers[i].alive)
						{
							DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, servers[i].IPAd, servers[i].port);
							try {
								sendSock.send(sendData);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		 
	 }
		//***********************************************************************************************************************
	 public static class juice implements Runnable//used by master to deal with new juice command
	 {
		 
		 //jucie + exe + taksnum + src + output + end + num  +end
		 String[] commands;
		 int tasks;
		 int num;
		 String exe;
		 String src;
		 String result;
		 public juice(String command)
		 {
			 taskNum = 0;
			 working = true;
			 commands = command.split(" ");
			 exe = commands[1];
			 tasks = Integer.parseInt(commands[2]);
			 src = commands[3];
			 result = commands[4];
			 num =  tasks = Integer.parseInt(commands[6]);
		 }
		@Override
		public void run() {
			// TODO Auto-generated method stub
			preTime = new Date().getTime();
			String IP = null;
			int port = 0;
			if(num != myNum)//download the exe file
			{
				for(int i = 0; i < maxServerNum; i++)
				{
					if(servers[i].alive && servers[i].num == num)
					{
						port = num + 1994;
						IP = servers[i].IPAd.toString();
						break;
					}
				}
				SimpleFileClient recver = new SimpleFileClient(port, IP , exe, -1);
				try {
					recver.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			//execute the file
			File f = new File(result);
			 
		     try {
				f.createNewFile();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//execute !
		    
			Runtime r = Runtime.getRuntime();
			Process p = null;
			try {
				String command = "ls";
				
				p = r.exec(command);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
	          
			String a;
			
		
			try {
				while((a = stdInput.readLine()) != null )
				{		
						if(a.length() < src.length())
							continue;
						if(!a.substring(0, src.length()).equals(src))
							continue;
						Thread juiceTasker = new Thread(new juiceTask(exe, a, result, num));
						juiceTasker.start();
						taskNum++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			          
		}
		 
	 }
	//***********************************************************************************************************************
	 public static class juiceTask implements Runnable//
	 {
		 String exe;
		 String src;
		 String result;
		 int num; 
		 
		 public juiceTask(String e, String s, String r, int machineNum)
		 {
			 exe = e;
			 src = s;
			 result = r;
			 num = machineNum;
			 //System.out.println("reading: " + src);
		 }
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String output = "";
			String sCurrentLine;
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(src));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
			try {
				while ((sCurrentLine = br.readLine()) != null) {
					String[] lines = sCurrentLine.split(" ");
					output = output + lines[1] + lines[2]+ lines[3] +" ";// make string to become key,value key,value
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Runtime r = Runtime.getRuntime();
			Process p = null;
			try {
				String command = "java -jar " + exe + " " + output;
				
				p = r.exec(command);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
	          
			String a = null;
			
		
			try {
				a = stdInput.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			writeToFile(a, result);
			boolean allDone = subTask();
			if(num ==0)
				return;
			if(allDone)//tell the server to download finish file
			{
				String message = "juiceDone " + result + " end";
				byte[] message_b  = message.getBytes();//the message we want to send
				for(int i = 0; i < maxServerNum; i++)
				{
					if(servers[i].alive && servers[i].num == num)
					{
						DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, servers[i].IPAd, servers[i].port);
						try {
							sendSock.send(sendData);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		 
	 }
	 
	//***********************************************************************************************************************
	 public static synchronized void writeToFile(String line, String fileName)
	 {
		 File file = new File(fileName);
		 PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		    out.println(line);
		    out.close();
	 }
	 
	//***********************************************************************************************************************
	 public static class maple implements Runnable//used by master to deal with new maple command
	 {
		 String commands[];
		 int num;
		 String exe;
		 String result;
		 //command =  maple + exe + result + src + end +  machineNum + end
		 public maple(String command)
		 {
			 working = true;/////////////////////////////////////////////////////////////////////////////////
			 taskNum = 0;
			 commands = command.split(" ");
			 exe = commands[1];
			 result = commands[2];
			 int i = 0;
			 while(!commands[i].equals("end"))
				 i++;
			 i++;
			 num = Integer.parseInt(commands[i]);
			 
		 }
		 
		@Override
		public void run() {
			// TODO Auto-generated method stub
			preTime = new Date().getTime();
			int i = 3;
			while(!commands[i].equals("end"))
			{
				String file = commands[i];
				boolean found = false;
				for(int j = 0; j < maxServerNum ; j++)
				{
					
					if(servers[j].alive)
					{
						for(int k = 0; k < servers[j].content.length; k++)
						{
							if(servers[j].content[k] != null && servers[j].content[k].equals(file))
							{
								//if we found the right server, the let him do the task!
								found = true;
								servers[j].tasks ++;
								addTask();
								String message = "mapleTask " + num + " " + exe + " " + file + " " + result + " end";
								byte[] message_b  = message.getBytes();//the message we want to send
								 DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, servers[j].IPAd, servers[j].port);
									try {
										sendSock.send(sendData);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
							}
						}
						if(found)
							break;
					}
				}
				if(found)
				{
					i++;
					continue;
				}
					
				for(int j = 0; j < maxItemNum; j++)
				{
					if(myContent[j] == null)
						break;
					if(myContent[j].equals(file))
					{
						addTask();
						Thread mapleTasker = new Thread(new mapleTask("mapleTask " + num + " " + exe + " " + file + " " + result));
						mapleTasker.start();
						break;
					}
				}
				i++;
			}
			System.out.println("all maple tasks sent...in all we have: " + taskNum + " tasks!" );
		}
		 
	 }
	 //***********************************************************************************************************************
	 public static class mapleTask implements Runnable//used by machines to deal with maple
	 {

		 int num;
		 String exe;
		 String file;
		 String result = null;
		 public mapleTask(String info)
		 {
			 //mapleTask + num + exe + file + result
			 logger1.info("new maple execution command received: " + info);
			 working = true;
			 String[] infos = info.split(" ");
			 num = Integer.parseInt(infos[1]);
			 exe = infos[2];
			 file = infos[3];
			 result = infos[4];
			 if(myNum != 0)
				 addTask();
			 result = result + myNum + "_" + taskNum;//set a unique file name!
		 }
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if(num != myNum)//download file if needed!
			{
				InetAddress IP = null;
				for(int i = 0; i < maxServerNum; i++)
				{
					if(servers[i].num == num)
					{
						IP = servers[i].IPAd;
						break;
					}
				}
				int hisSock = num + 1994;//get the send TCP number
				SimpleFileClient recver = new SimpleFileClient(hisSock, IP.toString() , exe, -1);
				try {
					recver.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//create the file
			 File f = new File(result);
			 
		     try {
				f.createNewFile();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//execute !
		     BufferedWriter output = null;
				try {
					output = new BufferedWriter(new FileWriter(result));
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			Runtime r = Runtime.getRuntime();
			Process p = null;
			try {
				String command = "java -jar " + exe + " " + file;
				
				p = r.exec(command);
				//p = r.exec("ls");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
	          
			String a;
			
			//write to file!
			try {
				while((a = stdInput.readLine()) != null )
				{
					
					output.write(a + "\n");
			          
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				output.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(myNum != 0)//send to master if needed
			{
				
				String message = "mapleDone " + fileSendSock + " "+  result + " end";
				byte[] message_b  = message.getBytes();//the message we want to send
				 DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, masterIP, masterPort);
					try {
						sendSock.send(sendData);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			logger1.info("maple task done for: " + exe +" " + result + " " + file);
			subTask();
		}
		 
	 }
	 
	 //***********************************************************************************************************************
	 public static class recvResult implements Runnable//get maple result files from other servers
	 {
		 int port;
		 String IP;
		 String file;
		 public recvResult(String command)
		 {
			 
			 
			 //IP + maple + port + file
			 String[] commands = command.split(" ");
			 port = Integer.parseInt(commands[2]);
			 IP = commands[0];
			 file = commands[3];
			 System.out.println("maple task done for: " + file);
		 }
		@Override
		public void run() {
			//if(true)
			//	 return;
			// TODO Auto-generated method stub
			SimpleFileClient recver = new SimpleFileClient(port, IP , file, -1);
			try {
				recver.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			subTask();
		}
		 
	 }
	 
	 public static synchronized void addTask()
	 {
		 taskNum ++;
	 }
	 
	 public static synchronized boolean subTask()
	 {
		 taskNum --;
		 if(taskNum <= 0)
		 {
			 	System.out.println("all tasks done!");
			 	
			 	if(myNum == 0)
			 		{
			 		sortFiles();

			 		long timeUsed = new Date().getTime() - preTime;
			 		System.out.println("Time used in all: " + timeUsed);
			 		//unlock every one
			 		for(int i = 0; i < maxServerNum; i++)
			 		{
			 			if(servers[i].alive)
			 			{
			 				String message = "free end";
			 				byte[] message_b  = message.getBytes();//the message we want to send
							 DatagramPacket sendData = new DatagramPacket(message_b, 0, message_b.length, servers[i].IPAd, servers[i].port);
								try {
									sendSock.send(sendData);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			 			}
			 		}
			 	working = false;
			 		}
			 	System.out.println("______________________________________________________________________");
				System.out.println("Please give command:");
			 	return true;
		 }
		 return false;
			 
		 
	 }
	 
	 public static void sortFiles()//after getting all files, sort them!
	 {
		 Runtime r = Runtime.getRuntime();
			Process p = null;
			try {
				p = r.exec("ls");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String a;
			//get all files' name we want
			try {
				while((a = stdInput.readLine()) != null )
					{
					//System.out.println(a + mapleOutput);
						if(a.length() < mapleOut.length())
							continue;
						if(!a.substring(0, mapleOut.length()).equals(mapleOut))
							continue;
						//or this is the file we want to read
						{
							BufferedReader br = null;
							String sCurrentLine;
							 
							try {
								br = new BufferedReader(new FileReader(a));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				 
							while ((sCurrentLine = br.readLine()) != null)
							{
								String[] lines = sCurrentLine.split(" ");
								String key = lines[1];
								String file_name = mapleOut + "_" + key;
								//create the file if it doesnt exist
								File file = new File(file_name);
								 if(!file.isFile())
								 {
							     try {
									file.createNewFile();
								} catch (IOException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
								 }
								 PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
								    out.println(sCurrentLine);
								    out.close();
							    
							    
							}
						}
						//then delete the original file...
						r = Runtime.getRuntime();
						
						try {
								r.exec("rm " + a);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	
	 public static class download implements Runnable
	 {
		 String file;

		 public download(String command)
		 {
			 String[] commands = command.split(" ");
			 file = commands[1];
		 }
		@Override
		public void run() {
			// TODO Auto-generated method stub
			SimpleFileClient recver = new SimpleFileClient(1994, masterIP.toString() , file, -1);
			try {
				recver.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("______________________________________________________________________");
			System.out.println("Please give command:");
		}
		 
	 }
}
