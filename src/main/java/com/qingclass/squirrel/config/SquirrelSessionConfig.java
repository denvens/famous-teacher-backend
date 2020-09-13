package com.qingclass.squirrel.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 *
 *
 * @author 苏天奇
 * */
@Configuration
@MapperScan(basePackages = "com.qingclass.squirrel.mapper.user", sqlSessionFactoryRef = "sqlSessionBeanSquirrel")
public class SquirrelSessionConfig {

    @Autowired
    @Qualifier("dataSourceSquirrel")
    private DataSource dataSourceSquirrel;

    @Bean("sqlSessionBeanSquirrel")
    public SqlSessionFactory sqlSessionSquirrelFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSourceSquirrel);


        return factoryBean.getObject();

    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionSquirrelFactory());
        return template;
    }

    @Bean(name = "squirrelTransaction")
    public PlatformTransactionManager platformTransactionManager(){
        return new DataSourceTransactionManager(dataSourceSquirrel);
    }

}

