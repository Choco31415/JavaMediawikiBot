package WikiBot;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.text.WordUtils;

import WikiBot.APIcommands.*;
import WikiBot.ContentRep.*;
import WikiBot.Core.BotPanel;
import WikiBot.MediawikiData.VersionNumber;

@SuppressWarnings("unused")
public class InterwikiBot extends BotPanel {
	
	private static final long serialVersionUID = 1L;

	/*
	 * This is where I initialize my custom Mediawiki bot.
	 */
	public InterwikiBot() {
		super("Test", "en");
		
		//Preferences
		panelName = "InterwikiBot";
		
		botUsername = "InterwikiBot";
		
		APIlimit = 30;
		revisionDepth = 5;
		
		APIthrottle = 0.5;//Minimum time between any API commands.
		waitTimeBetweenProposedCommands = 12;//Minimum time between edits.
		
		setLoggerLevel(Level.INFO);//How fine should the logger be? Visit NetworkingBase.java for logger level info.
	}
	
	/*
	 * This is where I put my bot code.
	 */
	@Override
	public void code() {
		proposeEdit(new Rollback(new PageLocation("bleh", "homeEn"), "ErnieParke", "Catching the vandal. Finally!"));
		
	}
}