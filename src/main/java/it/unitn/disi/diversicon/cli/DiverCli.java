package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.lalyos.jfiglet.FigletFont;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.internal.Internals;

public class DiverCli {

    private static final Logger LOG = LoggerFactory.getLogger(DiverCli.class);

    public static final String CMD = "divercli";

    public static final String DIVERCLI_INI = CMD + ".ini";

    public static final String CONF_TEMPLATE = "conf-template/";
    public static final String CONF_PATH = ".config/" + CMD + "/";

    private static JCommander jcom;

    @Nullable
    private Diversicon diversicon;

    @Nullable
    private DBConfig dbConfig;

    @Parameter(names = { "--import",
            "-i" }, variableArity = true, description = "Imports a space separated list of XML files in UBY-LMF format.")
    private List<String> filepaths = new ArrayList();

    @Parameter(names = { "--process-graph", "-p" }, description = "Processes the db graph to speed up "
            + " operations requiring the transitive closure. When files to import are provided "
            + " processing is automatically performed. To skip it, write -p false")
    private Boolean processGraph;

    @Parameter(names = { "--conf", "-c" }, description = "Path to the configuration folder. Defaults to USER_HOME/"
            + CONF_PATH)
    private String confDirParam = null;
    
    private File confDir = null;

    @Parameter(names = { "--reset-conf" }, description = "Resets the configuration in USER_DIR/" + CONF_PATH)
    private boolean resetConf = false;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    private List<String> args;

    public static void main(String... args) {

        DiverCli cli = new DiverCli();
        cli.args = Arrays.asList(args);

        jcom = new JCommander(cli, args);

        cli.run();
    }

    /**
     * @throws DivcliException
     */
    public void parseConfig() {

        if (resetConf) {
            LOG.info("");
            LOG.info("Resetting user configuration at " + confDirPath() + "   ...");
            replaceConfDir();
            LOG.info("Done.");
            LOG.info("");
        }

        if (confDirParam == null) {
            confDir = findOrCreateConfDir();
        } else {
            confDir = confDirPath();
            if (!confDir.exists()) {
                throw new DivcliException("Provided conf dir doesn't exist: " + confDir.getAbsolutePath());
            }            
        }

        dbConfig = new DBConfig();
        Wini ini;

        try {

            File confFile = findOrCreateConfFile(DIVERCLI_INI);
            ini = new Wini(confFile);
            dbConfig.setJdbc_driver_class(extract("database", "jdbc_driver_class", ini));
            dbConfig.setDb_vendor(extract("database", "dbVendor", ini));
            dbConfig.setJdbc_url(extract("database", "jdbcUrl", ini));
            dbConfig.setUser(extract("database", "user", ini));
            dbConfig.setPassword(extract("database", "password", ini));
            dbConfig.setShowSQL(false);

        } catch (Exception ex) {
            throw new DivcliException(configIsCorruptedMessage(), ex);
        }

    }

    /**
     * 
     * @throws DivcliNotFoundException
     */
    private static String extract(Object sectionName, Object optionName, Wini ini) {

        String ret = ini.get("Database", "jdbc_driver_class", String.class);

        if (ret == null || ret.trim()
                              .isEmpty()) {
            throw new DivcliNotFoundException("Couldn't find " + optionName + " in section "
                    + sectionName + " of file " + ini.getFile()
                                                     .getAbsolutePath()
                    + " !!");
        } else {
            return ret;
        }
    }

    /** This function ALWAYS succeed */
    private static String configIsCorruptedMessage() {
        return "Configuration directory "
                + confDirPath().getAbsolutePath()
                + " seems corrupted! You can try replacing it with the command"
                + "    divercli --reset-conf    or manually copying content of "
                + absTemplatePathLLabel() + " into " + confDirPath().getAbsolutePath();
    }

    /** This function ALWAYS succeed */
    private static String absTemplatePathLLabel() {
        try {
            File t = findConfTemplateDir();
            return t.getAbsolutePath();
        } catch (Exception ex) {
            LOG.error("Couldn't find " + CONF_TEMPLATE + " directory", ex);
        }
        return "!ERROR_MISSING!";
    }

    public void run() {

        if (args.isEmpty()) {
            jcom.usage();
        } else {
            parseConfig();
            // connect();
            // exec commands
        }
    }

    public void connect() {

        diversicon = Diversicon.create(dbConfig);

        LOG.info("");
        LOG.info(" Welcome to");
        try {
            String asciiArt1 = FigletFont.convertOneLine("diversicon");
            LOG.info("    " + asciiArt1.replace("\n", "\n    "));
            LOG.info("");
        } catch (Exception ex) {
            LOG.debug("MINOR ERROR: Couldn't display awesome ASCII banner!");
        }
    }

    public void disconnect() {

        diversicon.getSession()
                  .close();

        LOG.info("");
        LOG.info("Disconnected. Good bye!");
        LOG.info("");
    }

    /**
     * 
     * @throws DivcliNotFoundException
     */
    private static File findConfTemplateDir() {
        File ret = new File(CONF_TEMPLATE);
        if (ret.exists()) {
            if (!ret.isDirectory()) {
                throw new DivcliNotFoundException(
                        "Expected a directory for " + CONF_TEMPLATE + ", found instead a file! ");
            }
            return ret;
        } else {
            throw new DivcliNotFoundException("Couldn't find " + CONF_TEMPLATE + " directory!");
        }

    }

    /**
     * Finds configuration directory, if not existing it creates one with
     * default values.
     *
     * @return the conf folder
     * @throws DivcliNotFoundException
     *             if folder is not found.
     */
    private File findOrCreateConfDir() {

        if (confDir == null) {

            confDir = confDirPath();

            if (!(confDir.exists()
                    && confDir.isDirectory()
                    && confDir.list().length != 0)) {

                replaceConfDir();
            }

        }

        return confDir;

    }

    /**
     * 
     */
    private static void replaceConfDir() {

        File userDir = confDirPath();

        if (userDir.exists()) {
            checkArgument(userDir.getAbsolutePath()
                                 .endsWith(".config" + File.separator + "divercli"),
                    "Failed security check prior deleting Divercli configuration!");
            try {
                FileUtils.forceDelete(userDir);
            } catch (IOException e) {
                throw new DivcliException("Error while replacing conf dir at "
                        + userDir.getAbsolutePath() + " !", e);
            }
        }
        File confTemplate = findConfTemplateDir();

        try {
            FileUtils.copyDirectory(confTemplate, confDirPath());
        } catch (IOException e) {
            throw new DivcliException("Error while copying "
                    + confTemplate.getAbsolutePath() + " configuration template "
                    + " to " + confDirPath());
        }
    }

    private static File confDirPath() {
        return new File(System.getProperty("user.home") + File.separator
                + ".config" + File.separator + "divercli");
    }

    /**
     * Finds a configuration file. If configuration files are not
     * present in user home they are created.
     *
     * @param filepath
     *            Relative filepath with file name and extension included. i.e.
     *            abc/myfile.xml, which will be first searched in
     *            {@link #confDir}/abc/myfile.xml
     *
     * @throws DivcliNotFoundException
     *             if no file is found
     */
    public  File findOrCreateConfFile(String filepath) {

        Internals.checkNotEmpty(filepath, "Invalid filepath!");

        File confDir = findOrCreateConfDir();
        File candFile = new File(confDir + File.separator + filepath);

        if (candFile.exists()) {
            return candFile;
        } else {
            throw new DivcliNotFoundException("Can't find file "
                    + filepath + " in conf dir: " + confDir.getAbsolutePath());
        }

    }

}
