package it.unitn.disi.diversicon.cli;

import static it.unitn.disi.diversicon.internal.Internals.checkArgument;
import static it.unitn.disi.diversicon.internal.Internals.checkNotEmpty;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.ImportConfig;
import it.unitn.disi.diversicon.internal.Internals;

@Parameters()
class MainCommand {
    
    /**
     * @since 0.1.0
     */
    private static final Logger LOG = LoggerFactory.getLogger(MainCommand.class);

    @Parameter(names = { "--import",
            "-i" }, variableArity = true, description = "Imports a space separated list of XML files in UBY-LMF format."
                    + " Lexical resources must have a 'name' attribute. If there are already present resources with the"
                    + " same name, content will be merged.")
    // streamlined behaviour with respect to
    // https://github.com/DavidLeoni/diversicon/issues/6
    List<String> importXmlPaths = new ArrayList();




    @Parameter(names = { "--author",
            "-a" }, description = "The author of the operations on the db. Required in case of file imports.")
    String author = "";

    @Parameter(names = { "--log",
            "-l" }, description = "Show imports done so far.")
    boolean log = false;

    @Parameter(names = { "--show-import",
            "-s" }, description = "Display detailed import log of given import id.")
    long importIdToShow = -1;

    @Parameter(names = { "--description",
            "-d" }, description = "The description of the operation being performed on the db. Required in case of file imports.")
    String description = "";

    @Parameter(names = { "--process-graph", "-p" }, description = "Processes the db graph to speed up "
            + " operations requiring the transitive closure. When files to import are provided "
            + " processing is automatically performed. To skip it, write -p false")
    Boolean processGraph;

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
     * @throws DivcliException
     */
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

        Wini ini;

        try {
            DiverCli.checkConfDir(diverCli.confDir);

            File confFile = diverCli.findConfFile(DiverCli.DIVERCLI_INI, false);
            ini = new Wini(confFile);
            diverCli.dbConfig.setJdbc_driver_class(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_driver_class", ini));
            diverCli.dbConfig.setDb_vendor(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "db_vendor", ini));
            diverCli.dbConfig.setJdbc_url(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "jdbc_url", ini));
            diverCli.dbConfig.setUser(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "user", ini));
            diverCli.dbConfig.setPassword(DiverCli.extract(DiverCli.DATABASE_SECTION_INI, "password", ini));
            diverCli.dbConfig.setShowSQL(false);

        } catch (Exception ex) {
            throw new DivcliException(diverCli.configIsCorruptedMessage(), ex);
        }

        if (importXmlPaths.size() > 0)

        {
            checkNotEmpty(author, "Tried to import files without '--author' parameter! ");

            checkNotEmpty(description, "Tried to import files without '--description' parameter! ");
            importConfig = new ImportConfig();
            importConfig.setAuthor(author);
            importConfig.setDescription(description);

            if (processGraph == null) {
                importConfig.setSkipAugment(false);
            } else {
                importConfig.setSkipAugment(!processGraph);
            }

            for (String fileUrl : importXmlPaths) {
                importConfig.addLexicalResource(fileUrl);
            }
        }

    }

    void run(){
        if (debug) {
            LOG.info("\n * * * *    DEBUG MODE IS ON    * * * * * \n");
        }

        if (log) {
            diverCli.connect();
            LOG.info("\n");
            LOG.info(diverCli.diversicon.formatDbStatus(true));
            LOG.info("\n");
            LOG.info(diverCli.diversicon.formatImportJobs(false));
        }

        if (importIdToShow > -1) {
            diverCli.connect();
            LOG.info("\n");
            LOG.info(diverCli.diversicon.formatImportJob(importIdToShow, true));
        }                         

        if (importXmlPaths.size() > 0) {
            importXmlFiles();

        } else {
            if (processGraph != null && processGraph) {
                processGraph();
            }
        }
        
    }
    
    /**
     * @since 0.1.0
     */
    private void processGraph() {
        diverCli.connect();
        diverCli.diversicon.processGraph();
    }
    
    /**
     * @since 0.1.0
     */
    private void importXmlFiles() {
        diverCli.connect();

        ImportAppender importAppender = new ImportAppender(diverCli.diversicon);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                org.slf4j.Logger.ROOT_LOGGER_NAME);

        importAppender.setContext(logger.getLoggerContext());
        logger.addAppender(importAppender);

        diverCli.diversicon.importFiles(importConfig);

        logger.detachAppender(importAppender);

    }

    
    

}
