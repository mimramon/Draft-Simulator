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
		return pack;
	} 

	public void joinLobby()
	{
		sendMessage("join lobby");
		System.out.println("client asks to join lobby");

        //waits to receive the joined lobby message from the server
		if(receiveMessage().equals("joined lobby"))
		{
			System.out.println("lobby joined");	
		}
	}

	public void sendSelectedCard(String card)
	{
		System.out.println("client sends selected card msg");
		sendMessage("card selected");
		sendMessage(card);
	}
	
	public BufferedImage getCardImage(String card)
	{
		System.out.println("Requesting card image from server");
		sendMessage("get card");
		sendMessage(card.replace("//", "_"));
		BufferedImage img = null;
		System.out.println("Try reading card from input stream");
		int count = 0;
		while(img == null && count < 10)
		{
			try {img = ImageIO.read(new BufferedInputStream(dataInputStream));}
			catch(IOException ex)
			{
				System.out.println("error getting card image from server: " + ex);
			}
			count++;
		}
		if(img == null)
		{
			System.out.println("failiure fetching card image from server, set card to error image");
			img = App.ERRORIMG;
		}
		System.out.println("Card image to be used: " + img);
		return img;
	}
	
	public void sendMessage(String msg)
	{
		try {dataOutputStream.writeUTF(msg);}
		catch(IOException ex) 
		{
			System.out.println("couldn't send message to server: " + ex);
		}
	}
	
	public String receiveMessage()
	{
		String msg = null;
		while(msg == null)
		{
			try {msg = dataInputStream.readUTF();}
			catch(IOException ex)
			{
				System.out.println("couldn't receive message from server: " + ex);
			}
		}
		
		return msg;
	}
}