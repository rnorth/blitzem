/**
 * 
 */
package com.github.rnorth.blitzemj.console;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rnorth.blitzemj.UnresolvedReflectionException;
import com.github.rnorth.blitzemj.commands.Command;
import com.github.rnorth.blitzemj.commands.PerItemCommand;

/**
 * @author richardnorth
 *
 */
public class CommandArgsParser {

	private final Class<? extends Command>[] classes;
	private Class<? extends Command> defaultCommandClass = null;

	public CommandArgsParser(Class<? extends Command>... classes) {
		this.classes = classes;
	}

	public Command parse(String[] args) throws InstantiationException, IllegalAccessException {
		
		Command command = null;
		
		for (Class<? extends Command> clazz : classes) {
			final String verb = getVerb(args);
			if (clazz.getSimpleName().replace("Command", "").toLowerCase().equals(verb)) {
				command = clazz.newInstance();
			}
		}
		
		if (command == null && defaultCommandClass != null) {
			command = defaultCommandClass.newInstance();
		}
		
		if (command == null) {
			return null;
		}
		
		applyFlags(command, args);
		applyOptions(command, args);
		applyNoun(command, args);
		
		return command;
	}

	private void applyNoun(Command command, String[] args) {
		String arg = args[args.length - 1];
		if (!Pattern.matches("--.+", arg)) {
			command.setNoun(arg);
		}
	}

	private void applyOptions(Command command, String[] args) {
		for (String arg : args) {
			Matcher matcher = Pattern.compile("--([a-zA-Z-_]+)=(.+)").matcher(arg);
			matcher.find();
			
			if (matcher.matches()) {
				final String key = matcher.group(1);
				final String value = matcher.group(2);
				reflectionSetField(command, key, value);
			}
		}
	}

	private void reflectionSetField(Command command, final String key, final Object value) {
		try {
			
			String setter = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
			
			Method method = command.getClass().getMethod(setter, value.getClass());
			method.invoke(command, value);
		} catch (SecurityException e) {
			throw new UnresolvedReflectionException(e);
		} catch (IllegalArgumentException e) {
			throw new UnresolvedReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new UnresolvedReflectionException(e);
		} catch (NoSuchMethodException e) {
			System.out.println("Ignoring argument --"+key+"="+value+" - not recognised for this command");
		} catch (InvocationTargetException e) {
			throw new UnresolvedReflectionException(e);
		}
	}

	private void applyFlags(Command command, String[] args) {
		for (String arg : args) {
			if (Pattern.matches("--[a-zA-Z-_]+", arg)) {
				reflectionSetField(command, arg.substring(2), true);
			}
		}
	}

	private String getVerb(String[] args) {
		for (String arg : args) {
			if (!Pattern.matches("--.+", arg)) {
				return arg;
			}
		}
		return null;
	}

	public CommandArgsParser useDefault(Class<? extends PerItemCommand> defaultCommandClass) {
		this.defaultCommandClass = defaultCommandClass;
		return this;
	}

}
