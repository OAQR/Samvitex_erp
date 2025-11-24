package com.samvitex.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.StandardBasicTypes;

/**
 * Dialecto de PostgreSQL personalizado que extiende el dialecto estándar de Hibernate
 * para registrar funciones nativas adicionales como 'unaccent'.
 * <p>
 * Al extender PostgreSQLDialect, heredamos todo el comportamiento estándar para PostgreSQL
 * y solo añadimos nuestra funcionalidad específica.
 */
public class CustomPostgreSQLDialect extends PostgreSQLDialect {

    /**
     * Este método es invocado por Hibernate al inicializarse. Lo sobreescribimos
     * para añadir nuestra función personalizada al registro de funciones.
     *
     * @param functionContributions Objeto para registrar las contribuciones de funciones.
     */
    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        functionContributions.getFunctionRegistry()
                .registerPattern(
                        "unaccent",
                        "unaccent(?1)",
                        functionContributions.getTypeConfiguration().getBasicTypeRegistry().getRegisteredType(StandardBasicTypes.STRING.getName())
                );
    }
}