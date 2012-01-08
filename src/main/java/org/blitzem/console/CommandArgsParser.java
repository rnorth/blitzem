package org.blitzem.console;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blitzem.commands.Command;

/**
 * Utility to parse command line arguments into a {@link Command} object.
 * 
 * The first argument which does not start with '--' will be used to select the
 * command by name (e.g. 'up' will select 'UpCommand').
 * 
 * Any arguments of the format '--*=*' will attempt to set a String-typed
 * property on the command object.
 * 
 * Any arguments of the format '--*' will attempt to set to true a Boolean-typed
 * property on the command object (note Boolean object not primitive type).
 * 
 * The second argument which does not start with '--' will be used as a 'noun'
 * that the command is intended to apply to.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class CommandArgsParser {

	private final Class<? extends Command>[] classes;
	private Class<? extends Command> defaultCommandClass = null;

	/**
	 * @param classes
	 *            varargs list of {@link Command} classes which command line
	 *            args may map to.
	 */
	public CommandArgsParser(Class<? extends Command>... classes) {
		this.classes = classes;
	}

	/**
	 * Specify a default command class that should be used if no other class
	 * matches the command line args.
	 * 
	 * @param defaultCommandClass
	 * @return
	 */
	public CommandArgsParser useDefault(Class<? extends Command> defaultCommandClass) {
		this.defaultCommandClass = defaultCommandClass;
		return this;
	}

	/**
	 * Parse the arguments
	 * 
	 * @param args
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
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

	/**
	 * @param command
	 * @param args
	 */
	private void applyNoun(Command command, String[] args) {

        boolean isFirstUndashedArg = true;
        for (String arg : args) {
            if (!Pattern.matches("--.+", arg)) {
                if (isFirstUndashedArg) {
                    isFirstUndashedArg = false;
                } else {
                    // is noun
                    command.setNoun(arg);
                }
            }
        }
	}

	/**
	 * @param command
	 * @param args
	 */
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

	/**
	 * @param command
	 * @param key
	 * @param value
	 */
	private void reflectionSetField(Command command, final String key, final Object value) {
		try {

			String setter = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

			Method method = command.getClass().getMethod(setter, value.getClass());
			method.invoke(command, value);
		} catch (SecurityException e) {
			throw new UnhandledReflectionException(e);
		} catch (IllegalArgumentException e) {
			throw new UnhandledReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new UnhandledReflectionException(e);
		} catch (NoSuchMethodException e) {
			System.out.println("Ignoring argument --" + key + "=" + value + " - not recognised for this command");
		} catch (InvocationTargetException e) {
			throw new UnhandledReflectionException(e);
		}
	}

	/**
	 * @param command
	 * @param args
	 */
	private void applyFlags(Command command, String[] args) {
		for (String arg : args) {
			if (Pattern.matches("--[a-zA-Z-_]+", arg)) {
				reflectionSetField(command, arg.substring(2), true);
			}
		}
	}

	/**
	 * @param args
	 * @return
	 */
	private String getVerb(String[] args) {
		for (String arg : args) {
			if (!Pattern.matches("--.+", arg)) {
				return arg;
			}
		}
		return null;
	}

}
