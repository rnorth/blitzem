package org.blitzem.testsupport;

import java.util.regex.Pattern;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class TestingLogInterceptor extends TestWatcher {

	private ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
	private Class<?>[] classes;

	/**
	 * @param classes
	 *            which classes should have their Logger altered to point at our
	 *            list appender?
	 */
	public TestingLogInterceptor(Class<?>... classes) {
		this.classes = classes;
	}

	@Override
	protected void starting(Description description) {
		for (Class<?> clazz : classes) {
			Logger logger = (Logger) LoggerFactory.getLogger(clazz);
			logger.getLoggerContext().reset();
			logger.addAppender(listAppender);
			logger.setLevel(Level.ALL);
		}
		
		listAppender.list.clear();
		listAppender.start();
	}
	
	@Override
	protected void finished(Description description) {
		listAppender.stop();
		listAppender.list.clear();
	}
	
	public boolean didLogMessageMatching(Level level, String patternString) {
		Pattern pattern = Pattern.compile(patternString);
		for (ILoggingEvent line : listAppender.list) {
			if (pattern.matcher(line.getFormattedMessage()).matches() && level.equals(line.getLevel())) {
				return true;
			}
		}
		return false;
	}
}
