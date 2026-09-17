-- Apply this once to an existing MySQL database after taking a backup.
-- Stop and resolve the returned rows before running the ALTER TABLE statement.
SELECT LOWER(CategoryName) AS normalized_name, COUNT(*) AS duplicate_count
FROM categories
GROUP BY LOWER(CategoryName)
HAVING COUNT(*) > 1;

ALTER TABLE categories
    MODIFY Status TINYINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT UQ_categories_CategoryName UNIQUE (CategoryName),
    ADD CONSTRAINT CK_categories_Status CHECK (Status IN (0, 1));
