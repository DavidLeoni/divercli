package it.unitn.disi.diversicon.cli.test;

import static it.unitn.disi.diversicon.internal.Internals.checkNotNull;

import java.nio.file.Path;

/**
 * @since 0.1.0
 *
 */
public class TestEnv {
    private Path testHome;
    private Path testWorkingDir;        
    private Path testConfDir;
        
    /**
     * @since 0.1.0
     *
     */    
    public TestEnv(Path testHome, Path testWorkingDir, Path testConfDir) {
        super();
        
        checkNotNull(testHome);
        checkNotNull(testWorkingDir);
        checkNotNull(testConfDir);
        
        this.testHome = testHome;
        this.testWorkingDir = testWorkingDir;
        this.testConfDir = testConfDir;
    }
    
    /**
     * @since 0.1.0
     *
     */    
    public Path getTestHome() {
        return testHome;
    }
    
    /**
     * @since 0.1.0
     *
     */    
    public Path getTestWorkingDir() {
        return testWorkingDir;
    }
    
    /**
     * @since 0.1.0
     *
     */    
    public Path getTestConfDir() {
        return testConfDir;
    }       
    

}
