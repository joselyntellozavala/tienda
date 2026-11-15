package es.joselyn.proyecto.config;

public enum DatabaseEngine {
    MYSQL("sql/esquema-mysql.sql", "sql/procedimientos-mysql.sql"),
    H2("sql/esquema-h2.sql", null);

    private final String scriptEsquema;
    private final String scriptProcedimientos;

    DatabaseEngine(String scriptEsquema, String scriptProcedimientos) {
        this.scriptEsquema = scriptEsquema;
        this.scriptProcedimientos = scriptProcedimientos;
    }

    public String getScriptEsquema() {
        return scriptEsquema;
    }

    public String getScriptProcedimientos() {
        return scriptProcedimientos;
    }
}