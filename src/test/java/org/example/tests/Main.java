package org.example.tests;

import org.testng.TestNG;

import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        TestNG testNG = new TestNG();
        String testngXmlPath = Main.class.getClassLoader().getResource("testng.xml").getPath();
        testNG.setTestSuites(Collections.singletonList(testngXmlPath));

        testNG.run();
    }
}