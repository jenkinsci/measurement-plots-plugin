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
 * A measurement. Measurements have names and values.
 * @author krwalker
 */
public class Measurement {

    transient private TestAction testAction;
    private String name;
    private String value;

    Measurement(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the actual name (as opposed to the URL-safe component name).
     */
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets a URL-safe component name.
     */
    public String getUrlName() {
        try {
            return java.net.URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
        } catch (java.io.UnsupportedEncodingException exception) {
            // This shouldn't happen?
            return getName();
        }
    }

    public StringBuffer getAbsoluteUrl() {
        return getTestAction().getAbsoluteUrl().append(getUrlName() + '/');
    }

    public String getBuildName() {
        return getBuild().getDisplayName();
    }

    public int getBuildNumber() {
        return getBuild().number;
    }

    /**
     * Returns the node name on which this measurement was taken.
     */
    public String getNodeName() {
        return getBuild().getBuiltOnStr();
    }

    public java.util.Calendar getBuildTimestamp() {
        return getBuild().getTimestamp();
    }

    public hudson.model.AbstractBuild<?, ?> getBuild() {
        return getTestAction().getBuild();
    }

    public hudson.tasks.test.TestObject getTestObject() {
        return getTestAction().getTestObject();
    }

    /** Measurements need access to their TestAction. */
    void setTestAction(TestAction testAction) {
        this.testAction = testAction;
    }

    public TestAction getTestAction() {
        return testAction;
    }

    /**
     * @param build The build in which to find another measurement.
     * @return The measurement for the build or null if no measurement
     * exists in the build.
     */
    Measurement getMeasurementInBuild(hudson.model.AbstractBuild<?, ?> build) {
        hudson.tasks.test.TestObject otherTestObject = getTestObject().getResultInBuild(build);
        if (otherTestObject != null) {
            hudson.tasks.test.AbstractTestResultAction otherAbstractTestResultAction =
                    otherTestObject.getTestResultAction();
            if (otherAbstractTestResultAction != null) {
                hudson.tasks.junit.TestResultAction otherJunitTestResultAction =
                        (hudson.tasks.junit.TestResultAction)otherAbstractTestResultAction;
                if (otherJunitTestResultAction != null ) {
                    for (hudson.tasks.junit.TestAction otherJunitTestAction :
                            otherJunitTestResultAction.getActions(otherTestObject)) {
                        TestAction measurementAction = (TestAction)otherJunitTestAction;
                        if (measurementAction != null) {
                            Measurement otherMeasurement =
                                    measurementAction.getMeasurement(getName());
                            if (otherMeasurement != null) {
                                return otherMeasurement;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public History getHistory() {
        return new History(this);
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Measurement)) {
            return false;
        }
        Measurement measurement = (Measurement)other;
        return this.getName().equals(measurement.getName());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
