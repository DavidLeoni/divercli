<p class="josman-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://davidleoni.github.io/diversicon/" target="_blank">PROJECT WEBSITE</a>
</p>

This release allows to perform basic import / export of XML in IBY-LMF format and SQL dumps. Some function to query db metadata and import log is also provided.

### Getting started

You can download Diver CLI <a href="../releases/download/divercli-#{version}/divercli-#{version}.zip" target="_blank"> from here</a>, then unzip somewhere on your system.

To see usage commands:

In Linux / Mac, from terminal just type

```
bin/divercli
```

In Windows, click on Start menu, run command `cmd` and in the console type

```
bin\divercli.bat
```

In case updates are available, version numbers follow <a href="http://semver.org/" target="_blank">semantic versioning</a> rules.


### Commands list

From the client you can get a full command list:

```
$eval{help}
```

### Configuration

By default, DiverCli connects to the H2 database `$eval{it.unitn.disi.diversicon.cli.DEFAULT_H2_FILE_DB_PATH}`. H2 databases if not present are created upon first connection in the directory from which DiverCli is launched. Default username is `$eval{it.unitn.disi.diversicon.Diversicons.DEFAULT_H2_USER}` and password is `$eval{it.unitn.disi.diversicon.Diversicons.DEFAULT_H2_PASSWORD}`. To determine to which database to connect, DiverCli uses configuration file located in `$eval{it.unitn.disi.diversicon.cli.DiverCli.CONF_PATH}` inside user home, which you can edit to connect to the database of choice. 


#### Resetting configuration

In case configuration gets messed up, you can reset it by issuing:

$eval{resetConf}

#### Connecting to many databases

If you need to connect to many databases, you can create a different configuration folder for each database you want to connect to. Then just specify from the command line the folder you want to use with `--conf PUT_FOLDER_PATH` parameter:


#### Java options

To set Java options  in Linux / Mac (for example to give DiverCli more memory), you can do something like:
```
JAVA_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" ./divercli db-augment
```

  
### Getting help

To get some help about a specific command (say `import-xml`), you can issue something like 
    
$eval{helpImportXml}    
              
### Restoring packaged Wordnet

Wordnet 3.1 is packaged within DiverCli, in the format of a <a href="http://www.h2database.com" target="_blank">H2 database</a>, as a SQL dump, and as an LMF XML file. You can unpack the database where you like (i.e. db/my-wn31) by issuing:

$eval{wn31Restore}

To keep DiverCli using that database in the following commands, you can use the `--make-default` flag:

$eval{wn31RestoreMakeDefault}
$eval{wn31Restore}

              
### Showing import logs

For each resource imported via DiverCli, you can see an import log. For example, here we show the Wordnet 3.1 log :
 
$eval{dbRestore}
$eval{log}

Note each import has a numerical identifier. To get more details about a single import (like warnings occurred during the import), you can use this command:

$eval{importShow}


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
 