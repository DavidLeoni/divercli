package eu.kidf.diversicon.cli.exceptions;


/**
 * A generic runtime exception. 
 * 
 * @since 0.1.0
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