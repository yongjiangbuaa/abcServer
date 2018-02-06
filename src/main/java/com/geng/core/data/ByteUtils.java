package com.geng.core.data;

/**
 * Author: shushenglin
 * Date:   16/2/2 15:33
 */

import java.nio.ByteBuffer;

public class ByteUtils {
    private static final int HEX_BYTES_PER_LINE = 16;
    private static final char TAB = '\t';
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final char DOT = '.';

    public ByteUtils() {
    }

    public static byte[] resizeByteArray(byte[] source, int pos, int size) {
        byte[] tmpArray = new byte[size];
        System.arraycopy(source, pos, tmpArray, 0, size);
        return tmpArray;
    }

    public static String fullHexDump(ByteBuffer buffer, int bytesPerLine) {
        return fullHexDump(buffer.array(), bytesPerLine);
    }

    public static String fullHexDump(ByteBuffer buffer) {
        return fullHexDump((byte[])buffer.array(), 16);
    }

    public static String fullHexDump(byte[] buffer) {
        return fullHexDump((byte[])buffer, 16);
    }

    public static String fullHexDump(byte[] buffer, int bytesPerLine) {
        StringBuilder sb = (new StringBuilder("Binary size: ")).append(buffer.length).append("\n");
        StringBuilder hexLine = new StringBuilder();
        StringBuilder chrLine = new StringBuilder();
        int index = 0;
        int count = 0;

        do {
            byte currByte = buffer[index];
            String j = Integer.toHexString(currByte & 255);
            if(j.length() == 1) {
                hexLine.append("0");
            }

            hexLine.append(j.toUpperCase()).append(" ");
            char currChar = currByte >= 33 && currByte <= 126?(char)currByte:46;
            chrLine.append(currChar);
            ++count;
            if(count == bytesPerLine) {
                count = 0;
                sb.append(hexLine).append('\t').append(chrLine).append(NEW_LINE);
                hexLine.delete(0, hexLine.length());
                chrLine.delete(0, chrLine.length());
            }

            ++index;
        } while(index < buffer.length);

        if(count != 0) {
            for(int var10 = bytesPerLine - count; var10 > 0; --var10) {
                hexLine.append("   ");
                chrLine.append(" ");
            }

            sb.append(hexLine).append('\t').append(chrLine).append(NEW_LINE);
        }

        return sb.toString();
    }
}
