

### Creating databases

Currently you can create H2 databases which can be empty or already containing Wordnet 3.1. Let's see how to do it. 


#### Creating empty database

You can create an empty database in directory `myprj` by issuing `divercli --prj myprj init`.
Note however that the system will always preload in the db the `DivUpper` lexical resource:

$eval{empty.init}

```bash
$eval{empty.cd}
$eval{empty.dir}
```

$eval{empty.log}


#### Creating Wordnet 3.1 database

Wordnet 3.1 is packaged within DiverCli, in the format of a <a href="http://www.h2database.com" target="_blank">H2 database</a>. You can also [find it]($exec{wn31.manualWebsite}) in LMF XML and SQL dump formats. You can unpack the database where you like (i.e. `wn31/` directory) by issuing:

$eval{wn31.init}

```
$eval{wn31.cd}
$eval{wn31.dir} 
```


### XML import

You can import one or more XMLs with the `import-xml` command.

For example, here we first create a database with Wordnet inside, and then import the resource `smartphones.xml` which depends upon Wordnet. Process takes some time as database is normalized and transitive closure is computed ([more info](http://diversicon-kb.eu/manual/diversicon-core/index.html#xml-import)):

$eval{wn31.init}

```bash
$eval{wn31.cd}
``` 
$eval{smartphones.import.success}

### Invalid imports

When you try to import an XML, it is first validated to check XML is valid _and_ references to
current db are present. Let's try to import `bad-eamplicon.xml`, which we already know 
contains many schema errors:

$eval{empty.init}

```bash
$eval{empty.cd}
```
 
$eval{badexamplicon.import}

Predictably, the import failed. 

### Unsatisfied references

Even if you try to import the well-formed resource `smartphones.xml` into an empty db the process will fail, 
because smartphones is referencing the resource `Diversicon Wordnet 3.1` and the validator won't 
find it in the database. In this case, the best solution would be to first import Wordnet. 
(If you really want to import resources that reference unmet dependencies, you can use the `--force` flag, 
see next paragraph)

$eval{empty.init}

```bash
$eval{empty.cd}
``` 

$eval{smartphones.import.failed}



### Force imports

If you really want to import resources that reference unmet dependencies, you can use the `--force` flag,
like we are doing for `smartphones.xml` here:

$eval{empty.init}

```bash
$eval{empty.cd}
``` 


$eval{smartphones.import.force}


### Import many resources

If you need to import many resources, to avoid transitive closure recomputation you can either:

a) specify more than one xml with the `import-xml` command.
  The transitive closure will be automatically computed only once at the end of the two imports:

 $eval{wn31.init}

```bash
$eval{wn31.cd}
``` 
$eval{smartphones.examplicon.import.success}
 
b) equivalently, you can set the flag `--skip-augment` and execute the command `import-xml`  on each xml you want to import. After the imports, you will explicitly have to compute the transitive closure by calling the command
  `db-augment`: 
  

$eval{wn31.init}

```bash
$eval{wn31.cd}
``` 
$eval{smartphones.import.skipaugment}
$eval{examplicon.import.skipaugment}
$eval{smartphones.examplicon.dbaugment}
 
### Validating XML

You can just validate an XML without checking it is consistent with some db:


$eval{smartphones.validate}
 

In this case we try to validate file `bad-examplicon.xml`. As the file name implies, validator is
not going to be happy: 


$eval{badexamplicon.validate}



### Export

You can export a lexical resource by issuing the command `export-xml`. In this case, we are going to export the default lexical resource `DivUpper` which is always present in databases you create:

$eval{empty.init}

```bash
$eval{empty.cd}
```
$eval{divupper.export}


### Stored logs

For each resource imported via DiverCli, an import log with relevant information is stored in the database. For example, here we show the log for Wordnet 3.1:
 
 
$eval{wn31.init}

```bash
$eval{wn31.cd}
```

$eval{wn31.log}

Note each import has a numerical identifier. To get more details about a single import (like warnings occurred during the import), you can use this command:

$eval{wn31.importShow}

Further logs can be found in files with extension `.log` available in the directory where you run `divercli`
