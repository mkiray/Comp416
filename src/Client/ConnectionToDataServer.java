package Client;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToDataServer
{
    public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.24";
    public static final int DEFAULT_SERVER_PORT = 8889;
    private Socket s;
    //private BufferedReader br;
    private DataOutputStream dos;
    private FileInputStream fis;
    private DataInputStream dataInputStream;
    
    
    protected String serverAddress;
    protected int serverPort;

    /**
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port port number of the server
     */
    public ConnectionToDataServer(String address, int port)
    {
        serverAddress = address;
        serverPort    = port;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void Connect()
    {
        try
        {
            s=new Socket(serverAddress, serverPort);
            //br= new BufferedReader(new InputStreamReader(System.in));
            /*
            Read and write buffers on the socket
             */
            dataInputStream = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
          
   		 
            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

   
    
    
    public void Disconnect()
    {
        try
        {
            dos.close();
            //br.close();
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public String sendFile(String file) throws IOException {
        String response = new String();

    	
       
    	  fis = new FileInputStream(file);
		
		byte[] buffer = new byte[4096];
		
		while (fis.read(buffer) > -1) {
			dos.write(buffer);
		}
        
		/*
		  try {
				response = dataInputStream.readUTF();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		
		Disconnect();
		  
			
			return response;
	}
    
}