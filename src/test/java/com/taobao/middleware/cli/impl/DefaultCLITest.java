/*
 *  Copyright (c) 2011-2015 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package com.taobao.middleware.cli.impl;

import com.taobao.middleware.cli.Argument;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CLIs;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.MissingValueException;
import com.taobao.middleware.cli.Option;
import com.taobao.middleware.cli.annotations.CLIConfigurator;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 基本用法test case
 * 
 * 1，把命令行的字符串list，转换成CommandLine对象；
 * 2，输出命令行的使用说明
 * 
 * Tests the {@link DefaultCLI}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DefaultCLITest {


    @Test
    public void testFlag() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setShortName("f").setFlag(true))	//只是个flag，不需要设置value
                .addOption(new Option().setShortName("x"));
        
        //把字符串数组转换成CommandLine对象，然后通过getOptionValue()获取到option的值。
        //相当于把json字符串转换成JSON对象，然后通过getJsonNode()获取值
        final CommandLine evaluated = cli.parse(Arrays.asList("-f", "-x", "foo"));
        assertThat(evaluated.isFlagEnabled("f")).isTrue();	//判断flag是否传入
        assertThat((String) evaluated.getOptionValue("x")).isEqualToIgnoringCase("foo");
    }

    @Test
    public void testMissingFlag() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setShortName("f").setFlag(true))
                .addOption(new Option().setShortName("x"));
        final CommandLine evaluated = cli.parse(Arrays.asList("-x", "foo"));	//没有-f 的option
        assertThat(evaluated.isFlagEnabled("f")).isFalse();
        assertThat((String) evaluated.getOptionValue("x")).isEqualToIgnoringCase("foo");
    }

    @Test
    public void testUsageComputationWhenUsingOnlyFlagShortOption() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setShortName("f").setDescription("turn on/off").setFlag(true));
        
        /*
         * 获取命令行的使用说明。
         * 例如：
         * Usage: test [-f]

			Options and Arguments:
			 -f   turn on/off

         */
        StringBuilder builder = new StringBuilder();
        cli.usage(builder);
        
        assertThat(builder)
                .contains("test [-f]")
                .contains("Options and Arguments")
                .contains(" -f   turn on/off");
    }

    @Test
    public void testUsageWhenNoArgsAndOptions() {
        final CLI cli = CLIs.create("test").setDescription("A simple test command.");	//不含option和argument的命令行
        
        /*
         * 输出：
         * Usage: test

				A simple test command.

         */
        StringBuilder builder = new StringBuilder();
        cli.usage(builder);
        assertThat(builder)
                .contains("test")
                .doesNotContain("Options").doesNotContain("Arguments");
    }

    @Test
    public void testUsageComputationWhenUsingOnlyFlagLongOption() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("flag").setDescription("turn on/off").setFlag(true))
        		.addOption(new Option().setLongName("isDebug").setDescription("is or not debuge mode").setFlag(true));
        
        /*
         * Usage: test [--flag] [--isDebug]

			Options and Arguments:
			    --flag      turn on/off
			    --isDebug   is or not debuge mode

         */
        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test [--flag]")
                .contains(" --flag   turn on/off");
    }

    @Test
    public void testUsageComputationWhenUsingShortAndLongFlagOption() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("flag").setShortName("f").setDescription("turn on/off").setFlag(true));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test [-f]")
                .contains(" -f,--flag   turn on/off");
    }

    @Test
    public void testUsageComputationWhenUsingShortAndLongOption() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("file").setShortName("f").setDescription("a file"));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test [-f <value>]")
                .contains(" -f,--file <value>   a file");
    }

    @Test
    public void testUsageComputationWhenUsingOnlyShortOption() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setShortName("f").setDescription("a file"));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test [-f <value>]")
                .contains(" -f <value>   a file");
    }

    @Test
    public void testUsageComputationWhenUsingOnlyLongOption() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("file").setDescription("a file"));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test [--file <value>]")
                .contains(" --file <value>   a file");
    }

    @Test
    public void testUsageComputationWhenUsingRequiredOptionAndArgument() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("file").setShortName("f").setDescription("a file").setRequired(true))
                .addArgument(new Argument().setArgName("foo").setDescription("foo").setRequired(true));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test -f <value> foo")
                .contains(" -f,--file <value>   a file")
                .contains("<foo>               foo");
    }

    @Test
    public void testUsageComputationWithSeveralArguments() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("file").setShortName("f").setDescription("a file").setRequired(true))
                .addArgument(new Argument().setIndex(0).setArgName("foo").setDescription("foo"))
                .addArgument(new Argument().setIndex(1))
                .addArgument(new Argument().setIndex(2).setArgName("bar").setDescription("bar"));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test -f <value> foo value bar")
                .contains(" -f,--file <value>   a file")
                .contains("<foo>               foo")
                .contains("<value>")
                .contains("<bar>               bar");
    }

    @Test
    public void testUsageComputationWithHiddenArguments() {
        final CLI cli = CLIs.create("test")
                .addOption(new Option().setLongName("file").setShortName("f").setDescription("a file").setRequired(true))
                .addArgument(new Argument().setIndex(0).setArgName("foo").setDescription("foo"))
                .addArgument(new Argument().setIndex(1))
                .addArgument(new Argument().setIndex(2).setArgName("bar").setDescription("bar").setHidden(true));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test -f <value> foo value")
                .contains(" -f,--file <value>   a file")
                .contains("<foo>               foo")
                .contains("<value>")
                .doesNotContain("bar");
    }

    @Test
    public void testUsageComputationWhenUsingNotRequiredArgument() {
        final CLI cli = CLIs.create("test")
                .addArgument(new Argument().setArgName("foo").setRequired(false));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        assertThat(builder)
                .contains("test [foo]");
    }

    @Test
    public void testCommandLineValidationWhenValid() {
        final CLI cli = CLIs.create("test")
                .addArgument(new Argument().setArgName("foo").setRequired(true));

        CommandLine commandLine = cli.parse(Collections.singletonList("foo"));
        assertThat(commandLine.isValid()).isTrue();
    }

    @Test(expected = MissingValueException.class)
    public void testCommandLineValidationWhenInvalid() {
        final CLI cli = CLIs.create("test")
                .addArgument(new Argument().setArgName("foo").setRequired(true));

        cli.parse(Collections.<String>emptyList());
    }

    @Test
    public void testCommandLineValidationWhenInvalidWithValidationDisabled() {
        final CLI cli = CLIs.create("test")
                .addArgument(new Argument().setArgName("foo").setRequired(true));

        CommandLine commandLine = cli.parse(Collections.<String>emptyList(), false);
        
        //判断接收到的命令行 是否有效
        assertThat(commandLine.isValid()).isEqualTo(false);
    }

}