package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Exports the database into a sql dump")
class ExportSqlDumpCommand {

    @Parameter(names = "--compress", description = "Compress the file into a zip archive")
    Boolean compress = false;

    @Parameter(description = "the path where to save the generated sql dump")
    String sqlPath = "";

    DiverCli diverCli;
    
    public ExportSqlDumpCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    void configure(){
        // empty, for now diversicon will do the checks
    }
    
    void run() {
        diverCli.connect();
        diverCli.diversicon.exportToSql(sqlPath,  compress);
    }


}




