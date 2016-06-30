package WikiBot.Core;

import javax.swing.JFrame;

import WikiBot.InterwikiBot;

/**
 * This class is fairly uninteresting. All bot GUI instantiation code is provided for here.
 * 
 * @author ErnieParke/Choco31415
 *
 */
public class BotFrame {

    private static BotPanel myPanel;
    private static JFrame frame;
	
    private static void createAndShowUI() {
    	
        frame = new JFrame();
    	
    	myPanel = new InterwikiBot();//The bot to make a GUI of.
    	myPanel.setUpJMenuBar(frame);
       
    	frame.setTitle(myPanel.getPanelName());
    	frame.setSize(100, 100);
    	frame.setResizable(false);
    	frame.getContentPane().add(myPanel);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
    	frame.setSize(myPanel.getWidth(), myPanel.getHeight());
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);       
   }

   public static void main(String[] args) {
       createAndShowUI();
   }
}