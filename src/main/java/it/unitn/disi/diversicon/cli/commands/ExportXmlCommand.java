package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.exceptions.DivNotFoundException;

/** 
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Export a lexical resource to an XML file in UBY-LMF format.")
public class ExportXmlCommand implements DiverCliCommand {

    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "export-xml";

    
    @Parameter(names = "--name", required=true, description = "The name of the lexical resource to export.")
    private String name;

    @Parameter(names = "--compress", description = "Compress the file into a zip archive")
    private Boolean compress = false;


    @Parameter(required=true, arity=1, description = "Filepath of the xml to generate.")
    private List<String> xmlPath;

    private DiverCli diverCli;
    
    /**
     * @since 0.1.0
     */
    public ExportXmlCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

        /**
     * {@inheritDoc}
     * @since 0.1.0
     */
@Override
    public void configure(){
        // empty, for now diversicon will do the checks
    }
    
        /**
     * {@inheritDoc}
     * @since 0.1.0
     */
@Override
    public void run() {
        diverCli.connect();
        try {
            diverCli.getDiversicon().exportToXml(xmlPath.get(0), name, compress);
        } catch (DivNotFoundException ex){
            throw new ParameterException("Couldn't find lexical resource " + name + " !", ex);
        }
    }

        /**
     * {@inheritDoc}
     * @since 0.1.0
     */
@Override
    public String getName() {
        return CMD;
    }


}




