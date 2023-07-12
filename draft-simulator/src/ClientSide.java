import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class ClientSide 
{
    private DraftGui draftGUI;
    private MenuGui menuGUI;
    private Client client;

    public ClientSide()
    {
        menuGUI = new MenuGui();
    }

    public void setupDraft()
    {
        //creates client object
        client = new Client();

        if(!client.keepAlive)
        {
        	draftGUI.dispose();
        	menuGUI = new MenuGui();
        	return;
        }
        
        client.joinLobby();
        System.out.println("waiting for players to join");
        String message;
        do
        {
            message = client.receiveMessage();
            System.out.println("received " + message);
        }
        while(!message.equals("start draft"));

        //starts the draft
        System.out.println("starting draft");
        draftGUI.addCardsToGrid(client.getPack());
    }

    class MenuGui extends JFrame
    {
        private JPanel panel;

        //sets up the menu GUI
        public MenuGui()
        {
            panel = new JPanel();
            panel.setBackground(Color.blue);
            JButton startButton = new JButton("start draft");
            startButton.addActionListener(new ActionListener()
            { 
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    draftGUI = new DraftGui();
                    menuGUI.dispose();

                    SwingWorker w = new SwingWorker<Void, Void>()
                    {
                        protected Void doInBackground()
                        {
                            setupDraft();
                            return null;
                        }        
                    };
                    w.execute();
                }
            });
            
            
            JButton exitButton = new JButton("exit");
            exitButton.addActionListener(new ActionListener()
            { 
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    System.exit(-1);
                }
            });
            panel.add(exitButton);
            panel.add(startButton);
            this.add(panel, BorderLayout.CENTER);
            this.setPreferredSize(new Dimension(320, 180));
            this.pack();
            
            this.setVisible(true);
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    class DraftGui extends JFrame
    {
        private JPanel packPanel;
        private JPanel deckPanel;

        public DraftGui()
        {

            //make panels
            packPanel = new JPanel();
            deckPanel = new JPanel();
            packPanel.setBackground(Color.GREEN);
            deckPanel.setBackground(Color.CYAN);
            this.add(packPanel, BorderLayout.CENTER);
            this.add(deckPanel, BorderLayout.PAGE_END);
            JScrollPane scrollPane = new JScrollPane(deckPanel);
            this.add(scrollPane, BorderLayout.AFTER_LAST_LINE);

            //window settings
            this.setPreferredSize(new Dimension(1280, 720));
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setVisible(true);
            this.pack();
        }

        void addCardsToGrid(ArrayList<String> pack)
        {
            //for each card in the pack array a new jbutton is created that will allow you to select the card
            for(String card : pack)
            {
            	System.out.println("try get " + card);
            	while (client.waitForProcess) {System.out.println("waiting for a process");}
            	ImageIcon cardImage = new ImageIcon(client.getCardImage(card));
            	System.out.println("got " + card);
            	System.out.println(cardImage);
                JButton cardButton = new JButton(cardImage);
                String cardText = RequestHandler.GetCardText(card);
                System.out.println("try add cardbutton");
                cardButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e) 
                    {  
                        JButton selectedCard = new JButton(cardImage);
                        selectedCard.setToolTipText("<html><p width=\"250\">" + cardText + "</p></html>");
                        selectedCard.setPreferredSize(new Dimension(146, 204));
                        deckPanel.add(selectedCard);
                        packPanel.removeAll();
                        packPanel.revalidate();
                        repaint();
                        while(client.waitForProcess) {}    
                        client.sendSelectedCard(card);
                        new MessageWorker().execute();
                    }
                }    
                );
                System.out.println("added cardbutton");
                cardButton.setPreferredSize(new Dimension(146, 204));
                cardButton.setToolTipText("<html><p width=\"250\">" + cardText + "</p></html>");
                packPanel.add(cardButton);
            }
            packPanel.revalidate();
            pack();    
        }

        class MessageWorker extends SwingWorker<Void,Void>
        {
            protected Void doInBackground()
            {
            	String receive = "";
                do
                {
                	while(client.waitForProcess) {}
                	receive = client.receiveMessage();
                    System.out.println("receive message: " + receive);
                    if(receive.equals("end draft"))
                    {
                    	endDraft();
                        break;
                    }
                }
                while(!receive.equals("get pack"));
                System.out.println("add new pack to grid");
                while(client.waitForProcess) {}
                draftGUI.addCardsToGrid(client.getPack());
                return null;
            }
        }

        void endDraft()
        {
            // the following clears the gui and makes a new panel 
            // with all the cards selected for the deck
            System.out.println("end draft");
            Component components[] = deckPanel.getComponents();
            removeAll();
            JPanel deck = new JPanel();
            deck.setBackground(Color.CYAN);
            add(deck, BorderLayout.CENTER);
            for (Component c : components) 
            {
                deck.add(c);
            }
            repaint();
        }
    }
}

