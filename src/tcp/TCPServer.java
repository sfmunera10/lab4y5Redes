package tcp;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier; 

// Server class 
public class TCPServer{ 
	public static void main(String[] args) throws IOException{ 
		// server is listening on port 5056 
		ServerSocket ss = new ServerSocket(5056); 
		String fileToSend = "";
		int numClientsDesired = 0;
		System.out.println("WELCOME TO A SIMPLE FILE TRANSFER THROUGH TCP");
		String filename;
		filename= "C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/server/testl.wmv";
		String filename2;
		filename2= "C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/server/testm.wmv";
		System.out.println("Files: ");
		System.out.println(filename + " Capoeira (Large Footage)");
		System.out.println(filename2 + " Capoeira (Medium Footage)");
		System.out.println("------------------------------------------------------------------");

		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Which file would you like to send?");
		System.out.println("Type in 1 to send Capoeira (Large Footage)");
		System.out.println("Type in 2 to send Capoeira (Medium Footage)");
		String whichFile = "";

		while (!whichFile.equals("1") || !whichFile.equals("2")) {
			whichFile = br.readLine();
			if(whichFile.equals("1")){
				fileToSend = filename;
				break;
			} 
			else if(whichFile.equals("2")){
				fileToSend = filename2;
				break;
			}
			else{
				System.out.println("Not a valid entry :(");
			}
		}
		
		BufferedReader br2=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("How many clients would you like to connect to this server?");
		System.out.println("Type in the number of clients you would like: ");
		String whichFile2 = "";

		while (!whichFile2.equals("1") || !whichFile2.equals("2") || !whichFile2.equals("3") ||
				!whichFile2.equals("4") || !whichFile2.equals("5") || !whichFile2.equals("10") ||
				!whichFile2.equals("25")) {
			whichFile2 = br2.readLine();
			if(whichFile2.equals("1") || whichFile2.equals("2") || whichFile2.equals("3") ||
					whichFile2.equals("4") || whichFile2.equals("5") 
					|| whichFile2.equals("10") || whichFile2.equals("25")){
				numClientsDesired = Integer.parseInt(whichFile2);
				break;
			} 
			else{
				System.out.println("Not a valid entry :(");
			}
		}

		System.out.println("The file that will be sent is: " + fileToSend);

		final CyclicBarrier gate = new CyclicBarrier(numClientsDesired+1);
		// running infinite loop for getting 
		// client request 
		while (true)  
		{
			System.out.println(numClientsDesired + " CLIENTS WILL BE INCLUDED IN THIS FILE TRANSFER.");
			ArrayList<Socket> clients = new ArrayList<>();
			ArrayList<Thread> threads = new ArrayList<>();
			int numConnected = 0;
			while(numConnected < numClientsDesired){
				Socket s = null;
				s = ss.accept();
				if(s != null){
					clients.add(s);
					System.out.println("A new client is connected : " + s);
					numConnected = clients.size();
				}
			}
			try 
			{
				System.out.println("Assigning new thread for each new client...");
				for(Socket so: clients){
					// obtaining input and out streams 
					DataInputStream dis = new DataInputStream(so.getInputStream()); 
					DataOutputStream dos = new DataOutputStream(so.getOutputStream());
					// create a new thread object
					Thread t = new ClientHandler(so, dis, dos, fileToSend, gate);
					threads.add(t);
				}  
				System.out.println("Number of threads ready: " + threads.size());

				for(Thread th: threads){
					// Invoking the start() method 
					th.start();
				}
				
				BufferedReader br3=new BufferedReader(new InputStreamReader(System.in));
				System.out.println();
				System.out.println("Type Yes to start all threads: ");
				String yas = "";

				while(!(yas.equals("Yes"))){
					yas = br3.readLine();
					if(yas.equals("Yes")){
						gate.await();
						break;
					}
					else{
						System.out.println("Not a valid entry :(");
					}
				}
				System.out.println("All threads started");
				br.close();
				
				for (Thread thread : threads) {
				    thread.join();
				    System.out.println("Waiting for thread to finish...");
				}
				for(Socket so: clients){
					so.close();
					System.out.println("Done.");
				}
			} 
			catch (Exception e){ 
				ss.close();
				e.printStackTrace(); 
			}
		}
	} 
} 

// ClientHandler class 
class ClientHandler extends Thread{  
	final DataInputStream dis; 
	final DataOutputStream dos; 
	final Socket s;
	final String fileToSend;
	final CyclicBarrier gate;

	// Constructor 
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String fileToSend,
			CyclicBarrier gate)  
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;
		this.fileToSend = fileToSend;
		this.gate = gate;
	}
	
	@Override
	public void run()  
	{
		try{
			gate.await();
			System.out.println("Checking if client is ready...");
			String str="";  

			str=dis.readUTF();

			if(!str.equals("stop")){  
				System.out.println("Sending File: "+fileToSend);
				dos.writeUTF(fileToSend);  
				dos.flush();  

				BufferedInputStream bis = 
			               new BufferedInputStream(
			                    new FileInputStream(fileToSend));
				BufferedOutputStream bos = 
			               new BufferedOutputStream(s.getOutputStream());
				
				File f=new File(fileToSend);
				System.out.println("Attempting to read from file in: "+f.getCanonicalPath());
				
				long sz=(int) f.length();

				byte buffer[]=new byte [51200];

				dos.writeUTF(Long.toString(sz)); 
				dos.flush(); 

				System.out.println ("Size: "+sz);
				System.out.println ("Buf size: "+ s.getReceiveBufferSize());

				MessageDigest md = MessageDigest.getInstance("MD5");
				String md5 = "";
				int len = 0;
		        while ((len = bis.read(buffer)) > 0){
		        	md.update(buffer, 0, len);
					md5 += new BigInteger(1, md.digest()).toString(16) + ",";
		        	bos.write(buffer, 0, len);
		        }
		        System.out.println("Checking file integrity with client...");
		        dos.writeUTF(md5);
				bis.close();
				bos.flush();
				bos.close();
				dos.close();
				System.out.println("..ok");
				System.out.println("Send Complete");
			}   
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("An error occured");
		}
		try
		{ 
			// closing resources 
			this.dis.close(); 
			this.dos.close(); 

		}catch(IOException e){ 
			e.printStackTrace(); 
		} 
	} 
} 