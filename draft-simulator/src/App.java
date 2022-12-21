import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class App 
{
    public static void main(String[] args)
    {
        new MenuGui();
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
        JButton clientButton = new JButton("Client");
        clientButton.addActionListener(new ActionListener()
        { 
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                try {new ClientSide();} catch (Exception ex) {}
                dispose();
            }
        });

        JButton serverButton = new JButton("Server");
        serverButton.addActionListener(new ActionListener()
        { 
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                dispose();
            	System.out.println("server start button");
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
        this.add(panel, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(320, 180));
        this.pack();
        this.setVisible(true);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
