CREATE TABLE vector_store
(
    id        UUID         NOT NULL DEFAULT uuid() PRIMARY KEY,
    content   TEXT,
    metadata  JSON,
    embedding VECTOR(768)  NOT NULL,
    VECTOR INDEX vector_store_embedding_idx (embedding)
) ENGINE = InnoDB;

ALTER TABLE post
    ADD COLUMN embedded_at DATETIME(6) AFTER synced_at;