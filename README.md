To set up JavaMediawikiBot for an IDE, run ./gradlew eclipse or ./gradlew idea.

To create a Mediawiki bot, create a Java class. It will need to extend either one of these two classes:

src/WikiBot/core/BotPanel.java 

Extending from this class gives access to a GUI as well as better control over edits to a wiki. Please note, you will have to edit core/BotFrame.java for your bot to run.

src/WikiBot/core/GenericBot.java

Extending this class does not give a GUI.

One example bot is included in the project, and it is called InterwikiBot.java.

Useful bot methods and bot settings may be found in src/WikiBot/core/GenericBot.java, and if using the provided GUI, src/WikiBot/core/BotPanel.java.

Coding tips:
* To propose an edit, use ProposeEdit(APIcommand command, String editSummary).
* To automatically push a command, use APIcommand(APIcommand command).
