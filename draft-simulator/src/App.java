import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.io.*;


public class App 
{
	public static final String APPDIR =System.getProperty("user.dir") + "\\DraftFiles";
	public static final String IMGDIR = APPDIR + "\\CardImages";
	public static final String CUBEDIR = APPDIR + "\\cube.txt";
	public static final BufferedImage ERRORIMG = new BufferedImage(146, 204, ColorSpace.TYPE_RGB);
	
    public static void main(String[] args)
    {
    	initialiseFolders();
        new MenuGui();
    }  

    public static void initialiseFolders()
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
}

class MenuGui extends JFrame
{
    private JPanel panel;
    //sets up the menu GUI
    public MenuGui()
    {
        panel = new JPanel();
        panel.setBackground(Color.blue);
        JButton clientButton = new JButton("Join Game");
        clientButton.addActionListener(new ActionListener()
        { 
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                System.out.println("Proceed to client GUI");
                try {new ClientSide();} catch (Exception ex) {}
                dispose();
            }
        });

        JButton serverButton = new JButton("Host Game");
        serverButton.addActionListener(new ActionListener()
        { 
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                dispose();
            	System.out.println("Proceed to server");
            	Thread t = new Thread() 
            	{
            		public void run()
            		{
            			new Server();
            		}
            	};
            	t.start();
            }
        });

        panel.add(serverButton);
        panel.add(clientButton);
        add(panel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(320, 180));
        pack();
        setVisible(true);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
