package com.example.api.employee;

import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration("sample")
public class SampleConfig {

	private boolean initialize;
	private String[] firstNames;
	private String[] lastNames;
	private String[] emails;
}
