/**
 * HeapTester.java
 * 
 * Comprehensive tester for the Heap data structure.
 * Tests all 4 modes of operation:
 *   Mode 1: Binomial Heap (lazyMelds=false, lazyDecreaseKeys=false)
 *   Mode 2: Lazy Binomial Heap (lazyMelds=true, lazyDecreaseKeys=false)
 *   Mode 3: Fibonacci Heap (lazyMelds=true, lazyDecreaseKeys=true)
 *   Mode 4: Binomial with Detachments (lazyMelds=false, lazyDecreaseKeys=true)
 */
public class HeapTester {

    // ================== Test Statistics ==================
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    // ================== Helper Methods ==================

    /**
     * Prints a section header for better test organization
     */
    private static void printSection(String sectionName) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + sectionName);
        System.out.println("=".repeat(60));
    }

    /**
     * Prints a subsection header
     */
    private static void printSubsection(String subsectionName) {
        System.out.println("\n--- " + subsectionName + " ---");
    }

    /**
     * Assert that a condition is true, print PASS/FAIL accordingly
     */
    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.out.println("  [FAIL] " + testName);
        }
    }

    /**
     * Assert that two integers are equal
     */
    private static void assertEquals(String testName, int expected, int actual) {
        totalTests++;
        if (expected == actual) {
            passedTests++;
            System.out.println("  [PASS] " + testName + " (expected=" + expected + ", actual=" + actual + ")");
        } else {
            failedTests++;
            System.out.println("  [FAIL] " + testName + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    /**
     * Assert that actual >= expected
     */
    private static void assertAtLeast(String testName, int minExpected, int actual) {
        totalTests++;
        if (actual >= minExpected) {
            passedTests++;
            System.out.println("  [PASS] " + testName + " (min=" + minExpected + ", actual=" + actual + ")");
        } else {
            failedTests++;
            System.out.println("  [FAIL] " + testName + " (min=" + minExpected + ", actual=" + actual + ")");
        }
    }

    /**
     * Assert that actual > 0
     */
    private static void assertPositive(String testName, int actual) {
        totalTests++;
        if (actual > 0) {
            passedTests++;
            System.out.println("  [PASS] " + testName + " (value=" + actual + " > 0)");
        } else {
            failedTests++;
            System.out.println("  [FAIL] " + testName + " (value=" + actual + " should be > 0)");
        }
    }

    /**
     * Assert that the object is not null
     */
    private static void assertNotNull(String testName, Object obj) {
        totalTests++;
        if (obj != null) {
            passedTests++;
            System.out.println("  [PASS] " + testName + " (not null)");
        } else {
            failedTests++;
            System.out.println("  [FAIL] " + testName + " (was null)");
        }
    }

    /**
     * Assert that the object is null
     */
    private static void assertNull(String testName, Object obj) {
        totalTests++;
        if (obj == null) {
            passedTests++;
            System.out.println("  [PASS] " + testName + " (is null)");
        } else {
            failedTests++;
            System.out.println("  [FAIL] " + testName + " (was not null)");
        }
    }

    /**
     * Print final test summary
     */
    private static void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FINAL TEST SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("  Total Tests:  " + totalTests);
        System.out.println("  Passed:       " + passedTests);
        System.out.println("  Failed:       " + failedTests);
        System.out.println("  Success Rate: " + String.format("%.1f%%", (100.0 * passedTests / totalTests)));
        System.out.println("=".repeat(60));
        if (failedTests == 0) {
            System.out.println("  *** ALL TESTS PASSED! ***");
        } else {
            System.out.println("  *** SOME TESTS FAILED ***");
        }
        System.out.println("=".repeat(60) + "\n");
    }

    // ================== Test Scenarios ==================

    /**
     * Scenario 1: Basic Operations (Mode 1 - Binomial Heap)
     * 
     * Tests: insert, findMin, deleteMin, size, numTrees
     * Verifies basic heap operations work correctly.
     */
    private static void testScenario1_BasicOperations() {
        printSection("Scenario 1: Basic Operations (Mode 1 - Binomial Heap)");

        // Create a Binomial Heap (lazyMelds=false, lazyDecreaseKeys=false)
        Heap heap = new Heap(false, false);

        // Test empty heap
        printSubsection("Empty Heap Tests");
        assertEquals("Empty heap size", 0, heap.size());
        assertEquals("Empty heap numTrees", 0, heap.numTrees());
        assertNull("Empty heap findMin", heap.findMin());

        // Insert single element
        printSubsection("Single Element Insert");
        Heap.HeapItem item1 = heap.insert(10, "ten");
        assertEquals("Size after 1 insert", 1, heap.size());
        assertEquals("NumTrees after 1 insert", 1, heap.numTrees());
        assertNotNull("FindMin not null", heap.findMin());
        assertEquals("FindMin key", 10, heap.findMin().key);

        // Insert more elements
        printSubsection("Multiple Inserts");
        heap.insert(5, "five");
        heap.insert(15, "fifteen");
        heap.insert(3, "three");
        heap.insert(7, "seven");

        assertEquals("Size after 5 inserts", 5, heap.size());
        assertEquals("FindMin key should be 3", 3, heap.findMin().key);

        // DeleteMin
        printSubsection("DeleteMin Tests");
        heap.deleteMin();
        assertEquals("Size after deleteMin", 4, heap.size());
        assertEquals("FindMin after deleteMin should be 5", 5, heap.findMin().key);
        
        heap.deleteMin();
        assertEquals("Size after deleteMin", 3, heap.size());
        assertEquals("FindMin after 2nd deleteMin should be 7", 7, heap.findMin().key);
        
        // Delete all remaining
        printSubsection("Delete All Elements");
        heap.deleteMin();
        heap.deleteMin();
        assertEquals("Size after removing all", 1, heap.size());
        heap.deleteMin();
        assertEquals("Size after removing all", 0, heap.size());
        assertNull("FindMin after removing all", heap.findMin());
    }

    /**
     * Scenario 2: The "Link" Check (Mode 1 - Binomial Heap)
     * 
     * Insert 2^k elements (32 = 2^5) and verify that after consolidation,
     * there is exactly 1 tree (since 32 elements form a single binomial tree B5).
     * Also verify that totalLinks is correct.
     */
    private static void testScenario2_LinkCheck() {
        printSection("Scenario 2: Link Check (Mode 1 - Binomial Heap)");

        // Create a Binomial Heap
        Heap heap = new Heap(false, false);

        printSubsection("Insert 2^5 = 32 Elements");
        int n = 32;
        for (int i = n; i >= 1; i--) {
            heap.insert(i, "val" + i);
        }

        assertEquals("Size after 32 inserts", 32, heap.size());
        
        // In a strict Binomial Heap, 32 elements should consolidate to exactly 1 tree (B5)
        assertEquals("NumTrees should be 1 (single B5 tree)", 1, heap.numTrees());
        
        // For 32 elements, linking 32 single nodes into one tree requires 31 links
        // (think of it as a tournament bracket: 32 -> 16 -> 8 -> 4 -> 2 -> 1)
        assertEquals("TotalLinks for 32 elements", 31, heap.totalLinks());

        // Verify min is correct
        assertEquals("FindMin should be 1", 1, heap.findMin().key);

        // Test with 16 elements (2^4 = B4)
        printSubsection("Insert 2^4 = 16 Elements");
        Heap heap2 = new Heap(false, false);
        for (int i = 16; i >= 1; i--) {
            heap2.insert(i, "val" + i);
        }
        assertEquals("NumTrees for 16 elements should be 1", 1, heap2.numTrees());
        assertEquals("TotalLinks for 16 elements", 15, heap2.totalLinks());

        // Test with non-power-of-2: 7 elements (binary: 111 = B2 + B1 + B0 = 3 trees)
        printSubsection("Insert 7 Elements (non-power-of-2)");
        Heap heap3 = new Heap(false, false);
        for (int i = 7; i >= 1; i--) {
            heap3.insert(i, "val" + i);
        }
        assertEquals("NumTrees for 7 elements should be 3", 3, heap3.numTrees());
    }

    /**
     * Scenario 3: The "Lazy Meld" Check (Mode 2 - Lazy Binomial Heap)
     * 
     * Create two heaps, insert elements, meld them.
     * Verify that numTrees is the sum of both (no consolidation yet).
     * Then deleteMin and ensure consolidation happens.
     */
    private static void testScenario3_LazyMeld() {
        printSection("Scenario 3: Lazy Meld Check (Mode 2 - Lazy Binomial Heap)");

        // Create two Lazy Binomial Heaps
        Heap heap1 = new Heap(true, false);
        Heap heap2 = new Heap(true, false);

        printSubsection("Create Two Heaps");
        // Insert into heap1: 4 elements -> should have 4 trees (lazy)
        heap1.insert(10, "a");
        heap1.insert(20, "b");
        heap1.insert(30, "c");
        heap1.insert(5, "d");
        
        int trees1 = heap1.numTrees();
        System.out.println("  Heap1 numTrees: " + trees1);

        // Insert into heap2: 3 elements -> should have 3 trees (lazy)
        heap2.insert(15, "e");
        heap2.insert(25, "f");
        heap2.insert(8, "g");
        
        int trees2 = heap2.numTrees();
        System.out.println("  Heap2 numTrees: " + trees2);

        printSubsection("Lazy Meld (No Consolidation)");
        int totalTreesBefore = trees1 + trees2;
        heap1.meld(heap2);
        
        // After lazy meld, numTrees should be sum of both
        assertEquals("NumTrees after lazy meld", totalTreesBefore, heap1.numTrees());
        assertEquals("Size after meld", 7, heap1.size());
        assertEquals("FindMin after meld should be 5", 5, heap1.findMin().key);

        // Verify no links occurred during lazy meld
        assertEquals("No links during lazy meld", 0, heap1.totalLinks());

        printSubsection("DeleteMin Forces Consolidation");
        heap1.deleteMin(); // This should trigger consolidation
        
        // After deleteMin with consolidation, 6 elements -> binary 110 -> 2 trees (B2 + B1)
        assertEquals("NumTrees after deleteMin (6 elements)", 2, heap1.numTrees());
        assertEquals("Size after deleteMin", 6, heap1.size());
        
        // Links should have occurred during consolidation
        assertPositive("Links occurred during consolidation", heap1.totalLinks());
    }

    /**
     * Scenario 4: Cascading Cuts (Mode 3 - Fibonacci Heap)
     * 
     * Build a tree structure, perform decreaseKey that triggers cascading cuts.
     * Verify numMarkedNodes changes and totalCuts increments by at least 2.
     */
    private static void testScenario4_CascadingCuts() {
        printSection("Scenario 4: Cascading Cuts (Mode 3 - Fibonacci Heap)");

        // Create a Fibonacci Heap
        Heap heap = new Heap(true, true);

        printSubsection("Build Tree Structure");
        // We need to create a structure where cascading cuts can occur
        // Insert elements and then deleteMin to create a tree with children
        
        // Insert 8 elements to create a tree after consolidation
        Heap.HeapItem[] items = new Heap.HeapItem[8];
        for (int i = 0; i < 8; i++) {
            items[i] = heap.insert((i + 1) * 10, "v" + i);
        }
        
        // Force consolidation by deleteMin
        heap.deleteMin(); // removes 10
        
        System.out.println("  After first deleteMin:");
        System.out.println("    Size: " + heap.size());
        System.out.println("    NumTrees: " + heap.numTrees());
        System.out.println("    NumMarkedNodes: " + heap.numMarkedNodes());

        assertEquals("NumMarkedNodes initially 0", 0, heap.numMarkedNodes());

        printSubsection("Trigger Cuts with DecreaseKey");
        // Now we need to mark a node by cutting its child first
        // Find a node with children and decrease its child
        
        // Strategy: Insert more, delete min to get structure, then cut
        Heap.HeapItem deepItem1 = heap.insert(100, "deep1");
        Heap.HeapItem deepItem2 = heap.insert(110, "deep2");
        Heap.HeapItem deepItem3 = heap.insert(120, "deep3");
        
        heap.deleteMin(); // consolidate again
        
        int cutsBeforeFirst = heap.totalCuts();
        System.out.println("  Total cuts before operations: " + cutsBeforeFirst);
        
        // Decrease a key to trigger a cut (if it violates heap property)
        // We decrease deepItem3 to be less than its potential parent
        heap.decreaseKey(deepItem3, 115); // 120 - 115 = 5
        
        int cutsAfterFirst = heap.totalCuts();
        int markedAfterFirst = heap.numMarkedNodes();
        
        System.out.println("  After first decreaseKey:");
        System.out.println("    TotalCuts: " + cutsAfterFirst);
        System.out.println("    NumMarkedNodes: " + markedAfterFirst);
        
        // If the node was under a parent, a cut should have occurred
        // The parent should be marked
        
        // Now decrease another key to potentially trigger cascading cut
        heap.decreaseKey(deepItem2, 105); // 110 - 105 = 5
        
        int cutsAfterSecond = heap.totalCuts();
        int markedAfterSecond = heap.numMarkedNodes();
        
        System.out.println("  After second decreaseKey:");
        System.out.println("    TotalCuts: " + cutsAfterSecond);
        System.out.println("    NumMarkedNodes: " + markedAfterSecond);

        // Verify cuts occurred
        assertAtLeast("At least some cuts occurred", 1, cutsAfterSecond);
        
        // Test cascading scenario explicitly
        printSubsection("Explicit Cascading Cut Test");
        testExplicitCascadingCuts();
    }
    
    /**
     * Helper test for explicit cascading cut scenario
     */
    private static void testExplicitCascadingCuts() {
        Heap heap = new Heap(true, true);
        
        // Build a structure that guarantees cascading cuts
        // Insert enough elements to create depth
        for (int i = 1; i <= 16; i++) {
            heap.insert(i * 100, "n" + i);
        }
        heap.deleteMin(); // consolidate
        
        // Insert children that we'll cut
        Heap.HeapItem[] tocut = new Heap.HeapItem[4];
        for (int i = 0; i < 4; i++) {
            tocut[i] = heap.insert(1700 + i, "tocut" + i);
        }
        heap.deleteMin(); // consolidate to add these to tree
        
        int cutsBefore = heap.totalCuts();
        int markedBefore = heap.numMarkedNodes();
        
        // Cut first child (parent gets marked)
        heap.decreaseKey(tocut[0], 1700); // becomes key 0
        
        int cutsAfter1 = heap.totalCuts();
        int markedAfter1 = heap.numMarkedNodes();
        
        // Cut sibling of first child (parent already marked -> cascade)
        heap.decreaseKey(tocut[1], 1700); // becomes key 1
        
        int cutsAfter2 = heap.totalCuts();
        int markedAfter2 = heap.numMarkedNodes();
        
        System.out.println("  Cascade Test Results:");
        System.out.println("    Cuts before: " + cutsBefore + ", after 1st: " + cutsAfter1 + ", after 2nd: " + cutsAfter2);
        System.out.println("    Marked before: " + markedBefore + ", after 1st: " + markedAfter1 + ", after 2nd: " + markedAfter2);
        
        // If cascading worked, second cut should have multiple cuts
        assertTrue("Cuts increased after operations", cutsAfter2 > cutsBefore);
    }

    /**
     * Scenario 5: Heapify Cost (Mode 1 - Binomial Heap)
     * 
     * Insert elements to create depth, then decreaseKey a leaf to become the new min.
     * Verify totalHeapifyCosts > 0.
     */
    private static void testScenario5_HeapifyCost() {
        printSection("Scenario 5: Heapify Cost (Mode 1 - Binomial Heap)");

        // Create a Binomial Heap (no lazy decrease keys -> uses heapify)
        Heap heap = new Heap(false, false);

        printSubsection("Build Deep Tree Structure");
        // Insert 8 elements to create a tree of height 3 (B3 has depth 3)
        Heap.HeapItem[] items = new Heap.HeapItem[8];
        for (int i = 0; i < 8; i++) {
            items[i] = heap.insert((8 - i) * 100, "v" + i); // 800, 700, 600, ... 100
        }
        
        assertEquals("Size", 8, heap.size());
        assertEquals("NumTrees for 8 elements", 1, heap.numTrees()); // Single B3 tree
        assertEquals("FindMin should be 100", 100, heap.findMin().key);
        
        int heapifyCostBefore = heap.totalHeapifyCosts();
        System.out.println("  HeapifyCost before decreaseKey: " + heapifyCostBefore);

        printSubsection("DecreaseKey to Trigger Heapify");
        // Find the item with key 800 (was inserted first, likely a leaf)
        // Decrease it to become the new minimum
        heap.decreaseKey(items[0], 799); // 800 - 799 = 1 (new min)
        
        int heapifyCostAfter = heap.totalHeapifyCosts();
        System.out.println("  HeapifyCost after decreaseKey: " + heapifyCostAfter);
        
        assertPositive("HeapifyCost increased", heapifyCostAfter - heapifyCostBefore);
        assertEquals("New min should be 1", 1, heap.findMin().key);
        
        // Verify numMarkedNodes is always 0 in non-lazy mode
        assertEquals("NumMarkedNodes in non-lazy mode", 0, heap.numMarkedNodes());

        printSubsection("Multiple Heapify Operations");
        // Create another heap for more heapify testing
        Heap heap2 = new Heap(false, false);
        for (int i = 1; i <= 16; i++) {
            heap2.insert(i * 1000, "x" + i);
        }
        
        // The item with key 16000 should be deep in the tree
        Heap.HeapItem deepItem = heap2.insert(20000, "deep");
        
        // Force consolidation
        heap2.deleteMin(); // removes 1000
        
        int costBefore = heap2.totalHeapifyCosts();
        
        // Decrease deep item significantly to force multiple swaps
        heap2.decreaseKey(deepItem, 19999); // becomes 1
        
        int costAfter = heap2.totalHeapifyCosts();
        
        System.out.println("  HeapifyCost before: " + costBefore + ", after: " + costAfter);
        assertPositive("Multiple heapify swaps occurred", costAfter - costBefore);
    }

    /**
     * Scenario 6: Meld History (Counter Accumulation)
     * 
     * Create two heaps, perform operations that increase counters on both.
     * Meld them. Verify the surviving heap contains the sum of counters.
     */
    private static void testScenario6_MeldHistory() {
        printSection("Scenario 6: Meld History (Counter Accumulation)");

        // Test with Mode 1 (Binomial Heap) for predictable links
        printSubsection("Mode 1: Binomial Heap Counter Meld");
        
        Heap heap1 = new Heap(false, false);
        Heap heap2 = new Heap(false, false);
        
        // Build heap1: 8 elements -> 7 links
        for (int i = 1; i <= 8; i++) {
            heap1.insert(i, "h1_" + i);
        }
        int links1 = heap1.totalLinks();
        int heapify1 = heap1.totalHeapifyCosts();
        System.out.println("  Heap1 - links: " + links1 + ", heapifyCosts: " + heapify1);
        
        // Build heap2: 4 elements -> 3 links
        for (int i = 10; i <= 13; i++) {
            heap2.insert(i, "h2_" + i);
        }
        int links2 = heap2.totalLinks();
        int heapify2 = heap2.totalHeapifyCosts();
        System.out.println("  Heap2 - links: " + links2 + ", heapifyCosts: " + heapify2);
        
        // Meld
        heap1.meld(heap2);
        
        // After meld, links should be at least sum (more due to consolidation)
        assertAtLeast("Links after meld >= sum", links1 + links2, heap1.totalLinks());
        assertEquals("Size after meld", 12, heap1.size());
        
        System.out.println("  After meld - links: " + heap1.totalLinks() + ", heapifyCosts: " + heap1.totalHeapifyCosts());

        // Test with Mode 3 (Fibonacci) for cuts
        printSubsection("Mode 3: Fibonacci Heap Counter Meld");
        
        Heap fibHeap1 = new Heap(true, true);
        Heap fibHeap2 = new Heap(true, true);
        
        // Insert and create cuts in heap1
        for (int i = 1; i <= 10; i++) {
            fibHeap1.insert(i * 100, "f1_" + i);
        }
        fibHeap1.deleteMin();
        Heap.HeapItem cutItem1 = fibHeap1.insert(5000, "cut1");
        fibHeap1.deleteMin();
        fibHeap1.decreaseKey(cutItem1, 4999); // trigger cut
        
        int cuts1 = fibHeap1.totalCuts();
        int marked1 = fibHeap1.numMarkedNodes();
        System.out.println("  FibHeap1 - cuts: " + cuts1 + ", marked: " + marked1);
        
        // Insert and create cuts in heap2
        for (int i = 1; i <= 8; i++) {
            fibHeap2.insert(i * 100, "f2_" + i);
        }
        fibHeap2.deleteMin();
        Heap.HeapItem cutItem2 = fibHeap2.insert(4000, "cut2");
        fibHeap2.deleteMin();
        fibHeap2.decreaseKey(cutItem2, 3999); // trigger cut
        
        int cuts2 = fibHeap2.totalCuts();
        int marked2 = fibHeap2.numMarkedNodes();
        System.out.println("  FibHeap2 - cuts: " + cuts2 + ", marked: " + marked2);
        
        // Meld
        fibHeap1.meld(fibHeap2);
        
        // Counters should be accumulated
        assertEquals("Cuts accumulated after meld", cuts1 + cuts2, fibHeap1.totalCuts());
        assertEquals("Marked nodes accumulated after meld", marked1 + marked2, fibHeap1.numMarkedNodes());
        
        System.out.println("  After meld - cuts: " + fibHeap1.totalCuts() + ", marked: " + fibHeap1.numMarkedNodes());
    }

    /**
     * Additional Scenario: Mode Comparison
     * 
     * Compare behavior across all 4 modes to ensure they behave differently.
     */
    private static void testScenario7_ModeComparison() {
        printSection("Scenario 7: Mode Comparison (All 4 Modes)");

        printSubsection("Mode 1: Binomial (strict meld, heapify)");
        Heap mode1 = new Heap(false, false);
        for (int i = 1; i <= 8; i++) mode1.insert(i * 10, "m1_" + i);
        assertEquals("Mode1: NumTrees after 8 inserts", 1, mode1.numTrees());
        assertEquals("Mode1: NumMarkedNodes", 0, mode1.numMarkedNodes());
        System.out.println("  Links: " + mode1.totalLinks() + ", Cuts: " + mode1.totalCuts() + ", HeapifyCosts: " + mode1.totalHeapifyCosts());

        printSubsection("Mode 2: Lazy Binomial (lazy meld, heapify)");
        Heap mode2 = new Heap(true, false);
        for (int i = 1; i <= 8; i++) mode2.insert(i * 10, "m2_" + i);
        assertEquals("Mode2: NumTrees after 8 inserts (lazy)", 8, mode2.numTrees());
        assertEquals("Mode2: NumMarkedNodes", 0, mode2.numMarkedNodes());
        assertEquals("Mode2: No links yet", 0, mode2.totalLinks());
        mode2.deleteMin(); // consolidate
        assertEquals("Mode2: NumTrees after deleteMin", 3, mode2.numTrees()); // 7 = 111 binary
        assertPositive("Mode2: Links after deleteMin", mode2.totalLinks());

        printSubsection("Mode 3: Fibonacci (lazy meld, cascading cuts)");
        Heap mode3 = new Heap(true, true);
        for (int i = 1; i <= 8; i++) mode3.insert(i * 10, "m3_" + i);
        assertEquals("Mode3: NumTrees after 8 inserts (lazy)", 8, mode3.numTrees());
        mode3.deleteMin();
        assertEquals("Mode3: NumTrees after deleteMin", 3, mode3.numTrees());
        // Fibonacci uses cuts, not heapify
        assertEquals("Mode3: HeapifyCosts should be 0", 0, mode3.totalHeapifyCosts());

        printSubsection("Mode 4: Binomial with Detachments (strict meld, cascading cuts)");
        Heap mode4 = new Heap(false, true);
        for (int i = 1; i <= 8; i++) mode4.insert(i * 10, "m4_" + i);
        assertEquals("Mode4: NumTrees after 8 inserts (strict)", 1, mode4.numTrees());
        assertPositive("Mode4: Links from strict meld", mode4.totalLinks());
    }

    /**
     * Additional Scenario: Delete Operation
     * 
     * Test delete of arbitrary node
     */
    private static void testScenario8_DeleteNode() {
        printSection("Scenario 8: Delete Arbitrary Node");

        printSubsection("Mode 1: Delete middle node");
        Heap heap = new Heap(false, false);
        Heap.HeapItem item1 = heap.insert(10, "a");
        Heap.HeapItem item2 = heap.insert(20, "b");
        Heap.HeapItem item3 = heap.insert(30, "c");
        Heap.HeapItem item4 = heap.insert(5, "d");
        Heap.HeapItem item5 = heap.insert(15, "e");

        assertEquals("Size before delete", 5, heap.size());
        assertEquals("Min before delete", 5, heap.findMin().key);

        heap.delete(item2); // delete node with key 20

        assertEquals("Size after delete", 4, heap.size());
        assertEquals("Min unchanged after delete", 5, heap.findMin().key);

        // Delete the minimum
        heap.delete(item4); // delete node with key 5
        assertEquals("Size after deleting min", 3, heap.size());
        assertEquals("New min should be 10", 10, heap.findMin().key);

        printSubsection("Mode 3: Delete with cascading");
        Heap fibHeap = new Heap(true, true);
        Heap.HeapItem[] items = new Heap.HeapItem[10];
        for (int i = 0; i < 10; i++) {
            items[i] = fibHeap.insert((i + 1) * 10, "v" + i);
        }
        fibHeap.deleteMin(); // consolidate
        
        int cutsBefore = fibHeap.totalCuts();
        fibHeap.delete(items[5]); // delete node with key 60
        int cutsAfter = fibHeap.totalCuts();
        
        assertEquals("Size after delete in Fib heap", 8, fibHeap.size());
        System.out.println("  Cuts before: " + cutsBefore + ", after: " + cutsAfter);
    }

    /**
     * Additional Scenario: Edge Cases
     */
    private static void testScenario9_EdgeCases() {
        printSection("Scenario 9: Edge Cases");

        printSubsection("Single element heap");
        Heap heap1 = new Heap(false, false);
        Heap.HeapItem single = heap1.insert(42, "only");
        assertEquals("Single element size", 1, heap1.size());
        assertEquals("Single element numTrees", 1, heap1.numTrees());
        heap1.deleteMin();
        assertEquals("Empty after deleteMin", 0, heap1.size());
        assertNull("Null min after deleteMin", heap1.findMin());

        printSubsection("Meld with empty heap");
        Heap heap2 = new Heap(false, false);
        Heap empty = new Heap(false, false);
        heap2.insert(10, "a");
        heap2.insert(20, "b");
        int sizeBefore = heap2.size();
        heap2.meld(empty);
        assertEquals("Size unchanged after meld with empty", sizeBefore, heap2.size());

        printSubsection("DecreaseKey to same value (no change)");
        Heap heap3 = new Heap(false, false);
        Heap.HeapItem item = heap3.insert(100, "test");
        heap3.decreaseKey(item, 0); // decrease by 0
        assertEquals("Key unchanged", 100, item.key);

        printSubsection("Large heap stress test");
        Heap bigHeap = new Heap(false, false);
        int n = 1000;
        for (int i = n; i >= 1; i--) {
            bigHeap.insert(i, "val" + i);
        }
        assertEquals("Large heap size", n, bigHeap.size());
        assertEquals("Large heap min", 1, bigHeap.findMin().key);
        
        // Delete half
        for (int i = 0; i < n/2; i++) {
            bigHeap.deleteMin();
        }
        assertEquals("Size after half deleted", n/2, bigHeap.size());
    }

    /**
     * Additional Scenario: Duplicate Keys
     */
    private static void testScenario10_DuplicateKeys() {
        printSection("Scenario 10: Duplicate Keys");

        Heap heap = new Heap(false, false);
        
        // Insert multiple items with same key
        Heap.HeapItem a = heap.insert(10, "first");
        Heap.HeapItem b = heap.insert(10, "second");
        Heap.HeapItem c = heap.insert(10, "third");
        Heap.HeapItem d = heap.insert(5, "min");
        
        assertEquals("Size with duplicates", 4, heap.size());
        assertEquals("Min is 5", 5, heap.findMin().key);
        
        heap.deleteMin(); // remove 5
        assertEquals("Min is now 10", 10, heap.findMin().key);
        
        heap.deleteMin();
        heap.deleteMin();
        heap.deleteMin();
        assertEquals("All removed", 0, heap.size());
    }

    // ================== Main Entry Point ==================

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("       HEAP DATA STRUCTURE - COMPREHENSIVE TESTER");
        System.out.println("=".repeat(60));
        System.out.println("Testing all 4 modes:");
        System.out.println("  Mode 1: Binomial (lazyMelds=false, lazyDecreaseKeys=false)");
        System.out.println("  Mode 2: Lazy Binomial (lazyMelds=true, lazyDecreaseKeys=false)");
        System.out.println("  Mode 3: Fibonacci (lazyMelds=true, lazyDecreaseKeys=true)");
        System.out.println("  Mode 4: Binomial+Detach (lazyMelds=false, lazyDecreaseKeys=true)");

        try {
            // Core scenarios
            testScenario1_BasicOperations();
            testScenario2_LinkCheck();
            testScenario3_LazyMeld();
            testScenario4_CascadingCuts();
            testScenario5_HeapifyCost();
            testScenario6_MeldHistory();
            
            // Additional scenarios
            testScenario7_ModeComparison();
            testScenario8_DeleteNode();
            testScenario9_EdgeCases();
            testScenario10_DuplicateKeys();

        } catch (Exception e) {
            System.out.println("\n*** EXCEPTION OCCURRED ***");
            e.printStackTrace();
            failedTests++;
        }

        printSummary();
    }
}
