
For preprocessing and quering XMLs we suggest [BaseX](#basex) database, while for browsing H2 SQL databases you can use the included [H2](#h2) server. 


### BaseX 

If you need to browse large XMLs or update them for some preprocessing before importing them into Diversicon,
<a href="" target="_blank">BaseX</a> db could be a good choice. For some common tasks, we already provide some scripts in $exec{eu.kidf.diversicon.cli.DiverCli.SCRIPTS_XML_PATH}

For example, supposing you:

1) installed <a href="http://basex.org/products/download/all-downloads/" target="_blank">BaseX</a>
2) basex executable is on your path
3) you want to rename prefixes of an XML

From DiverCli installation directory you coud run a command like this:

```
basex -bold-prefix=sm -bnew-prefix=mysm -binfile=lexres/smartphones.xml  -o my-smartphones.xml  scripts/rename-prefixes.xql
```

You can import an LMF XML this way:

TODO


### H2

To browse H2 databases, you can use H2 own browsing tool. It is included in DiverCLI distribution under `bin/h2` directory. 

 

To run it on Linux / Mac:

```
cd bin/h2/bin

./h2.sh

```

On Windows:

```

cd bin/h2/bin

h2.bat

```

This should open a browser pointed at a screen like this:

<img src="img/h2-1.png">

If you put the correct db url, you should see a panel like this, with all the tables listed in the left panel:

<img src="img/h2-correct.png">
 

If you put the wrong url, probably h2 will create a new empty database at that url and you will see this panel:
 
<img src="img/h2-wrong.png">
 
 