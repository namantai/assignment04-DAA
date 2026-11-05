package algorithms;

public final class Counters {
    long comparisons;
    long recursiveCalls;
    long cellUpdates;
    long lpsComparisons;
    long allocatedCells;

    public long comparisons() { return comparisons; }
    public long recursiveCalls() { return recursiveCalls; }
    public long cellUpdates() { return cellUpdates; }
    public long lpsComparisons() { return lpsComparisons; }
    public long allocatedCells() { return allocatedCells; }

    void reset() {
        comparisons = recursiveCalls = cellUpdates = lpsComparisons = allocatedCells = 0;
    }
}
