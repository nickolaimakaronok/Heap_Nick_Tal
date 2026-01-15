import java.util.Random;

/**
 * HeapExperiments.java
 * DO NOT SUBMIT this file with Heap.java.
 *
 * Runs the experimental section:
 *  - 3 experiments
 *  - 4 heap types (defined by lazyMelds / lazyDecreaseKeys)
 *  - averages over RUNS permutations (default 20)
 *
 * Experiment definitions (per assignment):
 *  Exp 1:
 *    Insert keys 1..n (random permutation), then deleteMin once.
 *
 *  Exp 2:
 *    Insert keys 1..n (random permutation), deleteMin once,
 *    then delete the MAXIMUM (using a pointer: HeapItem) until size becomes 46.
 *
 *  Exp 3:
 *    Insert keys 1..n (random permutation), deleteMin once,
 *    then floor(0.1n) decreaseKey operations that reduce selected keys to 0,
 *    then deleteMin once again.
 *
 * Metrics printed (for the assignment table):
 *  - avgTimeMs
 *  - avgFinalSize
 *  - avgNumTrees
 *  - avgLinks
 *  - avgCuts
 *  - avgHeapifyUp
 *  - avgMaxOpCost
 *
 * Operation cost definition (per assignment):
 *  cost(op) = Δlinks + Δcuts + ΔheapifyUp  (delta for that single operation)
 *  maxOpCost = max cost(op) over all operations executed in that experiment run.
 */
public class HeapExperiments {

    private static final int DEFAULT_N = 464_646;
    private static final int DEFAULT_RUNS = 20;

    // Same permutations across heap types and experiments (per runIndex)
    private static final long BASE_SEED = 20260115L;

    // Exp2: target remaining size
    private static final int EXP2_TARGET_REMAIN = 46;

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
        double timeMs;
        int finalSize;
        int finalNumTrees;
        long links;
        long cuts;
        long heapify;
        long maxOpCost;
    }

    /** Aggregator for averaging over runs (2 decimal places in output) */
    private static final class Agg {
        double sumTimeMs = 0;
        double sumFinalSize = 0;
        double sumFinalNumTrees = 0;
        double sumLinks = 0;
        double sumCuts = 0;
        double sumHeapify = 0;
        double sumMaxOpCost = 0;
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
            if (count == 0) return "No runs";
            double avgTime = sumTimeMs / count;
            double avgSize = sumFinalSize / count;
            double avgTrees = sumFinalNumTrees / count;
            double avgLinks = sumLinks / count;
            double avgCuts = sumCuts / count;
            double avgHeapify = sumHeapify / count;
            double avgMaxCost = sumMaxOpCost / count;

            return String.format(
                    "avgTimeMs=%.2f | avgFinalSize=%.2f | avgNumTrees=%.2f | avgLinks=%.2f | avgCuts=%.2f | avgHeapifyUp=%.2f | avgMaxOpCost=%.2f",
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
                for (HeapType type : HeapType.values()) {
                    RunStats s = runSingle(exp, type, perm);
                    agg[exp - 1][type.ordinal()].add(s);
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
    }

    /**
     * Runs one experiment for one heap type using a fixed permutation.
     * Returns all metrics collected for the assignment table.
     */
    private static RunStats runSingle(int experimentId, HeapType type, int[] perm) {
        int n = perm.length;
        Heap heap = new Heap(type.lazyMelds, type.lazyDecreaseKeys);

        // Need key -> HeapItem pointers for Exp2 and Exp3
        Heap.HeapItem[] byKey = (experimentId == 2 || experimentId == 3)
                ? new Heap.HeapItem[n + 1]
                : null;

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

        // 3) Experiment-specific part
        if (experimentId == 2) {
            // Exp2 (per assignment): delete MAXIMUM using a pointer until size becomes 46.
            // Keys are 1..n; key 1 was removed by deleteMin above.
            if (n > EXP2_TARGET_REMAIN) {
                int k = n;
                while (heap.size() > EXP2_TARGET_REMAIN) {

                    // Find next existing max key (skip already deleted)
                    while (k > 0 && (byKey[k] == null || byKey[k].node == null)) {
                        k--;
                    }
                    if (k <= 0) break; // safety (should not happen)

                    Heap.HeapItem victim = byKey[k];
                    long cost = costOfOpBeforeAfter(heap, () -> heap.delete(victim));
                    if (cost > maxCost) maxCost = cost;

                    k--;
                }
            }
        } else if (experimentId == 3) {
            // Exp3: floor(0.1n) decreaseKey ops reducing chosen keys to 0, then deleteMin once.
            int m = (int) Math.floor(0.1 * n);

            // Reduce the largest keys: n, n-1, ..., n-m+1 down to 0
            for (int k = n; k >= n - m + 1; k--) {
                Heap.HeapItem it = byKey[k];
                if (it == null) continue;
                if (it.node == null) continue; // already deleted (shouldn't happen here)

                int diff = it.key; // reduce from current key to 0
                if (diff <= 0) continue;

                long cost = costOfOpBeforeAfter(heap, () -> heap.decreaseKey(it, diff));
                if (cost > maxCost) maxCost = cost;
            }

            // deleteMin once again
            if (heap.findMin() != null) {
                long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
                if (cost > maxCost) maxCost = cost;
            }
        }

        long t1 = System.nanoTime();
        stats.timeMs = (t1 - t0) / 1_000_000.0;

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

    /** Fisher–Yates shuffle: returns a random permutation of 1..n using the given seed */
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
