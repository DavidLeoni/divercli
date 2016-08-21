package it.unitn.disi.diversicon.cli.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.internal.Internals;



/**
 * Base to inherit from for DiverCli tests
 * @since 0.1.0
 */
public class DiverCliTestBase {
    
    TestEnv testEnv;
    
    @Before
    public void beforeMethod() throws IOException {

       testEnv = CliTester.createTestEnv();
        
    }

    @After
    public void afterMethod() {
        testEnv = null;
        CliTester.resetTestEnv();
    }
}
