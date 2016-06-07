package it.unitn.disi.diversicon.cli.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
import it.unitn.disi.diversicon.cli.DiverCli;
import static it.unitn.disi.diversicon.test.DivTester.createDbConfig;

public class DiverCliTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiverCliTest.class);

    private DBConfig dbConfig;

    @Before
    public void beforeMethod() {
        dbConfig = createDbConfig();
    }

    @After
    public void afterMethod() {
        dbConfig = null;
    }
    
    @Test
    public void test() throws IOException{
        
        try {
            DiverCli.main("-666");
            Assert.fail();
        } catch (ParameterException ex){
            
        }
        
        DiverCli.main();
                
    }
    
    @Test
    public void testReset(){
        DiverCli.main("--reset-conf");
    }
    
    @Test
    public void testCustomConfFolder() throws IOException{
        
        Path path = Files.createTempDirectory(DiverCli.CMD + "-test"); 
        
        FileUtils.copyDirectory(new File(DiverCli.CONF_TEMPLATE), path.toFile());
        
        DiverCli.main("--conf ", path.toString()) ;

    }
    
    @Test
    public void testIni() throws InvalidFileFormatException, IOException {
        LOG.info("I logged something!");
        Wini ini = new Wini(new DiverCli().findOrCreateConfFile(DiverCli.DIVERCLI_INI));
        assertEquals(null, ini.get("666", "666", String.class));
        assertEquals(null, ini.get("Database", "666", String.class));
        assertEquals("root", ini.get("Database", "user", String.class));
    }

}
