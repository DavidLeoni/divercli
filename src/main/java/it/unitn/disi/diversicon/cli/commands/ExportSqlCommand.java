package it.unitn.disi.diversicon.cli.commands;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.DiverCli;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Exports the database into a sql dump")
public class ExportSqlCommand implements DiverCliCommand {

    /**
     * @since 0.1
     */
    public static final String CMD = "export sql";

    
    @Parameter(names = "--compress", description = "Compress the file into a zip archive")
    Boolean compress = false;

    @Parameter(description = "The filepath where to save the generated sql dump", arity=1, required=true)
    List<String> sqlPaths = new ArrayList();

    DiverCli diverCli;
    
    public ExportSqlCommand(DiverCli diverCli) {
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
        diverCli.getDiversicon().exportToSql(sqlPaths.get(0),  compress);
    }

    @Override
    public String getName() {
        return CMD;
    }

}




