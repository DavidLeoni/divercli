package it.unitn.disi.diversicon.cli.exceptions;

/**
 * A runtime exception to raise when something is not found.
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class DiverCliNotFoundException extends DiverCliException {
    
    private static final long serialVersionUID = 1L;

    private DiverCliNotFoundException(){
        super();
    }
    
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