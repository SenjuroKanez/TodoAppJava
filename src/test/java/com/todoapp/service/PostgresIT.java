package com.todoapp.service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PostgresIT {
    @Test void migratesRealPostgresAndPreservesDataAcrossConnections(){
        try(var pg=new PostgreSQLContainer("postgres:17.6-alpine")){
            pg.start();var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
            Flyway flyway=Flyway.configure().dataSource(ds).load();flyway.migrate();flyway.validate();
            var db=new JdbcTemplate(ds);String id=UUID.randomUUID().toString();
            db.update("INSERT INTO users(id,username,email,password_hash,created_at) VALUES(?,?,?,?,?)",id,"persisted","persisted@example.test","hash","2026-01-01T00:00:00Z");
            var second=new JdbcTemplate(new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword()));
            assertEquals(id,second.queryForObject("SELECT id FROM users WHERE username='persisted'",String.class));
            assertEquals(0,flyway.migrate().migrationsExecuted);
        }
    }
}
