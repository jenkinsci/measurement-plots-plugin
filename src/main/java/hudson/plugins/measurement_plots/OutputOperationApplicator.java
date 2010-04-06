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
 * Apply a TestResultOutputOperation to the text-based outputs of a
 * TestResult.
 * @author krwalker
 */
class OutputOperationApplicator implements TestResultOperation {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(OutputOperationApplicator.class.getName());
    private static java.lang.reflect.Method getStdout = null;
    private static java.lang.reflect.Method getStderr = null;
    static {
        try {
            getStdout = hudson.tasks.test.TestResult.class.getMethod("getStdout");
        } catch (NoSuchMethodException exception) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Unable to get output.", exception);
        }
        try {
            getStderr = hudson.tasks.test.TestResult.class.getMethod("getStderr");
        } catch (NoSuchMethodException exception) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Unable to get output.", exception);
        }
    }

    private TestResultOutputOperation outputOperation;

    OutputOperationApplicator(TestResultOutputOperation outputOperation) {
        this.outputOperation = outputOperation;
    }

    @Override
    public void apply(hudson.tasks.test.TestResult result) {
        applyToOutputFromMethod(result, getStdout);
        applyToOutputFromMethod(result, getStderr);
    }

    /**
     * Avoid code duplication by using reflection to select the method to call.
     * This has the ugly side effect of making it necessary
     * to handle reflection-related exceptions.
     * 
     * @param result The TestResult from which to get the output.
     * @param outputMethod The method to call on TestResult to get the output.
     */
    private void applyToOutputFromMethod(hudson.tasks.test.TestResult result, java.lang.reflect.Method outputMethod) {
        try {
            String output = (String)outputMethod.invoke(result);
            if (isValidOutput(result, outputMethod, output)) {
                outputOperation.apply(result, output);
            }
        } catch (IllegalAccessException exception) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Attempted to invoke method with restricted access.", exception);
        } catch (java.lang.reflect.InvocationTargetException exception) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Attempted to invoke method on object that doesn't support it.", exception);
        }
    }

    /**
     * The hudson.tasks.junit package implements getStdout and getStderr in
     * ways that make walking the test tree using the general interfaces
     * difficult. CaseResult returns its own output if it has any, otherwise
     * it returns the SuiteResult output if it has any. TestResult loops
     * over all suites and concatenates the output from each SuiteResult.
     *
     * The only way to determine whether or not the output is
     * genuinely from the leaf is to compare it's result with those of
     * all its parents'.
     *
     * The only way currently known to invalidate the output from a TestResult
     * is to test if the result is a junit.TestResult.
     *
     * @param result The TestResult from which to start searching for parents.
     * @param outputMethod The method to call on parent results.
     * @param output The output being checked for validity.
     * @return true if valid for the leaf.
     */
    private static boolean isValidOutput(
            hudson.tasks.test.TestResult result,
            java.lang.reflect.Method outputMethod,
            String output) {
        if (output != null && output.length() > 0 && outputMethod != null) {
            // Ignore output from junit.TestResult
            if (result instanceof hudson.tasks.junit.TestResult) {
                return false;
            }
            // Search for parents with the exact same output. If one is found,
            // consider this output invalid.
            hudson.tasks.test.TestObject parentObject = result;
            while ((parentObject = parentObject.getParent()) != null) {
                hudson.tasks.test.TestResult parentResult =
                        (hudson.tasks.test.TestResult)parentObject;
                try {
                    if (parentResult != null && outputMethod.invoke(parentResult) == output) {
                        return false;
                    }
                } catch (IllegalAccessException exception) {
                    LOGGER.log(java.util.logging.Level.SEVERE, "Attempted to invoke method with restricted access.", exception);
                } catch (java.lang.reflect.InvocationTargetException exception) {
                    LOGGER.log(java.util.logging.Level.SEVERE, "Attempted to invoke method on object that doesn't support it.", exception);
                }
            }
            return true;
        }
        return false;
    }

    private void apply(hudson.tasks.test.TestResult result, String text) {
        outputOperation.apply(result, text);
    }
}
