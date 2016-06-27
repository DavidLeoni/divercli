package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.DivNotFoundException;
import it.unitn.disi.diversicon.cli.DiverCli;

/** 
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Exports a lexical resource to an XML file in UBY-LMF format.")
public class ExportXmlCommand implements DiverCliCommand {

    
    /**
     * @since 0.1
     */
    public static final String CMD = "export xml";

    
    @Parameter(names = "--name", required=true, description = "The name of the lexical resource to export.")
    String name;

    @Parameter(names = "--compress", description = "Compress the file into a zip archive")
    Boolean compress = false;


    @Parameter(required=true, arity=1, description = "Filepath of the xml to generate.")
    List<String> xmlPath;

    DiverCli diverCli;
    
    public ExportXmlCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    @Override
    public void configure(){
        // empty, for now diversicon will do the checks
    }
    
    @Override
    public void run() {
        diverCli.connect();
        try {
            diverCli.getDiversicon().exportToXml(xmlPath.get(0), name, compress);
        } catch (DivNotFoundException ex){
            throw new ParameterException("Couldn't find lexical resource " + name + " !");
        }
    }

    @Override
    public String getName() {
        return CMD;
    }


}




