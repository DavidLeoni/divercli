package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkArgument;
import static it.unitn.disi.diversicon.internal.Internals.checkNotEmpty;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.github.lalyos.jfiglet.FigletFont;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.DivIoException;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.ImportConfig;
import it.unitn.disi.diversicon.ImportJob;
import it.unitn.disi.diversicon.cli.commands.DbRestoreCommand;
import it.unitn.disi.diversicon.cli.commands.DbAugmentCommand;
import it.unitn.disi.diversicon.cli.commands.DbResetCommand;
import it.unitn.disi.diversicon.cli.commands.DiverCliCommand;
import it.unitn.disi.diversicon.cli.commands.ExportSqlCommand;
import it.unitn.disi.diversicon.cli.commands.ExportXmlCommand;
import it.unitn.disi.diversicon.cli.commands.ImportShowCommand;
import it.unitn.disi.diversicon.cli.commands.ImportXmlCommand;
import it.unitn.disi.diversicon.cli.commands.LogCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIllegalStateException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliNotFoundException;
import it.unitn.disi.diversicon.internal.Internals;
import it.unitn.disi.diversicon.internal.ExtractedStream;

/**
 * Provides a Command Line Interface to
 * {@link it.unitn.disi.diversicon.Diversicon}. Can be run as
 * program or you can use {@link #of} method to create instances of this object.
 *
 * @since 0.1.0
 */
public class DiverCli {

    /**
     * @since 0.1.0
     */
    private static final Logger LOG = LoggerFactory.getLogger(DiverCli.class);

    /**
     * @since 0.1.0
     */
    public static final String CMD = "divercli";

    /**
     * @since 0.1.0
     */
    public static final String DIVERCLI_INI = CMD + ".ini";

    public static final String CONF_TEMPLATE_DIR = "/it/unitn/disi/divercli/conf-template/";

    /**
     * @since 0.1.0
     */
    public static final String CONF_TEMPLATE_URI = "classpath:" + CONF_TEMPLATE_DIR;

    /**
     * @since 0.1.0
     */
    public static final String CONF_PATH = ".config/" + CMD + "/";

    /**
     * @since 0.1.0
     */
    static final String DATABASE_SECTION_INI = "Database";

    /**
     * @since 0.1.0
     */
    public static final String SYSTEM_CONF_DIR = "divercli-conf-dir";
         
    
    /**
     * @since 0.1.0
     */
    public static final String DEFAULT_H2_FILE_DB_PATH = "db" + File.separator + "my-diversicon.h2.db";

    private static JCommander jcom;

    private MainCommand mainCommand;


    @Nullable
    Diversicon diversicon;

    @Nullable
    DBConfig dbConfig;
    
    @Nullable
    Wini ini;

    File confDir = null;

    private String[] args;
    
    Map<String, DiverCliCommand> commands;

    private DiverCli() {
        String[] s = {};
        this.args = s;
        this.commands = new HashMap();
    }

    private DiverCli(String[] args) {
        this();
        checkNotNull(args);

        this.args = args;
    }

    /**
     * Entry point when program is run.
     * 
     * @since 0.1.0
     */
    public static void main(String... args) {

        DiverCli cli = new DiverCli(args);

        try {
            cli.run();
        } catch (ParameterException ex) {
            LOG.error("ERROR: " + ex.getMessage());
            System.exit(1);
        } catch (Exception ex) {
            LOG.error("Internal error occurred! Details:", ex);
        }
    }

    /**
     * 
     * Extracts given {@code option} from ini file. If option is empty throws
     * {@link DiverCliNotFoundException}
     * 
     * @throws DiverCliNotFoundException
     * @since 0.1.0
     */
    static String extract(String sectionName, String optionName, Wini ini) {
        checkNotEmpty(sectionName, "Invalid section name!");
        checkNotEmpty(optionName, "Invalid option name!");

        String ret = ini.get(sectionName, optionName, String.class);

        if (ret == null || ret.trim()
                              .isEmpty()) {
            throw new DiverCliNotFoundException("Couldn't find " + optionName + " in section "
                    + sectionName + " of file " + ini.getFile()
                                                     .getAbsolutePath()
                    + " !!");
        } else {
            return ret;
        }
    }

    /**
     * This function ALWAYS succeed
     * 
     * @since 0.1.0
     */
    String configIsCorruptedMessage() {

        String stringConf = confDir.getAbsolutePath()
                                   .equals(defaultConfDirPath()) ? "" : ("--conf " + confDir.getAbsolutePath());

        return "Configuration directory \n"
                + confDir.getAbsolutePath() + "\n"
                + "seems corrupted! You can try resetting content with the command\n"
                + "     " + CMD + " --reset-conf  " + stringConf + "  \n";
    }

    /**
     * @since 0.1.0
     * @throws DiverCliIllegalStateException
     */
    public DBConfig getDbConfig() {
        if (dbConfig == null) {
            throw new DiverCliIllegalStateException("Tried to access DbConfig without proper parsing of configuration!");
        } else {
            return dbConfig;
        }
    }
    
    
    
    private void addCommand(DiverCliCommand cmd){
        commands.put(cmd.getName(), cmd);
        jcom.addCommand(cmd.getName(), cmd);        
    }

    /**
     * @since 0.1.0
     */
    public void run() {
        try {

            mainCommand = new MainCommand(this);
            
            jcom = new JCommander(mainCommand);
            
            // doesn't work well, see https://github.com/DavidLeoni/divercli/issues/1
            // int terminalWidth = jline.TerminalFactory.get().getWidth();
            // jcom.setColumnSize(terminalWidth);            
            
            
            addCommand(new ExportXmlCommand(this));
            addCommand(new ExportSqlCommand(this));
            addCommand(new DbRestoreCommand(this));
            addCommand(new DbResetCommand(this));
            addCommand(new LogCommand(this));
            addCommand(new ImportShowCommand(this));
            addCommand(new ImportXmlCommand(this));
            addCommand(new DbAugmentCommand(this));
                        
            jcom.parse(args);

            if (args.length == 0) {
                jcom.usage();
            } else {

                mainCommand.configure();
                mainCommand.run();
                
                String parsedCmd = jcom.getParsedCommand();
                if (parsedCmd != null){
                    DiverCliCommand cmd = commands.get(parsedCmd);
                    cmd.configure();
                    cmd.run();                    
                }
            }
        } finally {
            disconnect();
        }
    }

    /**
     * @since 0.1.0
     */
    public boolean isConnected() {
        return diversicon != null && diversicon.getSession() != null && diversicon.getSession()
                                                                                  .isConnected();
    }

    /**
     * The config directory.
     * 
     * @since 0.1.0
     */
    public File getConfDir() {
        return confDir;
    }

    /**
     * Returns true if DiverCLI has been configured. If not you can run
     * {@link #configure()}
     * 
     * @since 0.1.0
     */
    public boolean isConfigured() {
        return confDir != null;
    }

    /**
     * Connects to Diversicon
     * 
     * @since 0.1.0
     */
    public void connect() {

        checkConfigured();

        if (!isConnected()) {           
                                       
            if (Diversicons.isH2Db(dbConfig)){                
                if (Diversicons.isEmpty(dbConfig)){
                    Diversicons.dropCreateTables(dbConfig);
                }               
            } 
            
            diversicon = Diversicon.connectToDb(dbConfig);
            
            LOG.info("");
            LOG.info(" Welcome to");
            try {
                String asciiArt1 = FigletFont.convertOneLine("diversicon");
                LOG.info("    " + asciiArt1.replace("\n", "\n    "));
            } catch (Exception ex) {
                LOG.debug("Minor error: Couldn't display awesome ASCII banner!", ex);
            }
        }

    }

    private void checkConfigured() {
        if (!isConfigured()) {
            throw new DiverCliIllegalStateException("Divercli was not configured!");
        }
    }

    /**
     * Disconnects from Diversicon
     * 
     * @since 0.1.0
     * 
     */
    public void disconnect() {

        if (isConnected()) {
            diversicon.getSession()
                      .close();

            LOG.info("");
            LOG.info("Disconnected.");
            LOG.info("");
        }
    }

    /**
     * Returns default conf folder dir path in user home.
     * 
     * @since 0.1.0
     * 
     */
    public static File defaultConfDirPath() {
        return new File(System.getProperty("user.home") + File.separator
                + CONF_PATH);
        
    }

    /**
     * Finds a configuration file in {@link #confDir}. Optionally, if configuration files are not
     * present in user home they are created.
     *
     * @param filepath
     *            Relative filepath with file name and extension included. i.e.
     *            abc/myfile.xml, which will be first searched in
     *            {@link #confDir}/abc/myfile.xml
     *
     * @param createIfMissing if true and file is not found, it is searched in 
     * {@code conf-template} resource folder and copied to current {@link #confDir}.
     * 
     * @throws DiverCliNotFoundException
     *             if no file is found
     * @since 0.1.0
     */
    public File findConfFile(String filepath, boolean createIfMissing) {

        Internals.checkNotEmpty(filepath, "Invalid filepath!");
        checkConfigured();

        File candFile = new File(confDir.getAbsolutePath() + File.separator + filepath);

        if (candFile.exists()) {
            return candFile;
        } else {
            
            if (createIfMissing){
                LOG.debug("Couldn't find conf file " + filepath + ", attempting copy from conf-template...");

                try {
                    ExtractedStream stream = Internals.readData(CONF_TEMPLATE_URI + filepath, false);
                    return stream.toTempFile();
                } catch (DivIoException ex) {
                    throw new DiverCliNotFoundException("Can't find file "
                            + filepath + " in " + CONF_TEMPLATE_URI);

                }
                
            } else {
                throw new DiverCliNotFoundException("Can't find file "
                        + filepath + " in " + CONF_TEMPLATE_URI);

            }
            

        }

    }

    /**
     * Factory method to create an instance.
     *
     * @param args
     *            the arguments of the command line
     * @throws DiverCliException
     * @throws ParameterException
     * 
     * @since 0.1.0
     */
    public static DiverCli of(String... args) {
        return new DiverCli(args);
    }

    /**
     * 
     * @since 0.1.0
     */
    public void showImportJob(long importId) {
        if (!isConnected()) {
            throw new DiverCliIllegalStateException("Tried to show import job without being connected!");
        }
        ImportJob job = diversicon.getImportJob(importId);
        diversicon.formatImportJob(job, true);
    }

    /**
     * @throws DiverCliIllegalStateException
     * 
     * @since 0.1.0
     */
    public Diversicon getDiversicon() {

        if (!isConnected()) {
            throw new DiverCliIllegalStateException(
                    "Tried to access Diversicon without having established a proper connection!");
        } else {
            return diversicon;
        }
    }


    /**
     * Copies template conf dir into {@link #getConfDir()}
     * 
     * @since 0.1.0
     */
    void copyTemplateConf() {
        // better not check, may still be initilalizing checkConfigured();

        Internals.copyDirFromResource(DiverCli.class, "it/unitn/disi/divercli/conf-template", confDir);
    }

    /**
     * Replaces configuration dir in conf directory with default one.
     * 
     * @since 0.1.0
     */
    void replaceConfDir() {

        // better not check, may still be initilalizing checkConfigured();

        if (confDir.exists()) {
            checkArgument(confDir.getAbsolutePath()
                                 .endsWith(".config" + File.separator + DiverCli.CMD),
                    "Failed security check prior deleting DiverCLI configuration!");
            try {
                FileUtils.forceDelete(confDir);
            } catch (IOException e) {
                throw new DiverCliException("Error while deleting default conf dir at "
                        + confDir.getAbsolutePath() + " !", e);
            }
        }

        copyTemplateConf();
    }

    /**
     * 
     * Checks provided conf dir for minimal integrity (some missing files may be
     * opied on the fly by the system)
     * 
     * @throws DiverCliNotFoundException
     * @throws DivIoException
     * 
     * @since 0.1.0
     */
    static void checkConfDir(File dir) {
        if (!dir.exists()) {
            throw new DiverCliNotFoundException("Couldn't find conf dir " + dir.getAbsolutePath() + "  !");
        }

        if (!dir.isDirectory()) {
            throw new DivIoException("Conf dir is not a directory! " + dir.getAbsolutePath() + "  !");
        }

        if (dir.list().length == 0) {
            throw new DivIoException("Conf dir is empty: " + dir.getAbsolutePath() + "  !");
        }
        
        
        
    }

    
    /**
     * Returns true if provided config points to default H2 file db .
     *  
     * @since 0.1.0
     */
    public static boolean isDefaultH2FileDb(DBConfig dbConfig){
        return dbConfig.getJdbc_url().contains("jdbc_url=jdbc:h2:file:db/my-diversicon");                               
    }

    /**
     * Returns true if provided config points to default H2 mem db . 
     *
     * @since 0.1.0
     */    
    public static boolean isH2MemDb(DBConfig dbConfig){
        return dbConfig.getJdbc_url().contains("jdbc_url=jdbc:h2:mem:");    
    }

    /**
     * Saves {@code dbConfig} to INI file in {@link #confDir} folder
     * 
     * @throws DiverCliIoException
     * 
     * @since 0.1.0
     */
    public void saveConfig() {
       
        checkConfigured();
        
        ini.put(DATABASE_SECTION_INI, "jdbc_driver_class", dbConfig.getJdbc_driver_class());
        ini.put(DATABASE_SECTION_INI, "db_vendor", dbConfig.getDb_vendor());
        ini.put(DATABASE_SECTION_INI, "jdbc_url", dbConfig.getJdbc_url());
        ini.put(DATABASE_SECTION_INI, "user", dbConfig.getUser());
        ini.put(DATABASE_SECTION_INI, "password", dbConfig.getPassword());
        
        try {
            ini.store();
        } catch (IOException ex) {        
            throw new DiverCliIoException("Error while saving INI file to " + confDir.getAbsolutePath() + File.separator + DIVERCLI_INI, ex);
        }
    }

    /**
     * @since 0.1.0
     */
    public void setDbConfig(DBConfig dbCfg) {
        checkNotNull(dbCfg);
        disconnect();
        this.dbConfig = dbCfg;        
    }
    

}

/**
 * Special command used for setting configuretion / help / debugging 
 *
 * @since 0.1.0
 */
@Parameters()
class MainCommand implements DiverCliCommand {
    
    
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
    String confDirParam = null;

    @Parameter(names = { "--reset-conf" }, description = "Resets the configuration in USER_DIR/" + DiverCli.CONF_PATH)
    boolean resetConf = false;

    @Parameter(names = "--help", help = true)
    boolean help = false;

    @Parameter(names = "--debug", hidden = true)
    boolean debug = false;

    private DiverCli diverCli;       
        
    @Nullable
    private ImportConfig importConfig;   
    
    public MainCommand(DiverCli diverCli) {    
        checkNotNull(diverCli);
        this.diverCli = diverCli;
    }

    /**
     * Configures conf folder searching in order:
     * <ol>
     * <li>{@code confDirParam}</li>
     * <li> Java {@link DiverCli#SYSTEM_CONF_DIR} system property argument </li>
     * <li> {@link DiverCli#defaultConfDirPath()}, if full </li>
     * <li> if {@link DiverCli#defaultConfDirPath()} is empty, populates it with {@code conf-template} folder </li>
     * </ol>
     * @since 0.1.0
     * @throws DiverCliException
     */
    @Override
    public void configure() {

        if (Internals.isBlank(confDirParam)) {
            String systemConfDir = System.getProperty(DiverCli.SYSTEM_CONF_DIR);
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
            diverCli.dbConfig.setJdbc_driver_class(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", diverCli.ini));
            diverCli.dbConfig.setDb_vendor(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "db_vendor", diverCli.ini));
            diverCli.dbConfig.setJdbc_url(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_url", diverCli.ini));
            diverCli.dbConfig.setUser(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "user", diverCli.ini));
            diverCli.dbConfig.setPassword(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "password", diverCli.ini));
            diverCli.dbConfig.setShowSQL(false);

        } catch (Exception ex) {
            throw new DiverCliException(diverCli.configIsCorruptedMessage(), ex);
        }


    }

    @Override
    public void run(){

        if (debug) {
            LOG.info("\n * * * *    DEBUG MODE IS ON    * * * * * \n");
        } 
        
    }    
    
    
    @Override
    public String getName() {
        return CMD;
    }

    
    

}
