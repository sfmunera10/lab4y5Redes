package udp;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;



public class UDPServer
{
	private static final int PORT =  3001;
	private static int numClientsDesired = 0;
	private static DatagramSocket datagramSocket;
	private static DatagramPacket inPacket;
	private static byte[] bufferMsg;
	private static String fileToSend;
	private static BufferedReader br;
	private static BufferedReader br2;
	private static BufferedReader br3;

	public static void main(String[] args)
	{
		System.out.println("Opening port \n");
		fileToSend = "";
		System.out.println("WELCOME TO A SIMPLE FILE TRANSFER THROUGH UDP");
		String filename;
		filename= "testl.wmv";
		String filename2;
		filename2= "testm.wmv";
		System.out.println("Files: ");
		System.out.println(filename + " Capoeira (Large Footage)");
		System.out.println(filename2 + " Capoeira (Medium Footage)");
		System.out.println("------------------------------------------------------------------");

		br=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Which file would you like to send?");
		System.out.println("Type in 1 to send Capoeira (Large Footage)");
		System.out.println("Type in 2 to send Capoeira (Medium Footage)");
		String whichFile = "";

		while (!whichFile.equals("1") || !whichFile.equals("2")) {
			try {
				whichFile = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

		br2=new BufferedReader(new InputStreamReader(System.in));
		System.out.println("How many clients would you like to connect to this server?");
		System.out.println("Type in the number of clients you would like: ");
		String whichFile2 = "";

		while (!whichFile2.equals("1") || !whichFile2.equals("2") || !whichFile2.equals("3") ||
				!whichFile2.equals("4") || !whichFile2.equals("5") || !whichFile2.equals("10") ||
				!whichFile2.equals("25")) {
			try {
				whichFile2 = br2.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		System.out.println(numClientsDesired + " CLIENTS WILL BE INCLUDED IN THIS FILE TRANSFER.");
		try
		{
			datagramSocket=new DatagramSocket(PORT);
		}
		catch(SocketException sockEx)
		{
			System.out.println("unable to open ");
			System.exit(1);
		}
		handleClient();
	}
	private static void handleClient()
	{
		try
		{
			String messageIn;
			InetAddress clientAddress=null;
			int clientPort;
			ArrayList<DatagramPacket> clients = new ArrayList<>();
			ArrayList<Thread> threads = new ArrayList<>();
			final CyclicBarrier gate = new CyclicBarrier(numClientsDesired+1);
			do
			{
				bufferMsg= new byte[256];
				inPacket=new DatagramPacket(bufferMsg,bufferMsg.length);
				datagramSocket.receive(inPacket);
				clientAddress=inPacket.getAddress();
				clientPort=inPacket.getPort();
				clients.add(inPacket);
				messageIn=new String(inPacket.getData(),0,inPacket.getLength());
				System.out.print(clientAddress);
				System.out.print(" : ");
				System.out.print(clientPort);
				System.out.print(" : ");
				System.out.println(messageIn);
				if(clients.size() == numClientsDesired){
					System.out.println("All clients required are ready.");
					System.out.println("Assigning new thread for each new client...");

					for(DatagramPacket pa: clients){
						//TODO Set Buffer Size
						Thread t = new ClientHandler(datagramSocket,pa, fileToSend, gate, 64000, numClientsDesired);
						threads.add(t);
					}
					System.out.println("Number of threads ready: " + threads.size());
					for(Thread th: threads){
						// Invoking the start() method 
						th.start();
					}

					br3=new BufferedReader(new InputStreamReader(System.in));
					System.out.println();
					System.out.println("Type Yes to start all threads: ");
					String yas = "";

					while(!(yas.equals("Yes"))){
						yas = br3.readLine();
						if(yas.equals("Yes")){
							try {
								gate.await();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (BrokenBarrierException e) {
								e.printStackTrace();
							}
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
						try {
							thread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("Waiting for thread to finish...");
					}
					System.out.println("Done.");
					clients.remove(0);
					break;
				}
			}while(true);
		}
		catch(IOException ioEx)
		{
			ioEx.printStackTrace();
		}
		finally
		{
			System.out.println("\n Closing connection.. ");
			datagramSocket.close();
		}
	}
}

//ClientHandler class 
class ClientHandler extends Thread{ 
	final DatagramSocket datagramSocket;
	final DatagramPacket pa;
	final String fileToSend;
	final CyclicBarrier gate;
	final int buffSize;
	final int clientsDesired;

	// Constructor 
	public ClientHandler(DatagramSocket ds, DatagramPacket pa, String fileToSend, CyclicBarrier gate,
			int buffSize, int clientsDesired)  
	{ 
		this.datagramSocket = ds;
		this.pa = pa; 
		this.fileToSend = fileToSend;
		this.gate = gate;
		this.buffSize = buffSize;
		this.clientsDesired = clientsDesired;
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
			System.out.println("File to send: "+ fileToSend);
			DatagramPacket outPacket=new DatagramPacket(fileToSend.getBytes(),fileToSend.length(),
					pa.getAddress(),pa.getPort());
			datagramSocket.send(outPacket); 

			File f=new File(fileToSend);
			System.out.println("Attempting to read from file in: "+f.getCanonicalPath());
			long sz=(int) f.length();
			//262144 (65536*4) for large file
			//65536 for medium file
			byte buffer[]=new byte [buffSize];

			outPacket=new DatagramPacket(Long.toString(sz).getBytes(),Long.toString(sz).length(),
					pa.getAddress(),pa.getPort());
			datagramSocket.send(outPacket); 

			System.out.println("");
			System.out.println ("Size: "+ sz);
			System.out.println ("Buf size: "+ buffSize);

			byte[] digest = createChecksum(fileToSend);

			String digStr = "";

			for(byte by: digest){
				digStr += by;
			}

			System.out.println("Sending checksum of the file to client...");
			outPacket=new DatagramPacket(digStr.getBytes(),digStr.length(),
					pa.getAddress(),pa.getPort());
			datagramSocket.send(outPacket);

			System.out.println("Sending File...");

			BufferedInputStream bis = 
					new BufferedInputStream(
							new FileInputStream(fileToSend));
			
			int len;
			int packetsSent = 0;
			while ((len = bis.read(buffer)) != -1){
				outPacket=new DatagramPacket(buffer,len,
						pa.getAddress(),pa.getPort());
				datagramSocket.send(outPacket);
				packetsSent++;
			}
			System.out.println("Packets Sent: " + packetsSent);
			buffer = "end".getBytes();
			outPacket = new DatagramPacket(buffer, buffer.length, pa.getAddress(),pa.getPort());
			System.out.println("Sending the end of the file transfer...");
			datagramSocket.send(outPacket);

			bis.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("An error occured");
		} 
	} 
} 