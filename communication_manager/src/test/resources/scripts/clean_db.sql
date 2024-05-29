DO
'
DECLARE
    table_name TEXT;
BEGIN
    FOR table_name in (select tablename from pg_tables where schemaname = ''public'') LOOP
        EXECUTE ''TRUNCATE '' || table_name || '' CASCADE;'';
    END LOOP;
END;
' LANGUAGE plpgsql;
