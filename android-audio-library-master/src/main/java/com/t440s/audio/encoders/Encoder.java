package com.t440s.audio.encoders;

public interface Encoder {

    void encode(short[] buf, int pos, int len);

    void close();

}
