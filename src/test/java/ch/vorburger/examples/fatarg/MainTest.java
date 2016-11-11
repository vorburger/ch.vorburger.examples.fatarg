/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.vorburger.examples.fatarg;

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.examples.fatarg.Main;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit Test of Main class with the argument parsing.
 *
 * @author Michael Vorburger
 */
public class MainTest {

    @Test
    public void noArguments() throws Throwable {
        Main main = Mockito.spy(new Main());
        assertThat(main.parseArguments(new String[] {})).isEqualTo(-1);
        Mockito.verify(main).printHelp();
    }

    @Test public void unrecognizedArgument() throws Throwable {
        Main main = Mockito.spy(new Main());
        assertThat(main.parseArguments(new String[] { "saywhat" })).isEqualTo(-1);
        Mockito.verify(main).unrecognizedOptions(Collections.singletonList("saywhat"));
        Mockito.verify(main).printHelp();
    }

    /**
     * Verify that allowsUnrecognizedOptions() is used, and "bad" arguments
     * print help message instead of causing an UnrecognizedOptionException.
     */
    @Test public void parsingError() throws Throwable {
        Main main = Mockito.spy(new Main());
        assertThat(main.parseArguments(new String[] { "-d" })).isEqualTo(-1);
        Mockito.verify(main).printHelp();
    }

    @Test public void exceptionWithoutX() throws Throwable {
        Main main = new Main() {
            @Override
            protected void printHelp() throws IOException {
                throw new IOException("boum");
            }
        };
        assertThat(main.parseArguments(new String[] { "-?" })).isEqualTo(-2);
    }

    @Test(expected=IllegalStateException.class) public void exceptionWithX() throws Throwable {
        Main main = new Main() {
            @Override
            protected void printHelp() throws IOException {
                throw new IllegalStateException("boum");
            }
        };
        assertThat(main.parseArguments(new String[] { "-hX" })).isEqualTo(-2);
    }

    @Test public void onlyAUser() throws Throwable {
        assertThat(new Main().parseArguments(new String[] { "-X", "-u admin" })).isEqualTo(0);
    }

    @Test public void onlyTwoUsers() throws Throwable {
        assertThat(new Main().parseArguments(new String[] { "-X", "-u", "admin", "-u", "auser" })).isEqualTo(0);
    }

    @Test public void userOptionWithoutArgument() throws Throwable {
        assertThat(new Main().parseArguments(new String[] { "-u" })).isEqualTo(-2);
    }

    @Test public void justPassword() throws Throwable {
        Main main = new Main();
        assertThat(main.parseArguments(new String[] { "-X", "-p", "newpass" })).isEqualTo(-3);
    }

    @Test public void userAndPassword() throws Throwable {
        Main main = Mockito.spy(new Main());
        assertThat(main.parseArguments(new String[] { "-X", "-u", "admin", "-p", "newpass" })).isEqualTo(0);
        Mockito.verify(main).resetPasswords(new File("db.db"), Collections.singletonList("admin"), Collections.singletonList("newpass"));
    }

    @Test public void userAndPasswordInNonDefaultDatabase() throws Throwable {
        Main main = Mockito.spy(new Main());
        assertThat(main.parseArguments(new String[] { "-X", "--db", "alt.db", "-u", "admin", "-p", "newpass" })).isEqualTo(0);
        Mockito.verify(main).resetPasswords(new File("alt.db"), Collections.singletonList("admin"), Collections.singletonList("newpass"));
    }

    @Test public void twoUsersAndPasswords() throws Throwable {
        Main main = Mockito.spy(new Main());
        assertThat(main.parseArguments(new String[] { "-X", "-u", "admin", "-p", "newpass1", "-u", "auser", "-p", "newpass2" })).isEqualTo(0);
        Mockito.verify(main).resetPasswords(new File("db.db"), Arrays.asList("admin", "auser"), Arrays.asList("newpass1", "newpass2"));
    }

    @Test public void morePasswordsThanUsers() throws Throwable {
        Main main = new Main();
        assertThat(main.parseArguments(new String[] { "-X", "-u", "admin", "-p", "newpass1", "-u", "auser", "-p", "newpass2", "-p", "newpass3" })).isEqualTo(-3);
    }

}
