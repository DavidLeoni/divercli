package eu.kidf.diversicon.cli.commands;

import static eu.kidf.diversicon.core.internal.Internals.checkNotNull;

import com.beust.jcommander.Parameters;

import eu.kidf.diversicon.cli.DiverCli;

/** 
 * 
 * @since 0.1.0
 *
 */
@Parameters(separators = "=", commandDescription = "Augment the db graph to speed up "
        + " operations requiring the transitive closure.")
public class DbAugmentCommand implements DiverCliCommand {
    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "db-augment";

    private DiverCli diverCli;
    
    public DbAugmentCommand(DiverCli diverCli) {
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
        diverCli.getDiversicon().processGraph();
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




