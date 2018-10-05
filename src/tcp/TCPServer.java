package tcp;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest; 

// Server class 
public class TCPServer{ 
	public static void main(String[] args) throws IOException{ 
		// server is listening on port 5056 
		ServerSocket ss = new ServerSocket(5056); 
		String fileToSend = "";
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

		System.out.println("The file that will be sent is: " + fileToSend);
		// running infinite loop for getting 
		// client request 
		while (true)  
		{ 
			Socket s = null; 

			try 
			{ 
				// socket object to receive incoming client requests 
				s = ss.accept(); 

				System.out.println("A new client is connected : " + s); 

				// obtaining input and out streams 
				DataInputStream dis = new DataInputStream(s.getInputStream()); 
				DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 

				System.out.println("Assigning new thread for this client"); 

				// create a new thread object 
				Thread t = new ClientHandler(s, dis, dos, fileToSend); 

				// Invoking the start() method 
				t.start();
			} 
			catch (Exception e){ 
				ss.close();
				s.close(); 
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


	// Constructor 
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String fileToSend)  
	{ 
		this.s = s; 
		this.dis = dis; 
		this.dos = dos;
		this.fileToSend = fileToSend;
	} 

	@Override
	public void run()  
	{
		try{
			System.out.println("Checking if client is ready...");
			String str="";  

			str=dis.readUTF();
			
			if(!str.equals("stop")){  
				System.out.println("Sending File: "+fileToSend);
				dos.writeUTF(fileToSend);  
				dos.flush();  

				File f=new File(fileToSend);
				System.out.println("Attempting to read from file in: "+f.getCanonicalPath());
				FileInputStream fin=new FileInputStream(f);
				long sz=(int) f.length();

				byte b[]=new byte [1024];

				int read;

				dos.writeUTF(Long.toString(sz)); 
				dos.flush(); 

				System.out.println ("Size: "+sz);
				System.out.println ("Buf size: "+ s.getReceiveBufferSize());

				MessageDigest md = MessageDigest.getInstance("MD5");

				while((read = fin.read(b)) != -1){
					dos.write(b, 0, read);
					dos.flush();
					md.update(b, 0, read);
					String md5 = new BigInteger(1, md.digest()).toString(16);
					dos.writeUTF(md5);
					dos.flush();
				}
				System.out.println("..ok");
				System.out.println("Closing this connection."); 
				this.s.close(); 
				System.out.println("Connection closed");
				dos.flush();
				fin.close();
			}  
			System.out.println("Send Complete");

			dos.flush();  
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