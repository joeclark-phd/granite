FROM postgres:11-alpine

ENV POSTGRES_USER=granite POSTGRES_PASSWORD=test

COPY ddl /docker-entrypoint-initdb.d/
