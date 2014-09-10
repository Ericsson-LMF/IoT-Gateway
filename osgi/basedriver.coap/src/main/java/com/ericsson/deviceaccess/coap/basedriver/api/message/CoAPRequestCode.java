package com.ericsson.deviceaccess.coap.basedriver.api.message;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author delma
 */
public enum CoAPRequestCode implements CoAPCode {

    EMPTY(0),
    GET(1),
    POST(2),
    PUT(3),
    DELETE(4);

    private final int detail;

    CoAPRequestCode(int detail) {
        this.detail = detail;
    }

    @Override
    public int getCodeClass() {
        return 0;
    }

    @Override
    public int getCodeDetail() {
        return detail;
    }

    @Override
    public String getPlainDescription() {
        return toString();
    }

    public static boolean isAllowed(int no) {
        int codeClass = CoAPCode.getCodeClass(no);
        int detail = CoAPCode.getCodeDetail(no);
        return codeClass == 0 && 0 < detail && detail < 32;
    }

    private static final Map<Integer, CoAPRequestCode> noMap = new HashMap<>();

    static {
        for (CoAPRequestCode content : CoAPRequestCode.values()) {
            noMap.put(content.getNo(), content);
        }
    }

    public static CoAPRequestCode get(int no) {
        return noMap.get(no);
    }
}
