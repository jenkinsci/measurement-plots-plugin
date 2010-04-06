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
public class TestAction extends hudson.tasks.junit.TestAction {

    transient private final hudson.tasks.test.TestObject testObject;
    transient private final TestObjectMeasurements measurements;

    TestAction(
            hudson.tasks.test.TestObject testObject,
            TestObjectMeasurements measurements) {
        this.testObject = testObject;
        this.measurements = measurements;
        // Give the measurements knowledge of their testActions.
        // FIXME: Why do they need it?
        for(Measurement measurement : measurements) {
            measurement.setTestAction(this);
        }
    }

    /**
     * @return Return the named measurement.
     */
    public Measurement getMeasurement(String name) {
        for (Measurement measurement : getMeasurements()) {
            if (measurement.getName().equals(name)) {
                return measurement;
            }
        }
        return null;
    }

    /**
     * @return Return the measurement based on the URL component name.
     */
    public Object getDynamic(String urlComponentName,
            org.kohsuke.stapler.StaplerRequest request,
            org.kohsuke.stapler.StaplerResponse response) {
        // There is no real page for "measurement_name",
        // so redirect to "measurement_name/history".
        /*
        StringBuffer url = request.getRequestURL();
        if (url.charAt(url.length() - 1) == '/') {
            url.deleteCharAt(url.length() - 1);
        }
        if (!url.toString().endsWith("history")) {
            try {
                url.append( "/history" );
                response.sendRedirect2(url.toString());
                return null;
            } catch (java.io.IOException exception) {
                // Silently fail, this was an attempt at helping out, anyway.
            }
        }
        */
        // urlComponentName has already been decoded?
        /*
        try {
            return getMeasurement(java.net.URLDecoder.decode(
                    urlComponentName.replaceAll("%20","+"), "UTF-8"));
        } catch (java.io.UnsupportedEncodingException exception) {
            // This shouldn't happen?
            return getMeasurement(urlComponentName);
        }
        */
        return getMeasurement(urlComponentName);
    }

    public TestObjectMeasurements getMeasurements() {
        return measurements;
    }

    public hudson.tasks.test.TestObject getTestObject() {
        return testObject;
    }

    public hudson.model.AbstractBuild<?, ?> getBuild() {
        return getTestObject().getOwner();
    }

    /**
     * Returns the full URL to the TestObject.
     */
    public StringBuffer getAbsoluteTestObjectUrl() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(hudson.model.Hudson.getInstance().getRootUrl());
        buffer.append(getBuild().getUrl());
        buffer.append(getTestObject().getTestResultAction().getUrlName());
        buffer.append(getTestObject().getUrl());
        return buffer;
    }

    /**
     * Returns the absolute URL to this object.
     */
    public StringBuffer getAbsoluteUrl() {
        return getAbsoluteTestObjectUrl().append('/' + getUrlName() + '/');
    }

    /**
     * This is the icon that is displayed in the sidebar.
     * This plugin does not currently use the sidebar.
     */
    @Override
    public String getIconFileName() {
        return null; //"measurement-plots.svg";
    }

    /**
     * This is the text that is displayed in the sidebar.
     * This plugin does not currently use the sidebar.
     */
    @Override
    public String getDisplayName() {
        return null; // "Measurement Plots";
    }

    @Override
    public String getUrlName() {
        return "measurementPlots";
    }

    /**
     * This method is apparently for annotating the the actual
     * "Error Message", "Stack Trace", "Standard Output", etc.
     * raw text. This is not currently necessary.
     */
    @Override
    public String annotate(String text) {
        return text;
    }
}
