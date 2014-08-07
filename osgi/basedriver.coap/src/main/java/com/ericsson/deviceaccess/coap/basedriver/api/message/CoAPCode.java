package com.ericsson.deviceaccess.coap.basedriver.api.message;

/**
 *
 * @author delma
 */
public interface CoAPCode {

    /**
     * @return Class value between 0 - 7
     */
    int getCodeClass();

    /**
     * @return Detail value between 0 - 31
     */
    int getCodeDetail();

    /**
     * @return Identifying number for communication
     */
    default int getNo() {
        return getCodeClass() << 5 | getCodeDetail();
    }

    /**
     * @return Description of codes meaning
     */
    String getPlainDescription();

    /**
     * @return Description of codes meaning with class and detail information
     */
    default String getDescription() {
        StringBuilder result = new StringBuilder();
        result.append(getCodeClass()).append(".");
        int detail = getCodeDetail();
        if (detail < 10) {
            result.append(0);
        }
        result.append(detail).append(" ").append(getPlainDescription());
        return result.toString();
    }
}
