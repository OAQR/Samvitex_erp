/**
 * V5: Instala la extensión 'unaccent' en la base de datos.
 *
 * Esta extensión proporciona la función unaccent(), que elimina los acentos
 * y otros diacríticos de una cadena de texto. Es esencial para implementar
 * búsquedas insensibles a los acentos de manera eficiente en PostgreSQL.
 *
 * Se ejecuta una sola vez por base de datos. Si la extensión ya está
 * instalada, el comando no hará nada.
 */
CREATE EXTENSION IF NOT EXISTS unaccent;
-- fin de nuevo archivo