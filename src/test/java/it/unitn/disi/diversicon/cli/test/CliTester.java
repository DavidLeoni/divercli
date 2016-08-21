package it.unitn.disi.diversicon.cli.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unitn.disi.diversicon.cli.DiverCli;
import it.unitn.disi.diversicon.cli.exceptions.DiverCliIoException;
import it.unitn.disi.diversicon.internal.Internals;

/**
 * @since 0.1.0
 *
 */
public class CliTester {

    private static final Logger LOG = LoggerFactory.getLogger(CliTester.class);

    /**
     * @since 0.1.0
     */
    public static TestEnv createTestEnv() {

        try {
            Path testHome = Internals.createTempDir("divercli-test-home");

            TestEnv ret = new TestEnv(testHome,
                    Files.createDirectories(Paths.get(testHome.toString(), "working")),
                    Files.createDirectories(Paths.get(testHome.toString(), ".config", "divercli")));

            System.setProperty(DiverCli.SYSTEM_GLOBAL_CONF_DIR,
                    ret.getTestGlobalConfDir()
                       .toString());

            System.setProperty(DiverCli.SYSTEM_USER_HOME,
                    ret.getTestHome()
                       .toString());
            
            System.setProperty(DiverCli.SYSTEM_WORKING_DIR,
                    ret.getTestWorkingDir()
                       .toString());
            
            
            
            // so it always work even in stupid Eclipse, see see
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=388683
            System.setProperty(DiverCli.SYSTEM_PROPERTY_TESTING, "true");

            // filter ini to have temp working dir...
            Internals.copyDirFromResource(DiverCli.class, "it/unitn/disi/diversicon/cli/templates/global-conf",
                    ret.getTestGlobalConfDir()
                       .toFile());           
            
            Path globalIniPath = Paths.get(ret.getTestGlobalConfDir()
                                        .toString(),
                    DiverCli.DIVERCLI_INI);
            byte[] encoded = Files.readAllBytes(globalIniPath);
            String filteredGlobalIni = new String(encoded, StandardCharsets.UTF_8)
                                                                            .replace(
                                                                                    DiverCli.DEFAULT_H2_FILE_DB_PATH,
                                                                                    ret.getTestWorkingDir()
                                                                                       .toString() + "/"
                                                                                            + DiverCli.DEFAULT_H2_FILE_DB_PATH);

            Files.write(globalIniPath, filteredGlobalIni.getBytes());
            return ret;
        } catch (IOException e) {
            throw new DiverCliIoException("Something went wrong!", e);
        }
    }

    /**
     * @since 0.1.0
     */
    public static void resetTestEnv() {
        System.setProperty(DiverCli.SYSTEM_GLOBAL_CONF_DIR, "");
    }

}
