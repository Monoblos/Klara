Klara
=====

> Denn mit Klara wird alles klarer.

What is this?
-------------

Klara is a command-line tool for java, which will add informational output to your program at runtime.
This information includes for example variable values before conditions are checked, stating which branch was executed at if/else-if/else statements or counting loop iterations done.

User stories
--------------------

As a user of Klara I want to 
* be able to run my program with additional, extended output on what the actual assigned values are for each variable.
* be able to track the exact path the program used by showing the exact order the program lines where executed.
* be able to track the exact path the program used by getting branch information and loop iterations done.
* easily set what features I want to have displayed as parameters in the initial call.
* interactively choose the options before the execution of the main program.
* choose a set of lines to be inspected instead of the entire program.

Usage
-----

Execution using klara usually looks like this:

    java -jar Klara.jar -v -t com.example.Test arg1 arg2

This would call the main-method of the class "com.example.Test" parsing the arguments "arg1" and "arg2".