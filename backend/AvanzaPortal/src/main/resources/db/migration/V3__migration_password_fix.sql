-- Migrerar gammalt users-schema:
-- password_md5 -> password_hash om password_hash inte redan finns.

DO
$$
BEGIN
    IF
EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'password_md5'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'password_hash'
    ) THEN
ALTER TABLE users
    RENAME COLUMN password_md5 TO password_hash;
END IF;
END $$;

ALTER TABLE users
ALTER
COLUMN password_hash TYPE VARCHAR(100);

UPDATE users
SET password_hash = '$2y$10$vb7QdzuTR07MtqNjngvG3udHGi9MiEvrNzvOWRJGmRDPG8mUsAZtK'
WHERE email IN ('anna@example.com', 'erik@example.com');

ALTER TABLE users
    ALTER COLUMN password_hash SET NOT NULL;