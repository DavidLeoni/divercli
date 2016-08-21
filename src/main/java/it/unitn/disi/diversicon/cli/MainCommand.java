package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkNotEmpty;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import it.unitn.disi.diversicon.cli.commands.DiverCliCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
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
            + DiverCli.GLOBAL_CONF_PATH)
    private boolean resetGlobalConf = false;

    private DiverCli cli;

    public MainCommand(DiverCli diverCli) {
        checkNotNull(diverCli);
        this.cli = diverCli;
    }

    /**
     * Loads a configuration file. Doesn't complain if there are missing fields.
     * 
     * @see #checkGlobalConfig()
     * @see #checkProjectConfig()()
     * @since 0.1.0
     */
    private Wini loadIni(File iniFile) {
        checkNotNull(iniFile);
        checkNotNull(cli.dbConfig);

        Wini ini;
        try {
            ini = new Wini(iniFile);
        } catch (IOException ex) {
            throw new DiverCliIoException("Error while loading ini file " + iniFile.getAbsolutePath(), ex);
        }

        cli.dbConfig.setDb_vendor(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "db_vendor", ini));
        cli.dbConfig.setJdbc_driver_class(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", ini));
        cli.dbConfig.setJdbc_url(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_url", ini));
        cli.dbConfig.setUser(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "user", ini));
        cli.dbConfig.setPassword(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "password", ini));
        cli.dbConfig.setShowSQL(false);       

        return ini;
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
            cli.globalConfIni = loadIni(cli.globalConfDir);
        } catch (Exception ex) {
            throw new DiverCliException(cli.globalConfigIsCorruptedMessage(), ex);
        }

        if (Internals.isBlank(projectDirParam)) {
            cli.projectDir = new File("");
        } else {
            cli.projectDir = new File(projectDirParam);
        }

        fixConfigIfTesting();

        checkGlobalConfig();
    }

    /**
     * @since 0.1.0
     */
    private void fixConfigIfTesting() {
        if (System.getProperty(DiverCli.SYSTEM_PROPERTY_TESTING) != null) {
            String workingDir = System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR);
            checkNotEmpty(workingDir, "When testing working dir shouldn't be empty!");

            cli.projectDir = new File(cli.projectDir.getAbsolutePath()
                                                    .replace(System.getProperty("user.dir"),
                                                            workingDir));

            String jdbcUrl = cli.dbConfig.getJdbc_url();
            if (jdbcUrl != null) {
                if (jdbcUrl.contains("jdbc:h2:file:")) {
                    String filePath;
                    int i = jdbcUrl.indexOf(";");
                    if (i >= 0) {
                        filePath = jdbcUrl.substring("jdbc:h2:file:".length(), i);
                    } else {
                        filePath = jdbcUrl.substring("jdbc:h2:file:".length());
                    }
                    if (new File(filePath).isAbsolute()){
                        cli.dbConfig.setJdbc_url(jdbcUrl.replace(System.getProperty("user.dir"), workingDir));    
                    } else {
                        cli.dbConfig.setJdbc_url(jdbcUrl.replace(filePath, workingDir + "/" + filePath));
                    }
                    LOG.debug("Fixed jdbc url:" + cli.dbConfig.getJdbc_url());
                    
                }

            }

        }

    }
    

    /**
     * To be called for commands requiring a project
     * 
     * @see {@link #configure()}
     * 
     * @since 0.1.0
     */
    public void configureProject() {

        DiverCli.checkProjectDir(cli.projectDir);

        try {
            cli.projectIni = loadIni(cli.findProjectFile(DiverCli.DIVERCLI_INI));

            fixConfigIfTesting();
            
            checkProjectConfig();

        } catch (Exception ex) {
            throw new DiverCliException(cli.projectConfigIsCorruptedMessage(), ex);
        }

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
     * 
     * @throws InvalidConfigException
     * 
     * @since 0.1.0
     */
    private void checkProjectConfig() {

        if (Internals.isBlank(cli.dbConfig.getDb_vendor())) {
            throw new InvalidConfigException("Expected db_vendor field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(cli.getProjectDir(), DiverCli.DIVERCLI_INI) + " file!");
        }

        if (Internals.isBlank(cli.dbConfig.getJdbc_driver_class())) {
            throw new InvalidConfigException("Expected jdbc_driver_class field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(cli.getProjectDir(), DiverCli.DIVERCLI_INI) + " file!");
        }

        if (Internals.isBlank(cli.dbConfig.getJdbc_url())) {
            throw new InvalidConfigException("Expected jdbc_url field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(cli.getProjectDir(), DiverCli.DIVERCLI_INI) + " file!");
        }

        if (cli.dbConfig.getUser() == null) {
            throw new InvalidConfigException("Expected user field in " + DiverCli.DATABASE_SECTION_INI + " section in "
                    + new File(cli.getProjectDir(), DiverCli.DIVERCLI_INI) + " file!");
        }

        if (cli.dbConfig.getPassword() == null) {
            throw new InvalidConfigException("Expected password field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(cli.getProjectDir(), DiverCli.DIVERCLI_INI) + " file!");
        }

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

}
