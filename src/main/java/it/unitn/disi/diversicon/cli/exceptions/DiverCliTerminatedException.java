package it.unitn.disi.diversicon.cli.exceptions;

/**
 * Exception thrown when app is in testing mode and terminates with an error.
 * It's used instead of calling {@link System#exit(int)} because
 * <a href="http://maven.apache.org/surefire/maven-surefire-plugin/faq.html#vm-
 * termination" target="_blank">Surefire
 * doesn't support it</a>
 * 
 * @since 0.1.0
 */
public class DiverCliTerminatedException extends DiverCliException {
    
    private static final long serialVersionUID = 1L;
    private int code;

    /**
     * @param code see {@link #getCode()}
     * 
     * @since 0.1.0
     */
    public DiverCliTerminatedException(int code) {
        this.code = code;
    }

    /**
     * The exit code, should be negative.
     * 
     * @since 0.1.0
     */
    public int getCode() {
        return code;
    }
}
