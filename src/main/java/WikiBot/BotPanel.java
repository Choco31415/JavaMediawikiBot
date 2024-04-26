package WikiBot;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.Box;

import APIcommands.APIcommand;
import Content.User;
import Utils.FileUtils;

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
public class BotPanel {
	
	private static final long serialVersionUID = 1L;
	
	// The bot
	private MediawikiBot bot;
	
	// The GUI
	private BotView view;
	
	// Adjustable preferences
    private int maxProposedEdits = -1;//The largest number of commands proposed per "run". -1 for no max.
	private int waitTimeBetweenProposedCommands = 12;//Minimum time between proposed commands.
	private File logFile = new File("Log.txt");
	private File editsFile = new File("Proposed and Accepted Edits.txt");
	
	//Account info
	private String botUsername = "";
	private String botPassword;//Never, never, NEVER store your password in the code.
	
    //More GUI stuff
	private boolean pushingCommands = false;//True if paused or not.
	private boolean pausedPushing = false;
	
	private ArrayList<APIcommand> proposedCommands = new ArrayList<APIcommand>();
	private ArrayList<APIcommand> acceptedCommands = new ArrayList<APIcommand>();
    
	private SwingWorker<Void, Void> pushWorker;//This allows multiple tasks to happen concurrently.
	
	public BotPanel(String panelName_, MediawikiBot bot_) {
		bot = bot_;
		
		view = new BotView(panelName_, bot, this);
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
		if (!proposedCommands.contains(edit) && !acceptedCommands.contains(edit) && (proposedCommands.size() < maxProposedEdits || maxProposedEdits <= 0)) {
			proposedCommands.add(0, edit);
			String summary = edit.getCommandName() + " at: " + edit.getPageLocation().getLanguage() + ": " + edit.getPageLocation().getTitle();
			view.insertToProposed(0, summary);
			
			validate();
			repaint();
		}
	}
	
	
	/**
	 * Set the panel's name.
	 * 
	 * @param name The new name.
	 */
	public void setPanelName(String name) {
		view.setTitle(name);
	}
	
	/*
	 * <notice>
	 * 
	 * 
	 * Below is GUI code. Unless you are an advanced user, you can safely ignore the code below.
	 * 
	 * 
	 * </notice>
	 */
	
	/**
	 * Print the log to the {@code logFile}.
	 */
	public void displayLog() {
		//TODO: Figure out how to fetch log data from log4j.
		//FileUtils.writeFile(log, logFile.getAbsolutePath());
	}
	
	/*
	 * Proposed/accepted commands methods
	 */
	
	/**
	 * This methods makes a swing worker to run the bot code, so the GUI does not freeze up.
	 */
	public void runCode() {
		if (pushingCommands) {
			bot.logWarning("Please wait for edits to finish pushing.");
		} else {
			if (proposedCommands.size() >= maxProposedEdits && maxProposedEdits > 0) {
				bot.logInfo("To continue, please review some proposed edits.");
			} else {	
			    SwingWorker<Void, Void> runWorker = new SwingWorker<Void, Void>() {
			        @Override
			        public Void doInBackground() {
			        	try {
			        		bot.logInfo("Running code.");
			        		// code(); TODO: Figure out new setup
			        	} catch (Throwable e) {
			        		bot.logError(e.getMessage());
			        		e.printStackTrace();
			        	}
			        	
			            return null;
			        }

			        @Override
			        protected void done() {
						bot.logInfo("Done running.");
						
						validate();
			        }
			    };
			    //code();
			    
			    runWorker.execute();
			}
		}
	}
	
	/**
	 * Handle for the GUI push button.
	 */
	public void pushButton() {
		if (!pushingCommands) {
			if (bot.loggedInAtLanguages.size() != 0) {
				pushCommands();
			} else {
				bot.logWarning("Please log in to push commands.");
			}
		} else {
			pausedPushing = !pausedPushing;
			if (pausedPushing) {
				view.setPushButtonText("Resume Push");
			} else {
				view.setPushButtonText("Pause Push");
			}
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
				    view.setPushButtonText("Pause Push");
				    pushingCommands = true;
				    pausedPushing = false;
		        	pushCommandsSwing();
					
		        	return null;
		        }

		        @Override
		        protected void done() {
		        	bot.logInfo("Done pushing commands.");
		    		view.setStatus("Done pushing commands.");
		    		
					pushingCommands = false;
				    view.setPushButtonText("Push Commands");
		        }
		    };

		    pushWorker.execute();
		} else {
			bot.logInfo("All work is done.");
		}
	}
	
	/**
	 * This method is where commands are actually executed.
	 */
	public void pushCommandsSwing() {
		int waitTime;
	    
		int numCommands = acceptedCommands.size();
		bot.logInfo("Pushing " + acceptedCommands.size() + " commands. Please wait.");
		
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
				bot.logDebug("Pushing edit.");
				bot.APIcommand(acceptedCommands.get(i));
				acceptedCommands.remove(i);
				view.removeAccepted(i);

				//Log info.
				if (i%10 == 0) {
					bot.logInfo(baseMessage + " | Waiting " + waitTimeBetweenProposedCommands + " sec.");
				}
				
				view.setStatus(baseMessage + " | Waiting " + waitTimeBetweenProposedCommands + " sec.");
			} catch (Error e) {
				//Fail.
				e.printStackTrace();
				bot.logError(e.getClass().getName());
			}
			
			if (i != 0) {
				//Are we paused?
				if (!pausedPushing) {
					//Wait a little between each command.
					for (int time = 0; time < waitTimeBetweenProposedCommands && !pausedPushing; time++) {
						bot.sleepInSeconds(1);
					}
				}
				
				if (pausedPushing) {
					//Please wait while we pause.
					view.setStatus(baseMessage + " | Paused.");
					
					do { 
						bot.sleepInSeconds(1);
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
		if (pushingCommands) {
			bot.logError("Please wait for edits to finish pushing.");
		} else {
			int index = view.getSelectedProposedIndex();
			while (index > -1) {
				view.removeProposed(index);
				proposedCommands.remove(index);
				index = view.getSelectedProposedIndex();
			}
			
			index = view.getSelectedAcceptedIndex();
			while (index > -1 ) {
				view.removeAccepted(index);
				acceptedCommands.remove(index);
				index = view.getSelectedAcceptedIndex();
			}
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
		if (index > -1 && index < acceptedCommands.size()) {
			view.removeAccepted(index);
			acceptedCommands.remove(index);
		}
	}
	
	/**
	 * Accept all selected proposed commands, hence moving them to the accepted list.
	 */
	public void acceptSelectedCommands() {
		if (!pushingCommands) {
			int index = view.getSelectedProposedIndex();
			while (index != -1) {
				acceptCommand(index);
				index = view.getSelectedProposedIndex();
			}
		} else {
			bot.logError("Please wait for the current edits to finish.");
		}
	}
	
	/**
	 * Accept all proposed proposed commands, hence moving them to the accepted list.
	 */
	public void acceptAllCommands() {
		if (!pushingCommands) {
			for (int i = proposedCommands.size(); i > -1; i--) {
				acceptCommand(i);
			}
		} else {
			bot.logError("Please wait for the current edits to finish.");
		}
	}
	
	/**
	 * Move a proposed command to the accepted command list.
	 * 
	 * @param index The command to accept.
	 */
	public void acceptCommand(int index) {
		if (index > -1 && index < proposedCommands.size()) {
			view.insertToAccepted(0, view.getProposed(index));
			acceptedCommands.add(0, proposedCommands.get(index));
			view.removeProposed(index);
			proposedCommands.remove(index);
		}
	}
	
	/**
	 * Export proposed and accepted edits to.
	 */
	public void exportEdits(Path outputFile) {
		String temp = "***Proposed Edits***";
		for (APIcommand et : proposedCommands) {
			temp += et.getSummary() + "\n";
		}
		temp += "\n***Accepted Edits***";
		for (APIcommand et : acceptedCommands) {
			temp += et.getSummary() + "\n";
		}
		FileUtils.writeFile(temp, outputFile.toString());
		bot.logInfo("Edits exported.");
	}
	
	/*
	 * Login methods.
	 */
	
	/**
	 * Using a pop-up window, prompt the user for a password.
	 * @return The password, or "" if the user canceled.
	 */
	public String promptForPassword() {
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
			return new String(passwordField.getPassword());
		} else {
			return "";
		}
	}
	
	/**
	 * Log in at the bot's wiki home.
	 */
	public void logInAtHome() {
		// Log in.
		ArrayList<String> languageCodes = new ArrayList<String>();
		languageCodes.add(bot.homeWikiLanguage);
		logInAt(languageCodes);
	}
	
	/**
	 * Log in at view selected wikis.
	 */
	public void logInAtSelected() {
		// Log in.
		ArrayList<String> languageCodes = view.getSelectedWikis();
		logInAt(languageCodes);
	}
	
	/**
	 * Log in to every wiki in the current family.
	 */
	public void logInEverywhere() {
		// Log in.
		bot.logInfo("Logging in to every wiki.");
		logInAt(bot.getMDM().getWikiPrefixes());
	}
	
	/**
	 * Log in at the specified wikis.
	 * 
	 * @param languageCodes An ArrayList of wiki language codes.
	 */
	private void logInAt(final ArrayList<String> languageCodes) {
		// Get password.
		botPassword = promptForPassword();
		if (botPassword.equals("")) {
			return;
		}
		
		// Log in.
	    pushWorker = new SwingWorker<Void, Void>() {
	        @Override
	        public Void doInBackground() {
	        	try {
				    for (String languageCode : languageCodes) {
				    	logInAt(languageCode);
				    }
	        	} catch (Error e) {
	        		bot.logError(e.getMessage());
	        		e.printStackTrace();
	        	}
	        	
	        	return null;
	        }

	        @Override
	        protected void done() {

	        }
	    };

	    pushWorker.execute();
	}
	
	/**
	 * Log in at the given wiki.
	 * 
	 * @param languageCode The wiki's language code.
	 */
	private void logInAt(String languageCode) {
		boolean success = bot.logIn(new User(languageCode, botUsername), botPassword);
		
		if (success) {
			bot.logInfo("Logged in at: " + languageCode);
			
			//Update the GUI
			view.setWikiLoginStatus(languageCode, true);
		} else {
			bot.logWarning("Log in failed at: " + languageCode);
		}
	}
}