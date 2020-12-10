package org.apache.poi.ss.usermodel;

public enum BorderStyle {
    NONE(0),
    THIN(1),
    MEDIUM(2),
    DASHED(3),
    DOTTED(4),
    THICK(5),
    DOUBLE(6),
    HAIR(7),
    MEDIUM_DASHED(8),
    DASH_DOT(9),
    MEDIUM_DASH_DOT(10),
    DASH_DOT_DOT(11),
    MEDIUM_DASH_DOT_DOT(12),
    SLANTED_DASH_DOT(13);

    private final short code;
    private static final BorderStyle[] _table = new BorderStyle[14];

    private BorderStyle(int code) {
        this.code = (short)code;
    }

    public short getCode() {
        return this.code;
    }

    public static BorderStyle valueOf(short code) {
        return _table[code];
    }

    static {
        BorderStyle[] arr$ = values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            BorderStyle c = arr$[i$];
            _table[c.getCode()] = c;
        }

    }
}
