package it.unitn.disi.diversicon.cli.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import it.unitn.disi.diversicon.Diversicon;
import it.unitn.disi.diversicon.Diversicons;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.internal.Internals;
import it.unitn.disi.diversicon.test.DivTester;



/**
 * @since 0.1.0
 */
public class DiverCliTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiverCliTest.class);

    private Path testHome;
    private Path testConfDir;
    private DBConfig dbConfig;

    @Before
    public void beforeMethod() throws IOException {
        
        testHome = Files.createTempDirectory("divercli-test-home");        
        testConfDir = Files.createDirectories(Paths.get(testHome.toString(), ".config" ,"divercli"));
        Internals.copyDirFromResource(DiverCli.class, "it/unitn/disi/divercli/conf-template", testConfDir.toFile());
        
        System.setProperty(DiverCli.SYSTEM_CONF_DIR,
                testConfDir.toString());
        
        dbConfig = DivTester.createNewDbConfig();
        Diversicons.dropCreateTables(dbConfig);
        
        FileUtils.deleteDirectory(new File("db/"));
    }

    
    
    @After
    public void afterMethod() {
        dbConfig = null;
        testHome = null;
        testConfDir = null;
        System.setProperty(DiverCli.SYSTEM_CONF_DIR, "");
    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testWrongCommand() throws IOException{               
                
        try {
            DiverCli.of("-666").run();
            Assert.fail();
        } catch (ParameterException ex){
            
        }                     
    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testNoCommand() throws IOException {
        DiverCli.of();
    }
    
    /**
     * @since 0.1.0
     */    
    @Test
    public void testReset(){
        DiverCli.of("--reset-conf").run();
        assertTrue(testConfDir.toFile().exists());
    }
    
    /**
     * @since 0.1.0
     */    
    @Test
    public void testCustomConfFolderExisting() throws IOException {
        
        Path existingPath = Files.createTempDirectory(DiverCli.CMD + "-test"); 
        
        Internals.copyDirFromResource(DiverCli.class, DiverCli.CONF_TEMPLATE_DIR, existingPath.toFile());        
         
        DiverCli.of("--conf ", existingPath.toString()) ;
    }
    
    /**
     * @since 0.1.0
     */    
    @Test
    public void testCustomConfFolderNonExisting() throws IOException {
        System.setProperty(DiverCli.SYSTEM_CONF_DIR, "");
               
        try {
            DiverCli.of("--conf", "666").run() ;
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex){
            
        }
                
    }
    
    /**
     * @since 0.1.0
     */    
    @Test
    public void testCustomConfFolderEmptyDir() throws IOException {
        
        Path emptyDir = Files.createTempDirectory(DiverCli.CMD + "-test");
        
        try {
            DiverCli.of("--conf ", emptyDir.toString()).run() ;
            Assert.fail("Shouldn't arrive here!");
        } catch (Exception ex){
            
        }
        
    }
    
    /**
     * Tests only Ini4J library
     * @since 0.1.0
     */
    @Test
    public void testIni() throws InvalidFileFormatException, IOException {
        LOG.info("I logged something!");
        DiverCli cli = DiverCli.of("--debug");
        cli.run();
        Wini ini = new Wini(cli.findConfFile(DiverCli.DIVERCLI_INI, false));
        assertEquals(null, ini.get("666", "666", String.class));
        assertEquals(null, ini.get("Database", "666", String.class));
        assertEquals("root", ini.get("Database", "user", String.class));
    }

    /**
     * @since 0.1.0
     */    
    @Test
    public void testImportXml(){
        
        File xmlFile = DivTester.writeXml(DivTester.GRAPH_1_HYPERNYM);
        
        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath()).run();
            Assert.fail("Should need author!");
        } catch (Exception ex){
        }
        
        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", " ").run();
            Assert.fail("Should need author!");
        } catch (Exception ex){
        }
        
        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "").run();
            Assert.fail("Should need author!");
        } catch (Exception ex){
        }
        
        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "a").run();
            Assert.fail("Need description!");
        } catch (Exception ex){
        }
        
        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "a", "--description", "").run();
            Assert.fail("Need description!");
        } catch (Exception ex){
        }
        
        try {
            DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "a", "--description", " ").run();
            Assert.fail("Need description!");
        } catch (Exception ex){
        }
        
        DiverCli cli = DiverCli.of("--import ", xmlFile.getAbsolutePath(), "--author", "a", "--description", "d");
        
        cli.run();
              
        Diversicon div = Diversicon.connectToDb(cli.getDbConfig());
        DivTester.checkDb(DivTester.GRAPH_1_HYPERNYM, div);
        div.getSession().close();
    }
    
   
}
