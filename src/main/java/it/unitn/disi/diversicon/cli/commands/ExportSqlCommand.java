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
     * @since 0.1.0
     */
    public static final String CMD = "export-sql";

    @Parameter(names = "--compress", description = "Compress the file into a zip archive")
    private Boolean compress = false;

    @Parameter(description = "The filepath where to save the generated sql dump", arity = 1, required = true)
    private List<String> sqlPaths = new ArrayList<>();

    private DiverCli diverCli;

    /**
     * @since 0.1.0
     */
    public ExportSqlCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void configure() {
        // empty, for now diversicon will do the checks
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void run() {
        diverCli.connect();
        diverCli.getDiversicon()
                .exportToSql(sqlPaths.get(0), compress);
    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public String getName() {
        return CMD;
    }

}
