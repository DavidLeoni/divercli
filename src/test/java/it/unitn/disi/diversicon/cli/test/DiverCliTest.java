package it.unitn.disi.diversicon.cli.test;

import static it.unitn.disi.diversicon.internal.Internals.checkNotBlank;
import static it.unitn.disi.diversicon.test.LmfBuilder.lmf;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;

import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;
import de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics;
import it.unitn.disi.diversicon.BuildInfo;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.ImportJob;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.MainCommand;
import it.unitn.disi.diversicon.cli.commands.DbAugmentCommand;
import it.unitn.disi.diversicon.cli.commands.DbResetCommand;
import it.unitn.disi.diversicon.cli.commands.InitCommand;
import it.unitn.disi.diversicon.cli.commands.ExportSqlCommand;
import it.unitn.disi.diversicon.cli.commands.ExportXmlCommand;
import it.unitn.disi.diversicon.cli.commands.HelpCommand;
import it.unitn.disi.diversicon.cli.commands.ImportShowCommand;
import it.unitn.disi.diversicon.cli.commands.ImportXmlCommand;
import it.unitn.disi.diversicon.cli.commands.LogCommand;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliNotFoundException;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliTerminatedException;
import it.unitn.disi.diversicon.data.DivWn31;
import it.unitn.disi.diversicon.internal.Internals;
import it.unitn.disi.diversicon.test.DivTester;
import static it.unitn.disi.diversicon.cli.MainCommand.PRJ_OPTION;
import static it.unitn.disi.diversicon.cli.test.CliTester.initEmpty;

/**
 * @since 0.1.0
 */
public class DiverCliTest extends DiverCliTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(DiverCliTest.class);         

    
    /**
     * 
     * Imports provided lexical resource and returns its xml file.
     * @since 0.1.0
     */
    private File importLexRes(LexicalResource lexRes) {
        File xmlFile = DivTester.writeXml(lexRes);
        DiverCli cli1 = DiverCli.of(ImportXmlCommand.CMD,
                "--author", "a",
                "--description", "d",
                xmlFile.getAbsolutePath());

        cli1.run();
        return xmlFile;
    }

    /**
     * Returns a non-existing file in a newly created temporary directory.
     * 
     * @param extension
     *            something like 'sql' or 'xml' without the dot
     *            l
     * @since 0.1.0
     */
    private File getNonExistingFile(String extension) {
        checkNotBlank(extension, "Invalid extension!");
        
        Path out;
 
        out = Internals.createTempDir("divercli-test");

        return new File(out.toString() + "/test." + extension);
    }
    
    
    /**
     * @since 0.1.0
     */
    private void readZipped(File zipFile) {

        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze;
            ze = zis.getNextEntry();

            while (ze != null) {
                ze = zis.getNextEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong!", e);
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException ex) {                    
                    LOG.error("Couldn't close ZipInputStream!", ex);
                }
            }
        }
    }


    /**
     * @since 0.1.0
     */
    @Test
    public void testWrongOption() throws IOException {

        try {
            DiverCli.of("-666")
                    .run();
            Assert.fail();
        } catch (ParameterException ex) {
        }
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testWrongCommand() throws IOException {

        try {
            DiverCli.of("666")
                    .run();
            Assert.fail();
        } catch (MissingCommandException ex) {

        }
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testDidYouMeanCommand(){
        try {
            DiverCli.main(LogCommand.CMD + "g");
            Assert.fail("Shouldn't arrive here!");
        } catch (DiverCliTerminatedException ex){
            
        }
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testHelpNoArgs(){
        DiverCli.of(HelpCommand.CMD).run();
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testHelp(){
        DiverCli.of(HelpCommand.CMD, LogCommand.CMD).run();
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testHelpSimilar(){
        DiverCli.of(HelpCommand.CMD, LogCommand.CMD + "o").run();
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testHelpCantFind(){
        DiverCli.of(HelpCommand.CMD, "666").run();
    }
    
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testUsage()  {
        DiverCli.of().run();
    }
    
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testNoCommandGiven()  {
        DiverCli.of("--prj",".").run();
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testReset() {
        DiverCli.of(MainCommand.RESET_GLOBAL_CONFIG_OPTION)
                .run();
        assertTrue(CliTester.getTestGlobalConfDir().toFile()
                              .exists());
       
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testCustomConfFolderExisting() throws IOException {
        

        Internals.copyDirFromResource(DiverCli.class, 
                DiverCli.GLOBAL_CONF_TEMPLATE_DIR, DiverCli.globalConfDirPath());

        DiverCli.of().run();
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testProjectNonExistingDir() throws IOException {        

        try {
            DiverCli.of(MainCommand.PRJ_OPTION, "666",
                        LogCommand.CMD)
                    .run();
            Assert.fail("Shouldn't arrive here!");
        } catch (DiverCliNotFoundException ex) {

        }

    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testProjectEmptyDir() throws IOException {

        Path emptyDir = Internals.createTempDir(DiverCli.CMD + "-test");

        try {
            DiverCli.of("--prj ", emptyDir.toString(),
                        LogCommand.CMD)
                    .run();
            Assert.fail("Shouldn't arrive here!");
        } catch (DiverCliNotFoundException ex) {

        }

    }

    /**
     * Tests only Ini4J library
     * 
     * @since 0.1.0
     */
    @Test
    public void testIni() throws InvalidFileFormatException, IOException {
        LOG.info("I logged something!");
        DiverCli cli = DiverCli.of("--debug");
        cli.run();
        Wini ini = new Wini(cli.findConfigFile(DiverCli.INI_FILENAME));
        assertEquals(null, ini.get("666", "666", String.class));
        assertEquals(null, ini.get("Database", "666", String.class));
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testImportXmlBadParams() {
        DiverCli.of(PRJ_OPTION, "db", InitCommand.CMD).run();
        
        File xmlFile = DivTester.writeXml(DivTester.GRAPH_1_HYPERNYM);

        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath())
                    .run();
            Assert.fail("Should need author!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }

        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", " ")
                    .run();
            Assert.fail("Should need author!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }

        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "")
                    .run();
            Assert.fail("Should need author!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }

        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "a")
                    .run();
            Assert.fail("Need description!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }

        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "a", "--description", "")
                    .run();
            Assert.fail("Need description!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }

        try {
            DiverCli.of(ImportXmlCommand.CMD, "--author", "a", "--description", " ", xmlFile.getAbsolutePath())
                    .run();
            Assert.fail("Need description!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }

    }

    /**
     * This also tests MainCommand is working
     * 
     * @since 0.1.0
     */
    @Test
    public void testDebug() {
        DiverCli cli = DiverCli.of("--debug");
        cli.run();
    }

    @Test
    public void testImportXml() {
        
        initEmpty();
        
        File xmlFile = DivTester.writeXml(DivTester.GRAPH_1_HYPERNYM);

        DiverCli cli = DiverCli.of(ImportXmlCommand.CMD,
                "--author", "a",
                "--description", "d",
                xmlFile.getAbsolutePath());

        cli.run();

        Diversicon div = Diversicon.connectToDb(cli.dbConfig());
        DivTester.checkDb(DivTester.GRAPH_1_HYPERNYM, div);
        div.getSession()
           .close();
    }

   

    /**
     * @since 0.1.0
     */
    @Test
    public void testLog() {
        initEmpty();
        DiverCli cli2 = DiverCli.of(LogCommand.CMD);
        cli2.run();
        // todo how to improve?
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testImportShow() {
        initEmpty();
        File xmlFile = DivTester.writeXml(DivTester.GRAPH_1_HYPERNYM);
        DiverCli cli1 = DiverCli.of(ImportXmlCommand.CMD, xmlFile.getAbsolutePath(), "--author", "Test author",
                "--description", "Test description");
        cli1.run();

        cli1.connect();
        ImportJob job = cli1.getDiversicon()
                            .getImportJobs()
                            .get(0);
        cli1.disconnect();

        DiverCli cli2 = DiverCli.of(ImportShowCommand.CMD, Long.toString(job.getId()));
        cli2.run();
    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testDbAugment() {
        
        initEmpty();               
        
        LexicalResource res = lmf().lexicon()
                                   .synset()
                                   .lexicalEntry()
                                   .synset()
                                   .synsetRelation(ERelNameSemantics.HYPONYM, 1)
                                   .build();

        File xmlFile = DivTester.writeXml(res);

        DiverCli cli1 = DiverCli.of(ImportXmlCommand.CMD,
                "--skip-augment",
                "--author=testAuthor",
                "--description=testDescr",
                xmlFile.getAbsolutePath());
        cli1.run();

        cli1.connect();
        assertEquals(1, cli1.getDiversicon()
                            .getSynsetRelationsCount());
        cli1.disconnect();

        DiverCli cli2 = DiverCli.of(DbAugmentCommand.CMD);
        cli2.run();

        cli2.connect();

        // graph should be normalized with hypernyms
        assertTrue(cli2.getDiversicon()
                       .getSynsetRelationsCount() > 1);
        cli2.disconnect();
    }

    @Test
    public void testExportXmlMissingXmlPath() {
        initEmpty();
        try {
            DiverCli cli = DiverCli.of(ExportXmlCommand.CMD);
            cli.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (ParameterException ex) {
            LOG.debug("Expected exception:", ex);
        }
    }

    @Test
    public void testExportXmlMissingLexicalResourceName() throws IOException {
        
        initEmpty();
        
        Path out = Internals.createTempDir("divercli-test");

        try {
            DiverCli cli = DiverCli.of(ExportXmlCommand.CMD, out.toString() + "/test.xml");
            cli.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (ParameterException ex) {
            LOG.debug("Expected exception:", ex);
        }
    }

    @Test
    public void testExportXmlWrongLexicalResource() throws IOException {

        initEmpty();
        
        Path out = Internals.createTempDir("divercli-test");

        try {
            DiverCli cli = DiverCli.of(ExportXmlCommand.CMD, "--name", "666", out.toString() + "/test.xml");
            cli.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (ParameterException ex) {
            LOG.debug("Expected exception:", ex);
        }
    }

    @Test
    public void testExportXmlExistingXml() throws IOException {

        initEmpty();
        
        importLexRes(DivTester.GRAPH_1_HYPERNYM);

        Path out = Internals.createTempFile("divercli-test", "xml");

        try {
            DiverCli cli2 = DiverCli.of(ExportXmlCommand.CMD,
                    "--name", DivTester.GRAPH_1_HYPERNYM.getName(),
                    out.toString());
            cli2.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }
    }

    @Test
    public void testExportXml() throws IOException {

        initEmpty();
        
        File outF = getNonExistingFile("xml");

        importLexRes(DivTester.GRAPH_1_HYPERNYM);

        DiverCli cli2 = DiverCli.of(ExportXmlCommand.CMD,
                "--name", DivTester.GRAPH_1_HYPERNYM.getName(),
                outF.getAbsolutePath());
        cli2.run();

        assertTrue(outF.exists());
        assertTrue(outF.length() > 0);

    }
    
    @Test
    public void testExportXmlCompressed() throws IOException {
        initEmpty();
        
        File outF = getNonExistingFile("zip");

        importLexRes(DivTester.GRAPH_1_HYPERNYM);

        DiverCli cli2 = DiverCli.of(ExportXmlCommand.CMD,
                "--name", DivTester.GRAPH_1_HYPERNYM.getName(),
                "--compress",
                outF.getAbsolutePath());
        cli2.run();

        assertTrue(outF.exists());
        assertTrue(outF.length() > 0);
        
        readZipped(outF);

    }

    @Test
    public void testExportSqlMissingSqlPath() {
        initEmpty();
        
        try {
            DiverCli cli = DiverCli.of(ExportSqlCommand.CMD);
            cli.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (ParameterException ex) {
            LOG.debug("Expected exception:", ex);
        }
    }

    @Test
    public void testExportSqlExistingSql() throws IOException {
        initEmpty();
        
        importLexRes(DivTester.GRAPH_1_HYPERNYM);

        Path out = Internals.createTempFile("divercli-test", "sql");

        DiverCli cli = DiverCli.of(ExportSqlCommand.CMD,
                out.toString());

        try {
            cli.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex) {
            LOG.debug("Expected exception:", ex);
        }
    }

    @Test
    public void testExportSql() throws IOException {
        initEmpty();
        
        File outF = getNonExistingFile("sql");

        importLexRes(DivTester.GRAPH_1_HYPERNYM);

        DiverCli cli = DiverCli.of(ExportSqlCommand.CMD,
                outF.getAbsolutePath());
        cli.run();

        assertTrue(outF.exists());
        assertTrue(outF.length() > 0);

    }

    /**
     * @since 0.1.0
     */
    @Test
    public void testExportSqlCompressed() throws IOException {
        initEmpty();

        File outF = getNonExistingFile(".sql.zip");
        importLexRes(DivTester.GRAPH_1_HYPERNYM);

        DiverCli cli = DiverCli.of(ExportSqlCommand.CMD,
                "--compress",
                outF.getAbsolutePath());
        cli.run();

        assertTrue(outF.exists());
        assertTrue(outF.length() > 0);

        readZipped(outF);

    }


    
    /**
     * @since 0.1.0
     */    
    @Test
    public void testDbInitWrongSql_1(){
        
        initEmpty();
        
        DiverCli cli1 = DiverCli.of(                
                InitCommand.CMD,
                "--sql");
        try {
            cli1.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (ParameterException ex){
            
        }                      
               
    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testDbInitWrongSql_2(){
        
        initEmpty();        
        
        DiverCli cli = DiverCli.of(
                InitCommand.CMD,
                "--sql", "");
        try {        
            cli.run();
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex){
            
        }                      
    }
    
    
    /**
     * @since 0.1.0
     */    
    @Test
    public void testDbReset(){
        
        initEmpty();
        
        DiverCli cli1 = DiverCli.of(DbResetCommand.CMD);
        
        cli1.run();
        
        assertTrue(Diversicons.exists(cli1.dbConfig()));
        DiverCli.of(DbResetCommand.CMD).run(); // shouldn't complain
        assertTrue(Diversicons.exists(cli1.dbConfig()));
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testInitTwice() throws IOException {
        
        initEmpty();
        
        try {
            initEmpty();
            Assert.fail("Shouldn't be able to init twice!");
        } catch (DiverCliException ex){
            
        };
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testInitToDirectory() throws IOException {

        Path dir = Internals.createTempDir("divercli-test");

        String target = dir.toString() + "/test";
        
        DiverCli cli = DiverCli.of(
                "--prj", target,
                InitCommand.CMD);

        cli.run();

        File outDb = new File(target + "/test.h2.db");

        assertTrue(outDb.exists());
        assertTrue(outDb.length() > 0);
        
        File outIni = new File(target + "/" + DiverCli.INI_FILENAME);

        assertTrue(outIni.exists());
        assertTrue(outIni.length() > 0);
                
        Diversicon div = Diversicon.connectToDb(cli.dbConfig());
        div.getSession()
           .close();
    }
    
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testInitToCurrentDirectory() throws IOException {        
        
        DiverCli cli = DiverCli.of(InitCommand.CMD);

        cli.run();

        File workingDir = new File(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR));
        
        File outDb = new File(workingDir,
                              workingDir.getName() + ".h2.db");

        assertTrue(outDb.exists());
        assertTrue(outDb.length() > 0);
        
        File outIni = new File(System.getProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR), DiverCli.INI_FILENAME);

        assertTrue(outIni.exists());
        assertTrue(outIni.length() > 0);
                
        Diversicon div = Diversicon.connectToDb(cli.dbConfig());
        div.getSession()
           .close();
    }    

    /**
     * @since 0.1.0
     */
    @Test
    @Ignore
    public void testValidation(){
        Internals.readData(Diversicons.SCHEMA_1_0_CLASSPATH_URL);
    }
    
    /**
     * @since 0.1.0
     */
    @Test
    public void testDiversiconBuildInfo(){
        BuildInfo bfDiv = BuildInfo.of(Diversicon.class);
        LOG.debug(bfDiv.getScmUrl());
        assertTrue(bfDiv.getScmUrl().toLowerCase().contains("diversicon"));
        
        BuildInfo bfWn31 = BuildInfo.of(DivWn31.class);
        LOG.debug(bfWn31.getScmUrl());
        assertTrue(bfWn31.getScmUrl().toLowerCase().contains("diversicon-wordnet-3.1"));
    }
}
