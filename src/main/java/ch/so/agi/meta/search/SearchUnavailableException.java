package ch.so.agi.meta.search;
public final class SearchUnavailableException extends RuntimeException {
    public SearchUnavailableException(Throwable cause) { super("Katalogsuche ist nicht verfügbar", cause); }
}
