package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.commands.DiverCliCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.cli.exceptions.InvalidConfigException;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * Special command used for setting configuretion / help / debugging
 *
 * @since 0.1.0
 */
@Parameters()
public class MainCommand implements DiverCliCommand {

    public static final String PRJ_OPTION = "--prj"; 
    
    // Notice we can't make a command out of this as global configuration must happen 
    // before commands are executed.  
    public static final String RESET_GLOBAL_CONFIG_OPTION =  "--reset-global-config" ;
    
    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "main";

    /**
     * @since 0.1.0
     */
    private static final Logger LOG = LoggerFactory.getLogger(MainCommand.class);

    

    @Parameter(names = { PRJ_OPTION}, description = "Path to the project directory. Defaults to current directory.")
    private String projectDirParam = null;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    @Parameter(names = "--debug", hidden = true)
    private boolean debug = false;

    @Parameter(names = {RESET_GLOBAL_CONFIG_OPTION }, description = "Resets the configuration in USER_DIR/"
            + DiverCli.GLOBAL_CONF_DIR)
    private boolean resetGlobalConf = false;

    private DiverCli cli;

    public MainCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.cli = diverCli;
    }


    /**
     * Configures global configuration folder searching in {@link DiverCli#globalConfDirPath()}. 
     * If folder is not found or empty populates it with {@code global-conf} template folder
     * 
     * To be called for all commands
     * 
     * @throws DiverCliException
     * @see {@link #configureProject()}
     * 
     * @since 0.1.0
     */
    @Override
    public void configure() {

        // create or replace default conf dir
        cli.globalConfDir = DiverCli.globalConfDirPath();

        if (resetGlobalConf) {
            LOG.info("");
            LOG.info("Resetting user configuration at " + cli.globalConfDir + "   ...");
            cli.resetGlobalConfDir();
            LOG.info("Done.");
            LOG.info("");
        } else {
            if ( 
                !(cli.globalConfDir.exists()
                && cli.globalConfDir.isDirectory()
                && cli.globalConfDir.list().length != 0)) {
                LOG.info("Found no global configuration, creating it at " + cli.globalConfDir.getAbsolutePath() + " ...");
                cli.resetGlobalConfDir();                
            }            
        }

        DiverCli.checkGlobalConfDir(cli.globalConfDir);

        try {
            cli.globalConfIni = cli.loadIni(cli.globalConfDir);
        } catch (Exception ex) {
            throw new DiverCliException(cli.globalConfigIsCorruptedMessage(), ex);
        }

        if (Internals.isBlank(projectDirParam)) {
            cli.projectDir =  new File(System.getProperty("user.dir"));
        } else {            
            try {
                cli.projectDir = new File(projectDirParam).getCanonicalFile();
            } catch (IOException e) {            
                throw new DiverCliException("invalid project dir: " + projectDirParam, e);
            }
        }

        cli.fixConfigIfTesting();

        checkGlobalConfig();
        
        cli.globallyConfigured = true;
    }

    

  

    /**
     * @throws InvalidConfigException
     * 
     * @since 0.1.0
     */
    private void checkGlobalConfig() {
        // does nothing for now
    }


    /**
     * {@inheritDoc}
     * 
     * @since 0.1.0
     */
    @Override
    public void run() {

        if (debug) {
            LOG.info("\n * * * *    DEBUG MODE IS ON    * * * * * \n");
        }

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


    /**
     * @since 0.1.0
     */
    public boolean isResetGlobalConf() {
        return resetGlobalConf;
    }

    /**
     * @since 0.1.0
     */
    public boolean isDebug() {
        return debug;
    }
    
    

    
}
