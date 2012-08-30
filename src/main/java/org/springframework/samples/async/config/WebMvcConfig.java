package org.springframework.samples.async.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.async.chat.ChatController;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
@ComponentScan(basePackageClasses = ChatController.class)
public class WebMvcConfig extends WebMvcConfigurationSupport {

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		configurer.setDefaultTimeout(30*1000L);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("chat");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("resources/");
	}

	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		return resolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(templateResolver());
		return engine;
	}

	@Bean
	public TemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setPrefix("/WEB-INF/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		resolver.setCacheable(false);
		return resolver;
	}

}
