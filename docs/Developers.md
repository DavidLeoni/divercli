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


