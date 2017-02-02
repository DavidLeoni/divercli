package eu.kidf.diversicon.cli;

import static eu.kidf.diversicon.core.internal.Internals.checkArgument;
import static eu.kidf.diversicon.core.internal.Internals.checkNotBlank;
import static eu.kidf.diversicon.core.internal.Internals.checkNotEmpty;
import static eu.kidf.diversicon.core.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.github.lalyos.jfiglet.FigletFont;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import eu.kidf.diversicon.cli.commands.DbAugmentCommand;
import eu.kidf.diversicon.cli.commands.DbResetCommand;
import eu.kidf.diversicon.cli.commands.DiverCliCommand;
import eu.kidf.diversicon.cli.commands.ExportSqlCommand;
import eu.kidf.diversicon.cli.commands.ExportXmlCommand;
import eu.kidf.diversicon.cli.commands.HelpCommand;
import eu.kidf.diversicon.cli.commands.ImportShowCommand;
import eu.kidf.diversicon.cli.commands.ImportXmlCommand;
import eu.kidf.diversicon.cli.commands.InitCommand;
import eu.kidf.diversicon.cli.commands.LogCommand;
import eu.kidf.diversicon.cli.commands.ValidateCommand;
import eu.kidf.diversicon.cli.exceptions.DiverCliException;
import eu.kidf.diversicon.cli.exceptions.DiverCliIllegalStateException;
import eu.kidf.diversicon.cli.exceptions.DiverCliIoException;
import eu.kidf.diversicon.cli.exceptions.DiverCliNotFoundException;
import eu.kidf.diversicon.cli.exceptions.DiverCliTerminatedException;
import eu.kidf.diversicon.cli.exceptions.InvalidConfigException;
import eu.kidf.diversicon.core.DivConfig;
import eu.kidf.diversicon.core.Diversicon;
import eu.kidf.diversicon.core.Diversicons;
import eu.kidf.diversicon.core.ImportJob;
import eu.kidf.diversicon.core.exceptions.DivIoException;
import eu.kidf.diversicon.core.ExtractedStream;
import eu.kidf.diversicon.core.internal.Internals;

/**
 * Provides a Command Line Interface to
 * {@link eu.kidf.diversicon.core.Diversicon Diversicon}. Can be run as
 * program or you can use {@link #of} factory method to create instances
 * of this object. After creation, you will need to call {@link #run()} method.
 *
 * <p>
 * All output for user is handled via {@code Logback} logging starting at
 * {@code info} level.
 * </p>
 * 
 * @since 0.1.0
 */
public final class DiverCli {

    public static final String DEFAULT_DB_IDENTIFIER = "default";

    /**
     * @since 0.1.0
     */
    private static final Logger LOG = LoggerFactory.getLogger(DiverCli.class);

    private static final int SUGGESTION_EDIT_DISTANCE = 3;
    
    /**
     * @since 0.1.0
     */
    public static final String CMD = "divercli";

    /**
     * @since 0.1.0
     */
    public static final String INI_FILENAME = CMD + ".ini";

    /**
     * @since 0.1.0
     */
    public static final String GLOBAL_CONF_DIR = ".config/" + CMD + "/";

    /**
     * @since 0.1.0
     */
    public static final String INI_PATH = GLOBAL_CONF_DIR + INI_FILENAME;
    

    /**
     * @since 0.1.0
     */
    public static final String TEMPLATES_DIR = "templates/";

    /**
     * @since 0.1.0
     */
    public static final String GLOBAL_CONF_TEMPLATE_DIR = TEMPLATES_DIR + "global-conf";

    /**
     * @since 0.1.0
     */
    public static final String GLOBAL_CONF_TEMPLATE_URI = "classpath:/" + GLOBAL_CONF_TEMPLATE_DIR;

    /**
     * @since 0.1.0
     */
    public static final String DEFAULT_PROJECT_TEMPLATE_DIR = TEMPLATES_DIR + DEFAULT_DB_IDENTIFIER;

    /**
     * @since 0.1.0
     */
    public static final String DEFAULT_PROJECT_TEMPLATE_URI = "classpath:/" + DEFAULT_PROJECT_TEMPLATE_DIR;

    /**
     * @since 0.1.0
     */
    public static final String H2_PROJECT_TEMPLATE_DIR = TEMPLATES_DIR + Diversicons.H2_IDENTIFIER;

    /**
     * @since 0.1.0
     */
    public static final String H2_PROJECT_TEMPLATE_URI = "classpath:/" + H2_PROJECT_TEMPLATE_DIR;

    /**
     * @since 0.1.0
     */
    public static final String DATABASE_SECTION_INI = "Database";
    
    /**
     * @since 0.1.0
     */
    public static final String FETCHER_SECTION_INI = "Fetcher";
    
    /**
     * @since 0.1.0
     */
    public static final String TIMOUT_PROPERTY = "timeout";    

    /**
     * @since 0.1.0
     */
    public static final String HTTP_PROXY_PROPERTY = "http_proxy";    
    

    /**
     * @since 0.1.0
     */
    // Because changing system vars is problematic: http://stackoverflow.com/questions/840190/changing-the-current-working-directory-in-java/8204584#8204584    
    public static final String SYSTEM_PROPERTY_USER_HOME = "divercli.user.home";    


    /**
     * 
     * @since 0.1.0
     */
    // Because changing system vars is problematic: http://stackoverflow.com/questions/840190/changing-the-current-working-directory-in-java/8204584#8204584
    public static final String SYSTEM_PROPERTY_WORKING_DIR = "divercli.user.dir";
   
    
    /**
     * NOTE: it doesn't end with '.h2.db' as H2 doesn't want the suffix in db
     * urls
     * 
     * @since 0.1.0
     */
    public static final String DEFAULT_H2_FILE_DB_PATH = "my-diversicon";

    public static final String SYSTEM_PROPERTY_TESTING = "divercli.testing";

    private static JCommander jcom;

    @Nullable
    private Diversicon diversicon;

    // used by MainCommand for initialization, don't make it private
    @Nullable
    DivConfig divConfig;

    // used by MainCommand for initialization, don't make it private
    @Nullable
    Wini globalConfIni;

    // used by MainCommand for initialization, don't make it private
    @Nullable
    Wini projectIni;

    // used by MainCommand for initialization, don't make it private
    File projectDir = null;

    // used by MainCommand for initialization, don't make it private
    File globalConfDir = null;

    // used by MainCommand for initialization, don't make it private
    boolean globallyConfigured;
    // used by MainCommand for initialization, don't make it private
    boolean projectConfigured;    
    
    private String[] args;

    private Map<String, DiverCliCommand> commands;

    

    /**
     * @since 0.1.0
     */
    private DiverCli() {
        String[] s = {};
        this.args = s;
        this.commands = new HashMap<>();
        this.divConfig = DivConfig.of();
        this.globallyConfigured = false;
        this.projectConfigured = false;
    }

    /**
     * @since 0.1.0
     */
    private DiverCli(String[] args) {
        this();
        checkNotNull(args);

        this.args = Arrays.copyOf(args, args.length);
        
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
        } catch (MissingCommandException ex) {
            final String GOT = "got ";
            int i = ex.getMessage()
                      .indexOf(GOT);
            if (i != -1) {
                String cmd = ex.getMessage()
                               .substring(i + GOT.length());
                cli.didYouMean(cmd);
            }
            exit(1);

        } catch (ParameterException ex) {
            LOG.error(ex.getMessage());
            exit(1);
        } catch (Exception ex) {
            LOG.error("Internal error occurred! Details:", ex);
            exit(1);
        }
    }

    /**
     * Normally exits the program. During testing throws
     * {@link DiverCliTerminatedException} (for explanation see
     * <a href=
     * "http://maven.apache.org/surefire/maven-surefire-plugin/faq.html#vm-
     * termination" target="_blank">surefire FAQ</a>)
     *
     * @since 0.1.0
     */
    private static void exit(int code) {
        if (Boolean.parseBoolean(System.getProperty(DiverCli.SYSTEM_PROPERTY_TESTING))) {
            throw new DiverCliTerminatedException(code);
        } else {
            System.exit(code);
        }
    }

    /**
     * 
     * Extracts given {@code option} from ini file.
     * 
     * @param isMandatory
     *            if false and option is empty throws
     *            {@link DiverCliNotFoundException}
     * 
     * @throws DiverCliNotFoundException
     * @since 0.1.0
     */
    @Nullable
    static String extract(String sectionName, String optionName, Wini ini) {
        checkNotEmpty(sectionName, "Invalid section name!");
        checkNotEmpty(optionName, "Invalid option name!");

        String ret = ini.get(sectionName, optionName, String.class);

        return ret;

    }

    /**
     * This function ALWAYS succeed
     * 
     * @since 0.1.0
     */
    String projectConfigIsCorruptedMessage() {

        return "Project configuration \n"
                + new File(projectDir, INI_FILENAME).getAbsolutePath() + "\n"
                + "seems corrupted!";
    }

    /**
     * Returns configuration of Diversicon.
     * 
     * @throws DiverCliIllegalStateException
     * 
     * @since 0.1.0
     */
    public DivConfig divConfig() {
        checkProjectConfigured();
        
        return divConfig;
        
    }

    /**
     * @since 0.1.0
     */
    private void addCommand(DiverCliCommand cmd) {
        commands.put(cmd.getName(), cmd);
        jcom.addCommand(cmd.getName(), cmd);
    }

    /**
     * Runs the cli, actually parsing the arguments.
     * 
     * @since 0.1.0
     */
    public void run() {
        try {
            LOG.info("");
            
            MainCommand mainCommand;

            mainCommand = new MainCommand(this);

            jcom = new JCommander(mainCommand);

            // doesn't work well, see
            // https://github.com/diversicon-kb/divercli/issues/1
            // int terminalWidth = jline.TerminalFactory.get().getWidth();
            // jcom.setColumnSize(terminalWidth);

            addCommand(new ExportXmlCommand(this));
            addCommand(new ExportSqlCommand(this));
            addCommand(new InitCommand(this));
            addCommand(new DbResetCommand(this));
            addCommand(new LogCommand(this));
            addCommand(new ImportShowCommand(this));
            addCommand(new ImportXmlCommand(this));
            addCommand(new DbAugmentCommand(this));
            addCommand(new HelpCommand(this));
            addCommand(new ValidateCommand(this));

            jcom.parse(args);

            if (args.length == 0) {
                StringBuilder sb = new StringBuilder();
                jcom.usage(sb);
                LOG.info(sb.toString());
                mainCommand.configure();
            } else {

                mainCommand.configure();     
                
                String parsedCmd = jcom.getParsedCommand();                
                
                if (!(null == parsedCmd
                        || HelpCommand.CMD.equals(parsedCmd)
                        || ValidateCommand.CMD.equals(parsedCmd)
                        || mainCommand.isHelp() // so it still behaves in a handy way with '--help'
                        || InitCommand.CMD.equals(parsedCmd))){
                    configureProject();    
                }
                
                mainCommand.run();
                
                if (parsedCmd == null){
                    
                    if (!mainCommand.isResetGlobalConf()) {
                        if (mainCommand.isHelp()){
                            // so it still behaves in a handy way with '--help'
                            HelpCommand hc = new HelpCommand(this);
                            hc.configure();
                            hc.run();
                        } else {
                            LOG.error("\n  No command given. Quitting... \n");
                        }
                        
                    }
                    
                } else {
                    DiverCliCommand cmd = commands.get(parsedCmd);                    
                    cmd.configure();
                    cmd.run();
                    if ( InitCommand.CMD.equals(parsedCmd)){
                        configureProject();
                    }
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
    public File getProjectDir() {
        return projectDir;
    }

    /**
     * Returns true if DiverCLI current project has been configured.
     * 
     * @since 0.1.0
     */
    public boolean isProjectConfigured() {
        return isGloballyConfigured() && projectConfigured;
    }
    
    /**
     * Returns true if DiverCLI has been configured. If not you can run
     * {@link #configure()}
     * 
     * @since 0.1.0
     */
    public boolean isGloballyConfigured() {        
        return globallyConfigured;
    }

    /**
     * To be called for commands requiring a project
     * 
     * @see {@link #configure()}
     * 
     * @since 0.1.0
     */
    // package visibility so we can access from MainCommand
    void configureProject() {

        LOG.debug("Going to read project configuration ...");
        
        DiverCli.checkProjectDir(projectDir);

        try {
            projectIni = loadIni(findProjectFile(DiverCli.INI_FILENAME));

            fixConfigIfTesting();
            
            checkProjectConfig();
            
            projectConfigured = true;
            
        } catch (Exception ex) {
            throw new DiverCliException(projectConfigIsCorruptedMessage(), ex);
        }

    }
    
   
    /**
     * @since 0.1.0
     */
    // package visibility so we can access from MainCommand
    void fixConfigIfTesting() {
        if (System.getProperty(DiverCli.SYSTEM_PROPERTY_TESTING) != null) {
            String workingDir = System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR);
            checkNotEmpty(workingDir, "When testing working dir shouldn't be empty!");

            projectDir = new File(projectDir.getAbsolutePath()
                                                    .replace(System.getProperty("user.dir"),
                                                            workingDir));
            DBConfig dbConfig = divConfig.getDbConfig();
            String jdbcUrl = dbConfig.getJdbc_url();
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
                        dbConfig.setJdbc_url(jdbcUrl.replace(System.getProperty("user.dir"), workingDir));    
                    } else {
                        dbConfig.setJdbc_url(jdbcUrl.replace(filePath, workingDir + "/" + filePath));
                    }
                    LOG.debug("Fixed jdbc url:" + dbConfig.getJdbc_url());
                    
                    divConfig = divConfig.withDbConfig(dbConfig);
                }

            }

        }

    }
    
    
    /**
     * Loads a configuration file. Doesn't complain if there are missing fields.
     * 
     * @see #checkGlobalConfig()
     * @see #checkProjectConfig()()
     * @since 0.1.0
     */
    // package visibility so we can access from MainCommand
    Wini loadIni(File iniFile) {
        checkNotNull(iniFile);
        checkNotNull(divConfig);

        checkArgument(iniFile.exists(), "Provided ini does not exist!" + iniFile.getAbsolutePath());
        
        checkArgument(iniFile.isFile(), "Provided ini is not a file!" + iniFile.getAbsolutePath());
        
        Wini ini;
        try {
            ini = new Wini(iniFile);
        } catch (IOException ex) {
            throw new DiverCliIoException("Error while loading ini file " + iniFile.getAbsolutePath(), ex);
        }
        
        DivConfig.Builder b = DivConfig.builder(divConfig);
        
        DBConfig dbConfig;
        if (divConfig.getDbConfig() == null){
            dbConfig = new DBConfig();
        } else {
            dbConfig = divConfig.getDbConfig(); 
        }
                        
        dbConfig.setDb_vendor(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "db_vendor", ini));
        dbConfig.setJdbc_driver_class(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", ini));
        dbConfig.setJdbc_url(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_url", ini));
        dbConfig.setUser(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "user", ini));
        dbConfig.setPassword(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "password", ini));
        dbConfig.setShowSQL(false);       
       
        b.setDbConfig(dbConfig);
        
        String timeoutString = (DiverCli.extract(
                DiverCli.FETCHER_SECTION_INI, 
                DiverCli.TIMOUT_PROPERTY, 
                ini));
        if (!Internals.isBlank(timeoutString)){
            b.setTimeout(Integer.parseInt(timeoutString));
        }
        
        String httpProxyString = (DiverCli.extract(
                DiverCli.FETCHER_SECTION_INI, 
                DiverCli.HTTP_PROXY_PROPERTY, 
                ini));
        if (!Internals.isBlank(httpProxyString)){
            b.setHttpProxy(httpProxyString);
        }        

        divConfig = b.build();
        
        return ini;
    }

    
    /**
     * 
     * @throws InvalidConfigException
     * 
     * @since 0.1.0
     */
    private void checkProjectConfig() {

        DBConfig dbConfig = divConfig.getDbConfig();
        
        if (Internals.isBlank(dbConfig.getDb_vendor())) {
            throw new InvalidConfigException("Expected db_vendor field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(getProjectDir(), DiverCli.INI_FILENAME) + " file!");
        }

        if (Internals.isBlank(dbConfig.getJdbc_driver_class())) {
            throw new InvalidConfigException("Expected jdbc_driver_class field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(getProjectDir(), DiverCli.INI_FILENAME) + " file!");
        }

        if (Internals.isBlank(dbConfig.getJdbc_url())) {
            throw new InvalidConfigException("Expected jdbc_url field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(getProjectDir(), DiverCli.INI_FILENAME) + " file!");
        }

        if (dbConfig.getUser() == null) {
            throw new InvalidConfigException("Expected user field in " + DiverCli.DATABASE_SECTION_INI + " section in "
                    + new File(getProjectDir(), DiverCli.INI_FILENAME) + " file!");
        }

        if (dbConfig.getPassword() == null) {
            throw new InvalidConfigException("Expected password field in " + DiverCli.DATABASE_SECTION_INI
                    + " section in " + new File(getProjectDir(), DiverCli.INI_FILENAME) + " file!");
        }

    }

    
    /**
     * Connects to Diversicon
     * 
     * @since 0.1.0
     */
    public void connect() {

        if (!isProjectConfigured()){
            configureProject();
        }

        if (!isConnected()) {

            if (Diversicons.isH2Db(divConfig.getDbConfig()) && Diversicons.isEmpty(divConfig.getDbConfig())) {

                Diversicons.dropCreateTables(divConfig.getDbConfig());
            }

            diversicon = Diversicon.connectToDb(divConfig);

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

    /**
     * @throws DiverCliIllegalStateException
     * 
     * @since 0.1.0
     */
    private void checkGloballyConfigured() {
        if (!isGloballyConfigured()) {
            throw new DiverCliIllegalStateException("Divercli has not read global configuration yet!");
        }
    }

    /**
     * @throws DiverCliIllegalStateException
     * 
     * @since 0.1.0
     */
    private void checkProjectConfigured() {
        if (!isProjectConfigured()) {
            throw new DiverCliIllegalStateException("Divercli has not read project configuration yet!");
        }
    }
    
    
    /**
     * Returns the registered commands
     * 
     * @since 0.1.0
     */
    public Map<String, DiverCliCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
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
     * Returns global conf folder dir path in user home.
     * 
     * @since 0.1.0
     * 
     */
    public static File globalConfDirPath() {
        if (System.getProperty(SYSTEM_PROPERTY_TESTING) == null){
            return new File(System.getProperty("user.home"), GLOBAL_CONF_DIR);            
        } else {
            String testHome = System.getProperty(SYSTEM_PROPERTY_USER_HOME);        
            checkNotBlank(testHome, "SYSTEM_PROPERTY_USER_HOME shouldn't be blank when testing!");
            return new File(testHome, GLOBAL_CONF_DIR);
            
        }
        
        

    }

    /**
     * Finds a configuration file in {@link #projectDir}. If
     * file is not present it is created from template indicated by
     * {@code databaseId}.
     * If database type is unknown, use {@link #DEFAULT_DB_IDENTIFIER} as
     * {@code databaseId}.
     *
     * @param filepath
     *            Relative filepath with file name and extension included. i.e.
     *            abc/myfile.xml, which will be first searched in
     *            {@link #projectDir}/abc/myfile.xml
     *
     *            if file is not found, it is searched in
     *            {@code template} resource folder for the specified database
     *            type and copied to current
     *            {@link #projectDir}.
     * @param databaseId
     *            If unknown use {@link #DEFAULT_DB_IDENTIFIER}
     * 
     * @throws DiverCliNotFoundException
     *             if no file is found
     * @see #findProjectFile(String)
     * @since 0.1.0
     */
    public File findProjectFileOrCreateDefault(String filepath, String databaseId) {

        checkNotEmpty("Invalid databaseId!", databaseId);

        Diversicons.checkSupportedDatabase(databaseId);

        Internals.checkNotEmpty(filepath, "Invalid filepath!");
        checkProjectConfigured();

        File candFile = new File(projectDir.getAbsolutePath() + File.separator + filepath);

        if (candFile.exists()) {
            return candFile;
        } else {

            LOG.debug("Couldn't find project file " + filepath + ", attempting copy from templates...");

            try {
                ExtractedStream stream = Diversicons.readData(
                        TEMPLATES_DIR + databaseId + File.separator + filepath,
                        false);
                return stream.toTempFile();
            } catch (DivIoException ex) {
                throw new DiverCliNotFoundException("Can't find file "
                        + filepath + " in " + "classpath:" + TEMPLATES_DIR + databaseId, ex);
            }

        }

    }

    /**
     * Factory method to create an instance. After creation you can call
     * {@link #run()}
     * to actually parse arguments and run commands.
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
     * Replaces global configuration dir in user home with default template.
     * 
     * @since 0.1.0
     */
    void resetGlobalConfDir() {

        // better not check, may still be initilalizing checkConfigured();

        if (globalConfDir.exists()) {
            checkArgument(globalConfDir.getAbsolutePath()
                                    .endsWith(".config" + File.separator + DiverCli.CMD),
                    "Failed security check prior deleting DiverCLI global configuration!");
            try {
                FileUtils.forceDelete(globalConfDir);
            } catch (IOException e) {
                throw new DiverCliException("Error while deleting global conf dir at "
                        + globalConfDir.getAbsolutePath() + " !", e);
            }
        }

        if (System.getProperty(DiverCli.SYSTEM_PROPERTY_TESTING) == null){
            Internals.copyDirFromResource(DiverCli.class, GLOBAL_CONF_TEMPLATE_DIR, globalConfDir);
        } else {                       
            
            // filter ini to have temp working dir...
            Internals.copyDirFromResource(DiverCli.class, GLOBAL_CONF_TEMPLATE_DIR,
                    globalConfDir);                                  
            
            Path globalIniPath = new File(globalConfDir, INI_FILENAME).toPath();
            byte[] encoded;
            try {
                encoded = Files.readAllBytes(globalIniPath);
                String filteredGlobalIni = new String(encoded, StandardCharsets.UTF_8);
                      //  .replace()  nothing to replace fo now!
                             
                Files.write(globalIniPath, filteredGlobalIni.getBytes());                
            } catch (IOException e) { 
                throw new DiverCliIoException("Error while filtering global conf dir!", e);
            }
            
        }
        
        
    }


    /**
     * 
     * Checks provided project directory for minimal integrity
     * 
     * @throws DiverCliNotFoundException
     * @throws DivIoException
     * 
     * @since 0.1.0
     */
    static void checkProjectDir(File projectDir) {
        if (!projectDir.exists()) {
            throw new DiverCliNotFoundException("Couldn't find project dir " + projectDir.getAbsolutePath() + "  !");
        }

        if (!projectDir.isDirectory()) {
            throw new DiverCliIoException("Project dir is not a directory! " + projectDir.getAbsolutePath() + "  !");
        }

        if (projectDir.list().length == 0) {
            throw new DiverCliNotFoundException("Project directory is empty: " + projectDir.getAbsolutePath() + "  !");
        }

        File ini = new File(projectDir, INI_FILENAME);

        if (!ini.exists()) {
            throw new DiverCliNotFoundException("Couldn't find file " + ini.getAbsolutePath());
        }

        if (ini.isDirectory()) {
            throw new DiverCliIoException(
                    "Expected a file a configuration file, found a directory instead:" + ini.getAbsolutePath());
        }

    }

    /**
     * 
     * Checks provided global configuration directory for minimal integrity
     * 
     * @throws DiverCliNotFoundException
     * @throws DivIoException
     * 
     * @since 0.1.0
     */
    static void checkGlobalConfDir(File confDir) {
        if (!confDir.exists()) {
            throw new DiverCliNotFoundException(
                    "Couldn't find global configuration dir " + confDir.getAbsolutePath() + "  !");
        }

        if (!confDir.isDirectory()) {
            throw new DiverCliIoException(
                    "Global configuration is not a directory! " + confDir.getAbsolutePath() + "  !");
        }

        if (confDir.list().length == 0) {
            throw new DiverCliNotFoundException(
                    "Global configuration directory is empty: " + confDir.getAbsolutePath() + "  !");
        }

        File ini = new File(confDir, INI_FILENAME);

        if (!ini.exists()) {
            throw new DiverCliNotFoundException("Couldn't find file " + ini.getAbsolutePath());
        }

        if (ini.isDirectory()) {
            throw new DiverCliIoException(
                    "Expected a configuration file, found a directory instead:" + ini.getAbsolutePath());
        }

    }

    /**
     * Returns true if provided config points to default H2 file db .
     * 
     * @since 0.1.0
     */
    public static boolean isDefaultH2FileDb(DBConfig dbConfig) {
        return dbConfig.getJdbc_url()
                       .contains("jdbc_url=jdbc:h2:file:db/my-diversicon");
    }

    /**
     * Returns true if provided config points to default H2 mem db .
     *
     * @since 0.1.0
     */
    public static boolean isH2MemDb(DBConfig dbConfig) {
        return dbConfig.getJdbc_url()
                       .contains("jdbc_url=jdbc:h2:mem:");
    }

    /**
     * Saves {@code dbConfig} to INI file in {@link #projectDir} folder
     * 
     * @throws DiverCliIoException
     * 
     * @since 0.1.0
     */
    public void saveConfig() {

        checkGloballyConfigured();
       
        DBConfig dbCfg = divConfig.getDbConfig();
        
        projectIni.put(DATABASE_SECTION_INI, "jdbc_driver_class", dbCfg.getJdbc_driver_class());
        projectIni.put(DATABASE_SECTION_INI, "db_vendor", dbCfg.getDb_vendor());
        projectIni.put(DATABASE_SECTION_INI, "jdbc_url", dbCfg.getJdbc_url());
        projectIni.put(DATABASE_SECTION_INI, "user", dbCfg.getUser());
        projectIni.put(DATABASE_SECTION_INI, "password", dbCfg.getPassword());

        try {
            projectIni.store();
        } catch (IOException ex) {
            throw new DiverCliIoException(
                    "Error while saving INI file to " + projectDir.getAbsolutePath() + File.separator + INI_FILENAME,
                    ex);
        }
    }

    /**
     * @since 0.1.0
     */
    public void setDbConfig(DBConfig dbCfg) {
        checkNotNull(dbCfg);
        disconnect();
        this.divConfig = divConfig.withDbConfig(dbCfg);
    }

    public JCommander getJCommander() {
        return jcom;
    }

    public void didYouMean(String commandName) {

        List<String> candidates = new ArrayList<>();
        for (String candidate : commands.keySet()) {
            if (Internals.editDistance(commandName, candidate) < SUGGESTION_EDIT_DISTANCE) {
                candidates.add(candidate);
            }
        }
        
        if (candidates.size() > 0) {
            LOG.error("Did you mean ... ?");
            for (String s : candidates) {
                LOG.error(" - " + s);
            }
        } else {
            LOG.error("Can't recognize the command '" + commandName + "'");
        }

    }


    /**
     * Finds a file in {@link #projectDir}.
     *
     * @param filepath
     *            Relative filepath with file name and extension included. i.e.
     *            abc/myfile.xml, which will be searched in
     *            {@link #projectDir}/abc/myfile.xml
     *
     * @throws DiverCliNotFoundException
     *             if no file is found
     * @see #findProjectFileOrCreateDefault(String, String)
     * @since 0.1.0
     */
    public File findProjectFile(String filepath) {

        Internals.checkNotEmpty(filepath, "Invalid filepath!");
        // better not, may be used during initalization 
        // checkProjectConfigured();

        File candFile = new File(projectDir.getAbsolutePath() + File.separator + filepath);

        if (candFile.exists()) {
            return candFile;
        } else {
            throw new DiverCliNotFoundException("Can't find file " + candFile.getAbsolutePath());
        }

    }

    /**
     * Finds a file in {@link #globalConfDir}.
     *
     * @param filepath
     *            Relative filepath with file name and extension included. i.e.
     *            abc/myfile.xml, which will be searched in
     *            {@link #projectDir}/abc/myfile.xml
     *
     * @throws DiverCliNotFoundException
     *             if no file is found
     * @since 0.1.0
     */
    public File findConfigFile(String filepath) {

        Internals.checkNotEmpty(filepath, "Invalid filepath!");
        // better not, may be used during initialization 
        // checkGloballyConfigured();

        File candFile = new File(globalConfDir.getAbsolutePath() + File.separator + filepath);

        if (candFile.exists()) {
            return candFile;
        } else {
            throw new DiverCliNotFoundException("Can't find file " + candFile.getAbsolutePath());
        }

    }
    
    
    /**
     * This function ALWAYS succeed
     * 
     * @since 0.1.0
     */
    String globalConfigIsCorruptedMessage() {

        return "Configuration directory \n"
                + globalConfDir.getAbsolutePath() + "\n"
                + "seems corrupted! You can try resetting content with the command\n"
                + "     " + CMD + " " + MainCommand.RESET_GLOBAL_CONFIG_OPTION + "  \n";       
    }

}
