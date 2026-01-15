import java.util.Arrays;
import java.util.Random;

/**
 * HeapExperiments.java
 * НЕ СДАВАТЬ вместе с Heap.java. Только для экспериментов/отчёта.
 *
 * Эксперименты (как в задании на скрине):
 * 1) insert n случайных ключей 1..n, затем deleteMin (1 раз)
 * 2) insert, deleteMin (1 раз), затем deleteMin пока size == 46
 * 3) insert, deleteMin (1 раз), затем floor(0.1n) раз decreaseKey для максимальных ключей до 0,
 *    затем deleteMin (1 раз)
 *
 * Для каждого эксперимента: 20 прогонов. В каждом прогоне используем случайную перестановку 1..n.
 * ВАЖНО: один и тот же набор перестановок должен использоваться для всех типов куч и всех экспериментов.
 * -> мы делаем seed = BASE_SEED + runIndex и используем ту же perm во всех случаях этого runIndex.
 *
 * Метрики (как на скрине):
 * - время (мс)
 * - размер кучи в конце
 * - число деревьев в конце
 * - links (итого)
 * - cuts (итого)
 * - суммарная стоимость heapifyUp (итого)
 * - максимальная стоимость одной операции за эксперимент:
 *   cost(op) = Δlinks + Δcuts + ΔheapifyUp  (дельты за одну операцию)
 */
public class HeapExperiments {

    // По умолчанию как в задании:
    private static final int DEFAULT_N = 464_646;
    private static final int DEFAULT_RUNS = 20;

    // Чтобы “набор перестановок” был воспроизводимым:
    private static final long BASE_SEED = 20260115L;

    private enum HeapType {
        BINOMIAL(false, false),            // lazyMelds=false, lazyDecreaseKeys=false
        LAZY_BINOMIAL(true, false),        // lazyMelds=true,  lazyDecreaseKeys=false
        FIBONACCI(true, true),             // lazyMelds=true,  lazyDecreaseKeys=true
        BINOMIAL_WITH_CUTS(false, true);   // lazyMelds=false, lazyDecreaseKeys=true

        final boolean lazyMelds;
        final boolean lazyDecreaseKeys;

        HeapType(boolean lm, boolean ldk) {
            this.lazyMelds = lm;
            this.lazyDecreaseKeys = ldk;
        }
    }

    private static final class RunStats {
        long timeMs;
        int finalSize;
        int finalNumTrees;
        long links;
        long cuts;
        long heapify;
        long maxOpCost;
    }

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
            // Средние (округление вниз):
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

        // Можно переопределить: java HeapExperiments 100000 10
        if (args.length >= 1) n = Integer.parseInt(args[0]);
        if (args.length >= 2) runs = Integer.parseInt(args[1]);

        System.out.println("n=" + n + ", runs=" + runs);
        System.out.println("SeedBase=" + BASE_SEED);
        System.out.println();

        // agg[experimentId-1][heapTypeIndex]
        Agg[][] agg = new Agg[3][HeapType.values().length];
        for (int e = 0; e < 3; e++) {
            for (int t = 0; t < HeapType.values().length; t++) {
                agg[e][t] = new Agg();
            }
        }

        for (int run = 0; run < runs; run++) {
            long seed = BASE_SEED + run;
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
            for (int t = 0; t < HeapType.values().length; t++) {
                HeapType type = HeapType.values()[t];
                System.out.println(type.name() + "  ->  " + agg[exp - 1][t].avgLine());
            }
        }

        System.out.println("\n(Подсказка: сначала можно запустить на меньшем n, например 50000, чтобы убедиться, что всё ок.)");
    }

    private static RunStats runSingle(int experimentId, HeapType type, int[] perm) {
        int n = perm.length;
        Heap heap = new Heap(type.lazyMelds, type.lazyDecreaseKeys);

        // Для эксперимента 3 нужны указатели на элементы по ключу:
        Heap.HeapItem[] byKey = (experimentId == 3) ? new Heap.HeapItem[n + 1] : null;

        RunStats stats = new RunStats();

        long t0 = System.nanoTime();
        long maxCost = 0;

        // ---- INSERT всех 1..n в случайном порядке ----
        for (int i = 0; i < n; i++) {
            int key = perm[i];
            long cost = costOfOpBeforeAfter(heap, () -> {
                Heap.HeapItem it = heap.insert(key, String.valueOf(key));
                if (byKey != null) byKey[key] = it;
            });
            if (cost > maxCost) maxCost = cost;
        }

        // ---- DELETE MIN (1 раз) ----
        if (heap.findMin() != null) {
            long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
            if (cost > maxCost) maxCost = cost;
        }

        // ---- EXPERIMENT-SPECIFIC ----
        if (experimentId == 2) {
            // deleteMin пока не останется 46 элементов
            while (heap.size() > 46) {
                long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
                if (cost > maxCost) maxCost = cost;
            }
        } else if (experimentId == 3) {
            // floor(0.1n) раз: уменьшить ключ максимальных элементов до 0
            int m = (int) Math.floor(0.1 * n);
            // Максимальные ключи: n, n-1, ..., n-m+1
            for (int k = n; k >= n - m + 1; k--) {
                Heap.HeapItem it = byKey[k];
                if (it == null) continue;
                // если элемент уже удалён, it.node == null (по твоей правке)
                if (it.node == null) continue;
                int diff = it.key; // уменьшить до 0
                if (diff <= 0) continue;

                int d = diff;
                long cost = costOfOpBeforeAfter(heap, () -> heap.decreaseKey(it, d));
                if (cost > maxCost) maxCost = cost;
            }

            // снова deleteMin (1 раз)
            if (heap.findMin() != null) {
                long cost = costOfOpBeforeAfter(heap, heap::deleteMin);
                if (cost > maxCost) maxCost = cost;
            }
        }

        long t1 = System.nanoTime();
        stats.timeMs = (t1 - t0) / 1_000_000L;

        // Итоговые метрики
        stats.finalSize = heap.size();
        stats.finalNumTrees = heap.numTrees();
        stats.links = heap.totalLinks();
        stats.cuts = heap.totalCuts();
        stats.heapify = heap.totalHeapifyCosts();
        stats.maxOpCost = maxCost;

        return stats;
    }

    /**
     * Стоимость одной операции по определению задания:
     * cost = Δlinks + Δcuts + ΔheapifyUp
     * Берём значения total* до/после и считаем дельту.
     */
    private static long costOfOpBeforeAfter(Heap heap, Runnable op) {
        long links0 = heap.totalLinks();
        long cuts0 = heap.totalCuts();
        long heapify0 = heap.totalHeapifyCosts();

        op.run();

        long links1 = heap.totalLinks();
        long cuts1 = heap.totalCuts();
        long heapify1 = heap.totalHeapifyCosts();

        long dLinks = links1 - links0;
        long dCuts = cuts1 - cuts0;
        long dHeapify = heapify1 - heapify0;

        return dLinks + dCuts + dHeapify;
    }

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
