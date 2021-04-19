package upariscommonmarkjava.buildsite.parallel;

/** Exception renvoyé quand une liste de contrainte n'est pas correcte */
public final class NeedsException extends Exception {
    public NeedsException(String errorName)
    {
        super(errorName);
    }
}
