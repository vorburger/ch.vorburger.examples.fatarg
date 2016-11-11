/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.vorburger.examples.fatarg;

import static java.util.Arrays.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Main.
 *
 * @author Michael Vorburger
 */
public class Main {

    public static void main(String[] args) throws Throwable {
        System.exit(new Main().parseArguments(args));
    }

    private final OptionParser optionParser = getOptionParser();

    @SuppressWarnings("unchecked")
    public int parseArguments(String[] args) throws Throwable {
        boolean isInDebugLogging = false;
        try {
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has("X")) {
                isInDebugLogging = true;
            }
            if (!optionSet.nonOptionArguments().isEmpty()) {
                unrecognizedOptions(optionSet.nonOptionArguments());
            }
            if (args.length == 0 || optionSet.has("h") || !optionSet.nonOptionArguments().isEmpty()) {
                printHelp();
                return -1;
            }

            File db = (File) optionSet.valueOf("db");
            List<String> userNames = (List<String>) optionSet.valuesOf("u");
            List<String> passwords = (List<String>) optionSet.valuesOf("p");
            if (passwords.size() > userNames.size()) {
                System.err.println("Cannot set more passwords than given user names");
                return -3;
            } else {
                resetPasswords(db, userNames, passwords);
                return 0;
            }

        } catch (Throwable t) {
            if (!isInDebugLogging) {
                System.err.println("Aborting due to " + t.getClass().getSimpleName() + " (use -X to see full stack trace): " + t.getMessage());
                return -2;
            } else {
                // Java will print the full stack trace if we rethrow it
                throw t;
            }
        }
    }

    private OptionParser getOptionParser() {
        return new OptionParser() { {
            acceptsAll(asList("h", "?" ), "Show help").forHelp();
            accepts("db", "database").withRequiredArg().ofType(File.class).defaultsTo(new File("db.db")).describedAs("path");
            acceptsAll(asList("u", "user"), "User Name").withRequiredArg();
            acceptsAll(asList("p", "passwd"), "New Password").withRequiredArg();
            // TODO accepts("v", "Display version information").forHelp();
            acceptsAll(asList("X", "debug"), "Produce execution debug output");

            allowsUnrecognizedOptions();
        } };
    }

    protected void unrecognizedOptions(List<?> unrecognizedOptions) {
        System.err.println("Unrecognized options: " + unrecognizedOptions);
    }

    protected void printHelp() throws IOException {
        optionParser.printHelpOn(System.out);
    }

    protected void resetPasswords(File db, List<String> userNames, List<String> passwords) {
        System.out.println(userNames);
        System.out.println(passwords);
    }

}
