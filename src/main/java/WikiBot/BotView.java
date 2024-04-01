package WikiBot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import MediawikiData.MediawikiDataManager;
import Utils.DocumentSizeFilter;
import Utils.FileUtils;

/**
 * This class is fairly uninteresting. All bot GUI instantiation code is provided for here.
 * 
 * @author ErnieParke/Choco31415
 *
 */
public class BotView extends JFrame implements ActionListener{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	//Bot and GUI preferences	
	protected final int WIDTH = 700;//GUI width
	protected final int HEIGHT = 335;//GUI height
	
	protected int maxConsoleLineSize = 80;//The maximum line length of the console box.
	
	//GUI stuff
	protected JPanel mainPanel;
	
	protected JMenuBar menuBar;
	protected JButton printLogButton;
	protected JButton runButton;
	protected JButton pushButton;
	protected JButton exitButton;
	protected JTextArea console;
	protected JList<String> proposedCommandsList;
	protected JList<String> acceptedCommandsList;
	protected JButton exportEditsButton;
	protected JButton removeButton;
	protected JButton acceptButton;
	protected JButton acceptAllButton;
	protected JTable wikiTable;
	protected JLabel statusLabel;
	protected JButton logInButton;
	protected JPopupMenu logInMenu;
	protected JMenuItem logInHomeButton;
	protected JMenuItem logInSelectedButton;
	protected JMenuItem logInAllButton;
	
	protected boolean firstConsoleMessage = true;//Used by the console.
	
	MediawikiBot bot;
	BotPanel panel;
	
    public BotView(String panelName_, MediawikiBot bot_, BotPanel panel_) {
    	super(panelName_);
    	
    	bot = bot_;
    	panel = panel_;

		setSize(WIDTH, HEIGHT);
    	setResizable(false);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setLocationRelativeTo(null);
    	
    	// Initialize main panel quickly. Will be customized later.
    	mainPanel = new JPanel();
		
	    // Construct the various GUI components
	    runButton = createPlainJButton("Run");
	    pushButton = createPlainJButton("Push Commands");
	    exitButton = createPlainJButton("Exit");
	    
	    console = new JTextArea();
	    AbstractDocument doc = (AbstractDocument) console.getDocument();
	    doc.setDocumentFilter(new DocumentSizeFilter(maxConsoleLineSize, console));
	    
	    printLogButton = createJButton("Export Log", new Dimension(100, 20));
	    exportEditsButton = createJButton("Export Edits", new Dimension(100, 20));
	    removeButton = createJButton("Remove", new Dimension(100, 20));
	    acceptButton = createJButton("Accept", new Dimension(100, 20));
	    acceptAllButton = createJButton("Accept All", new Dimension(100, 20));
	    
	    statusLabel = new JLabel("");
	    statusLabel.setPreferredSize(new Dimension(WIDTH-100, 20));
	    statusLabel.setMinimumSize(new Dimension(WIDTH-100, 20));
	    statusLabel.setMaximumSize(new Dimension(WIDTH-100, 20));
	    
	    logInButton = createJButton("Log In", new Dimension(100, 20));
	    logInButton.setHorizontalTextPosition(SwingConstants.LEFT);
	    BufferedImage image = FileUtils.readImage("/Images/DropdownArrow.png");
	    ImageIcon icon = new ImageIcon(image);
	    logInButton.setIcon(icon);
	    
	    logInHomeButton = new JMenuItem("Home");
		logInSelectedButton = new JMenuItem("Selected");
		logInAllButton = new JMenuItem("All");

	    DefaultListModel<String> proposedCommands = new DefaultListModel<>();
	    proposedCommandsList = new JList<String>(proposedCommands);
	    JScrollPane proposedCommandsPane = new JScrollPane(proposedCommandsList);
	    proposedCommandsPane.setPreferredSize(new Dimension(WIDTH/2, 150));
	    
	    DefaultListModel<String> acceptedCommands = new DefaultListModel<>();
	    acceptedCommandsList = new JList<String>(acceptedCommands);
	    JScrollPane acceptedCommandsPane = new JScrollPane(acceptedCommandsList);
	    acceptedCommandsPane.setPreferredSize(new Dimension(WIDTH/2, 150));
    	
	    //Set up the wiki table
	    MediawikiDataManager mdm = bot.getMDM();
	    Object[] columnHeaders = new Object[]{"Wiki", "Logged In"};
	    Object[][] rowData = new Object[mdm.getNumWikis()][2];
	    for (int i = 0; i < mdm.getNumWikis(); i++) {
	    	String iw = mdm.getWikiPrefixes().get(i);
	    	rowData[i][0] = iw;
	    	rowData[i][1] = false;
	    }
	    wikiTable = new JTable(rowData, columnHeaders);
	    
	    //Set column widths.
	    TableColumn column = null;
	    for (int i = 0; i < 2; i++) {
	    	column = wikiTable.getColumnModel().getColumn(i);
	    	switch (i) {
	    		case 0:
	    			column.setPreferredWidth(100);
	    			break;
	    		case 1:
	    			column.setWidth(40);
	    			break;
	    		default:
	    			throw new Error("Please update the GUI.");
	    	}
	    }
	    
	    //Make GUI components scrollable
	    JScrollPane consolePane = new JScrollPane(console);
	    consolePane.setPreferredSize(new Dimension(WIDTH-150, 100));
	    
	    JScrollPane wikiTablePane = new JScrollPane(wikiTable);
	    wikiTablePane.setPreferredSize(new Dimension(150, 100));
	    
	    //Give headers to various GUI components
	    JLabel header = new JLabel("Console");//Add a header to the console.
	    Font font = header.getFont();
	    header.setFont(font.deriveFont(12f));
	    header.setOpaque(true);
	    header.setBackground(new Color(242, 242, 242));
	    consolePane.setColumnHeaderView(header);
	    header.setBorder(new EmptyBorder(0, 2, 0, 0));
	    
	    header = new JLabel("Proposed Commands");
	    header.setOpaque(true);
	    header.setBackground(new Color(242, 242, 242));
	    header.setFont(font);
	    header.setBorder(new EmptyBorder(0, 2, 0, 0));
	    proposedCommandsPane.setColumnHeaderView(header);
	    
	    header = new JLabel("Accepted Commands");
	    header.setOpaque(true);
	    header.setBackground(new Color(242, 242, 242));
	    header.setFont(font);
	    header.setBorder(new EmptyBorder(0, 2, 0, 0));
	    acceptedCommandsPane.setColumnHeaderView(header);
	    
	    //Group buttons into boxes
	    menuBar = new JMenuBar();
	    menuBar.add(runButton);
	    menuBar.add(pushButton);
	    menuBar.add(Box.createHorizontalGlue());
	    menuBar.add(exitButton);
	    
	    Box commandProcessingBar =Box.createHorizontalBox();
	    commandProcessingBar.add(removeButton);
	    commandProcessingBar.add(acceptButton);
	    commandProcessingBar.add(acceptAllButton);
	    commandProcessingBar.add(Box.createHorizontalStrut(WIDTH-500));
	    commandProcessingBar.add(printLogButton);
	    commandProcessingBar.add(exportEditsButton);
	    
	    logInMenu = new JPopupMenu("Log In");
	    logInMenu.setPreferredSize(new Dimension(100, 60));
	    logInMenu.add(logInHomeButton);
	    logInMenu.add(logInSelectedButton);
	    logInMenu.add(logInAllButton);
	    
	    Box bottomBar = Box.createHorizontalBox();
	    bottomBar.add(statusLabel);
	    bottomBar.add(logInButton);
	    
	    //Set up action handlers, so buttons actually work. No one wants a non-functioning button.
	    runButton.addActionListener(this);
	    pushButton.addActionListener(this);
	    printLogButton.addActionListener(this);
	    exitButton.addActionListener(this);
	    exportEditsButton.addActionListener(this);
	    removeButton.addActionListener(this);
	    acceptButton.addActionListener(this);
	    acceptAllButton.addActionListener(this);
	    logInButton.addActionListener(this);
	    logInHomeButton.addActionListener(this);
	    logInSelectedButton.addActionListener(this);
	    logInAllButton.addActionListener(this);

	    //Add the GUI components to the GUI
	    setJMenuBar(menuBar);
	    
	    SpringLayout layout = new SpringLayout();
	    mainPanel.setLayout(layout);
        
        mainPanel.add(consolePane);
	    
        mainPanel.add(proposedCommandsPane);
	    mainPanel.add(acceptedCommandsPane);
	    
	    mainPanel.add(commandProcessingBar);
	    
	    mainPanel.add(wikiTablePane);
	    mainPanel.add(bottomBar);
	    
	    //Control a few rendering aspects.   
	    mainPanel.setComponentZOrder(commandProcessingBar, 1);
	    mainPanel.setComponentZOrder(bottomBar, 0);
        
        /*
         * Constraints n' stuff
         */
	    //Top half
	    layout.putConstraint(SpringLayout.WEST, proposedCommandsPane,
	    		0,
	    		SpringLayout.WEST, mainPanel);
	    layout.putConstraint(SpringLayout.NORTH, proposedCommandsPane,
	    		0,
	    		SpringLayout.NORTH, mainPanel);
	    
	    layout.putConstraint(SpringLayout.WEST, acceptedCommandsPane,
	    		0,
	    		SpringLayout.EAST, proposedCommandsPane);
	    layout.putConstraint(SpringLayout.NORTH, acceptedCommandsPane,
	    		0,
	    		SpringLayout.NORTH, proposedCommandsPane);
	    
	    layout.putConstraint(SpringLayout.EAST, commandProcessingBar,
	    		0,
	    		SpringLayout.EAST, mainPanel);
	    layout.putConstraint(SpringLayout.NORTH, commandProcessingBar,
	    		-1,
	    		SpringLayout.SOUTH, proposedCommandsPane);
	    
	    //Bottom half
	    layout.putConstraint(SpringLayout.NORTH, consolePane,
                0,
                SpringLayout.SOUTH, commandProcessingBar);
        layout.putConstraint(SpringLayout.WEST, consolePane,
                0,
                SpringLayout.WEST, mainPanel);
	    
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, wikiTablePane,
                0,
                SpringLayout.VERTICAL_CENTER, consolePane);
        layout.putConstraint(SpringLayout.EAST, wikiTablePane,
                0,
                SpringLayout.EAST, mainPanel);
        
        layout.putConstraint(SpringLayout.WEST, bottomBar,
                0,
                SpringLayout.WEST, mainPanel);
        layout.putConstraint(SpringLayout.NORTH, bottomBar,
               -1,
                SpringLayout.SOUTH, wikiTablePane);
	    
	    // Finish GUI organization
	    add(mainPanel);
        
	    //Make sure everything renders.    
	    setVisible(true);
   }
    
	/**
	 * Create a JButton without a border.
	 * @param text The button's text.
	 * @return A JButton.
	 */
	public JButton createPlainJButton(String text) {
		JButton toReturn = new JButton(text);
		
		toReturn.setBorderPainted(false);
		
		Border emptyBorder = new EmptyBorder(4, 8, 4, 8);
		toReturn.setBorder(emptyBorder);
		
		return toReturn;
	}
	
	/**
	 * Create a JButton.
	 * @param text The button's text.
	 * @param size The button's size.
	 * @return A JButton.
	 */
	public JButton createJButton(String text, Dimension size) {
		JButton toReturn = new JButton(text);

		toReturn.setPreferredSize(size);
		toReturn.setMinimumSize(size);
		toReturn.setMaximumSize(size);
		
		return toReturn;
	}
	
	/*
	 * Button handling code.
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == logInButton) {
			logInMenu.show(logInButton, 0, logInButton.getHeight());
		} else if (e.getSource() == logInHomeButton) {
			panel.logInAtHome();
		} else if (e.getSource() == logInSelectedButton) {
			panel.logInAtSelected();
		} else if (e.getSource() == logInAllButton) {
			panel.logInEverywhere();
		} else if (e.getSource() == exitButton){
			System.exit(0);
		} else if (e.getSource() == printLogButton) {
			panel.printLog();
		} else if (e.getSource() == exportEditsButton) {
			panel.exportEdits();
		} else if (e.getSource() == removeButton) {
			panel.removeSelectedCommands();
		}else if (e.getSource() == acceptButton) {
			panel.acceptSelectedCommands();
		} else if (e.getSource() == acceptAllButton) {
			panel.acceptAllCommands();
		} else if (e.getSource() == runButton) {
			panel.runCode();
		} else if (e.getSource() == pushButton){
			panel.pushButton();
		}
	}
	
	/**
	 * This method prints messages to the console.
	 * @param line The message to show.
	 */
	protected void printToConsole(String line) {
		if (firstConsoleMessage) {
			firstConsoleMessage = false;
			console.append(line);
		} else {
			console.append("\n" + line);
		}
		
		//Move console caret position.
		if (console.getLineCount() > 2) {
			try {
				int lineOffset = console.getLineEndOffset(console.getLineCount()-2) + 1;
				
				//Move the caret.
				try {
					console.setCaretPosition(lineOffset);
				} catch (IllegalArgumentException e) {
					//If console complains about bad caret position, go somewhere we know 100% is valid.
					console.setCaretPosition(console.getLineEndOffset(console.getLineCount() - 1));
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Set the bot's status.
	 * 
	 * @param status The new status.
	 */
	protected void setStatus(String status) {
		statusLabel.setText(status);
	}
	
	/**
	 * Set the push button's text.
	 * 
	 * @param text The new text.
	 */
	protected void setPushButtonText(String text) {
		pushButton.setText(text);
	}
	
	/**
	 * Get the selected wikis in the wiki table.
	 * 
	 * @return An arraylist of wiki codes.
	 */
	protected ArrayList<String> getSelectedWikis() {
		ArrayList<String> languageCodes = new ArrayList<String>();
		int[] rows = wikiTable.getSelectedRows();
		for (int row : rows) {
			languageCodes.add((String) wikiTable.getValueAt(row, 0));
		}
		
		return languageCodes;
	}
	
	/**
	 * Set the login status of a wiki, for the wiki table.
	 * 
	 * @param languageCode Wiki's language.
	 * @param status The login status.
	 */
	protected void setWikiLoginStatus(String languageCode, boolean status) {
		MediawikiDataManager mdm = bot.getMDM();
		wikiTable.setValueAt(true, mdm.getWikiPrefixes().indexOf(languageCode), 1);
	}
	
	/**
	 * Get the currently selected index of the proposed list. -1 if nothing selected.
	 * 
	 * @return Selected index.
	 */
	protected int getSelectedProposedIndex() {
		return proposedCommandsList.getSelectedIndex();
	}
	
	/**
	 * Get the currently selected index of the accepted list. -1 if nothing selected.
	 * 
	 * @return Selected index.
	 */
	protected int getSelectedAcceptedIndex() {
		return acceptedCommandsList.getSelectedIndex();
	}
	
	/**
	 * Get the item of the proposed list.
	 * 
	 * @param index The item's index.
	 * @return The item's text.
	 */
	protected String getProposed(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
		return dm.get(index);
	}
	
	/**
	 * Get the item of the accepted list.
	 * 
	 * @param index The item's index.
	 * @return The item's text.
	 */
	protected String getAccepted(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedCommandsList.getModel();
		return dm.get(index);
	}
	
	/**
	 * Insert an item to the proposed list.
	 * 
	 * @param index The index to insert at.
	 * @param item The new item.
	 */
	protected void insertToProposed(int index, String item) {
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
		dm.add(index, item);
	}
	
	/**
	 * Insert an item to the accepted list.
	 * 
	 * @param index The index to insert at.
	 * @param item The new item.
	 */
	protected void insertToAccepted(int index, String item) {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedCommandsList.getModel();
		dm.add(index, item);
	}
	
	/**
	 * Remove the index from the proposed list.
	 * 
	 * @param index The index to remove.
	 */
	protected void removeProposed(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
		dm.remove(index);
	}
	
	/**
	 * Remove the index from the accepted list.
	 * 
	 * @param index The index to remove.
	 */
	protected void removeAccepted(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedCommandsList.getModel();
		dm.remove(index);
	}
}