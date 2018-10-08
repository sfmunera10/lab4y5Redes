package tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar; 

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
		InetAddress ip = InetAddress.getByName("157.253.205.115"); 
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
				if(filename.contains("testl")){
					buffSize = buffSize*4;
				}
				filename = s.getLocalPort() + filename;
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
				long bef = System.currentTimeMillis();
		        while ((len = bis.read(buffer)) > 0) {
		            bos.write(buffer, 0, len);
		        }
		        bos.flush();
		        System.out.println("Checking file integrity with server...");
		        long aft = System.currentTimeMillis();
		        byte[] digest = createChecksum(filename);
		        String cliCS = "";
				for(byte bytee: digest){
					cliCS += bytee;
				}
				boolean isComplete = true;
				if(!cliCS.equals(servCS)){
					System.out.println("FILE INTEGRITY COMPROMISED. FILE MAY BE CORRUPTED.");
					isComplete = false;
				}
				else{
					System.out.println("SUCCESSFULL INTEGRITY VERIFICATION.");
				}
				System.out.println("Completed.");
				
				
				BufferedWriter writer = null;
				try {
					//create a temporary file
					String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
					File logFile = new File(filename+".txt");

					// This will output the full path where the file will be written to...
					System.out.println(logFile.getCanonicalPath());

					writer = new BufferedWriter(new FileWriter(logFile));
					writer.write("THIS IS A TEST FOR TCP"+";");
					writer.write("Time log;" + timeLog+";");
					writer.write("File Name;"+ filename+";");
					writer.write("File Size;"+(sz/(1024*1024))+" MB"+";");
					writer.write("Test for client;" + s+";");
					if(isComplete){
						writer.write("Successfull?;Successful integrity verification transfer."+";");
					}
					else{
						writer.write("Successfull?;File integrity compromised. File may be corrupted."+";");
					}
					double timeElapsed = (aft-bef)/1000;
					writer.write("Time elapsed in seconds;" + timeElapsed+";");
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						// Close the writer regardless of what happens...
						writer.close();
					} catch (Exception e) {
					}
				}
				
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