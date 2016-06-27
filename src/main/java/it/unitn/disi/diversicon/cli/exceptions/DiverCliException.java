package it.unitn.disi.diversicon.cli.exceptions;


/**
 * A generic runtime exception. 
 * 
 * @author David Leoni <david.leoni@unitn.it>
 * @since 0.1
 */
public class DiverCliException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    protected DiverCliException(){
        super();
    }
    
    public DiverCliException(Throwable tr) {
        super(tr);
    }

    public DiverCliException(String msg, Throwable tr) {
        super(msg, tr);
    }

    public DiverCliException(String msg) {
        super(msg);
    }
}