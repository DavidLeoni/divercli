package it.unitn.disi.diversicon.cli.exceptions;

/**
 * @since 0.1.0
 */
public class InvalidConfigException extends DiverCliException {

    private static final long serialVersionUID = 1L;


    /**
     * @since 0.1.0
     * 
     */
    public InvalidConfigException() {
        super();
    }

    /**
     * @since 0.1.0
     * 
     */
    public InvalidConfigException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * @since 0.1.0
     * 
     */
    public InvalidConfigException(String msg) {
        super(msg);
    }

    /**
     * @since 0.1.0
     * 
     */
    public InvalidConfigException(Throwable tr) {
        super(tr);
    }

}
