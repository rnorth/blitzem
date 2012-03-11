package org.blitzem.console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

/**
 * Logback Console appender which uses the JAnsi library to produce
 * appropriately-coloured output on terminals which support it.
 * 
 * @author Richard North <rich.north@gmail.com>
 * @param <E>
 * 
 */
public class JAnsiLogConsoleEncoder<E> extends LayoutWrappingEncoder<E> {

	String pattern;

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		PatternLayout patternLayout = new PatternLayout();
		patternLayout.setContext(context);
		patternLayout.setPattern(getPattern());
		patternLayout.start();
		this.layout = (Layout<E>) patternLayout;
		super.start();
	}

	@Override
	public void init(OutputStream os) throws IOException {
		OutputStream wrappedOutputStream = AnsiConsole.wrapOutputStream(os);
		super.init(wrappedOutputStream);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doEncode(E event) throws IOException {
		String txt = layout.doLayout(event);

		if (event instanceof ILoggingEvent) {
			ILoggingEvent loggingEvent = (ILoggingEvent) event;
			switch (loggingEvent.getLevel().toInt()) {
			case Level.WARN_INT:
				txt = "@|bold,orange " + txt + "|@";
				break;
			case Level.ERROR_INT:
				txt = "@|bold,red " + txt + "|@";
				break;
			case Level.DEBUG_INT:
				txt = "@|faint " + txt + "|@";
				break;
			}
			
			if (loggingEvent.getMessage().contains("SUCCESS")) {
				txt = "@|green " + txt + "|@";
			}
		}
		String renderedTxt = AnsiRenderer.render(txt);

		outputStream.write(convertToBytes(renderedTxt));
		outputStream.flush();
	}

	/**
	 * Copied from {@link LayoutWrappingEncoder} (private method).
	 * 
	 * @param s
	 * @return
	 */
	private byte[] convertToBytes(String s) {
		if (getCharset() == null) {
			return s.getBytes();
		} else {
			try {
				return s.getBytes(getCharset().name());
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("An existing charset cannot possibly be unsupported.");
			}
		}
	}


	/**
	 * @return 
	 */
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public void setLayout(Layout<E> layout) {
		throw new UnsupportedOperationException("one cannot set the layout of " + this.getClass().getName());
	}
}
