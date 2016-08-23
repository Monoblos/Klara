Klara
=====

> Denn mit Klara wird alles klarer.

What is this?
-------------

Klara is a command-line tool for java, which will add informational output to your program at runtime.
This method of debugging is called tracing. The result is similar to adding a huge amount of prints.  
There are currently two modifying modules available:

* The variable change printer will print out the value of a variable whenever it is assigned.
* The line tracer will ensure a least one print per line, to have a trace of the exact line order executed.

Usage
-----

Execution using Klara usually looks like this:

    java -jar Klara.jar -v -t com.example.Test arg1 arg2

This would call the main-method of the class "com.example.Test" parsing the arguments "arg1" and "arg2".

How to build
------------

Using the default ant-task in the "ant.xml" all code will be compiled and packed as a single jar-file. Requires ant 1.7.

How to get the documentation
----------------------------

Using the ant task "generate_javadoc" the entire documentation will be generated in a folder "docs".

Additional information
----------------------

This program was created as a bachelor thesis. The thesis can be found as a separate git-project.