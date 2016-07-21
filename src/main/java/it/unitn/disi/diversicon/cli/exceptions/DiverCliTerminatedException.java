package it.unitn.disi.diversicon.cli.exceptions;

/**
 * Using this in testing instead of calling {@link System#exit(int)} because
 * <a href="http://maven.apache.org/surefire/maven-surefire-plugin/faq.html#vm-
 * termination" target="_blank">Surefire
 * doesn't support it</a>
 * 
 * @author da
 *
 */
public class DiverCliTerminatedException extends DiverCliException {

    private int code;

    public DiverCliTerminatedException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
