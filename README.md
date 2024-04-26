[![Discord Profile](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://www.discordapp.com/users/244908008155512832)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

## Description

JavaMediawikiBot is a library used for writing Mediawiki bots in Java.

The library comes with experimental support for page parsing of templates, links, and more.

## Setup

To create a bot, first create a GenericBot instance:

```
import JavaMediawikiBot.GenericBot;

public static void main(String[] args) {
	GenericBot bot = new GenericBot(new File("resources/mediawikiFamily.txt"), "en");
}
```

GenericBot takes two parameters on instantiation. The first parameter points to a Mediawiki Family file, and the second parameter specifies the home family for the bot.

### Mediawiki Family File

A Mediawiki Family file is a file containing all the information about the wikis a bot might be working with. This includes the url, abbreviation, and more.

To generate a Mediawiki Family file, or borrow a pre generated family, please see the [JMB Family Generator repository](https://github.com/Choco31415/JMBFamilyGenerator).

## Coding a Bot

A GenericBot supports two different sets of commands: queries and actions.

Querying a Mediawiki site can be done through methods provided in the GenericBot class. A variety of methods are available.

Below is an example command to get the "Scratch Cat" page:

```
PageLocation catPage = new PageLocation("en", "Scratch Cat");
WikiPage page = bot.getWikiPage(cat, false);
```

Editing or changing a Mediawiki site can be done through the APIcommand objects. The proper way to do so is to make an APIcommand object and to commit with the `APICommand()` command.

Below is an example command to append text to the "Scratch Cat" page:

```
APIcommand command = new AppendText(catPage, "\n[[de:Scratch Katze]]", "This page needs an interwiki! ");
APIcommand(command); // Run the command now!
```

The two exceptions to the above project structure are site logins and file uploads. Methods for both are located directly in GenericBot:

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
bot.displayGUI();
```

It will look like this:

[] #TODO Add image.

## Building from Source

To build the project from source, run the following bash command:

```
./gradlew build
```

## Contributing

Want to contribute? Feel free to submit a pull request or open an issue.