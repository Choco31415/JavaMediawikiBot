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

The first parameter specifies the file containing the mediawiki family that it will be working with.

The second parameter specifies the wiki that operations will default to.

### Mediawiki Families

A mediawiki family is a group of related wikis. It tells a bot data about the wikis it can work with, including their abbreviation, API location, and Mediawiki version.


To generate a mediawiki family, or borrow a pre generated family, please see the [JMBFamilyGenerator project](https://github.com/Choco31415/JMBFamilyGenerator).

## Coding a Bot

API commands are split into two groups. Queries and actions.

Any commands that query a Mediawiki site are a query. Methods are provided for easily passing in request info and receiving nicely packaged info in return. These methods can be found directly within a GenericBot instance.

Below is an example query:

```
PageLocation cat = new PageLocation("en", "Scratch Cat");
WikiPage page = bot.getWikiPage(cat, false);
```

Any commands that alter a Mediawiki site are an action. Is it recommended to create an object for an action and running it when convenient. All Object classes can be found within the APICommand directory.

Below is an example action:

```
APIcommand command = new AppendText(cat, "\n[[de:Scratch Katze]]", "This page needs an interwiki! ");
APIcommand(command); // Push the command now.
```

The two exceptions to the above division are logins and file uploads. There are convenient methods for both located in GenericBot:

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

JavaMediawikiBot comes with a optional, built in GUI. To display it, run the following command:

```
bot.displayGUI(); # TODO Update
```

A screenshot of the GUI is below.

[] #TODO Add image.

## Contributing

Every contribution is welcome! If you are interested in helping out, please submit an issue or pull request.