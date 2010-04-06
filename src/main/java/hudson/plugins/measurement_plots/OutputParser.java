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
 * Parses text-based output for measurements.
 * @author krwalker
 */
class OutputParser {
    private static final String REGEX =
            "(<measurement>.+?</measurement>)";
    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(OutputParser.class.getName());
    private static final java.util.regex.Pattern PATTERN = 
            java.util.regex.Pattern.compile(REGEX);
    private static final com.thoughtworks.xstream.XStream XSTREAM = new hudson.util.XStream2();
    static {
        XSTREAM.alias("measurement", Measurement.class);
    }

    OutputParser() {
    }

    TestObjectMeasurements parse(String text) {
        // Search for measurements.
        // <measurement><name>name with spaces</name><value>some value</value></measurement>
        // Add each name/value to the map
        TestObjectMeasurements measurements = new TestObjectMeasurements();
        java.util.regex.Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                Measurement measurement = (Measurement)XSTREAM.fromXML(matcher.group(1));
                if (measurement != null) {
                    measurements.add(measurement);
                }
            } catch (com.thoughtworks.xstream.XStreamException exception) {
                // Do nothing. The parse failed.
            }
        }
        return measurements;
    }
}
