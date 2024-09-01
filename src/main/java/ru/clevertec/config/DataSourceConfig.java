package ru.clevertec.config;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class DataSourceConfig {
    public DataSource getDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(System.getProperty("datasource.url"));
        dataSource.setUser(System.getProperty("datasource.username"));
        dataSource.setPassword(System.getProperty("datasource.password"));
        return dataSource;
    }
}
