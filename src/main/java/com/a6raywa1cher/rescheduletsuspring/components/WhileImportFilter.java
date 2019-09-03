package com.a6raywa1cher.rescheduletsuspring.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class WhileImportFilter extends OncePerRequestFilter {
	private TsuDbImporter importer;

	@Autowired
	public WhileImportFilter(TsuDbImporter importer) {
		this.importer = importer;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (importer.isUpdatingLocalDatabase()) {
			response.addHeader("Retry-After", "10");
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}
		filterChain.doFilter(request, response);
	}
}
