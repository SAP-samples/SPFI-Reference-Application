package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
@EnableAsync
public class SpfiV2RefAppApplication extends AsyncConfigurerSupport {

	public static void main(String[] args) {
		SpringApplication.run(SpfiV2RefAppApplication.class, args);
	}

}
