package it.unitn.disi.diversicon.cli.exceptions;

/**
 * @since 0.1
 * 
 */
public class DiverCliIoException extends DiverCliException {

    private static final long serialVersionUID = 1L;


    /**
     * @since 0.1
     * 
     */
    public DiverCliIoException() {
        super();
    }

    /**
     * @since 0.1
     * 
     */
    public DiverCliIoException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * @since 0.1
     * 
     */
    public DiverCliIoException(String msg) {
        super(msg);
    }

    /**
     * @since 0.1
     * 
     */
    public DiverCliIoException(Throwable tr) {
        super(tr);
    }

}
