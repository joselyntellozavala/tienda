package es.joselyn.proyecto.config;

public enum DatabaseEngine {
    MYSQL("sql/esquema-mysql.sql"),
    H2("sql/esquema-h2.sql");

    private final String scriptEsquema;

    DatabaseEngine(String scriptEsquema) {
        this.scriptEsquema = scriptEsquema;
    }

    public String getScriptEsquema() {
        return scriptEsquema;
    }
}