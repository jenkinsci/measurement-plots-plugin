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
 * @author krwalker
 */
public class TestDataPublisher extends hudson.tasks.junit.TestDataPublisher {

    @org.kohsuke.stapler.DataBoundConstructor
    public TestDataPublisher() {
    }

    /**
     * Called after test results are collected by Hudson, to create a resolver for TestActions.
     */
    @Override
    public hudson.tasks.junit.TestResultAction.Data getTestData(
            hudson.model.AbstractBuild<?, ?> build,
            hudson.Launcher launcher,
            hudson.model.BuildListener listener,
            hudson.tasks.junit.TestResult testResult) throws
            java.io.IOException, InterruptedException {

        final BuildMeasurements buildMeasurements = new BuildMeasurements();

        final OutputParser parser = new OutputParser();
        TestResultWalker walker = new TestResultWalker(testResult);
        walker.apply(new OutputOperationApplicator(new TestResultOutputOperation() {
            @Override
            public void apply(hudson.tasks.test.TestResult result, String text) {
                TestObjectMeasurements testObjectMeasurements = parser.parse(text);
                if (!testObjectMeasurements.isEmpty()) {
                    buildMeasurements.put(TestObjectId.fromString(result.getId()), testObjectMeasurements);
                }
            }
        }));

        TestActionResolver resolver = null;
        if (!buildMeasurements.isEmpty()) {
            resolver = new TestActionResolver(buildMeasurements, build, listener);
        }
        return resolver;
    }

    @hudson.Extension
    public static class DescriptorImpl extends hudson.model.Descriptor<hudson.tasks.junit.TestDataPublisher> {

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Measurement Plots";
        }
    }
}
