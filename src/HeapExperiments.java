import java.util.Random;

/**
 * HeapExperiments.java
 * DO NOT SUBMIT this file with Heap.java.
 * This is a standalone runner for the experimental section.
 *
 * It runs 3 experiments, for 4 heap types (controlled by lazyMelds / lazyDecreaseKeys),
 * and averages results over 20 runs.
 *
 * Experiments (as in the assignment screenshots):
 *  Exp 1: insert n items (permutation of 1..n), then deleteMin once.
 *  Exp 2: insert n items, deleteMin once, then deleteMin until heap size becomes 46.
 *  Exp 3: insert n items, deleteMin once,
 *         then floor(0.1n) decreaseKey operations that reduce selected keys down to 0,
 *         then deleteMin once.
 *
 * Important:
 *  - Keys are positive initially (1..n).
 *  - In experiment 3 we reduce some keys from positive to 0.
 *  - One and the same set of permutations must be used for all heap types and experiments:
 *    we use seed = BASE_SEED + runIndex to generate the permutation for that runIndex,
 *    and reuse it everywhere.
 *
 * Metrics (per the assignment table):
 *  - time (ms)
 *  - final heap size
 *  - final number of trees (numTrees)
 *  - total links
 *  - total cuts
 *  - total heapifyUp costs
 *  - max operation cost during the experiment:
 *      opCost = Δlinks + Δcuts + ΔheapifyUp  (delta per single operation)
 */
public class HeapExperiments {

    // Default parameters (assignment uses n = 464,646)
    private static final int DEFAULT_N = 464_646;
    private static final int DEFAULT_RUNS = 20;

    // Base seed so that permutations are reproducible across runs
    private static final long BASE_SEED = 20260115L;

    /** The 4 heap variants required by the assignment */
    private enum HeapType {
        BINOMIAL(false, false),
        LAZY_BINOMIAL(true, false),
        FIBONACCI(true, true),
        BINOMIAL_WITH_CUTS(false, true);

        final boolean lazyMelds;
        final boolean lazyDecreaseKeys;

        HeapType(boolean lm, boolean ldk) {
            this.lazyMelds = lm;
            this.lazyDecreaseKeys = ldk;
        }
    }

    /** Per-run result (one run = one permutation) */
    private static final class RunStats {
        long timeMs;
        int finalSize;
        int finalNumTrees;
        long links;
        long cuts;
        long heapify;
        long maxOpCost;
    }

    /** Aggregator for averaging over runs */
    private static final class Agg {
        long sumTimeMs = 0;
        long sumFinalSize = 0;
        long sumFinalNumTrees = 0;
        long sumLinks = 0;
        long sumCuts = 0;
        long sumHeapify = 0;
        long sumMaxOpCost = 0;
        int count = 0;

        void add(RunStats s) {
            sumTimeMs += s.timeMs;
            sumFinalSize += s.finalSize;
            sumFinalNumTrees += s.finalNumTrees;
            sumLinks += s.links;
            sumCuts += s.cuts;
            sumHeapify += s.heapify;
            sumMaxOpCost += s.maxOpCost;
            count++;
        }

        String avgLine() {
            long avgTime = sumTimeMs / count;
            long avgSize = sumFinalSize / count;
            long avgTrees = sumFinalNumTrees / count;
            long avgLinks = sumLinks / count;
            long avgCuts = sumCuts / count;
            long avgHeapify = sumHeapify / count;
            long avgMaxCost = sumMaxOpCost / count;

            return String.format(
                    "avgTimeMs=%d | avgFinalSize=%d | avgNumTrees=%d | avgLinks=%d | avgCuts=%d | avgHeapifyUp=%d | avgMaxOpCost=%d",
                    avgTime, avgSize, avgTrees, avgLinks, avgCuts, avgHeapify, avgMaxCost
            );
        }
    }

    public static void main(String[] args) {
        int n = DEFAULT_N;
        int runs = DEFAULT_RUNS;

        // Optional CLI overrides: java HeapExperiments 100000 10
        if (args.length >= 1) n = Integer.parseInt(args[0]);
        if (args.length >= 2) runs = Integer.parseInt(args[1]);

        System.out.println("n=" + n + ", runs=" + runs);
        System.out.println("SeedBase=" + BASE_SEED);
        System.out.println();

        Agg[][] agg = new Agg[3][HeapType.values().length];
        for (int e = 0; e < 3; e++) {
            for (int t = 0; t < HeapType.values().length; t++) {
                agg[e][t] = new Agg();
            }
        }

        for (int run = 0; run < runs; run++) {
            long seed = BASE_SEED + run;

            // One permutation per runIndex; reused for all experiments and heap types
            int[] perm = makePermutation(n, seed);

            for (int exp = 1; exp <= 3; exp++) {
                for (int t = 0; t < HeapType.values().length; t++) {
                    HeapType type = HeapType.values()[t];
                    RunStats s = runSingle(exp, type, perm);
                    agg[exp - 1][t].add(s);
                }
            }

            System.out.println("run " + (run + 1) + "/" + runs + " done");
        }

        System.out.println("\n===== RESULTS (AVERAGE OVER RUNS) =====");
        for (int exp = 1; exp <= 3; exp++) {
            System.out.println("\n--- Experiment " + exp + " ---");
            for (HeapType type : HeapType.values()) {
                System.out.println(type.name() + "  ->  " + agg[exp - 1][type.ordinal()].avgLine());
            }
        }

        System.out.println("\nTip: First run with smaller n (e.g. 50000) to sanity-check, then switch to 464646.");
    }

    /**
     * Runs one experiment for one heap type using a fixed permutation.
     * Returns all metrics collected for the assignment table.
     */
    private static RunStats runSingle(int experimentId, HeapType type, int[] perm) {
        int n = perm.length;
        Heap heap = new Heap(type.lazyMelds, type.lazyDecreaseKeys);

        // For experiment 3 we must be able to access items by key quickly.
        // We store the HeapItem returned by insert(key) at index [key].
        Heap.HeapItem[] byKey = (experimentId == 3) ? new Heap.HeapItem[n + 1] : null;

        RunStats stats = new RunStats();
        long maxCost = 0;

        long t0 = System.nanoTime();

        // 1) Insert all keys in perm order
        for (int i = 0; i < n; i++) {
            int key = perm[i];
            long cost = costOfOpBeforeAfter(heap, () -> {
                Heap.HeapItem it = heap.insert(key, String.valueOf(key));
                if (byKey != null) byKey[key] = it;
            });
            if (cost > maxCost) maxCost = cost;
        }

        // 2) deleteMin once
        if (heap.findMin() != null) {
            long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
            if (cost > maxCost) maxCost = cost;
        }

        // 3) experiment-specific part
        if (experimentId == 2) {
            // deleteMin until only 46 elements remain
            while (heap.size() > 46) {
                long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
                if (cost > maxCost) maxCost = cost;
            }
        } else if (experimentId == 3) {
            // Perform floor(0.1n) decreaseKey operations that reduce selected keys to 0
            int m = (int) Math.floor(0.1 * n);

            // We reduce the largest keys: n, n-1, ..., n-m+1 down to 0
            // (This matches the "reduce max keys to 0" scenario.)
            for (int k = n; k >= n - m + 1; k--) {
                Heap.HeapItem it = byKey[k];
                if (it == null) continue;

                // If the item was deleted earlier, your Heap implementation should set it.node = null
                if (it.node == null) continue;

                int diff = it.key; // reduce from current key to 0
                if (diff <= 0) continue;

                int d = diff;
                long cost = costOfOpBeforeAfter(heap, () -> heap.decreaseKey(it, d));
                if (cost > maxCost) maxCost = cost;
            }

            // deleteMin once again
            if (heap.findMin() != null) {
                long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
                if (cost > maxCost) maxCost = cost;
            }
        }

        long t1 = System.nanoTime();
        stats.timeMs = (t1 - t0) / 1_000_000L;

        // Final metrics to fill the assignment table
        stats.finalSize = heap.size();
        stats.finalNumTrees = heap.numTrees();
        stats.links = heap.totalLinks();
        stats.cuts = heap.totalCuts();
        stats.heapify = heap.totalHeapifyCosts();
        stats.maxOpCost = maxCost;

        return stats;
    }

    /**
     * Operation cost definition from the assignment:
     * cost(op) = Δlinks + Δcuts + ΔheapifyUp
     * We compute deltas by reading totals before and after a single operation.
     */
    private static long costOfOpBeforeAfter(Heap heap, Runnable op) {
        long links0 = heap.totalLinks();
        long cuts0 = heap.totalCuts();
        long heapify0 = heap.totalHeapifyCosts();

        op.run();

        long links1 = heap.totalLinks();
        long cuts1 = heap.totalCuts();
        long heapify1 = heap.totalHeapifyCosts();

        return (links1 - links0) + (cuts1 - cuts0) + (heapify1 - heapify0);
    }

    /** Fisher-Yates shuffle to build a random permutation of 1..n */
    private static int[] makePermutation(int n, long seed) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i + 1;

        Random rnd = new Random(seed);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return a;
    }
}
