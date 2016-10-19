<p class="josman-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://davidleoni.github.io/diversicon/" target="_blank">PROJECT WEBSITE</a>
</p>

This release allows to perform basic import / export of XML in IBY-LMF format and SQL dumps. Some function to query db metadata and import log is also provided.

### Getting started

You can download Diver CLI <a href="../releases/download/divercli-#{version}/divercli-#{version}.zip" target="_blank"> from here</a>, then unzip somewhere on your system. In case updates are available, version numbers follow <a href="http://semver.org/" target="_blank">semantic versioning</a> rules. 
This manual assumes you have added `bin/divercli` to the path. 

To see usage commands:

TODO review

In Linux / Mac, from terminal just type

```
bin/divercli
```

In Windows, click on Start menu, run command `cmd` and in the console type

```
bin\divercli.bat
```

You should see a command list like this:

```
$eval{help}
```

To get mre help about a specific command (say `import-xml`), you can issue something like 
    
$eval{helpImportXml}    

### Your first project

In DiverCli, a project is a folder with configuration to connect to a database, plus possibly the database itself and other custom scripts, so it might look like this :

```
divercli.ini
my-diversicon.db.h2
my-script.sql
...
``` 

DiverCli comes with full support for <a href="http://h2database.com" target="_blank"> H2 database </a>, which is shipped with DiverCli and doesn't require separate installation. 

Let's create our first H2 file-based database with Wordnet 3.1 inside:

$eval{init.wn31}

`--prj wn31` told DiverCLI in which folder to put the project, `init` was the actual comand given to DiverCli and `--db` specified to `init` command where to take the db. In this case Wordnet 3.1 is pre-packaged in the DiverCLI distribution so we picked it with the special `classpath:` url. Notice that `--prj` must always go _before_ commands. Let's see which files where generated:

```
> cd wn31
$eval{dir.wn31}      
        
```

The `divercli.ini` file will tell DiverCli where to connect when we launch DiverCli from this project directory. In this particular case the database is in the same folder, but it could be anywhere, even a remote connection. You can check things are working by issuing the `log` command, you can then see a status of the database and a log of the imports done into it so far:

$eval{log.wn31}

   
### Configuration


To determine to which database to connect, DiverCli uses configuration file `$eval{it.unitn.disi.diversicon.cli.DiverCli.INI_FILENAME}` inside project directory, which you can edit to connect to the database of choice. Currently only H2 databases are supported (DiverCli uses H2 v1.3.160). 


When a database is created default username is `$eval{it.unitn.disi.diversicon.Diversicons.DEFAULT_USER}` and password is `$eval{it.unitn.disi.diversicon.Diversicons.DEFAULT_PASSWORD}`. 


#### Global configuration

There is also global config in `USER_HOME/$eval{it.unitn.disi.diversicon.cli.DiverCli.INI_PATH}` shared by all projects. Settings there will be overridden by individual project settings. 

In case global configuration gets messed up, you can reset it by issuing:

$eval{resetGlobalConf}


#### Java options

To set Java options  in Linux / Mac (for example to give DiverCli more memory), you can do something like:
```
JAVA_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" divercli db-augment
```

              
### Creating databases

Currently you can create H2 databases which can be empty or already containing Wordnet 3.1 or a sample 
database called 'sample-lmf'. 

#### Create empty H2 database

You can create an empty database in directory 'myprj' by issuing `divercli --prj myprj init`:

$eval{empty.init}

```bash
> cd myprj
$eval{empty.dir}
```

#### Creating database with Wordnet 3.1

Wordnet 3.1 is packaged within DiverCli, in the format of a <a href="http://www.h2database.com" target="_blank">H2 database</a>, as a SQL dump, and as an LMF XML file. You can unpack the database where you like (i.e. `wn31/` directory) by issuing:

$eval{init.wn31}

```
> cd wn31
$eval{dir.wn31} 
```


### Importing XMLs

#### Preprocessing XMLs

TODO check this
```
java -cp  ~/.m2/repository/org/basex/basex/8.5/basex-8.5.jar org.basex.BaseX -bold-prefix=wn31 -bnew-prefix=peppo -binfile=../../../src/testources/experiments/xml/basex-2.xml  -o prova.xml   src/main/resources/rename-prefixes.xql
```


You can import an LMF xml this way:

$eval{}

### Exporting XMLs

### Showing import logs

For each resource imported via DiverCli, you can see an import log. For example, here we show the Wordnet 3.1 log :
 
 
$eval{wn31.init}

```bash
cd wn31
```

$eval{wn31.log}

Note each import has a numerical identifier. To get more details about a single import (like warnings occurred during the import), you can use this command:

$eval{wn31.importShow}


### Logging

DiverCLI uses <a href="http://www.slf4j.org" target="_blank">SLF4J </a> logging system with <a href="http://logback.qos.ch/" target="_blank"> Logback</a> as SLF4J implementation. They are configured by default via xml files looked upon in this order :

#### Logging during execution

1. whatever is passed by command line: ` java -jar divercli.jar -Dlogback.configurationFile=path-to-my-logback.xml` 
2. `logback.xml` in [main resources](src/main/resources/logback.xml). 

#### Logging when testing (for developers)

1. whatever is passed by command line: ` mvn test -Dlogback.configurationFile=path-to-my-logback.xml`
2. `conf/logback-test.xml` as indicated in Maven surefire plugin configuration 
3. `logback-test.xml` in [test resources](src/test/resources/logback-test.xml). 

CAVEAT: stupid Eclipse doesn't pick those surefire properties [by design](https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683) , nor allows to apply run settings to all tests (O_o) so I went to `Windows->Preferences->Java->Installed JREs->Default one->Edit` and set default VM arguments to `-Dlogback.configurationFile=conf/logback-test.xml`. It's silly but could somewhat make sense for other projects too. 
 