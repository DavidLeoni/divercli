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

### Usage

TODO put usage

To set Java options in Linux / Mac, you can do something like:
```
JAVA_OPTS="-Xms1g -Xmx3g -XX:-UseGCOverheadLimit" ./divercli db-augment
```


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
 