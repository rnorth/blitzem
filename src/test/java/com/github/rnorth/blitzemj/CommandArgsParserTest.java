package com.github.rnorth.blitzemj;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.github.rnorth.blitzemj.commands.BaseCommand;
import com.github.rnorth.blitzemj.commands.Command;
import com.github.rnorth.blitzemj.commands.HelpCommand;
import com.github.rnorth.blitzemj.commands.StatusCommand;
import com.github.rnorth.blitzemj.commands.UpCommand;
import com.github.rnorth.blitzemj.console.CommandArgsParser;

public class CommandArgsParserTest {

	@Before
	public void setup() {
		
	}
	
	@Test
	public void convertsStandaloneVerbToCommand() throws Exception {
		Command command = new CommandArgsParser(StatusCommand.class).parse(new String[] {"status"});
		
		assertNotNull(command);
		assertTrue(command instanceof StatusCommand);
	}
	
	@Test
	public void convertsUnknownVerbToDefaultCommand() throws Exception {
		Command command = new CommandArgsParser(StatusCommand.class).useDefault(HelpCommand.class).parse(new String[] {"notstatus"});
		
		assertNotNull(command);
		assertTrue(command instanceof HelpCommand);
	}
	
	@Test
	public void capturesBooleanFlag() throws Exception {
		Command command = new CommandArgsParser(StatusCommand.class).parse(new String[] {"--verbose", "--source=./file.txt", "status"});
		
		assertNotNull(command);
		assertTrue(((StatusCommand)command).isVerbose());
	}
	
	@Test
	public void capturesStringOption() throws Exception {
		Command command = new CommandArgsParser(UpCommand.class, StatusCommand.class).parse(new String[] {"--verbose", "--source=./file.txt", "up"});
		
		assertNotNull(command);
		assertEquals("./file.txt", ((BaseCommand)command).getSource());
	}
	
	@Test
	public void doesNotFailForUnknownStringOption() throws Exception {
		Command command = new CommandArgsParser(UpCommand.class, StatusCommand.class).parse(new String[] {"--verbose", "--notsource=./file.txt", "up"});
		
		assertNotNull(command);
		assertFalse("./file.txt".equals(((BaseCommand)command).getSource()));
	}
	
	@Test
	public void capturesNoun() throws Exception {
		Command command = new CommandArgsParser(StatusCommand.class).parse(new String[] {"--verbose", "--source=./file.txt", "status", "web"});
		
		assertNotNull(command);
		assertEquals("web", command.getNoun());
	}
    
    @Test
    public void capturesStandaloneVerb() throws Exception {
        Command command = new CommandArgsParser(StatusCommand.class).parse(new String[] {"--verbose", "status"});

        assertNotNull(command);
        assertTrue(command instanceof  StatusCommand);
        assertEquals(null, command.getNoun());
    }
}
