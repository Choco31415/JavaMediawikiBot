[![Discord Profile](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://www.discordapp.com/users/244908008155512832)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

## Description

JavaMediawikiBot is a Java library used for writing Mediawiki bots. It supports most API requests and MW versions 1.18+. // TODO: Verify

## Setup

To create a bot, first create a GenericBot instance:

```
GenericBot bot = new GenericBot(new File("resources/mediawikiFamily.txt"), "en");
```

A GenericBot takes two parameters. The first parameter points to a Mediawiki Family file, and the second parameter specifies the home family for the bot.

### Mediawiki Family File

Called a Mediawiki Family file, this file contains information about the wikis a bot might be working with. This includes the address, abbreviation, and more.

To generate a Mediawiki Family file, or borrow a pre generated family, please visit the [JMB Family Generator repository](https://github.com/Choco31415/JMBFamilyGenerator).

## Coding a Bot

There are two main ways to perform actions with a GenericBot.

Querying a Mediawiki site is done through methods provided in the GenericBot class.

Below is an example snippet to download the "Scratch Cat" page:

```
PageLocation catPage = new PageLocation("en", "Scratch Cat");
WikiPage page = bot.getWikiPage(cat, false);
```

Editing a Mediawiki site is done through APIcommand objects. First make an APIcommand object representing the desired action, then commit it with the `APICommand()` command.

Below is an example snippet to append text to the "Scratch Cat" page:

```
APIcommand command = new AppendText(catPage, "\n[[de:Scratch Katze]]", "This page needs an interwiki!");
APIcommand(command); // Run the command!
```

The two exceptions to the overarching organization are logging in and uploading files. Both can be done with methods located in GenericBot:

```
bot.logIn(new User("en", "InterwikiBot"), "secretPassword");
bot.uploadFile(cat, new Path("cat.png"), "Replacing the page with an image.", "The new cat.");
```

### Configuration

GenericBot can be customized through several different variables. These include:

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

//TODO: Add BotPanel and PageParser configuration.

### GUI

GenericBot supports an optional GUI. To display it, run the following command:

```
bot.displayGUI("Mediawiki Bot", botCode);
```

[] #TODO Add image.

## Building from Source

The following command builds the project from source:

```
./gradlew build
```

## Contributing

Want to contribute? Every bit of help is appreciated! Just submit a pull request or open an issue.