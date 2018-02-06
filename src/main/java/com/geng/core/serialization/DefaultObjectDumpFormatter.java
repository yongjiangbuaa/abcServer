package com.geng.core.serialization;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:35
 */

import java.util.Arrays;

public class DefaultObjectDumpFormatter {
    public static final char TOKEN_INDENT_OPEN = '{';
    public static final char TOKEN_INDENT_CLOSE = '}';
    public static final char TOKEN_DIVIDER = ';';

    public DefaultObjectDumpFormatter() {
    }

    public static String prettyPrintByteArray(byte[] bytes) {
        return bytes == null?"Null":String.format("Byte[%s]", new Object[]{Integer.valueOf(bytes.length)});
    }

    public static String prettyPrintDump(String rawDump) {
        StringBuilder buf = new StringBuilder();
        int indentPos = 0;

        for(int i = 0; i < rawDump.length(); ++i) {
            char ch = rawDump.charAt(i);
            if(ch == 123) {
                ++indentPos;
                buf.append("\n").append(getFormatTabs(indentPos));
            } else if(ch == 125) {
                --indentPos;
                if(indentPos < 0) {
                    throw new IllegalStateException("Argh! The indentPos is negative. TOKENS ARE NOT BALANCED!");
                }

                buf.append("\n").append(getFormatTabs(indentPos));
            } else if(ch == 59) {
                buf.append("\n").append(getFormatTabs(indentPos));
            } else {
                buf.append(ch);
            }
        }

        if(indentPos != 0) {
            throw new IllegalStateException("Argh! The indentPos is not == 0. TOKENS ARE NOT BALANCED!");
        } else {
            return buf.toString();
        }
    }

    private static String getFormatTabs(int howMany) {
        return strFill('\t', howMany);
    }

    private static String strFill(char c, int howMany) {
        char[] chars = new char[howMany];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
