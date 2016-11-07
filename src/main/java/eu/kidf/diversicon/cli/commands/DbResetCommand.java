package eu.kidf.diversicon.cli.commands;

import static eu.kidf.diversicon.core.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameters;

import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.core.Diversicons;

/**
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Reset current database. If it doesn't exist it is created empty.")
public class DbResetCommand implements DiverCliCommand {

    /**
     * @since 0.1.0
     */
    public static final String CMD = "db-reset";

    private DiverCli diverCli;

    public DbResetCommand(DiverCli diverCli) {
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

    }

    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void run() {
        Diversicons.dropCreateTables(diverCli.divConfig().getDbConfig());
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
