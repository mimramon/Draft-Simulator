import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Server 
{
    private static final int PORT = 4999;
    private static final String APPDIR ="C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\DraftFiles";
    private static final String IMGDIR = APPDIR + "\\CardImages";
    private static final String CUBEDIR = APPDIR + "\\cube.txt";
    public Lobby lobbies[] = new Lobby[5];

    public Server()
    {
    	initialiseFolders();
    	//Makes the server socket
    	System.out.println("server start");
    	ServerSocket serverSocket = null;
    	boolean makeServer = true;
    	while(makeServer)
    	{
    		try
    		{
    			serverSocket = new ServerSocket(PORT);
    			makeServer = false;
    		}
    		catch(IOException ex) 
    		{
    			System.out.println("failed to open server socket: " + ex);
    			int option = JOptionPane.showConfirmDialog(null, "couldn't start server, try again?", "ERROR", JOptionPane.YES_NO_OPTION);
    			if(option != 0)
    			{
    				System.exit(-1);
    			}
    		}
    	}
        
        //infinite loop for client connection requests
        while(true)
        {
        	System.out.println("in while loop");
            Socket socket = null;
            try 
            {
                // socket object to receive incoming client requests
                socket = serverSocket.accept();
                  
                System.out.println("A new client is connected : " + socket);
                  
                // obtaining input and out streams
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                ClientHandler client = new ClientHandler(socket, dataInputStream, dataOutputStream, this);
                  
                System.out.println("Assigning new thread for this client");
                // create a new thread object
                Thread t = client;
                // Invoking the start() method
                t.start();
            }
            catch (IOException ex)
            {
                System.out.println("failiure connecting to client: " + ex);
            }
        }
    }
    
    public void initialiseFolders()
    {
    	File appDir = new File(APPDIR);
    	if (!appDir.exists())
    	{
    	    appDir.mkdirs();
    	}
    	File imgDir = new File(IMGDIR);
    	if (!imgDir.exists())
    	{
    	    imgDir.mkdirs();
    	}
    }
    
    public void saveCardImage(String _card)
    {
    	System.out.println("saving " + _card);
    	BufferedImage cardImg = RequestHandler.GetCardImage(_card);
    	String card = _card.replaceAll("//", "_");
    	File imgFile = new File(IMGDIR + "\\" + card + ".jpg");
    	try{ImageIO.write(cardImg, "jpg", imgFile);}
    	catch(IOException ex) {System.out.println("couldnt write image file: " + ex);}
    }

    class Lobby
    {
        private ArrayList<Player> players = new ArrayList<Player>();
        public boolean isFull;

        public void addPlayer(Player player)
        {
            System.out.println("adding player");
            players.add(player);
            if(players.size() == 8)
            {
                System.out.println("lobby now full");
                isFull = true;
            }
        }

        public void startDraft()
        {
            setupPacks();
            for (Player p : players) 
            {
                try
                {
                    System.out.println("sending start message");
                    p.playerClient.OUTPUTSTREAM.writeUTF("start draft");
                }
                catch(IOException e){System.out.println("error sending start message: " + e);}
            }
        }

        void setupPacks()
        {
            //create list for cards
            ArrayList<String> cardList = new ArrayList<String>();
            try 
            {
                BufferedReader br = new BufferedReader(new FileReader(CUBEDIR));
                String line;
                //read cards from file to list
                while ((line = br.readLine()) != null) 
                {
                    cardList.add(line);
                }
                br.close();
            }
            catch(Exception ex){System.out.println("card list failed to initialize:"+ ex);}
            //shuffle list
            Collections.shuffle(cardList);

            for(Player p : players)
            {
                //add first cards from list to packs
                for(int j = 0; j < 3; j++)
                {
                    for(int i = 0;  i < 15; i++)
                    {
                    	String currentCard = cardList.get(i);
                    	System.out.println("checking image for: " + currentCard);
                    	if(!new File(IMGDIR + "\\" + currentCard.replace("//", "_") + ".jpg").exists())
                    	{
                    		saveCardImage(currentCard);
                    	}
                        p.packs[j][i] = currentCard;
                        cardList.remove(i);
                    }
                }

                for(int i = 0; i < 15; i++)
                {
                    p.currentPack.add(p.packs[0][i]);
                }
            }
            
        }

        void cardSelected(Player player, String selectedCard)
        {
            System.out.println("running card selected method");
            player.currentPack.remove(selectedCard);
            player.ready = true;
            boolean rotate = true;
            for (Player p : players) 
            {
                if(!p.ready)
                {
                    System.out.println("dont rotate");
                    rotate = false;
                    break;
                }
            }
            if(rotate)
            {
                System.out.println("rotate");
                rotatePacks();
                for (Player p : players) 
                {
                    p.ready = false;
                    System.out.println("sending get pack");
                    p.playerClient.sendMessage("get pack");
                    
                }
            }
        }

        void rotatePacks()
        {
            System.out.println("running rotate pack method");

            //if the packs are empty and on the last pack then end the draft
            if(players.get(0).currentPack.size() == 0 && players.get(0).packNo == 2)
            {
                try
                {
                    endDraft();
                }
                catch(Exception e){System.out.println(e);}
            }
            else if(players.get(0).currentPack.size() == 0)
            {
                System.out.println("new pack");
                for (Player p : players) 
                {
                    p.packNo++;
                    for(int i = 0; i < 15; i++)
                    {
                        p.currentPack.add(p.packs[p.packNo][i]);
                    }
                }
            }
            else
            {
                System.out.println("packs are being rotated");
                ArrayList<ArrayList<String>> packBuffer = new ArrayList<ArrayList<String>>();
                for (Player p : players) 
                {
                    packBuffer.add(p.currentPack);
                }

                for(int i = 0; i < players.size(); i++)
                {
                    if(players.get(0).packNo%2 == 0)
                    {
                        if(i == 7)
                        {
                            players.get(i).currentPack = packBuffer.get(0);
                        }
                        else
                        {
                            players.get(i).currentPack = packBuffer.get(i+1);
                        }
                    }
                    else
                    {
                        if(i == 0)
                        {
                            players.get(i).currentPack = packBuffer.get(7);
                        }
                        else
                        {
                            players.get(i).currentPack = packBuffer.get(i-1);
                        }
                    }
                    
                }

            }
        }

        void endDraft()
        {
            for (Player p : players) 
            {
                p.playerClient.sendMessage("end draft");
            }
        }
    }

    class Player
    {
        ClientHandler playerClient;
        boolean ready;
        int packNo;
        String packs[][] = new String[3][15];
        ArrayList<String> currentPack = new ArrayList<String>();
        public Player(ClientHandler _playerClient)
        {
            packNo = 0;
            playerClient = _playerClient;
        }
    }


    // ClientHandler class
    class ClientHandler extends Thread
    {
        private final Server SERVER;
        private final DataInputStream INPUTSTREAM;
        private final DataOutputStream OUTPUTSTREAM;
        private final Socket SOCKET;
        private Lobby lobby;
        private Player player;
    
        // Constructor
        public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream, Server server)
        {
            this.SERVER = server;
            this.SOCKET = socket;
            this.INPUTSTREAM = dataInputStream;
            this.OUTPUTSTREAM = dataOutputStream;
        }
    
        @Override
        public void run()
        {
            String recievedMsg;
            boolean run = true;
            while (run)
            {
                
                try 
                {
                    recievedMsg = INPUTSTREAM.readUTF();
                    if(recievedMsg.equals("exit")){break;}
                    // write on output stream based on the answer from the client
                    switch (recievedMsg) 
                    {
                        case "join lobby":
                            joinLobby();
                            break;
                        
                        case "get pack":
                            getPack();
                            break;

                        case "card selected":
                            System.out.println("recieved card selected");
                            String selectedCard = INPUTSTREAM.readUTF();
                            lobby.cardSelected(player, selectedCard);
                            break;
                        
                        case "get card":
                        	System.out.println("start sending image: " + SOCKET);
                        	String card = INPUTSTREAM.readUTF();
                        	File cardFile = new File(IMGDIR + "\\" + card + ".jpg");
                        	System.out.println(cardFile);
                        	BufferedImage cardImg = ImageIO.read(cardFile);
                        	ImageIO.write(cardImg, "png", OUTPUTSTREAM);
                        	System.out.println("finished sending image: " + SOCKET);
                        	OUTPUTSTREAM.flush();
                        	break;
                            
                        default:
                            OUTPUTSTREAM.writeUTF("Invalid input");
                            break;
                    }
                } 
                catch (IOException e) 
                {
                    run = false;
                    e.printStackTrace();
                }
            }
        
            try
            {
                // closing resources
                this.INPUTSTREAM.close();
                this.OUTPUTSTREAM.close();	
            }
            catch(IOException e)
            {
                System.out.println(e);
            }
        }

        public void joinLobby()
        {
            System.out.println("join lobby process started");
            Server.Lobby lobbyToJoin = null;
            for(Server.Lobby l : SERVER.lobbies)
            {
                System.out.println("checking lobby");
                if(l == null){System.out.println("null lobby");}
                else if(!l.isFull)
                {
                    lobbyToJoin = l;
                    System.out.println("joined lobby");
                    break;
                }
            }
                            
            if(lobbyToJoin == null)
            {
                System.out.println("all lobbies null/full");
                for(int i = 0; i < 5; i++)
                {
                    System.out.println("checking lobby space " + i);
                    if(SERVER.lobbies[i] == null)
                    {
                        System.out.println("creating lobby in space "+i);
                        SERVER.lobbies[i] = SERVER.new Lobby();
                        System.out.println("lobby made");
                        lobbyToJoin = SERVER.lobbies[i];               
                        break;
                    }
                }
            }
            player = new Player(this);
            lobbyToJoin.addPlayer(player);
            lobby = lobbyToJoin;
            sendMessage("joined lobby");
            if(lobby.isFull)
            {
                lobby.startDraft();
            }
        }

        public void getPack()
        {
            System.out.println("server starts to get pack");
            ArrayList<String> pack = player.currentPack;
            for(String s : pack)
            {
                sendMessage(s);
            }
            sendMessage("done");
        }

        public void sendMessage(String msg)
    	{
    		//boolean msgSent = false;
    		//while(!msgSent)
    		//{
    			try {OUTPUTSTREAM.writeUTF(msg);}
    			catch(IOException ex) 
    			{
    				System.out.println("couldn't send message to server: " + ex);
    			}
    		//}
    	}
    }
}