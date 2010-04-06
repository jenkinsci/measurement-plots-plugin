/*
 * The MIT License
 *
 * Copyright (c) 2010, Stellar Science Ltd Co, K. R. Walker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.measurement_plots;

/**
 * Applies a TestResultOperation to all TestResults in the hierarchy rooted at
 * the TestResult passed into the constructor. The order in which the operation
 * is applied to children is unspecified.
 * @author krwalker
 * @see TestResultOperation
 */
class TestResultWalker {

    final private hudson.tasks.test.TestResult root;

    TestResultWalker(hudson.tasks.test.TestResult root) {
        this.root = root;
    }

    void apply(TestResultOperation operation) {
        java.util.Queue<hudson.tasks.test.TestResult> frontier =
                new java.util.LinkedList<hudson.tasks.test.TestResult>();
        frontier.offer(root);
        while (!frontier.isEmpty()) {
            // The following is due to the in-flux nature of the 
            // hudson.tasks.test and junit class hierarchies.
            // For now, just deal with it, though be aware that
            // this may fail in the future.
            hudson.tasks.test.TestResult current = frontier.remove();
            if (current instanceof hudson.tasks.test.TabulatedResult) {
                offerToFrontier(frontier, ((hudson.tasks.test.TabulatedResult)current).getChildren());
            } else {
                // It is currently unclear what the future holds for
                // test.TestResult.getChildren().
//                try {
//                    offerToFrontier(frontier, current.getPassedTests());
//                } catch (UnsupportedOperationException exception) {
//                    // Doesn't support the operation, so ignore.
//                }
//                try {
//                    offerToFrontier(frontier, current.getFailedTests());
//                } catch (UnsupportedOperationException exception) {
//                    // Doesn't support the operation, so ignore.
//                }
//                try {
//                    offerToFrontier(frontier, current.getSkippedTests());
//                } catch (UnsupportedOperationException exception) {
//                    // Doesn't support the operation, so ignore.
//                }
            }
            operation.apply(current);
        }
    }

    private static void offerToFrontier(
            java.util.Queue<hudson.tasks.test.TestResult> frontier,
            java.util.Collection<? extends hudson.tasks.test.TestResult> elements ) {
        for (hudson.tasks.test.TestResult element : elements ) {
            frontier.offer(element);
        }
    }
}
