ALTER TABLE articles
    ADD COLUMN publication_year INTEGER CHECK (publication_year BETWEEN 1000 AND 9999),
    ADD COLUMN category VARCHAR(60) NOT NULL DEFAULT 'Без категорії',
    ADD COLUMN language VARCHAR(7) NOT NULL DEFAULT 'unknown' CHECK (language IN ('ukr', 'eng', 'other', 'unknown'));

UPDATE articles SET category = 'Міста', language = 'ukr'
    WHERE title = 'Як міста повертають собі річки' AND author = 'Марія Левченко';
UPDATE articles SET category = 'Навчання', language = 'ukr'
    WHERE title IN ('Навіщо вести нотатки від руки', 'Як читати уважніше');
