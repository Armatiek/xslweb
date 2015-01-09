package nl.armatiek.xslweb.web.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class XSLWebFilterConfig implements FilterConfig {

	private final ServletContext servletContext;

	private final String filterName;

	private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

	public XSLWebFilterConfig() {
		this(null, "");
	}

	public XSLWebFilterConfig(String filterName) {
		this(null, filterName);
	}

	public XSLWebFilterConfig(ServletContext servletContext) {
		this(servletContext, "");
	}
	
	public XSLWebFilterConfig(ServletContext servletContext, String filterName) {
		this.servletContext = servletContext;
		this.filterName = filterName;
	}

	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	public void addInitParameter(String name, String value) {		
		this.initParameters.put(name, value);
	}

	@Override
	public String getInitParameter(String name) {		
		return this.initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(this.initParameters.keySet());
	}

}