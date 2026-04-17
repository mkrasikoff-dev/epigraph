--liquibase formatted sql
--changeset mkrasikoff:002-extend-source-tags-columns

ALTER TABLE 
  quotes ALTER COLUMN source TYPE VARCHAR(500), 
  ALTER COLUMN tags TYPE VARCHAR(500);
