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

JavaMediawikiBot offers two ways to accomplish what you want.

Most methods are abstractions of the Mediawiki API. Roughly half of the commands are queries.

APIcommand classes are also abstractions to the Mediawiki API, and are meant to be used as Objects. For example:

```
APIcommand command = new AppendText(loc, "\n[[de:Scratch Katze]]", "This page needs an interwiki! ");
APIcommand(command); // Push the command now.
```

Half of the API commands are recommended used raw, half are not. This can be checked in the command's class documentation. If not recommended used raw, use GenericBot methods instead.

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