package org.maxur.justj.core.cli

import org.maxur.justj.core.cli.annotation.*
import org.maxur.justj.core.cli.exception.InvalidCommandArgumentException
import org.maxur.justj.core.cli.exception.InvalidCommandLineException
import spock.lang.Specification

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>21.02.2016</pre>
 */
public class CliMenuSpec extends Specification {

    private CliMenu sut

    void setup() {
        sut = new CliMenu()
    }

    def "Should returns command if command line contains command flag"() {
        given: "command line with commands flag"
        String[] args = ["--help"]
        when: "Client registers the command in the menu"
        sut.register(HelpCommand)
        then: "Menu returns command by command line flag"
        assert sut.makeCommand(args) instanceof HelpCommand;
    }

    def "Should returns null if command line is not contains any command flag"() {
        given: "command line without any commands flag"
        String[] args = []
        when: "Client registers the command in the menu"
        sut.register(HelpCommand)
        then: "Menu returns null"
        assert sut.makeCommand(args) == null;
    }

    def "Should returns error if command line contains two and more commands names"() {
        given: "command line with two commands flag"
        String[] args = ["--help", "--version"]
        when: "Client registers the command in the menu as default"
        sut.register(HelpCommand, VersionCommand)
        and: "try get command from menu"
        sut.makeCommand(args)
        then: "Menu throws Invalid Command Line"
        thrown InvalidCommandLineException;
    }

    def "Should returns command on valid flag only "() {
        given: "command line with commands flag and a operand"
        String[] args = ["--help", "++version"]
        when: "Client registers the command in the menu as default"
        sut.register(HelpCommand, VersionCommand)
        and: "try get command from menu"
        def result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof HelpCommand;
    }

    def "Should returns command if command line contains command and commands option "() {
        given: "command line with any commands flag and option flag"
        String[] args = ["--help", "--all"]
        when: "Client registers the command in the menu as default"
        sut.register(HelpCommand)
        and: "try get command from menu"
        HelpCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof HelpCommand;
        and: "Flag is set by annotations value"
        assert result.all
    }

    def "Should returns command if command line contains command and commands option by method name"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["--version", "--all"]
        when: "Client registers the command in the menu as default"
        sut.register(VersionCommand)
        and: "try get command from menu"
        VersionCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof VersionCommand;
        and: "Flag is set by field name"
        assert result.all
    }

    def "Should returns error if command line contains unknown commands option"() {
        given: "command line without any commands flag and invalid option flag"
        String[] args = ["--help", "--invalid"]
        when: "Client registers the command in the menu as default"
        sut.register(HelpCommand)
        and: "try get command from menu"
        sut.makeCommand(args)
        then: "Menu throws Command not Found Exception"
        thrown InvalidCommandArgumentException;
    }

    def "Should returns default commands if default command is registered and command line is empty"() {
        given: "command line without any commands flag"
        String[] args = []
        when: "Client registers the command in the menu"
        sut.register(ProcessCommand)
        then: "Menu returns default command by command line flag"
        assert sut.makeCommand(args) instanceof ProcessCommand;
    }

    def "Should returns command if command line contains command and commands option without flag annotation"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["--build", "--all"]
        when: "Client registers the command in the menu as default"
        sut.register(BuildCommand)
        and: "try get command from menu"
        BuildCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof BuildCommand;
        and: "Flag is set by field name"
        assert result.all
    }

    def "Should returns command if command line contains short command flag without shortkey annotation"() {
        given: "command line with commands flag"
        String[] args = ["-v"]
        when: "Client registers the command in the menu"
        sut.register(VersionCommand)
        then: "Menu returns command by command line flag"
        assert sut.makeCommand(args) instanceof VersionCommand;
    }

    def "Should returns command if command line contains short command flag"() {
        given: "command line with commands flag"
        String[] args = ["-?"]
        when: "Client registers the command in the menu"
        sut.register(HelpCommand)
        then: "Menu returns command by command line flag"
        assert sut.makeCommand(args) instanceof HelpCommand;
    }

    def "Should returns command if command line contains command and commands option by shortkey"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["-v", "-a"]
        when: "Client registers the command in the menu as default"
        sut.register(VersionCommand)
        and: "try get command from menu"
        VersionCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert sut.makeCommand(args) instanceof VersionCommand;
        and: "Flag is set by field name"
        assert result.all
    }

    def "Should returns command if command line contains command and commands option by shortkey from superclass"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["-q", "-v", "-a"]
        when: "Client registers the command in the menu as default"
        sut.register(VersionCommand)
        and: "try get command from menu"
        VersionCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof VersionCommand;
        and: "Flag is set by field name"
        assert result.quiet
    }

    def "Should returns command if command line contains command and commands option in compact form"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["-va"]
        when: "Client registers the command in the menu as default"
        sut.register(VersionCommand)
        and: "try get command from menu"
        VersionCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof VersionCommand;
        and: "Flag is set by field name"
        assert result.all
    }

    def "Should returns error if command line contains two and more commands names in compact form"() {
        given: "command line with two commands flag"
        String[] args = ["-?v"]
        when: "Client registers the command in the menu as default"
        sut.register(HelpCommand, VersionCommand)
        and: "try get command from menu"
        sut.makeCommand(args)
        then: "Menu throws Invalid Command Line"
        thrown InvalidCommandLineException;
    }

    def "Should returns command if command line contains command and commands option as trigger (compact form)"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["-bx"]
        when: "Client registers the command in the menu as default"
        sut.register(BuildCommand)
        and: "try get command from menu"
        BuildCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof BuildCommand;
        and: "Flag is set by field name"
        assert result.logLevel == LogLevel.DEBUG
    }

    def "Should returns command if command line contains command and commands option as trigger"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["-b", "--debug"]
        when: "Client registers the command in the menu as default"
        sut.register(BuildCommand)
        and: "try get command from menu"
        BuildCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof BuildCommand;
        and: "Flag is set by field name"
        assert result.logLevel == LogLevel.DEBUG
    }

    def "Should returns command if command line contains command and commands option as trigger with flag"() {
        given: "command line with any commands flag and option flag"
        String[] args = ["-b", "--quiet"]
        when: "Client registers the command in the menu as default"
        sut.register(BuildCommand)
        and: "try get command from menu"
        BuildCommand result = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert result instanceof BuildCommand;
        and: "Flag is set by field name"
        assert result.logLevel == LogLevel.OFF
    }

    def "Should returns command if command line contains short command flag with alias"() {
        given: "command line with commands flag"
        String[] args = ["-h"]
        when: "Client registers the command in the menu"
        sut.register(HelpCommand)
        then: "Menu returns command by command line flag"
        assert sut.makeCommand(args) instanceof HelpCommand;
    }

    def "Should returns error if command line contains unknown commands option in compact form"() {
        given: "command line without any commands flag and invalid option flag"
        String[] args = ["-?i"]
        when: "Client registers the command in the menu as default"
        sut.register(HelpCommand)
        and: "try get command from menu"
        sut.makeCommand(args)
        then: "Menu throws Command not Found Exception"
        thrown InvalidCommandArgumentException;
    }

    def "Should returns command if command line contains options name with options argument"() {
        given: "command line with commands flag"
        String[] args = ["--build", "--settings", "~/settings.xml"]
        when: "Client registers the command in the menu"
        sut.register(BuildCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof BuildCommand;
        assert command.settings == "~/settings.xml"
    }

    def "Should returns command if command line contains options key with options argument"() {
        given: "command line with commands flag"
        String[] args = ["--build", "-s", "~/settings.xml"]
        when: "Client registers the command in the menu"
        sut.register(BuildCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof BuildCommand;
        assert command.settings == "~/settings.xml"
    }

    def "Should returns command if command line contains options key with options argument without separator"() {
        given: "command line with commands flag"
        String[] args = ["--build", "-s~/settings.xml"]
        when: "Client registers the command in the menu"
        sut.register(BuildCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof BuildCommand;
        assert command.settings == "~/settings.xml"
    }

    def "Should returns command if command line contains options key with options argument as string"() {
        given: "command line with commands flag"
        String[] args = ["--build", "-m", "\"This", "text", "is", "option's", "argument\""]
        when: "Client registers the command in the menu"
        sut.register(BuildCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof BuildCommand;
        assert command.message == "This text is option's argument"
    }

    def "Should returns command if command line contains options key with options argument as list"() {
        given: "command line with commands flag"
        String[] args = ["--build", "-p", "dev,", "qa",  ",test", ",", "ci,prod"]
        when: "Client registers the command in the menu"
        sut.register(BuildCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof BuildCommand;
        assert command.profiles == ["dev","qa","test","ci","prod"]
    }

    def "Should returns command if command line contains options key with options argument as list of enum"() {
        given: "command line with commands flag"
        String[] args = ["-dl", "INFO,", "DEBUG"]
        when: "Client registers the command in the menu"
        sut.register(DeployCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof DeployCommand;
        assert command.levels == [LogLevel.INFO, LogLevel.DEBUG] as Set
    }

    def "Should returns error if command line contains options with options argument as invalid list"() {
        given: "command line with commands flag"
        String[] args = ["--build", "-p", "dev,"]
        when: "Client registers the command and default command in the menu"
        sut.register(BuildCommand)
        and: "try get command from menu"
        sut.makeCommand(args)
        then: "Menu throws Invalid List Exception"
        thrown InvalidCommandLineException;
    }

    def "Should returns command if command line contains options with id of default command"() {
        given: "command line with commands flag"
        String[] args = ["--build", "-p", "dev"]
        when: "Client registers the command and default command in the menu"
        sut.register(BuildCommand, ProcessCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns command by command line flag"
        assert command instanceof BuildCommand;
    }

    def "Should returns default commands with it's operands"() {
        given: "command line without any commands flag"
        String[] args = ["clean", "install", "site"]
        when: "Client registers the command in the menu"
        sut.register(ProcessCommand)
        and: "try get command from menu"
        def command = sut.makeCommand(args)
        then: "Menu returns default command by command line flag"
        assert command instanceof ProcessCommand;
        assert command.phases == ["clean", "install", "site"];
    }

    @Command("version")
    static class VersionCommand extends TestCommand {
        @Flag
        boolean all
    }

    @Command

    static class BuildCommand extends TestCommand {
        boolean all
        LogLevel logLevel
        @Option()
        String settings
        @Option()
        String message
        @Option()
        List<String> profiles
    }

    @Command
    static class DeployCommand extends TestCommand {
        @Option()
        Set<LogLevel> levels
    }


    @Command
    @Default
    static class ProcessCommand extends TestCommand {
        @Operands
        List<String> phases;
    }



}
