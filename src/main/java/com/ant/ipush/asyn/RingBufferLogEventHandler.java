package com.ant.ipush.asyn;//package com.hll.ipush.asyn;
//
//import com.lmax.disruptor.Sequence;
//import com.lmax.disruptor.SequenceReportingEventHandler;
//
//public class RingBufferLogEventHandler implements
//        SequenceReportingEventHandler<SendMessageEvent> {
//
//    private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
//    private Sequence sequenceCallback;
//    private int counter;
//
//    @Override
//    public void setSequenceCallback(final Sequence sequenceCallback) {
//        this.sequenceCallback = sequenceCallback;
//    }
//
//    @Override
//    public void onEvent(final SendMessageEvent event, final long sequence,
//            final boolean endOfBatch) throws Exception {
//        event.execute(endOfBatch);
//        event.clear();
//
//        // notify the BatchEventProcessor that the sequence has progressed.
//        // Without this callback the sequence would not be progressed
//        // until the batch has completely finished.
//        if (++counter > NOTIFY_PROGRESS_THRESHOLD) {
//            sequenceCallback.set(sequence);
//            counter = 0;
//        }
//    }
//
//}