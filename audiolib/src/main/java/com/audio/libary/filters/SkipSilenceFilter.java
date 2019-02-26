package com.audio.libary.filters;

import com.audio.libary.app.RawSamples;
import com.audio.libary.app.Sound;
import com.audio.libary.encoders.EncoderInfo;

public class SkipSilenceFilter extends Filter {

    public static final int SILENCE_DB = 33;

    EncoderInfo info;
    long start;
    long samples;

    public SkipSilenceFilter(EncoderInfo info) {
        this.info = info;
        this.start = -1;
    }

    @Override
    public void filter(Buffer buf) {
        double db = RawSamples.getDB(buf.buf, buf.pos, buf.len);
        db = Sound.MAXIMUM_DB + db;
        if (db <= SILENCE_DB) {
            if (start < 0) {
                start = samples;
            }
            long diff = samples - start;
            if (diff > info.hz) {
                buf.pos = 0;
                buf.len = 0;
            }
        } else {
            start = -1;
        }
        samples += buf.len / info.channels;
    }
}
