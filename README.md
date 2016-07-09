## Description

JavaMediawikiBot (JMB) is a bot designed to interface with MediaWiki (MW).
It communicates with MediaWiki through the web API. JMB also optionally parses
MW pages into its various core components.

## Set Up

To set up JavaMediawikiBot for an IDE, run `./gradlew eclipse` or `./gradlew idea`.

### Making a Wiki Family

A wiki family is a group of wikis that a bot can see and edit. It also
contains information pertinent to the wikis.

To make a new wiki family, simply run `src/WikiBot/MediawikiData/FamilyGenerator.java'.
This can be done via your IDE (console required), or through the command line:

`./gradlew run -Pmain=WikiBot.MediawikiData.FamilyGenerator -q`

### Making a Bot

To create a Mediawiki bot, create a Java class. It will need to extend either one of
these two classes:

`src/WikiBot/core/BotPanel.java`

* Extending from this class gives access to a GUI as well as better control over edits
to a wiki. Please note, you will have to update `core/BotFrame.java` for your bot to run.
Your bot's main method will also be in `core/BotFrame.java`. All bot code should be
in the bot class under the required method named `code()`.

`src/WikiBot/core/GenericBot.java`

* Extending this class does not give a GUI. You will have to create
a main method to run bot code.

### Examples

One example bot is included in the project, and it is called `InterwikiBot.java`.

## Project Tree

Useful bot methods and bot settings may be found in various places.

`src/WikiBot/core/GenericBot.java`

* For bot methods.

`src/WikiBot/core/BotPanel.java`

* For GUI methods.

`src/WikiBot/core/NetworkingBase.java`

* For logger methods.

JavaMediawikiBot uses several classes to store data. A few classes that store general information include:

`src/WikiBot/ContentRep/Revision.java`

* This class stores information on page revisions.

`src/WikiBot/ContentRep/ImageInfo.java`

* This class stores information on images. This might or might not include: direct url, dimension, size, ect...

`src/WikiBot/ContentRep/PageLocation.java`

* This class stores information on how to find a page. Spefically, it contains the page title and wiki.

## Tips

Coding tips:
* To propose an edit, use `proposeEdit(APIcommand command, String editSummary)`. This requires the GUI.
* To automatically push a command, use `APIcommand(APIcommand command)`.
