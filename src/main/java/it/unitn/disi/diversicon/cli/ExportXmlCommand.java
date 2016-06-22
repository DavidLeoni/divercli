package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Exports a lexical resource to an XML file in UBY-LMF format.")
class ExportXmlCommand {

    @Parameter(names = "--name", description = "The name of the lexical resource to export.")
    String name;

    @Parameter(names = "--compress", description = "Compress the file into a zip archive")
    Boolean compress = false;


    @Parameter(description = "the path where to save the generated xml")
    String xmlPath;

    DiverCli diverCli;
    
    public ExportXmlCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    void configure(){
        // empty, for now diversicon will do the checks
    }
    
    void run() {
        diverCli.connect();
        diverCli.diversicon.exportToXml(xmlPath, name, compress);
    }


}




