  
### Configuration

To determine to which database to connect, DiverCli uses configuration file `$eval{eu.kidf.diversicon.cli.DiverCli.INI_FILENAME}` inside project directory. You can edit the file to connect to the database of choice, but note currently only <a href="http://www.h2database.com" target="_blank">H2 databases </a> are supported (DiverCli uses H2 v1.3.160). 


When a database is created default username is `$eval{eu.kidf.diversicon.core.Diversicons.DEFAULT_USER}` and password is `$eval{eu.kidf.diversicon.core.Diversicons.DEFAULT_PASSWORD}`. 


#### Global configuration

There is also global config in `USER_HOME/$eval{eu.kidf.diversicon.cli.DiverCli.INI_PATH}`,
where you can set i.e. proxy and timeouts. The config is shared by all projects, and settings there will be overridden by individual project settings. 

In case global configuration gets messed up, you can reset it by issuing:

$eval{resetGlobalConfig}


#### Java options

To set Java options  in Linux / Mac (for example to give DiverCli more memory), you can do something like:

```bash
JAVA_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" divercli db-augment
```


### Logging configuration

Logs can be found in files with extension `.log` available in the directory where you run `divercli`.

DiverCLI uses <a href="http://www.slf4j.org" target="_blank">SLF4J </a> logging system with <a href="http://logback.qos.ch/" target="_blank"> Logback</a> as SLF4J implementation. They are configured by default via xml files looked upon in this order :

#### Logging during execution

1. whatever is passed by command line: ` java -jar divercli.jar -Dlogback.configurationFile=path-to-my-logback.xml` 
2. `logback.xml` in [main resources](src/main/resources/logback.xml). 

#### Logging when testing (for developers)

1. whatever is passed by command line: ` mvn test -Dlogback.configurationFile=path-to-my-logback.xml`
2. `conf/logback-test.xml` as indicated in Maven surefire plugin configuration 
3. `logback-test.xml` in [test resources](src/test/resources/logback-test.xml). 

CAVEAT: stupid Eclipse doesn't pick those surefire properties [by design](https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683) , nor allows to apply run settings to all tests (O_o) so I went to `Windows->Preferences->Java->Installed JREs->Default one->Edit` and set default VM arguments to `-Dlogback.configurationFile=conf/logback-test.xml`. It's silly but could somewhat make sense for other projects too. 
 