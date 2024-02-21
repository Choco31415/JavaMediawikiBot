[![Discord Profile](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://www.discordapp.com/users/244908008155512832)

## Description

JavaMediawikiBot is a JAva library meant for interfacing with the Mediawiki web API.

The library comes with an experimental, if thoroughly tested, page parser and editor.

## Set Up

All bots requires some setup in order to run properly.

First import the JMB library:

```
import JMB # TODO
```

Then create a bot instance:

```
jmb.createInstance(); # TODO
```

All code and operations will be done through this bot instance.

Next, the bot needs to know which sites it will be working with as well as their associated data. To do that, run the following code:

```
jmb.readFamily("src/familyFile.txt"); # TODO
```

For convenience, each bot also requires that a home wiki is specified:

```
jmb.setHome("en"); # TODO
```

### Mediawiki Families

Mediawiki families provide the backbone for interacting with a group of mediawikis. At it's core, a file stores the mediawiki abbreviation, the mediawiki API location, and the mediawiki version.


To generate a mediawiki family, or borrow a pre generated family, see the [JMBFamilyGenerator repo](). # TODO Insert Link

## Coding a Bot

JavaMediawikiBot offers two ways to accomplish what you want.

Most methods are abstractions of the Mediawiki API, and are mostly for querying data.

APIcommand classes are also abstractions to the Mediawiki API, and are meant to be used as
Objects. For example:

```
APIcommand command = new AppendText(loc, "\n[[de:Scratch Katze]]", "This page needs an interwiki! ");
APIcommand(command); // Push the command now.
```

Half of the API commands are recommended used raw, half are not. This can be checked in the command's class documentation. If not recommended used raw, use GenericBot methods instead.

### Customization

Each bot can be customized in several different ways. These include:

```
jmb.
```

### GUI

JavaMediawikiBot comes with a optional, built in GUI. To enable it, run the following command:

```
bot.enableGUI(); # TODO Update
```

The GUI will look like below:

[] #TODO Add image.

## Contributing

Every contribution is welcome! If you are interested in helping out, please submit an issue or pull request.