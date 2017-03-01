<p class="josman-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://diversicon-kb.eu/manual/divercli" target="_blank">PROJECT WEBSITE</a>
</p>

This release allows to perform basic import / export of XML in IBY-LMF format and SQL dumps. Some function to query db metadata and import log is also provided.

### Install

You can download Diver CLI <a href="../releases/download/divercli-#{version}/divercli-#{version}.zip" target="_blank"> from here</a>, then unzip somewhere on your system. In case updates are available, version numbers follow <a href="http://semver.org/" target="_blank">semantic versioning</a> rules. 

**NOTE: This manual assumes you have added `bin/divercli` to the path of your shell,
see <a href="INSTALL.txt" target="_blank">INSTALL.txt</a> instructions.**

### Getting started

To see usage commands:

In Linux / Mac, from terminal just type

```
bin/divercli
```

In Windows, click on Start menu, run command `cmd` and in the console type

```
bin\divercli.bat
```

### Your first project

In DiverCli, a project is a folder with configuration for connecting to a database, plus possibly the database itself and other custom scripts. It might look like this :

```
divercli.ini
my-diversicon.db.h2
my-script.sql
 . 
 .
 .

``` 

DiverCli comes with full support for <a href="http://h2database.com" target="_blank"> H2 database </a>, which is shipped with DiverCli and doesn't require separate installation (for info on browsing databases, see [Tools page](Tools.md#h2)). 

Let's create our first H2 file-based database with Wordnet 3.1 inside:

$eval{wn31.init}

In detail:
 
* `--prj wn31` tells DiverCLI in which folder to put the project
* `init` is the actual comand given to DiverCli
* `--db` specifies to `init` command where to take the db. In this case Wordnet 3.1 is pre-packaged in the DiverCLI distribution so we picked it with the special URL beginning with `classpath:` 

Notice that `--prj` always goes _before_ commands. 

These files were generated:

```
$eval{wn31.cd}
$eval{wn31.dir}
        
```

The file `divercli.ini` tells DiverCli where to connect when we launch the tool from the project directory. In this particular case the database is in the same folder, but it could be anywhere, even a remote connection. 

You can check things are working by issuing the `log` command, which will show a status of the database and a log of the imports done so far. Notice the first import is always the [`div-upper`](https://github.com/diversicon-kb/diversicon-model/blob/master/src/main/resources/div-upper.xml) lexical resource:

$eval{wn31.log}

We can now try to import the lexical resource `smartphones.xml`, which depends upon the already imported Wordnet:

$eval{smartphones.import.success}

After the import we can check the log:

$eval{smartphones.import.success.log}


### Empty databases

If instead of Wordnet you want to start with an empty database, you can create an empty database for example in directory `myprj` by issuing `divercli --prj myprj init`. Note that the system will always preload in the db the lexical resource `DivUpper`:

$eval{empty.init}

```bash
$eval{empty.cd}
$eval{empty.dir}
```
$eval{empty.log}

### More info 

You can learn more about handling XMLs in [Commands](Commands.md) page. In [Configuration](Configuration.md) you can read about global and per-project configuration, and logging.


