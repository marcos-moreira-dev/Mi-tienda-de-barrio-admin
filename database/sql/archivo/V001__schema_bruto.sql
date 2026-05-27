-- Mi tienda de barrio admin
-- V001 - schema bruto / 1FN legacy
--
-- Este archivo conserva el corte bruto formal de la base de datos antes de llegar a la 3FN oficial.
-- Se mantiene por trazabilidad documental y legacy del procedimiento de base de datos.
--
-- Para ejecución real de la aplicación usar:
-- database/sql/migrations/V001__schema_3fn_oficial.sql

-- El contenido físico de este archivo replica el corte 1FN legacy.
-- Si se necesita revisar la versión explicada, ver:
-- database/sql/archivo/V001__schema_1fn_legacy.sql

.read V001__schema_1fn_legacy.sql
