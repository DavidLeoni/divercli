package eu.kidf.diversicon.cli.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import eu.kidf.diversicon.cli.DiverCli;
import eu.kidf.diversicon.cli.exceptions.DiverCliIoException;
import eu.kidf.diversicon.core.internal.Internals;



/**
 * Base to inherit from for DiverCli tests
 * @since 0.1.0
 */
public class DiverCliTestBase {       
    public static final String WORKING = "working";
    
    @Before
    public void beforeMethod() throws IOException {
       
        try {
            Path testHome = Internals.createTempDir("divercli-test-home");


            Path userHome = Internals.createTempDir("divercli-test-home");
            Path workingDir = Files.createDirectories(Paths.get(testHome.toString(), WORKING));
            
            System.setProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME,
                    userHome.toString());
            
            System.setProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR,
                    workingDir.toString());                       
            
            // so it always work even in stupid Eclipse, see see
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683
            System.setProperty(DiverCli.SYSTEM_PROPERTY_TESTING, "true");

        } catch (IOException e) {
            throw new DiverCliIoException("Something went wrong!", e);
        }

    }

    @After
    public void afterMethod() {

        System.setProperty(DiverCli.SYSTEM_PROPERTY_USER_HOME, "");
        System.setProperty(DiverCli.SYSTEM_PROPERTY_WORKING_DIR, "");
        System.setProperty(DiverCli.SYSTEM_PROPERTY_TESTING, "");

    }
}
