package tcp;

import java.io.*;
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
		int buffSize = 65536;
		System.out.println("WELCOME TO A SIMPLE FILE TRANSFER THROUGH TCP");
		String filename;
		filename= "testl.wmv";
		String filename2;
		filename2= "testm.wmv";
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
				buffSize = buffSize*4;
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
					Thread t = new ClientHandler(so, dis, dos, fileToSend, gate, buffSize);
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
				br2.close();
				br3.close();

				for (Thread thread : threads) {
					thread.join();
					System.out.println("Waiting for thread to finish...");
				}
				for(Socket so: clients){
					so.close();
					System.out.println("Done.");
				}
				ss.close();
				break;
			} 
			catch (Exception e){ 
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
	final int buffSize;
	
	// Constructor 
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String fileToSend,
			CyclicBarrier gate, int buffSize)  
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;
		this.fileToSend = fileToSend;
		this.gate = gate;
		this.buffSize = buffSize;
	}

	public byte[] createChecksum(String filename) throws Exception {
		InputStream fis =  new FileInputStream(filename);

		byte[] buffer = new byte[buffSize];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
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
				//262144 (65536*4) for large file
				//65536 for medium file
				byte buffer[]=new byte [buffSize];

				dos.writeUTF(Long.toString(sz)); 
				dos.flush(); 
				
				System.out.println("");
				System.out.println ("Size: "+sz);
				System.out.println ("Buf size: "+ s.getReceiveBufferSize());
				
				byte[] digest = createChecksum(fileToSend);
				
				String digStr = "";
				
				for(byte by: digest){
					digStr += by;
				}
				System.out.println("Sending checksum of the file to client...");
				dos.writeUTF(digStr);
				dos.flush();

				int len = 0;
				long bef = System.currentTimeMillis();
				while ((len = bis.read(buffer)) > 0){
					bos.write(buffer, 0, len);
				}
				dos.flush();
				bos.flush();
				long aft = System.currentTimeMillis();
				System.out.println("Time elapsed: " + ((aft-bef)/1000));
				System.out.println("..ok");
				System.out.println("Send Complete");
				bis.close();
				bos.close();
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