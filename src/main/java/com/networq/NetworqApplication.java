package com.networq;

import com.networq.logging.StartupFailureLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NetworqApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(NetworqApplication.class);
		application.addListeners(new StartupFailureLogger());
		application.run(args);
	}

}
