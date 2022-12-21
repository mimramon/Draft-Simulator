import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

// Client class
class Client
{
	public DataInputStream dataInputStream;
	public DataOutputStream dataOutputStream;
	public boolean keepAlive;
	private InetAddress hostIP;
	private Socket socket;
	public boolean waitForProcess;

	public Client()
	{
		keepAlive = true;
		boolean tryConnect = true;
        do
        {
            try
		    {
		    	// getting localhost ip
		    	hostIP = InetAddress.getByName("localhost");
		    	// establish the connection with server port 4999
		    	socket = new Socket(hostIP, 4999);
		    	// obtaining input and out streams
		    	dataInputStream = new DataInputStream(socket.getInputStream());
		    	dataOutputStream = new DataOutputStream(socket.getOutputStream());
		    	waitForProcess = false;
		    	tryConnect = false;
		    }
            catch(Exception e)
            {
		    	System.out.println("failed to connect to host: " + e);
		    	int option = JOptionPane.showConfirmDialog(null, "couldn't connect to server, try again?", "ERROR", JOptionPane.YES_NO_OPTION);
		    	if(option != 0)
		    	{
		    		keepAlive = false;
		    		return;
		    	}
		    }
        }
        while(tryConnect);
	}

	public ArrayList<String> getPack()
	{
		waitForProcess = true;
		System.out.println("send get pack");
		sendMessage("get pack");
		String received = null;
		ArrayList<String> pack = new ArrayList<String>();
		received = receiveMessage();
        //receives the cards one by one and adds them to list
		while(!received.equals("done"))
		{
			System.out.println(received + "added to pack");
			pack.add(received);
			received = receiveMessage();
		}
		System.out.println("pack completed");
		waitForProcess = false;
		return pack;
	} 

	public void joinLobby()
	{
		waitForProcess = true;
		sendMessage("join lobby");
		System.out.println("client asks to join lobby");

        //waits to receive the joined lobby message from the server
		if(receiveMessage().equals("joined lobby"))
		{
			System.out.println("lobby joined");	
		}
		waitForProcess = false;
	}

	public void sendSelectedCard(String card)
	{
		waitForProcess = true;
		System.out.println("client sends selected card msg");
		sendMessage("card selected");
		sendMessage(card);
		waitForProcess = false;
	}
	
	public BufferedImage getCardImage(String card)
	{
		waitForProcess = true;
		System.out.println("ask server for card image");
		sendMessage("get card");
		sendMessage(card.replace("//", "_"));
		BufferedImage img = null;
		int count = 0;
		while(count < 5)
		{
			System.out.println("try getting card image");
			try {img = ImageIO.read(new BufferedInputStream(dataInputStream));}
			catch(IOException ex)
			{
				System.out.println("error getting image from server: " + ex);
			}
			count++;
		}
		
		
		System.out.println("got card image from server " + img);
		waitForProcess = false;
		return img;
	}
	
	public void sendMessage(String msg)
	{
		waitForProcess = true;
		//boolean msgSent = false;
		//while(!msgSent)
		//{
			try {dataOutputStream.writeUTF(msg);}
			catch(IOException ex) 
			{
				System.out.println("couldn't send message to server: " + ex);
			}
		//}
		waitForProcess = false;
	}
	
	public String receiveMessage()
	{
		waitForProcess = true;
		String msg = null;
		try {msg = dataInputStream.readUTF();}
		catch(IOException ex)
		{
			System.out.println("couldn't receive message from server: " + ex);
		}
		waitForProcess = false;
		return msg;
	}
}