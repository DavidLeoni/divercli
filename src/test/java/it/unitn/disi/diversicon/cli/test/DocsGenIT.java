package it.unitn.disi.diversicon.cli.test;

import static it.unitn.disi.diversicon.internal.Internals.checkNotBlank;
import static it.unitn.disi.diversicon.internal.Internals.checkNotEmpty;
import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import it.unitn.disi.diversicon.BuildInfo;
import it.unitn.disi.diversicon.ImportJob;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.MainCommand;
import it.unitn.disi.diversicon.cli.commands.InitCommand;
import it.unitn.disi.diversicon.cli.commands.HelpCommand;
import it.unitn.disi.diversicon.cli.commands.ImportShowCommand;
import it.unitn.disi.diversicon.cli.commands.ImportXmlCommand;
import it.unitn.disi.diversicon.cli.commands.LogCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
import it.unitn.disi.diversicon.data.Smartphones;
import it.unitn.disi.diversicon.data.DivWn31;

/**
 * Creates documentation from executions of DiverCli
 *
 * @since 0.1.0
 */

// Options to show a command on database: 

// mkdir my-wn31
// cd my-wn31
// divercli init --db classpath:/.....div-wn31
// divercli log
//
//   OR
//
// divercli --prj wn31  init --db classpath:/.....div-wn31
// cd wn31
// divercli log

public class DocsGenIT extends DiverCliTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(DocsGenIT.class);    

    private final static String DIVERCLI = "divercli";
    
    private static DocAppender docAppender;
    private static ch.qos.logback.classic.Logger logger;

    /**
     * @deprecated kept here just in case we ever need it
     *
     * @since 0.1.0
     */
    private static PrintStream oldSystemOut;

    /**
     * @deprecated kept here just in case we ever need it
     *
     * @since 0.1.0
     */    
    private static PrintStream oldSystemErr;
    
    /**
     * @deprecated kept here just in case we ever need it
     *
     * @since 0.1.0
     */    
    private static ByteArrayOutputStream byteOutputStreamOut;
    
    /**
     * @deprecated kept here just in case we ever need it
     *
     * @since 0.1.0
     */    
    private static ByteArrayOutputStream byteOutputStreamErr;

    public final static HashMap<String, String> evals = new HashMap<>();



    /**
     * (Copied from Josmans)
     * 
     * @since 0.1.0
     */
    public static final Object[] EVAL_CSV_FILE_HEADER = { "expr", "eval" };

    /**
     * 
     * (Copied from Josmans)
     * 
     * @since 0.1.0
     */
    public static final CSVFormat EVAL_CSV_FORMAT = CSVFormat.DEFAULT.withRecordSeparator("\n");

    /**
     * @since 0.1.0
     */
    private static final String MYPRJ = "myprj";
    
    /**
     * @since 0.1.0
     */
    private static final String WN31 = "wn31";

    /**
     * @since 0.1.0
     */
    private String origWorkingDir;

    @Before
    public void beforeMethod() throws IOException {        
        super.beforeMethod();
        origWorkingDir = System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR);
        DiverCli.of(MainCommand.RESET_GLOBAL_CONFIG_OPTION).run();
    }    
    
    
    /**
     * @since 0.1.0    
     */    
    @AfterClass
    public static void afterClass() {
        evals.put("it.unitn.disi.diversicon.Diversicons.DEFAULT_USER",
                it.unitn.disi.diversicon.Diversicons.DEFAULT_USER);
        evals.put("it.unitn.disi.diversicon.Diversicons.DEFAULT_PASSWORD",
                it.unitn.disi.diversicon.Diversicons.DEFAULT_PASSWORD);
        evals.put("it.unitn.disi.diversicon.cli.DiverCli.INI_FILENAME",
                it.unitn.disi.diversicon.cli.DiverCli.INI_FILENAME);
        evals.put("it.unitn.disi.diversicon.cli.DiverCli.INI_PATH",
                it.unitn.disi.diversicon.cli.DiverCli.INI_PATH);
        saveEvalMap(evals, new File("target/apidocs/resources/josman-eval.csv"));
    }

    
    /**
     * (Copied from Josmans)
     * 
     * Writes eval map to a CSV file, creating parent directories if needed.
     *
     * @throws DiverCliIoException
     * 
     * @since 0.1.0
     */
    public static void saveEvalMap(Map<String, String> evals, File file) {
        checkNotNull(file);
        checkNotNull(evals);

        LOG.info("Writing file " + file.getAbsolutePath() + "   ...");

        FileWriter fileWriter = null;

        if (!file.exists()) {
            if (!file.getParentFile()
                     .exists()) {

                boolean ret = file.getParentFile()
                                  .mkdirs();
                if (!ret) {
                    throw new DiverCliIoException("Couldn't create directory " + file.getParentFile()
                                                                                     .getAbsolutePath());
                }
            }
        }

        CSVPrinter csvFilePrinter = null;

        try {

            fileWriter = new FileWriter(file);
            csvFilePrinter = new CSVPrinter(fileWriter, EVAL_CSV_FORMAT);
            csvFilePrinter.printRecord(EVAL_CSV_FILE_HEADER);

            for (String expr : evals.keySet()) {
                List<String> evalRecord = new ArrayList<>();
                checkNotEmpty(expr, "Invalid eval expression!");
                evalRecord.add(expr);
                String val = evals.get(expr);
                checkNotNull(val);
                evalRecord.add(val);
                csvFilePrinter.printRecord(evalRecord);
            }

            LOG.info("CSV file created successfully: " + file.getAbsolutePath());

        } catch (IOException ex) {
            throw new DiverCliIoException("Error while writing CSV file " + file.getAbsolutePath(), ex);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                LOG.error("Error while flushing/closing fileWriter/csvPrinter for file " + file.getAbsolutePath(), e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets same logback config as in main, so we can be realistic.
     * 
     * @since 0.1.0
     */
    private static void loadMainLogbackConfig() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        try {

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(DiverCli.class.getResourceAsStream("/logback.xml")); // loads
                                                                                          // logback
                                                                                          // file
        } catch (JoranException je) {
            // StatusPrinter will handle this
        } catch (Exception ex) {
            ex.printStackTrace(); // Just in case, so we see a stacktrace

        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context); // Internal status
                                                              // data is printed
                                                              // in case of
                                                              // warnings or
                                                              // errors.

    }

    /**
     * Restores test config
     * 
     * @since 0.1.0
     */
    private static void restoreTestLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            String path = System.getProperty("logback.configurationFile");
            InputStream res;
            if (path == null) {
                res = DiverCli.class.getResourceAsStream("/logback-test.xml");
                if (res == null) {
                    res = DiverCli.class.getResourceAsStream("/logback.xml");
                }
            } else {
                res = new FileInputStream(path);
            }
            configurator.doConfigure(res); // loads logback file

        } catch (JoranException je) {
            // StatusPrinter will handle this
        } catch (Exception ex) {
            ex.printStackTrace(); // Just in case, so we see a stacktrace

        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context); // Internal status
                                                              // data is printed
                                                              // in case of
                                                              // warnings or
                                                              // errors.

    }

    
    /**
     * @since 0.1.0    
     */
    public static void startCaptureSlf4j() {

        loadMainLogbackConfig();

        docAppender = new DocAppender();

        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                org.slf4j.Logger.ROOT_LOGGER_NAME);

        docAppender.setContext(logger.getLoggerContext());
        logger.addAppender(docAppender);

    }

    /**
     * @since 0.1.0    
     */    
    public static String stopCaptureSlf4j() {

        logger.detachAppender(docAppender);

        String ret = docAppender.asString();
        restoreTestLogback();
        return ret;
    }

    /**
     * @deprecated Method kept here just in case we ever need it
     *
     * @since 0.1.0
     */    
    public static void startCaptureSystemOutErr() {
        // Create a stream to hold the output
        byteOutputStreamOut = new ByteArrayOutputStream();
        byteOutputStreamErr = new ByteArrayOutputStream();
        PrintStream psOut = new PrintStream(byteOutputStreamOut);
        PrintStream psErr = new PrintStream(byteOutputStreamErr);
        // IMPORTANT: Save the old System.out!
        oldSystemOut = System.out;
        oldSystemErr = System.err;
        // Tell Java to use your special stream
        System.setOut(psOut);
        System.setErr(psErr);
    }

    /**
     * @deprecated Method kept here just in case we ever need it
     *
     * @since 0.1.0
     */    
    public String stopCaptureSystemOutErr() {
        System.out.flush();
        System.setOut(oldSystemOut);
        // Show what happened
        byteOutputStreamOut.toString();

        System.err.flush();
        System.setErr(oldSystemErr);
        // Show what happened
        return byteOutputStreamErr.toString();

    }

    /**
     * @deprecated Method kept here just in case we ever need it
     *
     * @since 0.1.0
     */
    public static String logSystemOutErr() {

        System.out.println("\n\n ******  Going to fucking log?  ****** \n\n");

        // Print some output: goes to your special stream

        startCaptureSystemOutErr();
        DiverCli.of(LogCommand.CMD)
                .run();

        startCaptureSystemOutErr();
        String ret = byteOutputStreamOut.toString() + byteOutputStreamErr.toString();
        System.out.println("\n\n ************   I GOT THIS ****************   \n\n " + ret);
        return ret;
    }

        
    
    /**
     * Execs a bash command
     *  
     * @since 0.1.0
     */
    private void bash(String key, String line){
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR)));
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        
        try {
            int exitValue = executor.execute(cmdLine);
            if (exitValue > 0){
                Assert.fail("Command returned value greater than 0:  "+ exitValue);
            }
            String val = "> " + line + "\n\n" + outputStream.toString();
            LOG.info("\n" + val);
            evals.put(key, val);
        } catch (IOException e) { 
            throw new Error("Failure while executing bash command!", e);
        }        
    }

    
    

    /**
     * 
     * Associates to args key something like this:
     * <pre>
     * ```bash
     * > ./divercli help import-xml
     * 
     * Bla bla bla bla
     * bla bla bla bla
     * ```
     * </pre>
     * 
     * @since 0.1.0
     */
    private DiverCli diver(String key, String... args) { 
        
        startCaptureSlf4j();
        
        DiverCli cli = DiverCli.of(args);
        
        if (evals.containsKey(key)){
            LOG.debug("Found already evaluated cli command key '" + key + "', recalculating...");
        } 
        
        cli.run();                
        
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = args.length; i < il; i++) {
            if (i > 0)
                sbStr.append(" ");
            sbStr.append(args[i]);
        }

        String cap = stopCaptureSlf4j();

        String val = "```bash\n"
                + "> " + DIVERCLI + " " + sbStr.toString() + "\n"
                + cap.replace(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR) + "/", "")
                     .replace(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR), "")
                     .replace(System.getProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME) + "/", "/home/divergeek/")
                     .replace(System.getProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME), "/home/divergeek/")
                + "\n"
                + "```\n";
        
        evals.put(key, val);
        
        return cli;
    }

    

    @Test
    public void help() {
        diver("help",  HelpCommand.CMD);
        
        diver("help.importXml", 
                HelpCommand.CMD, ImportXmlCommand.CMD);
    }

    @Test
    public void wn31Init() {       
                
        diver("wn31.init",
                "--prj", "wn31",
                InitCommand.CMD, "--db", DivWn31.H2DB_URI 
                );  
        cd("wn31.cd", "wn31");
        bash("wn31.dir", "dir");
    }
    
    @Test
    public void emptyInit(){
        diver("empty.init",
                "--prj", MYPRJ,
                InitCommand.CMD);
        
        cd("empty.cd", MYPRJ);
        bash("empty.dir", "dir");
    }

    /*
    @Test
    public void emptyDir(){
        diver("empty.dir",
                "--prj", MYPRJ,
                InitCommand.CMD, "--db", DivWn31.H2DB_URI);
    } */      
    
    
    
    /**
     * Sets working dir with respect to {@link #origWorkingDir} 
     * and saves the cd command in the key.
     *
     * @since 0.1.0
     */
    private void cd(String key, String subdir){
        checkNotBlank(key, "Invalid key");
        checkNotBlank(subdir, "Invalid dir!");
        
        cd(subdir);
        
        String val = "> cd " + subdir;
        
        LOG.info("\n" + val);
                        
        
        evals.put(key, val);
    }
    
    /**
     * Sets working dir with respect to {@link #origWorkingDir} 
     *
     * @since 0.1.0
     */
    private void cd(String subdir){
        checkNotBlank(subdir, "Invalid sub dir");
        String post;
                
        String pre;
        if (origWorkingDir.endsWith("/")){
            pre = "";
        } else {
            pre = "/";
        }
        
        if (subdir.endsWith("/")){
            post = "";
        } else {
            post = "/";            
        }       

        System.setProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR, 
                origWorkingDir + pre + subdir + post);
    }
    
    @Test
    public void log() throws IOException {
               
        wn31Init();
        cd("wn31");
                
        DiverCli cli = diver("wn31.log", LogCommand.CMD);               
        cli.connect();
        List<ImportJob> jobs = cli.getDiversicon().getImportJobs();
        assertEquals(1, jobs.size());
        long id = jobs.get(0).getId();
        
        diver("wn31.importShow", ImportShowCommand.CMD, Long.toString(id));
    }
    
    @Test
    public void reset(){
        diver("resetGlobalConfig", MainCommand.RESET_GLOBAL_CONFIG_OPTION);        
    }
          
    @Test
    public void importSampleXml(){
        
        emptyInit();
        cd(MYPRJ);       
        diver(ImportXmlCommand.CMD, Smartphones.of().getSqlUri());
        
    }
}
