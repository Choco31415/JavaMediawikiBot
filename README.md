[![Discord Profile](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://www.discordapp.com/users/244908008155512832)

## Description

JavaMediawikiBot is a Java library meant for interfacing with the Mediawiki web API.

The library comes with experimental, if thoroughly tested, support for page parsing and editing.

## Set Up

Here is an example on how to setup a bot.

```
import JavaMediawikiBot.GenericBot;

public static void main(String[] args) {
	GenericBot bot = new GenericBot(new File("resources/mediawikiFamily.txt"), "en");
}
```

A bot takes two parameters on setup.

The first parameter specifies the mediawiki family that it will be working with.

The second parameter specifies the wiki that operations will default to.

### Mediawiki Families

A mediawiki family is a group of related wikis. It gives a bot data information the wikis so that it can work with them.

To generate a mediawiki family, or borrow a pre generated family, please see the [JMB Family Generator project](https://github.com/Choco31415/JMBFamilyGenerator).

## Coding a Bot

API commands are split into two groups. Queries and actions.

Any commands that query a Mediawiki site are a query. There are simple methods provided for querying all sorts of information from Mediawiki. These methods can be found directly within a GenericBot instance.

Below is an example query:

```
PageLocation cat = new PageLocation("en", "Scratch Cat");
WikiPage page = bot.getWikiPage(cat, false);
```

Any commands that alter a Mediawiki site are an action. The proper way to do an action are to create an object describing what you want done, then to commit it when convenient. All relevant action classes can be found within the APICommand directory.

Below is an example action:

```
APIcommand command = new AppendText(cat, "\n[[de:Scratch Katze]]", "This page needs an interwiki! ");
APIcommand(command); // Run the command now!
```

The two exceptions to the above are logins and file uploads. There are convenient methods for both located in GenericBot:

```
bot.logIn(new User("en", "InterwikiBot"), "secretPassword");
bot.uploadFile(cat, new Path("cat.png"), "Replacing the page with an image.", "The new cat.");
```

### Configuration

A bot can be customized through several different variables. These include:

```
bot.APIdelay = 0.5;
bot.queryLimit = 10;
bot.revisionLimit = 10;
bot.getRevisionContent = false;
bot.maxFileChunkSize = 20000;
bot.parseThrough = false;
bot.interruptedConnectionWait = 5;
```

The values listed above are the default values.

### GUI

JavaMediawikiBot comes with an optional GUI. To display it, run the following command:

```
bot.displayGUI();
```

A screenshot of the GUI is below.

[] #TODO Add image.

## Contributing

Do you want to contribute? Then feel free to make a pull request or open an issue!