package MultiThreadServer;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import com.dropbox.core.v2.files.Metadata;

import Client.FileTuples;
 
public class MultiThreadedServer {
   ServerSocket myServerSocket;
   final String MASTERPATH = Constants.Constants.MASTERPATH;
   final String FOLLOWERPATH = Constants.Constants.FOLLOWERPATH;
   boolean ServerOn = true;
   public MultiThreadedServer() { 
      try {
         myServerSocket = new ServerSocket(8888);
         
      } catch(IOException ioe) { 
         System.out.println("Could not create server socket on port 8888. Quitting.");
         System.exit(-1);
      } 
      
      ConsoleReader consoleReader = new ConsoleReader();
      
      Thread a = new Thread(consoleReader);
      a.start();
		
      Calendar now = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat(
         "E yyyy.MM.dd 'at' hh:mm:ss a zzz");
      System.out.println("It is now : " + formatter.format(now.getTime()));
      
      while(ServerOn) { 
         try { 
            Socket clientSocket = myServerSocket.accept();
            ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
            cliThread.start(); 
         } catch(IOException ioe) { 
            System.out.println("Exception found on accept. Ignoring. Stack Trace :"); 
            ioe.printStackTrace(); 
         }  
      } 
      try { 
    	  a.interrupt();
         myServerSocket.close(); 
         System.out.println("Server Stopped"); 
      } catch(Exception ioe) { 
         System.out.println("Error Found stopping server socket"); 
         System.exit(-1); 
      } 
   }
	
   public static void main (String[] args) { 
	   new MultiThreadedServer();
   
   }

    class ClientServiceThread extends Thread { 
      Socket myClientSocket;
     
      boolean m_bRunThread = true; 
      public ClientServiceThread() { 
         super(); 
      } 
		
      ClientServiceThread(Socket s) { 
         myClientSocket = s; 
      } 
		
      public void run() { 
    	  DataInputStream dis = null;
   	   DataOutputStream dos = null;
   	   DropBoxFileManagement dropBoxFileManagement = new DropBoxFileManagement();
   	   System.out.println(
            "Accepted Client Address - " + myClientSocket.getInetAddress().getHostName());
         try { 
            dis = new DataInputStream(myClientSocket.getInputStream());
            dos = new DataOutputStream(myClientSocket.getOutputStream());
            

            while(m_bRunThread) { 
               String clientCommand = dis.readUTF(); 
               System.out.println("Client Says :" + clientCommand);
             
               if(!ServerOn) { 
                  System.out.print("Server has already stopped"); 
                  dos.writeUTF("Server has already stopped"); 
                  dos.flush(); 
                  m_bRunThread = false;
                  break;
               } 
               if(clientCommand.equalsIgnoreCase("quit")) {
                  m_bRunThread = false;
                  System.out.print("Stopping client thread for client : ");
                  break;
               } else if(clientCommand.equalsIgnoreCase("end")) {
                  m_bRunThread = false;
                  System.out.print("Stopping client thread for client : ");
                  ServerOn = false;
                  break;
               } else if(clientCommand.equalsIgnoreCase("send")){
                 	Scanner scanner = new Scanner(System.in);
            	   String meString = scanner.nextLine();
            	   dos.writeUTF(meString);
            	   dos.flush();
            	   scanner.close();
            
               }else if(clientCommand.equalsIgnoreCase("dropbox sync")){
            	   
            	   SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
            	   
            	   
            	   
            	   BufferedReader br = null;
              		FileReader fr = null;
              		
              		String lastActionDateString="";
               	   try {

              			//br = new BufferedReader(new FileReader(FILENAME));
              			fr = new FileReader("LastAction");
              			br = new BufferedReader(fr);

              			String sCurrentLine;

              			while ((sCurrentLine = br.readLine()) != null) {
              				lastActionDateString = sCurrentLine;
              			}

              		} catch (IOException e) {

              			e.printStackTrace();

              		} finally {

              			try {

              				if (br != null)
              					br.close();

              				if (fr != null)
              					fr.close();

              			} catch (IOException ex) {

              				ex.printStackTrace();

              			}

              		}
               	   Date date1 = new Date();
               	
               	Date lastActionDate = null;
               	   try{
               	   lastActionDate = dateFormat.parse(lastActionDateString);
               	   }catch (ParseException e) {
                       e.printStackTrace();
               	   }
               	   
            	   
            	   
            	   
            	   
            	   
            	   
            	           	   
            	   
            	   
            	   
            	   
            	   
            	   
            	   
            	   
            	   String ans= "The following files are going to be synchronized with the Dropbox\n";
            	   boolean updateNeeded = false;
            	   ArrayList<Metadata> alreadyUploadedFiles = dropBoxFileManagement.getAlreadyUploadedFiles();
            	   ArrayList<FileTuples> filesInFolder = getFilesFromFolder("DropSync");
            	   boolean found;
            	   for(FileTuples x : filesInFolder){
            		   found = false;
            		   for(Metadata y : alreadyUploadedFiles){
            			   if(x.getName().equals(y.getName())){
            				   found=true;
            				   
            				   if(lastActionDate.before(x.getUpdateDate())){
            					   //newer
            					   updateNeeded=true;
                    			   dropBoxFileManagement.deleteFile(y.getPathLower());
            					   dropBoxFileManagement.uploadFile(x.getName(), MASTERPATH+x.getName());
            					   ans+=x.getName()+" "+"update" +" "+ x.getSize()/1048576+" MB\n";
            				   }
            			   }
            		   }
            		   if(!found){
            			   //upload to dropbox
            			   updateNeeded=true;
            			   System.out.println("upload "+x.getName());            			   

            			   dropBoxFileManagement.uploadFile(x.getName(), MASTERPATH+x.getName());
            			   ans+=x.getName()+" "+"add" +" "+ x.getSize()+"\n";
            			   
            		   }
            	   }
            	   
            	   for(Metadata y : alreadyUploadedFiles){
            		   found = false;
            		   for(FileTuples x : filesInFolder){
            			   if(x.getName().equals(y.getName())){
            				   found = true;
            			   }
            		   }
            		   if(!found){
            			   updateNeeded=true;
            			   dropBoxFileManagement.deleteFile(y.getPathLower());
            			   ans+=y.getName()+" "+"delete" +" "+ "\n";
            		   }
            	   }
            	   
            	   
            	   
            	   
            	   
            	   
            	   ans+="Synchronization done with Dropbox";
            	   if(!updateNeeded){
            		   ans="No update is needed. Already synced!";
            	   }
            	   dos.writeUTF(ans);
    			   dos.flush();
    			   Date date = new Date();
    			   

    			   
    			   PrintWriter writer = new PrintWriter("lastAction", "UTF-8");
    			   writer.println(dateFormat.format(date));
    			   writer.close();
//            	   dropBoxFileManagement.uploadFile("meme.jpg", PATH+"meme.jpg");
            	   
            	   
            	   
            	   
            	              	   
            	   
               }else if(clientCommand.contains("delete")){
            	   String[] messageParsed = clientCommand.split(" ");
	        		String fileName = messageParsed[1];
            	   File file = new File(MASTERPATH+fileName);
					file.delete();
					dos.writeUTF("File Deleted.");
					dos.flush();
				
               }
               else if(clientCommand.contains("sendFile")){
            	   String[] messageParsed = clientCommand.split(" ");
	        		String fileName = messageParsed[1];

            	   MultiThreadedDataServer dataServer = new MultiThreadedDataServer(fileName);
            	   Thread t = new Thread(dataServer);
            	   t.start();       
                
                dos.writeUTF("File Sent: "+fileName);
                dos.flush();
                
               }else if(clientCommand.contains("getFile")){
            	   String[] messageParsed = clientCommand.split(" ");
	        		String fileName = messageParsed[1];
            	   MultiThreadedDataServer dataServer = new MultiThreadedDataServer(true,MASTERPATH+fileName);
           	   
            		Thread t = new Thread(dataServer);
            		t.start();
            		
            		
            	   dos.writeUTF("started");
            	   dos.flush();
            	   
               }
               
               else if(clientCommand.equalsIgnoreCase("uploadFile")){
//            	   dropBoxFileManagement.uploadFile("plan.txt", PATH+"plan.txt");
               }
               else if(clientCommand.equalsIgnoreCase("sync check")){
            	   
            	   
            	   ObjectOutputStream objectOutputStream = new ObjectOutputStream(dos);
            	   
            	   objectOutputStream.writeObject( getFilesFromFolder("DropSync"));
            	   objectOutputStream.flush();
            	   
           	   
               }
            	   else{
            		   dos.writeUTF(clientCommand);
            		   dos.flush();
               }
             
            }
         } catch(Exception e) { 
            e.printStackTrace(); 
         } 
         finally { 
            try { 
           
            	dis.close();
            	dos.close();
               myClientSocket.close(); 
               System.out.println("...Stopped"); 
            } catch(IOException ioe) { 
               ioe.printStackTrace(); 
            } 
         }
      }
   }
    
    
	public ArrayList<FileTuples> getFilesFromFolder(String dir){

    	File folder = new File(MASTERPATH);

		File[] listOfFiles = folder.listFiles();
		ArrayList<FileTuples> fileList = new ArrayList<>();
		
			for(int i = 0; i< listOfFiles.length;i++){
				
				//Use MD5 algorithm
				MessageDigest md5Digest = null;
				try {
					md5Digest = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				//Get the checksum
				String checksum = null;
				try {
					checksum = getFileChecksum(md5Digest, listOfFiles[i]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FileTuples tuple = new FileTuples(listOfFiles[i].getName(),checksum,new Date(listOfFiles[i].lastModified()));
				tuple.setSize(listOfFiles[i].length());
				fileList.add(tuple);
				}
			

		return fileList;
    }
	
	private static String getFileChecksum(MessageDigest digest, File file) throws IOException
	{
	    //Get file input stream for reading the file content
	    FileInputStream fis = new FileInputStream(file);
	     
	    //Create byte array to read data in chunks
	    byte[] byteArray = new byte[1024];
	    int bytesCount = 0;
	      
	    //Read file data and update in message digest
	    while ((bytesCount = fis.read(byteArray)) != -1) {
	        digest.update(byteArray, 0, bytesCount);
	    };
	     
	    //close the stream; We don't need it now.
	    fis.close();
	     
	    //Get the hash's bytes
	    byte[] bytes = digest.digest();
	     
	    //This bytes[] has bytes in decimal format;
	    //Convert it to hexadecimal format
	    StringBuilder sb = new StringBuilder();
	    for(int i=0; i< bytes.length ;i++)
	    {
	        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	     
	    //return complete hash
	   return sb.toString();
	}
   
}

   
        
   
