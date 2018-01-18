package com.geng.test;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class JunitTests {
    @Test
    public void testEcho(){
        System.out.println("junit test echo!");
        assertTrue(1 == 2);
    }

}
