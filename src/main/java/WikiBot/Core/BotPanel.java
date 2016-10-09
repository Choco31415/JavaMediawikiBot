package WikiBot.Core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import WikiBot.APIcommands.APIcommand;
import WikiBot.ContentRep.User;
import WikiBot.Utils.DocumentSizeFilter;
import WikiBot.Utils.FileUtils;

/**
 * BotPanel is an extension of GenericBot that gives a GUI to a bot.
 * 
 * Some GUI methods may be found in this class.
 * Most bot methods may be found in GenericBot.
 * Some logger methods may be found in NetworkingBase.
 * 
 * Implementation:
 * To create a bot with a GUI, make a class that extends BotPanel.
 * In the class, make a method called code(). This is where your bot code should be.
 * In BotFrame, make myPanel an instance of your bot class.
 * 
 * To make a bot without a GUI, check out GenericBot.
 * 
 * @author: ErnieParke/Choco31415
 */
public abstract class BotPanel extends GenericBot implements ActionListener {

	protected static final long serialVersionUID = 1L;
	
	//Bot and GUI preferences
	protected String panelName = "Bot Panel";
	
	protected final int WIDTH = 700;//GUI width
	protected final int HEIGHT = 335;//GUI height

	protected String botUsername = "";
	private String botPassword;//Never, never, NEVER store your password in the code.
	
	protected int maxConsoleLineSize = 80;//The maximum line length of the console box.
    protected int maxProposedEdits = -1;//The largest number of commands proposed per "run". -1 for no max.
	protected int waitTimeBetweenProposedCommands = 12;//Minimum time between proposed commands.
	
	//GUI stuff
	protected JMenuBar menuBar;
	protected JButton printLogButton;
	protected JButton runButton;
	protected JButton pushButton;
	protected JButton exitButton;
	protected JTextArea console;
	protected JList<String> proposedCommandsList;
	protected ArrayList<APIcommand> proposedCommands = new ArrayList<APIcommand>();
	protected JList<String> acceptedCommandsList;
	protected ArrayList<APIcommand> acceptedCommands = new ArrayList<APIcommand>();
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
	
    //More GUI stuff
	protected boolean pushingCommands = false;//True if paused or not.
	protected boolean pausedPushing = false;
    
	protected SwingWorker<Void, Void> pushWorker;//This allows multiple tasks to happen concurrently.
	
	protected boolean firstConsoleMessage = true;//Used by the console.
	
	public BotPanel(String family_, String homeLanguage_) {
		super(family_, homeLanguage_);
		
		setSize(WIDTH, HEIGHT);
		
	    // Construct the various GUI components
		printLogButton = createPlainJButton("Export Log");
	    runButton = createPlainJButton("Run");
	    pushButton = createPlainJButton("Push Commands");
	    exitButton = createPlainJButton("Exit");
	    
	    console = new JTextArea();
	    AbstractDocument doc = (AbstractDocument) console.getDocument();
	    doc.setDocumentFilter(new DocumentSizeFilter(maxConsoleLineSize, console));
	    
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
	    menuBar.add(printLogButton);
	    menuBar.add(runButton);
	    menuBar.add(pushButton);
	    menuBar.add(Box.createHorizontalGlue());
	    menuBar.add(exitButton);
	    
	    Box commandProcessingBar =Box.createHorizontalBox();
	    commandProcessingBar.add(exportEditsButton);
	    commandProcessingBar.add(removeButton);
	    commandProcessingBar.add(acceptButton);
	    commandProcessingBar.add(acceptAllButton);
	    commandProcessingBar.add(Box.createHorizontalStrut(WIDTH-400));
	    
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
	    SpringLayout layout = new SpringLayout();
        setLayout(layout);
        
        add(consolePane);
	    
	    add(proposedCommandsPane);
	    add(acceptedCommandsPane);
	    
	    add(commandProcessingBar);
	    
	    add(wikiTablePane);
	    add(bottomBar);
	    
	    //Control a few rendering aspects.
	    this.setComponentZOrder(commandProcessingBar, 1);
	    this.setComponentZOrder(bottomBar, 0);
        
        /*
         * Constraints n' stuff
         */
	    //Top half
	    layout.putConstraint(SpringLayout.WEST, proposedCommandsPane,
	    		0,
	    		SpringLayout.WEST, this);
	    layout.putConstraint(SpringLayout.NORTH, proposedCommandsPane,
	    		0,
	    		SpringLayout.NORTH, this);
	    
	    layout.putConstraint(SpringLayout.WEST, acceptedCommandsPane,
	    		0,
	    		SpringLayout.EAST, proposedCommandsPane);
	    layout.putConstraint(SpringLayout.NORTH, acceptedCommandsPane,
	    		0,
	    		SpringLayout.NORTH, proposedCommandsPane);
	    
	    layout.putConstraint(SpringLayout.EAST, commandProcessingBar,
	    		0,
	    		SpringLayout.EAST, this);
	    layout.putConstraint(SpringLayout.NORTH, commandProcessingBar,
	    		-1,
	    		SpringLayout.SOUTH, proposedCommandsPane);
	    
	    //Bottom half
	    layout.putConstraint(SpringLayout.NORTH, consolePane,
                0,
                SpringLayout.SOUTH, commandProcessingBar);
        layout.putConstraint(SpringLayout.WEST, consolePane,
                0,
                SpringLayout.WEST, this);
	    
        layout.putConstraint(SpringLayout.VERTICAL_CENTER, wikiTablePane,
                0,
                SpringLayout.VERTICAL_CENTER, consolePane);
        layout.putConstraint(SpringLayout.EAST, wikiTablePane,
                0,
                SpringLayout.EAST, this);
        
        layout.putConstraint(SpringLayout.WEST, bottomBar,
                0,
                SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, bottomBar,
               -1,
                SpringLayout.SOUTH, wikiTablePane);
	    
	    //Make sure everything renders.
	    validate();
	    repaint();
	}
	
	/**
	 * @param edit The edit being proposed.
	 * @param displayedAction A very brief description of the edit. For example "Editing Scratch".
	 */
	public void proposeCommand(APIcommand edit) {
		proposeEdit(edit);
	}
	
	/**
	 * @param edit The APIcommand that you are proposing.
	 * @param displayedAction A very short command summary. It should preferably be one or two words at most. It is used in the graphical edit lists. 
	 */
	public void proposeEdit(APIcommand edit) {
		if (!proposedCommands.contains(edit) && !acceptedCommands.contains(edit) && (proposedCommands.size() < maxProposedEdits || maxProposedEdits <= -1)) {
			proposedCommands.add(0, edit);
			DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
			dm.add(0, edit.getCommandName() + " at: " + edit.getPageLocation().getLanguage() + ": " + edit.getPageLocation().getTitle());
			
			validate();
			repaint();
		}
	}
	
	public int getWidth() { return WIDTH; }
	public int getHeight() { return HEIGHT; }
	public String getPanelName() { return panelName; }
	
	/**
	 * Put all bot code in this method.
	 */
	public abstract void code();
	
	/*
	 * <notice>
	 * 
	 * 
	 * Below is GUI code. Unless you are an advanced user, you can safely ignore the code below.
	 * 
	 * 
	 * </notice>
	 */
	
	/*
	 * Status/log methods
	 */
	
	protected void setStatus(String status) {
		statusLabel.setText(status);
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
	
	@Override
	public boolean log(Level level, String line) {
		boolean toReturn = super.log(level, line);
		
		if (toReturn) {
			printToConsole(getNewestLoggerLine());
		}
		
		return toReturn;
	}
	
	/*
	 * GUI methods
	 */
	
	/**
	 * Set the JFrame's menu bar.
	 * @param frame The JFrame.
	 */
	public void setUpJMenuBar(JFrame frame) {
		frame.setJMenuBar(menuBar);
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
	 * Proposed/accepted commands methods
	 */
	
	/**
	 * This methods makes a swing worker to run the bot code, so the GUI does not freeze up.
	 */
	public void runCode() {
		if (proposedCommands.size() < maxProposedEdits || maxProposedEdits <= -1) {
			
		    SwingWorker<Void, Void> runWorker = new SwingWorker<Void, Void>() {
		        @Override
		        public Void doInBackground() {
		        	try {
		        		logInfo("Running code.");
		        		code();
		        	} catch (Throwable e) {
		        		logError(e.getMessage());
		        		e.printStackTrace();
		        	}
		        	
		            return null;
		        }

		        @Override
		        protected void done() {
					logInfo("Done running.");
					
					validate();
		        }
		    };
		    //code();
		    
		    runWorker.execute();
		} else {
			logInfo("To continue, review some proposed edits.");
		}
	}

	/**
	 * This method creates a swing worker to push commands, so the GUI does not freeze up.
	 */
	public void pushCommands() {		
		
		if (acceptedCommands.size() > 0) {

		    pushWorker = new SwingWorker<Void, Void>() {
		        @Override
		        public Void doInBackground() {
				    pushButton.setText("Pause Push");
				    pushingCommands = true;
				    pausedPushing = false;
		        	pushCommandsSwing();
					
		        	return null;
		        }

		        @Override
		        protected void done() {
		        	logInfo("Done pushing commands.");
		    		setStatus("Done pushing commands.");
		    		
					pushingCommands = false;
				    pushButton.setText("Push Commands");
		        }
		    };

		    pushWorker.execute();
		} else {
			logInfo("No accepted commands detected.");
		}
	}
	
	/**
	 * This method is where commands are actually executed.
	 */
	public void pushCommandsSwing() {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedCommandsList.getModel();
		int waitTime;
	    
		int numCommands = acceptedCommands.size();
		logInfo("Pushing " + acceptedCommands.size() + " commands. Please wait.");
		
		//Push the commands!
		for (int i = acceptedCommands.size()-1; i >= 0; i--) {
			//Create a wait message.
			waitTime = waitTimeBetweenProposedCommands*(i);
			String timeMessage = "";
			if (waitTime >= 3600)
				timeMessage += waitTime/3600 + " hr, ";
			if (waitTime >= 60)
				timeMessage += (waitTime/60)%60 + " min, ";
			timeMessage += waitTime%60 + " sec";
			
			String baseMessage = "Pushing edit: " + (numCommands - i) + "/" + numCommands + " | Time left: " + timeMessage;
			
			//Try pushing a command.
			try {
				logFinest("Pushing edit.");
				APIcommand(acceptedCommands.get(i));
				acceptedCommands.remove(i);
				dm.remove(i);

				//Log info.
				if (i%10 == 0) {
					logInfo(baseMessage + " | Waiting " + waitTimeBetweenProposedCommands + " sec.");
				}
				
				setStatus(baseMessage + " | Waiting " + waitTimeBetweenProposedCommands + " sec.");
			} catch (Error e) {
				//Fail.
				e.printStackTrace();
				logError(e.getClass().getName());
			}
			
			if (i != 0) {
				//Are we paused?
				if (!pausedPushing) {
					//Wait a little between each command.
					for (int time = 0; time < waitTimeBetweenProposedCommands && !pausedPushing; time++) {
						sleepInSeconds(1);
					}
				}
				
				if (pausedPushing) {
					//Please wait while we pause.
					setStatus(baseMessage + " | Paused.");
					
					do { 
						sleepInSeconds(1);
					} while (pausedPushing);
				}
			}
		}
		
		//The push finish code should be in the swing worker.
	}
	
	/**
	 * Remove the selected proposed and accepted commands.
	 * 
	 * This method handles GUI overhead and management.
	 * 
	 * @param index The command to remove.
	 */
	public void removeSelectedCommands() {
		int index = proposedCommandsList.getSelectedIndex();
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
		while (index > -1) {
			dm.remove(index);
			proposedCommands.remove(index);
			index = proposedCommandsList.getSelectedIndex();
		}
		
		index = acceptedCommandsList.getSelectedIndex();
		dm = (DefaultListModel<String>) acceptedCommandsList.getModel();
		while (index > -1 ) {
			dm.remove(index);
			acceptedCommands.remove(index);
			index = acceptedCommandsList.getSelectedIndex();
		}
	}
	
	/**
	 * Remove an accepted command, as if it were successfully pushed.
	 * 
	 * This method handles GUI overhead and management.
	 * 
	 * @param index The command to remove.
	 */
	public void removeAcceptedCommand(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedCommandsList.getModel();
		if (index > -1 && index < dm.size()) {
			dm.remove(index);
			acceptedCommands.remove(index);
		}
	}
	
	/**
	 * Move a proposed command to the accepted command list.
	 * 
	 * This method handles GUI overhead and management.
	 * 
	 * @param index The command to accept.
	 */
	public void acceptCommand(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
		if (index > -1 && index < dm.size()) {
			DefaultListModel<String> dm2 = (DefaultListModel<String>) acceptedCommandsList.getModel();
			dm2.add(0, dm.get(index));
			acceptedCommands.add(0, proposedCommands.get(index));
			dm.remove(index);
			proposedCommands.remove(index);
		}
	}
	
	/*
	 * Button methods
	 */

	/**
	 * All buttons, when clicked, call this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == logInSelectedButton || e.getSource() == logInAllButton || e.getSource() == logInHomeButton) {
			//Build window.
		    JPasswordField passwordField = new JPasswordField(24);//24 is the width, in columns, of the password field.
		    JLabel label = new JLabel("Enter bot password:");
		    Box box = Box.createHorizontalBox();
		    box.add(label);
		    box.add(passwordField);
		    
		    //Show window asking for password.
			int button = JOptionPane.showConfirmDialog(null, box, "Password:", JOptionPane.OK_CANCEL_OPTION);
			
			//What button was pressed?
			if (button == JOptionPane.OK_OPTION) {
				botPassword = new String(passwordField.getPassword());
			} else {
				return;
			}
		}
		if (e.getSource() == logInButton) {
			logInMenu.show(logInButton, 0, logInButton.getHeight());
		} else if (e.getSource() == logInHomeButton) {
			logInAtHome();
		} else if (e.getSource() == logInSelectedButton) {
			logInAtSelected();
		} else if (e.getSource() == logInAllButton) {
			logInEverywhere();
		} else if (e.getSource() == exitButton){
			System.exit(0);
		} else if (e.getSource() == printLogButton) {
			String log = exportLog();
			FileUtils.writeFile(log, "Log.txt");
		} else if (e.getSource() == exportEditsButton) {
			String temp = "***Proposed Edits***";
			for (APIcommand et : proposedCommands) {
				temp += et.getSummary() + "\n";
			}
			temp += "\n***Accepted Edits***";
			for (APIcommand et : acceptedCommands) {
				temp += et.getSummary() + "\n";
			}
			FileUtils.writeFile(temp, "Proposed and Accepted Edits.txt");
			logInfo("Edits exported.");
		} else if (e.getSource() == removeButton) {
			if (pushingCommands) {
				logError("Please wait for edits to finish pushing.");
			} else {
				removeSelectedCommands();
			}
		}else if (e.getSource() == acceptButton) {
			if (!pushingCommands) {
				int index = proposedCommandsList.getSelectedIndex();
				while (index != -1) {
					acceptCommand(index);
					index = proposedCommandsList.getSelectedIndex();
				}
			} else {
				logError("Please wait for the current edits to finish.");
			}
		} else if (e.getSource() == acceptAllButton) {
			if (!pushingCommands) {
				DefaultListModel<String> dm = (DefaultListModel<String>) proposedCommandsList.getModel();
				for (int i = dm.size(); i > -1; i--) {
					acceptCommand(i);
				}
			} else {
				logError("Please wait for the current edits to finish.");
			}
		} else if (e.getSource() == runButton) {
			if (pushingCommands) {
				logError("Please wait for edits to finish pushing.");
			} else {
				runCode();
			}
		} else if (e.getSource() == pushButton){
			if (!pushingCommands) {
				if (loggedInAtLanguages.size() != 0) {
					pushCommands();
				} else {
					logWarning("Please log in to push commands.");
				}
			} else {
				pausedPushing = !pausedPushing;
				if (pausedPushing) {
					pushButton.setText("Resume Push");
				} else {
					pushButton.setText("Pause Push");
				}
			}
		}
	}
	
	/*
	 * Logging in methods for GUI only.
	 */
	
	public void logInAtHome() {
		ArrayList<String> languageCodes = new ArrayList<String>();
		languageCodes.add(homeWikiLanguage);
		logInAt(languageCodes);
	}
	
	public void logInAtSelected() {
		ArrayList<String> languageCodes = new ArrayList<String>();
		int[] index = wikiTable.getSelectedRows();
		for (int i : index) {
			languageCodes.add(mdm.getWikiPrefix(i));
		}
		logInAt(languageCodes);
	}
	
	public void logInEverywhere() {
		logInfo("Attempting login everywhere.");
		logInAt(mdm.getWikiPrefixes());
	}
	
	private void logInAt(final ArrayList<String> languageCodes) {
	    pushWorker = new SwingWorker<Void, Void>() {
	        @Override
	        public Void doInBackground() {
			    for (String languageCode : languageCodes) {
			    	logInAt(languageCode);
			    }
			    
			    return null;
	        }

	        @Override
	        protected void done() {

	        }
	    };

	    pushWorker.execute();
	}
	
	private void logInAt(String languageCode) {
		boolean success = logIn(new User(languageCode, botUsername), botPassword);
		
		if (success) {
			logInfo("Logged in at: " + languageCode);
			
			//Update the GUI
			TableModel model = wikiTable.getModel();
			model.setValueAt(true, mdm.getWikiPrefixes().indexOf(languageCode), 1);
		} else {
			logWarning("Log in failed at: " + languageCode);
		}
	}
}