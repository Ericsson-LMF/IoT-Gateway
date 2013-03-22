package com.ericsson.deviceaccess.tutorial;

import com.ericsson.deviceaccess.spi.schema.SchemaBasedGenericDevice;

class DummyDevice extends SchemaBasedGenericDevice {
    @Override
    public String getSerializedNode(String path, int format) {
        try {
            return super.getSerializedNode(path, format);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String serialize(int format) {
        try {
            return super.serialize(format);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}