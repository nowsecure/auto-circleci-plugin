package com.nowsecure.auto.domain;

interface ColorConstants {
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_RESET = "\u001B[0m";
}

public enum Color {
    Black(ColorConstants.ANSI_BLACK),
    Red(ColorConstants.ANSI_RED),
    Green(ColorConstants.ANSI_GREEN),
    Yellow(ColorConstants.ANSI_YELLOW),
    Blue(ColorConstants.ANSI_BLUE),
    Purple(ColorConstants.ANSI_PURPLE),
    Cyan(ColorConstants.ANSI_CYAN),
    White(ColorConstants.ANSI_WHITE);

    public final String code;

    Color(String code) {
        this.code = code;
    }

    public String format(String msg) {
        return this.code + msg + ColorConstants.ANSI_RESET;
    }

}
