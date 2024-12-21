package com.haleydu.cimoc.global;

public class FastClick {

    private static long last = 0;

    public static boolean isClickValid() {
        long cur = System.currentTimeMillis();
        boolean valid = cur - last > 400;
        if (valid) {
            last = cur;
        }
        return valid;
    }
}
