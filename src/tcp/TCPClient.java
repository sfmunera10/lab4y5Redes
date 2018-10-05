package tcp;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest; 

// Client class 
public class TCPClient{ 
	public static void main(String[] args) throws IOException{ 
		// getting localhost ip 
		InetAddress ip = InetAddress.getByName("localhost"); 
		// establish the connection with server port 5056 
		Socket s = new Socket(ip, 5056);
		try
		{   
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
				filename = "C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/client/"+ s.getLocalPort() + filename;
				System.out.println("Saving as file: "+filename);

				BufferedInputStream bis = 
						new BufferedInputStream(s.getInputStream());

				BufferedOutputStream bos = 
						new BufferedOutputStream(new FileOutputStream(filename));

				long sz=Long.parseLong(dis.readUTF());
				System.out.println ("File Size: "+(sz/(1024*1024))+" MB");

				byte buffer[]=new byte [51200];
				System.out.println("Receiving file..");
				
				
				String md5Server = "", md5Client = "";
				MessageDigest md = MessageDigest.getInstance("MD5");
				int countErrors = 0;
				int len = 0;
		        while ((len = bis.read(buffer)) > 0) {
					md.update(buffer, 0, len);
					md5Client += new BigInteger(1, md.digest()).toString(16) + ",";
		            bos.write(buffer, 0, len);
		        }
		        md5Server = dis.readUTF();
		        System.out.println("Checking file integrity with server...");
		        String[] servMD5 = md5Server.split(",");
		        String[] cliMD5 = md5Client.split(",");
		        
		        if(servMD5.length != cliMD5.length){
		        	countErrors = 100;
		        }
		        
		        for(int i = 0; i<servMD5.length; i++){
		        	if(!servMD5[i].equals(cliMD5[i])){
		        		countErrors++;
		        	}
		        }
		        
				if(countErrors > 1){
					System.out.println("FILE INTEGRITY COMPROMISED. FILE MAY BE CORRUPTED.");
				}
				else{
					System.out.println("SUCCESSFULL INTEGRITY VERIFICATION.");
				}
				System.out.println("Completed.");
				bis.close();
				bos.flush();
				bos.close();
				dis.close();
				dos.close();
			}
			catch(EOFException e)
			{
				//do nothing
			}
		}catch(Exception e){
			s.close();
			e.printStackTrace(); 
		} 
	} 
} 