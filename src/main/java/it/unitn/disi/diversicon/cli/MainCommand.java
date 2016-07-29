package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;

import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.cli.commands.DiverCliCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * Special command used for setting configuretion / help / debugging 
 *
 * @since 0.1.0
 */
@Parameters()
public class MainCommand implements DiverCliCommand {
        
    /**
     * @since 0.1.0
     */
    public static final String CMD = "main";

    
    /**
     * @since 0.1.0
     */
    private static final Logger LOG = LoggerFactory.getLogger(MainCommand.class);


    @Parameter(names = { "--conf", "-c" }, description = "Path to the configuration folder. Defaults to USER_HOME/"
            + DiverCli.CONF_PATH)
    private String confDirParam = null;

    @Parameter(names = { "--reset-conf" }, description = "Resets the configuration in USER_DIR/" + DiverCli.CONF_PATH)
    private boolean resetConf = false;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    @Parameter(names = "--debug", hidden = true)
    private boolean debug = false;

    private DiverCli diverCli;       
            
    public MainCommand(DiverCli diverCli) {    
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    /**
     * Configures conf folder searching in order:
     * <ol>
     * <li>{@code confDirParam}</li>
     * <li> Java {@link DiverCli#SYSTEM_PROPERTY_CONF_DIR} system property argument </li>
     * <li> {@link DiverCli#defaultConfDirPath()}, if full </li>
     * <li> if {@link DiverCli#defaultConfDirPath()} is empty, populates it with {@code conf-template} folder </li>
     * </ol>
     * 
     * @throws DiverCliException
     * 
     * @since 0.1.0
     */
    @Override
    public void configure() {

        if (Internals.isBlank(confDirParam)) {
            String systemConfDir = System.getProperty(DiverCli.SYSTEM_PROPERTY_CONF_DIR);
            if (Internals.isBlank(systemConfDir)) {

                // create or replace default conf dir
                diverCli.confDir = DiverCli.defaultConfDirPath();
                if (!(diverCli.confDir.exists()
                        && diverCli.confDir.isDirectory()
                        && diverCli.confDir.list().length != 0)) {

                    diverCli.copyTemplateConf();
                }

            } else {
                diverCli.confDir = new File(systemConfDir);                                               
            }
        } else {
            diverCli.confDir = new File(confDirParam);            
        }

        if (resetConf) {
            LOG.info("");
            LOG.info("Resetting user configuration at " + diverCli.confDir + "   ...");
            diverCli.replaceConfDir();

            LOG.info("Done.");
            LOG.info("");
        }

        diverCli.dbConfig = new DBConfig();


        try {
            DiverCli.checkConfDir(diverCli.confDir);

            File confFile = diverCli.findConfFile(DiverCli.DIVERCLI_INI, false);
            diverCli.ini = new Wini(confFile);
            diverCli.dbConfig.setDb_vendor(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "db_vendor", false, diverCli.ini));
            diverCli.dbConfig.setJdbc_driver_class(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", false, diverCli.ini));
            diverCli.dbConfig.setJdbc_url(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_url", false,  diverCli.ini));
            diverCli.dbConfig.setUser(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "user", true, diverCli.ini));
            diverCli.dbConfig.setPassword(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "password", true, diverCli.ini));
            diverCli.dbConfig.setShowSQL(false);

        } catch (Exception ex) {
            throw new DiverCliException(diverCli.configIsCorruptedMessage(), ex);
        }


    }

    /**
     * {@inheritDoc}
     * @since 0.1.0
     */    
    @Override
    public void run(){

        if (debug) {
            LOG.info("\n * * * *    DEBUG MODE IS ON    * * * * * \n");
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
