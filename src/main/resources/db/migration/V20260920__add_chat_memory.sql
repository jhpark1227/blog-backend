CREATE TABLE SPRING_AI_CHAT_MEMORY
(
    conversation_id VARCHAR(36) NOT NULL,
    content         TEXT        NOT NULL,
    type            VARCHAR(10) NOT NULL,
    `timestamp`     TIMESTAMP   NOT NULL,
    sequence_id     BIGINT      NOT NULL,
    CONSTRAINT type_check CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
    INDEX chat_memory_conversation_id_sequence_id_idx (conversation_id, sequence_id)
) ENGINE = InnoDB;
