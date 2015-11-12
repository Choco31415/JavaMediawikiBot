package WikiBot.Core;

import javax.swing.JFrame;

import WikiBot.InterwikiBot;

public class BotFrame {

    private static BotPanel myPanel;
	
    private static void createAndShowUI() {

       myPanel = new InterwikiBot();
       JFrame frame = new JFrame(myPanel.getName());
       frame.setSize(100, 100);
       frame.setResizable(false);
       frame.getContentPane().add(myPanel);
       //frame.setJMenuBar(myPanel.methodThatReturnsJMenuBar());
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
       frame.setSize(myPanel.getWidth(), myPanel.getHeight());
       frame.setLocationRelativeTo(null);
       frame.setVisible(true);
       
   }

   public static void main(String[] args) {
       createAndShowUI();
   }
   
	
}