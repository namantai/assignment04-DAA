# Assignment 4: Dynamic Programming and String Algorithms

Nurtas Amantay, SE-2506

## 1. Experiment Design Card

This card was written before the first benchmark run. The sizes below replace the suggested recursive sizes before measurement, not after seeing results.

| Field | Edit Distance | String search |
|---|---|---|
| Question | How do plain recursion, a full table and two rows grow with string length? | When and why does KMP use fewer character comparisons than Naive? |
| Sizes (n) | All three: 3, 6, 9, 12, with m = n. Table and Two Rows also: 100, 200, 400, 800. | Text: 1000, 2000, 4000, 8000, 16000. Pattern: m = 32 at every size. |
| Inputs | Random independent strings over ABCD test branching with some matches. Repetitive mismatch A^n vs B^n forces all three recursive branches. Identical A^n vs A^n shows the best recursive path. | Independent random text and pattern over ABCD test early mismatches. Adversarial A^n and A^31B force Naive to check all 32 characters at every valid start. |
| Metrics | Recursive calls, interior DP cell updates, allocated integer cells and median time from 5 trials. Calls and updates are different work units. | Main-search comparisons and separate LPS comparisons; their sum gives total KMP work. Median time includes LPS construction. |
| Prediction | Mismatch recursion grows exponentially; DP updates grow quadratically. Identical recursion takes n+1 calls. Two rows need linear storage, while the table needs quadratic storage. | Random Naive may do fewer comparisons because most starts fail early. KMP should save many comparisons on adversarial input. With fixed m, both search curves are linear in n, with different constants. |
| Plots | Shared-size operation growth on a log axis; allocated DP integer cells at large sizes. | Total comparisons for random and adversarial inputs, including LPS. |

The recursion range is 3 to 12 (4x), instead of the suggested 4 to 16. On full mismatch, every non-base call has three children, so 16 is too expensive for repeated trials. The chosen limit keeps the experiment bounded; it is not a claim about the largest possible input. The adversarial search input attains the maximum m comparisons per valid Naive start. No larger recursive size will be timed in this experiment.

Seed: 20261105. Each input has a derived seed based on family, type and size. Inputs are generated once and shared by the algorithms. Timing uses System.nanoTime(), 5 measured batches after global and case-specific warm-up, with time divided by the batch size. Generation, validation and CSV writing stay outside timing. Every measured answer is checked. Counter values are per call, not per batch; time may vary between runs.

