package com.staircase13.apperta.cms;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

public class LoadLogAsserter {

    public static void assertLogAdded(LoadLog loadLog, LoadLog.Severity severity, String message) {
        assertThat(loadLog.getLogs(), hasItem(
                allOf(
                        hasProperty("severity",is(severity)),
                        hasProperty("message",is(message))
                )
        ));
    }

}
