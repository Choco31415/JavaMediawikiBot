package WikiBot.Core;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.swing.Box;

import WikiBot.APIcommands.APIcommand;
import WikiBot.Content.Interwiki;
import WikiBot.Content.Page;
import WikiBot.Content.PageLocation;
import WikiBot.Content.Template;

public abstract class BotPanel extends GenericBot implements ActionListener, Runnable {

	protected static final long serialVersionUID = 1L;
	
	protected String panelName = "Bot Panel";
	
	protected final int WIDTH = 600;
	protected final int HEIGHT = 550;
	
	protected Button logInButton;
	protected Button logInAllButton;
	protected Button runButton;
	protected Button pushButton;
	protected Button exitButton;
	protected Label consoleBox;
	protected static Label statusBox;
	protected Button printLogButton;
	protected static Label errorBox;
	protected static Label changesBox;
	protected JList<String> proposedList;
	protected ArrayList<APIcommand> proposedEdits = new ArrayList<APIcommand>();
	protected JList<String> acceptedList;
	protected ArrayList<APIcommand> acceptedEdits = new ArrayList<APIcommand>();
	protected Button exportEditsButton;
	protected Button removeButton;
	protected Button acceptButton;
	protected Button acceptAllButton;
	protected static Label infoBox;
	protected List interwikiList;
    
    protected String myWikiLanguage = "en";
    protected int maxProposedEdits = -1;//The largest number of changes proposed per "run".
    
    protected String family = "";//The wiki family.
	protected String botUsername = "";
	protected String botPassword = "";
	protected boolean loggedIn = false;
	protected int waitTimeBetweenEdits = 12;
	protected double statusUpdateWaitTime = 0.05;
    
    Thread clockThread;
	protected boolean pushingChanges = false;//True if paused or not.
	protected boolean pausedPushing = false;
	private int errorCounter = 0;
	private int errorMessageLifeSpan = 5;
	
	ArrayList<String> loggedInAt = new ArrayList<String>();
	
	SwingWorker<Void, Void> pushWorker;
	
	public BotPanel(String family_) {
		family = family_;
		
	    if (clockThread == null) {
	        clockThread = new Thread(this, "Clock");
	        clockThread.start();
	     }
		
		setSize(WIDTH, HEIGHT);
		
		setPPD(true);
		
	    // Set up in interwikis
		ArrayList<ArrayList<String>> temp = readFamily(family, 0);
		Interwiki = temp.get(0);
		InterwikiURL = temp.get(1);
		
	    // Construct the button
		logInButton = new Button("Log In");
		logInAllButton = new Button("Log In All");
	    runButton = new Button("Run");
	    pushButton = new Button("Push Changes");
	    exitButton = new Button("Exit");
	    consoleBox = new Label("ConsoleBox");
	    statusBox = new Label("...");
	    printLogButton = new Button("Print Log");
	    errorBox = new Label("...");
	    changesBox = new Label("Proposed Edits  ||  Accepted Edits");

	    DefaultListModel<String> proposedChanges = new DefaultListModel<>();
	    proposedList = new JList<String>(proposedChanges);
	    JScrollPane proposedScroller = new JScrollPane(proposedList);
	    proposedScroller.setPreferredSize(new Dimension(290, 150));
	    
	    DefaultListModel<String> acceptedChanges = new DefaultListModel<>();
	    acceptedList = new JList<String>(acceptedChanges);
	    JScrollPane acceptedScroller = new JScrollPane(acceptedList);
	    acceptedScroller.setPreferredSize(new Dimension(290, 150));

	    exportEditsButton = new Button("Export Edits");
	    removeButton = new Button("Remove");
	    acceptButton = new Button("Accept");
	    acceptAllButton = new Button("Accept All");
	    infoBox = new Label("The interwikis supported are:");
	    
	    interwikiList = new List(5, true);
	    for (String iw : Interwiki) {
	    	interwikiList.add(iw); 	
	    }
	    
	    // button should be handled by a new BeepAction object
	    logInButton.addActionListener(this);
	    logInAllButton.addActionListener(this);
	    runButton.addActionListener(this);
	    pushButton.addActionListener(this);
	    printLogButton.addActionListener(this);
	    exitButton.addActionListener(this);
	    exportEditsButton.addActionListener(this);
	    removeButton.addActionListener(this);
	    acceptButton.addActionListener(this);
	    acceptAllButton.addActionListener(this);

        setLayout(new FlowLayout());

        add(logInButton);
        add(logInAllButton);
	    add(printLogButton);
        add(runButton);
        add(pushButton);
        add(exitButton);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(consoleBox);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(statusBox);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(errorBox);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(changesBox);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(proposedScroller);
	    add(acceptedScroller);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(exportEditsButton);
	    add(removeButton);
	    add(acceptButton);
	    add(acceptAllButton);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(infoBox);
	    add(Box.createRigidArea(new Dimension(WIDTH,0)));
	    add(interwikiList);
	    
	    validate();
	    repaint();
	}
	
	public void runCode() {
		if (proposedEdits.size() < maxProposedEdits || maxProposedEdits <= -1) {
			
		    SwingWorker<Void, Void> runWorker = new SwingWorker<Void, Void>() {
		        @Override
		        public Void doInBackground() {
		        	//code();
		        	
		            return null;
		        }

		        @Override
		        protected void done() {
					setConsoleText("Done");
					
					validate();
		        }
		    };
		    
		    code();
		    runWorker.execute();
		} else {
			setConsoleText("To continue, review some proposed edits.");
		}
	}
	
	/**
	 * Put all bot code in this method.
	 */
	public abstract void code();
	
	 public void run() {
	      Thread myThread = Thread.currentThread();
	        while (clockThread == myThread) {
	            sleep(statusUpdateWaitTime);
	            updateGUI();
	        }
	    }
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		consoleBox.paint(g);
	}
	
	public void updateGUI() {
		if (log.size() > 0) {
			statusBox.setText(log.get(log.size()-1) + " (" + log.size() + ")");
		}
		
		errorCounter++;
		if (errorCounter > 10*errorMessageLifeSpan) {
			errorBox.setText("Number of Errors: " + numErrors);
		}
		
		validate();
	}
	
	public void pushChanges() {		
		
		if (acceptedEdits.size() > 0) {

		    pushWorker = new SwingWorker<Void, Void>() {
		        @Override
		        public Void doInBackground() {
				    pushButton.setLabel("Pause Push");
				    pushingChanges = true;
				    pausedPushing = false;
		        	pushChanges2();
					
		        	return null;
		        }

		        @Override
		        protected void done() {
					setConsoleText("Changes pushed.");
					pushingChanges = false;
				    pushButton.setLabel("Push Changes");
		        }
		    };

		    pushWorker.execute();
		    //pushChanges2();
		} else {
			setConsoleText("No accepted edits detected.");
		}
	}
	
	public void pushChanges2() {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedList.getModel();
		int waitTime;
	    
		int numChanges = acceptedEdits.size();
		//Push the changes!
		for (int i = acceptedEdits.size()-1; i >= 0; i--) {
			//Create the wait message.
			waitTime = waitTimeBetweenEdits*(i);
			String timeMessage = "";
			if (waitTime >= 3600)
				timeMessage += waitTime/3600 + " hr, ";
			if (waitTime >= 60)
				timeMessage += (waitTime/60)%60 + " min, ";
			timeMessage += waitTime%60 + " sec";
			
			String baseMessage = "Pushing edit: " + (numChanges - i) + "/" + numChanges + " | Time left: " + timeMessage;
			
			try {
				setConsoleText(baseMessage + " | Pushing.");
				APIcommand(acceptedEdits.get(i));
				acceptedEdits.remove(i);
				dm.remove(i);
				setConsoleText(baseMessage + " | Waiting " + waitTimeBetweenEdits + " sec.");
			} catch (Error e) {
				e.printStackTrace();
				logError(e.getClass().getName());
			}
			if (i != 0) {
				//Are we paused?
				if (!pausedPushing) {
					for (int time = 0; time < waitTimeBetweenEdits && !pausedPushing; time++) {
						sleep(1);
					}
				}
				
				if (pausedPushing) {
					setConsoleText(baseMessage + " | Paused.");
					do { 
						sleep(1);
					} while (pausedPushing);
				}
			}
		}
	}
	
	public Page getWikiPage(PageLocation pl) {
		Page temp = super.getWikiPage(pl);
		processFurther(temp);
		return temp;
	}
	
	/**
	 * IMPORTANT: This method only accepts pages from the same wiki.
	 */
	public ArrayList<Page> getWikiPagesBatch(ArrayList<PageLocation> pls) {
		
		if (pls.size() == 0) {
			//System.out.println("um");
			throw new Error();
		}
		
		//Check that everything is from the same language.
		String wikiLang = pls.get(0).getLanguage();
		for (PageLocation pl : pls) {
			if (!pl.getLanguage().equals(wikiLang)) {
				throw new Error();
			}
		}
		
		ArrayList<Page> temp = getWikiPages(pls);
		
		for (Page pg : temp) {
			processFurther(pg);
		}
		
		return temp;
	}
	
	public void processFurther(Page pg) {
		Template temp = pg.getTemplate("en");
		if (temp != null) {
			if (temp.getParameterCount() > 0) {
				String text = temp.getParameter(0).trim();
				if (text.length() <10 || !text.substring(0,10).equalsIgnoreCase("Interwiki=")) {
					pg.addInterwiki(new Interwiki(text, "En:"));	
				}
			}
		}
	}
	
	public void writeLogFiles() {
		writeFile(concatLog(), "log.txt");
	}
	
	public void writeFile(String text, String location) {
		PrintWriter writer = null;
		try {
			System.out.println(location);
			writer = new PrintWriter(location, "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("Err1 File Not Found");
			return;
		} catch (UnsupportedEncodingException e) {
			System.out.println("Err2 Unsupported file format");
			return;
		}
		writer.write(text);
		writer.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == logInButton) {
			setConsoleText("Attempting login at " + myWikiLanguage);
			logInAt(myWikiLanguage);
			loggedIn = true;
			printLog();
		} else if (e.getSource() == logInAllButton) {
		    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
		        @Override
		        public Void doInBackground() {
		        	logInEverywhere();
					
		        	return null;
		        }

		        @Override
		        protected void done() {
		        }
		    };

		    worker.execute();
			loggedIn = true;
		}
		
		if (e.getSource() == exitButton){
			System.exit(0);
		} else if (e.getSource() == printLogButton) {
			writeFile(concatLog(), "Log.txt");
		} else if (e.getSource() == exportEditsButton) {
			String temp = "***Proposed Edits***";
			for (APIcommand et : proposedEdits) {
				temp += et.getSummary() + "\n";
			}
			temp += "\n***Accepted Edits***";
			for (APIcommand et : acceptedEdits) {
				temp += et.getSummary() + "\n";
			}
			writeFile(temp, "Proposed and Accepted Edits.txt");
			setConsoleText("Edits exported.");
		} else if (e.getSource() == removeButton) {
			if (pushingChanges) {
				setErrorText("Please wait for edits to finish pushing.");
			} else {
				removeProposedChanges();
			}
		}else if (e.getSource() == acceptButton) {
			if (!pushingChanges) {
				int index = proposedList.getSelectedIndex();
				while (index != -1) {
					acceptChange(index);
					index = proposedList.getSelectedIndex();
				}
			} else {
				setErrorText("Please wait for the current edits to finish.");
			}
		} else if (e.getSource() == acceptAllButton) {
			if (!pushingChanges) {
				DefaultListModel<String> dm = (DefaultListModel<String>) proposedList.getModel();
				for (int i = dm.size(); i > -1; i--) {
					acceptChange(i);
				}
			} else {
				setErrorText("Please wait for the current edits to finish.");
			}
		} else if (e.getSource() == runButton) {
			if (pushingChanges) {
				setErrorText("Please wait for edits to finish pushing.");
			} else {
				runCode();
			}
		} else if (e.getSource() == pushButton){
			if (!pushingChanges) {
				if (loggedIn) {
					pushChanges();
				} else {
					setConsoleText("Please log in to push changes.");
				}
			} else {
				pausedPushing = !pausedPushing;
				if (pausedPushing) {
					pushButton.setLabel("Resume Push");
				} else {
					pushButton.setLabel("Pause Push");
				}
			}
		}
	}
	
	public void logInEverywhere() {
		setConsoleText("Attempting login everywhere.");
		for (String languageCode : Interwiki) {
			logInAt(languageCode);
		}
	}
	
	public void logInAt(String languageCode) {
		//TODO: Log in at every wiki!!!

		loggedIn = logIn(botUsername, botPassword, languageCode);
		
		if (loggedIn) {
			setConsoleText("Logged in at: " + languageCode);
			
			String lan = languageCode;
			if (!loggedInAt.contains(lan)) {
				loggedInAt.add(lan);
			}
		} else {
			setConsoleText("Log in failed at: " + languageCode);
			logError("Log in failed at: " + languageCode);
		}
	}
	
	public void removeProposedChanges() {
		int index = proposedList.getSelectedIndex();
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedList.getModel();
		while (index > -1) {
			dm.remove(index);
			proposedEdits.remove(index);
			index = proposedList.getSelectedIndex();
		}
		
		index = acceptedList.getSelectedIndex();
		dm = (DefaultListModel<String>) acceptedList.getModel();
		while (index > -1 ) {
			dm.remove(index);
			acceptedEdits.remove(index);
			index = acceptedList.getSelectedIndex();
		}
	}
	
	public void pushAcceptedChange(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) acceptedList.getModel();
		if (index > -1 && index < dm.size()) {
			dm.remove(index);
			acceptedEdits.remove(index);
		}
	}
	
	public void acceptChange(int index) {
		DefaultListModel<String> dm = (DefaultListModel<String>) proposedList.getModel();
		if (index > -1 && index < dm.size()) {
			DefaultListModel<String> dm2 = (DefaultListModel<String>) acceptedList.getModel();
			dm2.add(0, dm.get(index));
			acceptedEdits.add(0, proposedEdits.get(index));
			dm.remove(index);
			proposedEdits.remove(index);
		}
	}
	
	public void proposeCommand(APIcommand edit, String displayedAction) {
		proposeEdit(edit, displayedAction);
	}
	
	/**
	 * @param edit The APIcommand that you are proposing.
	 * @param displayedAction A very short command summary. It should preferably be one or two words at most. It is used in the graphical edit lists. 
	 */
	int test = 0;
	public void proposeEdit(APIcommand edit, String displayedAction) {
		if (!proposedEdits.contains(edit) && !acceptedEdits.contains(edit) && (proposedEdits.size() < maxProposedEdits || maxProposedEdits <= -1)) {
			proposedEdits.add(0, edit);
			DefaultListModel<String> dm = (DefaultListModel<String>) proposedList.getModel();
			dm.add(0, displayedAction + " at: " + edit.getPageLocation().getLanguage() + ": " + edit.getPageLocation().getTitle());
			
			validate();
			repaint();
		}
	}
	
	/**
	 * This method returns the file as two ArrayLists of:
	 * 0 - The interwiki prefix
	 * 1 - The interwiki url
	 */
	public ArrayList<ArrayList<String>> readFamily(String family, int commentBufferLineCount) {
		ArrayList<String> lines = readFileAsList("/Families/" + family + ".txt", commentBufferLineCount, false, true);
		
		// Gather array size
		ArrayList<String> interwiki = new ArrayList<String>();
		ArrayList<String> interwikiUrl = new ArrayList<String>();
		
		for (String line : lines) {
			if (!line.equals("")) {
				int index = line.indexOf(":");
				interwiki.add(line.substring(0, index).trim());
				interwikiUrl.add(line.substring(index+1).trim());
			}
		}
		
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		temp.add(interwiki);
		temp.add(interwikiUrl);
		return temp;
	}
	
	public void setConsoleText(String text) {
		consoleBox.setText(text);
	}
	
	public void setErrorText(String text) {
		errorBox.setText(text);
		errorCounter = 0;
	}
	
	public int getWidth() { return WIDTH; }
	public int getHeight() { return HEIGHT; }
	public String getPanelName() { return panelName; }
	
	public void sleep(double time) {
		try {
			Thread.sleep((int)(1000*time));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
