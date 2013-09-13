package pl.codearte;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramData;

import java.text.DecimalFormat;

public class AtomicMonitorTest {

    private static final int WARMUP_ITERATIONS = 1_000_000;
    private static final int TEST_ITERATIONS = 10_000_000;
    private static final DecimalFormat FORMAT = new DecimalFormat("###,###");
    private static long counter = 0L;
    private static long term = 0L;
    private static Thread last;

    public static void main(String[] args) throws InterruptedException {
        final Monitor[] monitors = {new YieldingMonitor(), new ParkingMonitor(), new PausingMonitor(16), new LockingMonitor(false)};
        final Histogram histogram = new Histogram(10_000_000, 5);

        for (final Monitor monitor : monitors) {
            final Adder adder = new Adder(monitor, histogram);
            sout("\n=========================\n %s \n=========================", adder.name);
            warmup(adder);
            for (int i = 1; i < 5; i++) {
                final int threadCount = 1 << i;
                testAdder(threadCount, TEST_ITERATIONS, adder);
            }
        }
    }

    private static void testAdder(int threadCount, int iterations, Adder adder) throws InterruptedException {
        final long duration = runThreads(threadCount, iterations, adder);
        printSummary(threadCount, iterations, adder, duration);
        cleanup(adder);
    }

    private static void warmup(Adder adder) throws InterruptedException {
//        System.out.println("\n+++++++++++++++++ WARMUP START +++++++++++++++++\n");
        runThreads(4, WARMUP_ITERATIONS, adder);
        cleanup(adder);
        System.gc();
        Thread.sleep(2000);
//        System.out.println("\n+++++++++++++++++ WARMUP FINISH +++++++++++++++++\n");
    }

    private static void cleanup(Adder adder) {
        counter = 0L;
        term = 0L;
        last = null;
        adder.histogram.reset();
    }

    private static void printSummary(int threadCount, int iterations, Adder adder, long duration) {
        final String ops = FORMAT.format((iterations * threadCount * 1000L) / duration);
        final HistogramData histogram = adder.histogram.getHistogramData();

        sout("Threads: %s", threadCount);
        sout("Ops/s: %s", ops);
        sout("Fairness stats [term - number of successive (in a row) monitor enters for an individual thread]:");
        sout("\tMin term: %s", histogram.getMinValue());
        sout("\tMax term: %s", histogram.getMaxValue());
        sout("\tMean term: %s", histogram.getMean());
        sout("\tStd dev: %s", histogram.getStdDeviation());

        sout("\t%sth percentile: %s", 50, histogram.getValueAtPercentile(50));
        sout("\t%sth percentile: %s", 90, histogram.getValueAtPercentile(90));
        sout("\t%sth percentile: %s", 99, histogram.getValueAtPercentile(99));
        sout("\t%sth percentile: %s", 99.9, histogram.getValueAtPercentile(999));
        sout("\t%sth percentile: %s", 99.99, histogram.getValueAtPercentile(9999));
        sout("-------------------------------------------");
    }

    private static long runThreads(int threadCount, int iterations, Adder adder) throws InterruptedException {
        long expected = 0L;
        final TestThread[] threads = new TestThread[threadCount];
        final long start = System.currentTimeMillis();
        for (int i = 1; i <= threadCount; i++) {
            expected += (i * iterations);
            final TestThread thread = new TestThread(i, iterations, adder);
            (threads[i-1] = thread).start();
        }
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        final long duration = System.currentTimeMillis() - start;
        if (counter != expected) throw new IllegalStateException("Inconsistency detected.\nExpected: " + expected +
                "\nActual: " + counter + "\nthreadCount: " + threadCount + " monitor: " + adder.name);
        return duration;
    }

    static class Adder {
        final Monitor monitor;
        final String name;
        final Histogram histogram;

        Adder(final Monitor monitor, final Histogram histogram) {
            this.monitor = monitor;
            this.name = monitor.getClass().getSimpleName();
            this.histogram = histogram;
        }

        void add(int i) {
            try {
                monitor.enter();
                final Thread current = Thread.currentThread();
                if (last == current) term++;
                else {
                    histogram.recordValue(term);
                    term = 0;
                }
                last = current;
                counter += i;
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            } finally { monitor.exit(); }
        }
    }

    static class TestThread extends Thread {
        private final int increment, count;
        private final Adder adder;

        public TestThread(final int i, final int c, final Adder adder) {
            increment = i; count = c;
            this.adder = adder;
        }

        @Override
        public void run() {
            for (int i = 0; i < count; i++) adder.add(increment);
        }
    }

    private static void sout(String str, Object... args) {
        System.out.println(String.format(str, args));
    }

}
