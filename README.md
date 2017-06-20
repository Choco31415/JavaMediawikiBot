## Description

JavaMediawikiBot (JMB) is a bot designed to interface with MediaWiki (MW).
It communicates with MediaWiki through the web API. JMB also optionally parses
MW pages into its various core components.

## Set Up

To set up JavaMediawikiBot for an IDE, run `./gradlew eclipse` or `./gradlew idea`.

### Making a Wiki Family

A wiki family is a group of wikis that a bot can see and edit. It also
contains information pertinent to the wikis.

To make a new wiki family, simply run `src/WikiBot/MediawikiData/FamilyGenerator.java`.
This can be done via your IDE (console required), or through the command line:

`./gradlew run -Pmain=WikiBot.MediawikiData.FamilyGenerator -q`

### Making a Bot

To create a Mediawiki bot, create a Java class. It will need to extend either one of
these two classes:

`src/WikiBot/core/GenericBot.java`

* Extending this class gives access to a plethora of methods for interfacing with Mediawiki.

`src/WikiBot/core/BotPanel.java`

* Extending this class gives access to GenericBot's methods, plus a GUI for better control of the bot's edit flow. Please note, the bot's main method will have to instantiate itself for the GUI to appear. See `InterwikiBot.java` for an example.

When extending a class, the constructor must include either:

`
super(String family, String homeWikiLanguage)
super(File family, String homeWikiLanguage)
`

`family` specifies a wiki family file. A bot may then access the wikis in the family file. A String `family` refers to a default included wiki family, and must be "Scratch", "DwarfFortress", or "Wikipedia". A File `family` simply must point to a wiki family file.

`homeWikiLanguage` specifies the default wiki of a bot.

### Coding the bot.

Half of the API commands are recommended used raw, half are not. This can be checked in the command's class documentation.

If a command is recommended used raw, then simply run `APIcommand(APIcommand command)`.

If a command is not recommended user raw, then use GenericBot methods.

### Tips

Coding Tips:
* To queue a command for review, use `proposeEdit(APIcommand command)`. This requires the GUI.

## Examples

One example bot is included in the project, and it is called `InterwikiBot.java`.

## Contributing

To contribute, make an issue or a pull request! In each Pull Request, make sure to include the situations under which your code was tested.

### Project Tree

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