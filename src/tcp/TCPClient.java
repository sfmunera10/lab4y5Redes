package tcp;

import java.io.*;
import java.net.*;
import java.security.MessageDigest; 

// Client class 
public class TCPClient{
	
	private static int buffSize = 65536;
	
	public static byte[] createChecksum(String filename) throws Exception {
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
			System.out.println("Connected to server with status 'isConnected' = " + s.isConnected());
			System.out.println("Notifying to server that client is ready...");
			String str="ready",filename="";  
			try{ 
				dos.writeUTF(str); 
				dos.flush();  

				filename=dis.readUTF(); 
				System.out.println("Receving file: "+filename);
				String[] pathSeparator = filename.split("/");
				if(filename.contains("testl")){
					buffSize = buffSize*4;
				}
				filename = pathSeparator[pathSeparator.length-1];
				filename = "C:/Users/Lenovo/Desktop/SEMESTRE 201820/Redes/LAB 4 y 5/TCP/client/"+ s.getLocalPort() + filename;
				System.out.println("Saving as file: "+filename);

				BufferedInputStream bis = 
						new BufferedInputStream(s.getInputStream());

				BufferedOutputStream bos = 
						new BufferedOutputStream(new FileOutputStream(filename));

				long sz=Long.parseLong(dis.readUTF());
				System.out.println ("File Size: "+(sz/(1024*1024))+" MB");
				
				System.out.println("Receiving checksum from server...");
				String servCS = dis.readUTF();

				byte buffer[]=new byte [buffSize];
				System.out.println("Receiving file...");
				
				int len = 0;
				
		        while ((len = bis.read(buffer)) > 0) {
		            bos.write(buffer, 0, len);
		        }
		        bos.flush();
		        System.out.println("Checking file integrity with server...");
		        
		        byte[] digest = createChecksum(filename);
		        String cliCS = "";
				for(byte bytee: digest){
					cliCS += bytee;
				}
				
				if(!cliCS.equals(servCS)){
					System.out.println("FILE INTEGRITY COMPROMISED. FILE MAY BE CORRUPTED.");
				}
				else{
					System.out.println("SUCCESSFULL INTEGRITY VERIFICATION.");
				}
				System.out.println("Completed.");
				dos.flush();
				bis.close();
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