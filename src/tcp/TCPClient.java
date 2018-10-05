package tcp;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest; 

// Client class 
public class TCPClient{ 
	public static void main(String[] args) throws IOException{ 
		try
		{  
			// getting localhost ip 
			InetAddress ip = InetAddress.getByName("localhost"); 

			// establish the connection with server port 5056 
			Socket s = new Socket(ip, 5056); 
			// obtaining input and out streams 
			DataInputStream dis = new DataInputStream(s.getInputStream()); 
			DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 

			System.out.println("Notifying to server that client is ready...");
			String str="ready",filename="";  
			try{ 
				dos.writeUTF(str); 
				dos.flush();  

				filename=dis.readUTF(); 
				System.out.println("Receving file: "+filename);
				String[] pathSeparator = filename.split("/");
				filename = pathSeparator[pathSeparator.length-1];
				File tmpDir = new File("C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/client/"+filename);
				while(tmpDir.exists()){
					filename = s.getLocalPort() + filename;
					tmpDir = new File("C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/client/"+filename);
				}
				filename = "C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/client/"+filename;
				System.out.println("Saving as file: "+filename);
				long sz=Long.parseLong(dis.readUTF());
				System.out.println ("File Size: "+(sz/(1024*1024))+" MB");

				byte b[]=new byte [1024];
				System.out.println("Receiving file..");
				FileOutputStream fos=new FileOutputStream(new File(filename),true);
				String md5Server = "", md5Client = "";
				MessageDigest md = MessageDigest.getInstance("MD5");
				int countErrors = 0;
				long bytesRead = dis.read(b, 0, b.length);
				while(!(bytesRead<1024)){
					md5Server = dis.readUTF();
					md.update(b, 0, b.length);
					md5Client = new BigInteger(1, md.digest()).toString(16);
					if(!md5Server.equals(md5Client)){
						countErrors++;
					}
					fos.write(b,0,b.length);
					bytesRead = dis.read(b, 0, b.length);
				}
				if(countErrors > 1){
					System.out.println("FILE INTEGRITY COMPROMISED. FILE MAY BE CORRUPTED.");
				}
				else{
					System.out.println("SUCCESSFULL INTEGRITY VERIFICATION.");
				}
				System.out.println("Completed.");
				fos.close();
				dis.close();
				dos.close();
				s.close();
			}
			catch(EOFException e)
			{
				//do nothing
			}
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
	} 
} 