package com.ant.ipush.asyn;

import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;

public class LogEventHandler implements
        SequenceReportingEventHandler<AsynLogEvent> {

    private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
    private Sequence sequenceCallback;
    private int counter;

    @Override
    public void setSequenceCallback(final Sequence sequenceCallback) {
        this.sequenceCallback = sequenceCallback;
    }

    @Override
    public void onEvent(final AsynLogEvent event, final long sequence,
                        final boolean endOfBatch) throws Exception {
        event.execute(endOfBatch);
        event.clear();

        if (++counter > NOTIFY_PROGRESS_THRESHOLD) {
            sequenceCallback.set(sequence);
            counter = 0;
        }
    }

}
