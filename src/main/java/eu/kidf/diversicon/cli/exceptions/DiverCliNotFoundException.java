package eu.kidf.diversicon.cli.exceptions;

/**
 * A runtime exception to raise when something is not found.
 * 
 * @since 0.1.0
 */
public class DiverCliNotFoundException extends DiverCliException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Creates the NotFoundException using the provided throwable
     */
    public DiverCliNotFoundException(Throwable tr) {
        super(tr);
    }

    /**
     * Creates the NotFoundException using the provided message and throwable
     */
    public DiverCliNotFoundException(String msg, Throwable tr) {
        super(msg, tr);
    }

    /**
     * Creates the NotFoundException using the provided message
     */
    public DiverCliNotFoundException(String msg) {
        super(msg);
    }
}