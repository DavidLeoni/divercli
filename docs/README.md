<p class="josman-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://davidleoni.github.io/diversicon/" target="_blank">PROJECT WEBSITE</a>
</p>

This release allows to perform basic import / export of XML in IBY-LMF format and SQL dumps. Some function to query db metadata and import log is also provided.

### Getting started

You can download Diver CLI <a href="../releases/download/divercli-#{version}/divercli-#{version}.zip" target="_blank"> from here</a>, then unzip somewhere on your system. In case updates are available, version numbers follow <a href="http://semver.org/" target="_blank">semantic versioning</a> rules. 
This manual assumes you have added `bin/divercli` to the path. 

To see usage commands:

In Linux / Mac, from terminal just type

```
bin/divercli
```

In Windows, click on Start menu, run command `cmd` and in the console type

```
bin\divercli.bat
```

You should see a command list like this:

$eval{help}


To get more help about a specific command (say `import-xml`), you can issue `help` command: 
    
$eval{help.importXml}    

### Your first project

In DiverCli, a project is a folder with configuration for connecting to a database, plus possibly the database itself and other custom scripts. It might look like this :

```
divercli.ini
my-diversicon.db.h2
my-script.sql
 . 
 .

``` 

DiverCli comes with full support for <a href="http://h2database.com" target="_blank"> H2 database </a>, which is shipped with DiverCli and doesn't require separate installation (for info on browsing databases, see [Tools page](Tools#h2)). 

Let's create our first H2 file-based database with Wordnet 3.1 inside:

$eval{wn31.init}

In detail:
 
* `--prj wn31` tells DiverCLI in which folder to put the project
* `init` is the actual comand given to DiverCli
* `--db` specifies to `init` command where to take the db. In this case Wordnet 3.1 is pre-packaged in the DiverCLI distribution so we picked it with the special URL beginning with `classpath:`. 

Notice that `--prj` always goes _before_ commands. 

These files where generated:

```
$eval{wn31.cd}
$eval{wn31.dir}      
        
```

The file `divercli.ini` tells DiverCli where to connect when we launch the tool from the project directory. In this particular case the database is in the same folder, but it could be anywhere, even a remote connection. 

You can check things are working by issuing the `log` command, which will show a status of the database and a log of the imports done so far:

$eval{wn31.log}

   
### Configuration


To determine to which database to connect, DiverCli uses configuration file `$eval{eu.kidf.diversicon.cli.DiverCli.INI_FILENAME}` inside project directory. You can edit the file to connect to the database of choice, but note currently only <a href="http://www.h2database.com" target="_blank">H2 databases </a> are supported (DiverCli uses H2 v1.3.160). 


When a database is created default username is `$eval{eu.kidf.diversicon.core.Diversicons.DEFAULT_USER}` and password is `$eval{eu.kidf.diversicon.core.Diversicons.DEFAULT_PASSWORD}`. 


#### Global configuration

There is also global config in `USER_HOME/$eval{eu.kidf.diversicon.cli.DiverCli.INI_PATH}` shared by all projects. Settings there will be overridden by individual project settings. 

In case global configuration gets messed up, you can reset it by issuing:

$eval{resetGlobalConfig}


#### Java options

To set Java options  in Linux / Mac (for example to give DiverCli more memory), you can do something like:
```
JAVA_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" divercli db-augment
```

              
### Creating databases

Currently you can create H2 databases which can be empty or already containing Wordnet 3.1. Let's see how to do it. 

#### Creating empty database

You can create an empty database in directory 'myprj' by issuing `divercli --prj myprj init`:

$eval{empty.init}

```bash
$eval{empty.cd}
$eval{empty.dir}
```

#### Creating Wordnet 3.1 database

Wordnet 3.1 is packaged within DiverCli, in the format of a <a href="http://www.h2database.com" target="_blank">H2 database</a>. You can also [find it]($exec{wn31.manualWebsite}) in LMF XML and SQL dump formats. You can unpack the database where you like (i.e. `wn31/` directory) by issuing:

$eval{wn31.init}

```
$eval{wn31.cd}
$eval{wn31.dir} 
```

### Importing XMLs

#### Preprocessing XMLs


### Exporting XMLs

You can export a _LexicalResource_ by issuing this command 

$eval{empty.init}

```bash
$eval{empty.cd}
```


### Import logs

For each resource imported via DiverCli, an import log is available. For example, here we show the log for Wordnet 3.1:
 
 
$eval{wn31.init}

```bash
$eval{wn31.cd}
```

$eval{wn31.log}

Note each import has a numerical identifier. To get more details about a single import (like warnings occurred during the import), you can use this command:

$eval{wn31.importShow}


### Logging

TODO review

DiverCLI uses <a href="http://www.slf4j.org" target="_blank">SLF4J </a> logging system with <a href="http://logback.qos.ch/" target="_blank"> Logback</a> as SLF4J implementation. They are configured by default via xml files looked upon in this order :

#### Logging during execution

1. whatever is passed by command line: ` java -jar divercli.jar -Dlogback.configurationFile=path-to-my-logback.xml` 
2. `logback.xml` in [main resources](src/main/resources/logback.xml). 

#### Logging when testing (for developers)

1. whatever is passed by command line: ` mvn test -Dlogback.configurationFile=path-to-my-logback.xml`
2. `conf/logback-test.xml` as indicated in Maven surefire plugin configuration 
3. `logback-test.xml` in [test resources](src/test/resources/logback-test.xml). 

CAVEAT: stupid Eclipse doesn't pick those surefire properties [by design](https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683) , nor allows to apply run settings to all tests (O_o) so I went to `Windows->Preferences->Java->Installed JREs->Default one->Edit` and set default VM arguments to `-Dlogback.configurationFile=conf/logback-test.xml`. It's silly but could somewhat make sense for other projects too. 
 