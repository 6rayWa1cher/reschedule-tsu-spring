package com.a6raywa1cher.rescheduletsuspring.components.tsudbimporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "app.tsudb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WhileImportOncePerRequestFilter extends OncePerRequestFilter {
	private final TsuDbImporterComponent importer;

	@Autowired
	public WhileImportOncePerRequestFilter(TsuDbImporterComponent importer) {
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
