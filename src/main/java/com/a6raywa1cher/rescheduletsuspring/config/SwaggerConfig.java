package com.a6raywa1cher.rescheduletsuspring.config;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	private final BuildProperties buildProperties;

	@Autowired
	public SwaggerConfig(BuildProperties buildProperties) {
		this.buildProperties = buildProperties;
	}

	@Bean
	public Docket api(TypeResolver typeResolver) {
		List<SecurityScheme> schemeList = new ArrayList<>();
		schemeList.add(new BasicAuth("Realm"));
		ApiInfo apiInfo = new ApiInfoBuilder()
			.title("reschedule-tsu-spring")
			.version(buildProperties.getVersion())
			.license("MIT License")
			.licenseUrl("https://github.com/monkey-underground-coders/reschedule-tsu-spring/blob/master/LICENSE")
			.build();

		//noinspection Guava
		return new Docket(DocumentationType.SWAGGER_2)
			.produces(Collections.singleton("application/json"))
			.consumes(Collections.singleton("application/json"))
			.host("")
			.ignoredParameterTypes(Authentication.class)
			.securitySchemes(schemeList)
			.useDefaultResponseMessages(true)
			.apiInfo(apiInfo)
			.securityContexts(Arrays.asList(securityContext()))
			.select()
//				.apis(Predicates.or(
//						Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")),
//						RequestHandlerSelectors.basePackage("org.springframework.boot.actuate")))
//				.apis(RequestHandlerSelectors.any())
			.apis(RequestHandlerSelectors.basePackage("com.a6raywa1cher.rescheduletsuspring.rest"))
			.paths(PathSelectors.any())
			.build();
	}

	private SecurityContext securityContext() {
		//noinspection Guava
		return SecurityContext.builder()
			.securityReferences(defaultAuth())
			.forPaths(Predicates.or(
				PathSelectors.ant("/user/**"),
				PathSelectors.ant("/cells/**")))
			.build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope
			= new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
		return Collections.singletonList(
			new SecurityReference("Realm", authorizationScopes));
	}
}
