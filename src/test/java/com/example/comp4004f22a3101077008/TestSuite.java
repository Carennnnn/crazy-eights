package com.example.comp4004f22a3101077008;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({CardTest.class, PlayerTest.class, GameTest.class, AcceptanceTest.class})
public class TestSuite {}
