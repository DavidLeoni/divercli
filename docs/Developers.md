
### Getting started for developers

If you want to include the CLI into your own programs, read the following instructions. For more info about developing DiverCLI itself, see [the wiki](../../../wiki).

**With Maven**: If you use Maven as build system, put this in the `dependencies` section of your `pom.xml`:

```xml
    <dependency>
        <groupId>eu.kidf</groupId>
        <artifactId>divercli</artifactId>
        <version>${project.version}</version>
    </dependency>
```

Note that Wordnet 3.1 dependency will be automatically included.


**Without Maven**: you can download DiverCLI jar and its dependencies <a href="/releases/download/divercli-#{version}/divercli-${project.version}.zip" target="_blank"> from here</a>, then copy the jars to your project classpath.

In case updates are available, version numbers follow <a href="http://semver.org/" target="_blank">semantic versioning</a> rules.

### Dependencies

* See <a href="https://diversicon-kb.eu/manual/diversicon-core" target="_blank">Diversicon Core</a> dependencies. 
* <a href="http://diversicon-kb.eu/manual/diversicon-wordnet-3.1" target="_blank"> Diversicon Wordnet 3.1</a>
* <a href="http://jcommander.org/" target="_blank"> JCommander</a>

### Logging when testing

Logging is configured with `logback.xml` files, which during developing / testing are found in this order: 

1. whatever is passed by command line: ` mvn test -Dlogback.configurationFile=path-to-my-logback.xml`
2. `conf/logback-test.xml` as indicated in Maven surefire plugin configuration 
3. `logback-test.xml` in [test resources](../src/test/resources/logback-test.xml). 

CAVEAT: stupid Eclipse doesn't pick those surefire properties [by design](https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683) , nor allows to apply run settings to all tests (O_o) so I went to `Windows->Preferences->Java->Installed JREs->Default one->Edit` and set default VM arguments to `-Dlogback.configurationFile=conf/logback-test.xml`. It's silly but could somewhat make sense for other projects too. 
 
