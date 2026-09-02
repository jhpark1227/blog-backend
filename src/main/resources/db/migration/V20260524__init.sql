CREATE TABLE BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID BIGINT       NOT NULL PRIMARY KEY,
    VERSION         BIGINT,
    JOB_NAME        VARCHAR(100) NOT NULL,
    JOB_KEY         VARCHAR(32)  NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID BIGINT      NOT NULL PRIMARY KEY,
    VERSION          BIGINT,
    JOB_INSTANCE_ID  BIGINT      NOT NULL,
    CREATE_TIME      DATETIME(6) NOT NULL,
    START_TIME       DATETIME(6) DEFAULT NULL,
    END_TIME         DATETIME(6) DEFAULT NULL,
    STATUS           VARCHAR(10),
    EXIT_CODE        VARCHAR(2500),
    EXIT_MESSAGE     VARCHAR(2500),
    LAST_UPDATED     DATETIME(6),
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID BIGINT       NOT NULL,
    PARAMETER_NAME   VARCHAR(100) NOT NULL,
    PARAMETER_TYPE   VARCHAR(100) NOT NULL,
    PARAMETER_VALUE  VARCHAR(2500),
    IDENTIFYING      CHAR(1)      NOT NULL,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  BIGINT       NOT NULL PRIMARY KEY,
    VERSION            BIGINT       NOT NULL,
    STEP_NAME          VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID   BIGINT       NOT NULL,
    CREATE_TIME        DATETIME(6)  NOT NULL,
    START_TIME         DATETIME(6) DEFAULT NULL,
    END_TIME           DATETIME(6) DEFAULT NULL,
    STATUS             VARCHAR(10),
    COMMIT_COUNT       BIGINT,
    READ_COUNT         BIGINT,
    FILTER_COUNT       BIGINT,
    WRITE_COUNT        BIGINT,
    READ_SKIP_COUNT    BIGINT,
    WRITE_SKIP_COUNT   BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT     BIGINT,
    EXIT_CODE          VARCHAR(2500),
    EXIT_MESSAGE       VARCHAR(2500),
    LAST_UPDATED       DATETIME(6),
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE =InnoDB;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE =InnoDB;
CREATE SEQUENCE BATCH_JOB_INSTANCE_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE =InnoDB;



CREATE TABLE category
(
    notion_option_id VARCHAR(50) NOT NULL PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    color            VARCHAR(20) NOT NULL,
    sort_order       INT         NOT NULL,
    synced_at        DATETIME(6) NOT NULL
);

CREATE TABLE tag
(
    notion_option_id VARCHAR(50) NOT NULL PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    color            VARCHAR(20) NOT NULL,
    sort_order       INT         NOT NULL,
    synced_at        DATETIME(6) NOT NULL
);

CREATE TABLE post
(
    notion_page_id          VARCHAR(50)                              NOT NULL PRIMARY KEY,
    title                   VARCHAR(255)                             NOT NULL,
    content                 LONGTEXT,
    status                  ENUM ('DELETED', 'PUBLISHED', 'PENDING') NOT NULL,
    published_date          DATE,
    notion_last_edited_time DATETIME(6),
    synced_at               DATETIME(6)                              NOT NULL,
    excerpt                 VARCHAR(255),
    excerpt_generated_at    DATETIME(6),
    pinned                  BIT                                      NOT NULL,
    view_count              INT                                      NOT NULL DEFAULT 0,
    created_at              DATETIME(6)                              NOT NULL,
    updated_at              DATETIME(6)                              NOT NULL,
    category_id             VARCHAR(50),
    CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES category (notion_option_id) ON DELETE SET NULL
);

CREATE TABLE post_tag
(
    post_id VARCHAR(50) NOT NULL,
    tag_id  VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post (notion_page_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (notion_option_id) ON DELETE CASCADE
);
