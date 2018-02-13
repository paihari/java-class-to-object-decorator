package com.gundi.decorator.test;

import java.io.PrintStream;

/**
 * Created by pai on 12.02.18.
 */
public class HelloWorld {

    public static void main(String args []) {
        print(System.out);
    }

    public static void print(PrintStream out) {
        out.print("Hello World - ");
    }
}
