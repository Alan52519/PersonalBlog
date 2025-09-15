package com.example.PersonalBlog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.PersonalBlog.mapper")
public class PersonalBlogApplication {

	public static void main(String[] args) {

		SpringApplication.run(PersonalBlogApplication.class, args
		);
	}

}
