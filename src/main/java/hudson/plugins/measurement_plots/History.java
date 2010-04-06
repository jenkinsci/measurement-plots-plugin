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
 * The history of a measurement.
 * @author krwalker
 */
public class History {

    transient private Measurement measurement;
    transient private java.util.List<Measurement> measurements;

    public History(Measurement measurement) {
        this.measurement = measurement;
        this.measurements = null;
    }
    
    public String getUrlName() {
        return "history";
    }

    public StringBuffer getAbsoluteUrl() {
        return getMeasurement().getAbsoluteUrl().append(getUrlName() + '/');
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    private void collectMeasurements() {
		measurements = new java.util.ArrayList<Measurement>();
		for (hudson.model.AbstractBuild<?,?> build: getMeasurement().getBuild().getParent().getBuilds()) {
			if (build.isBuilding()) continue;
			Measurement candidate = getMeasurement().getMeasurementInBuild(build);
			if (candidate != null) {
				measurements.add(candidate);
			}
		}
    }

    /**
     * @return The list of measurements in this measurement's history.
     * The returned list may be empty.
     */
	public java.util.List<Measurement> getMeasurements() {
		if (measurements == null) {
            collectMeasurements();
        }
        return measurements;
	}

    /**
     * @return The graph for this measurement or null if there is no graph.
     */
    public Graph getGraph() {
        return new Graph(getMeasurement().getName(), getMeasurement().getBuildTimestamp()) {
            @Override
            protected hudson.util.DataSetBuilder<String, GraphLabel> getDataSetBuilder() {
                hudson.util.DataSetBuilder<String, GraphLabel> data =
                        new hudson.util.DataSetBuilder<String, GraphLabel>();
                for (final Measurement measurement : getMeasurements()) {
                    //data.add(value, rowKey, columnKey);
                    Double value = null;
                    try {
                        value = Double.valueOf(measurement.getValue());
                    } catch (NumberFormatException exception) {
                        value = null;
                    }
                    data.add(value, "", new GraphLabel(measurement));
                }
                return data;
            }
        };
    }
}
