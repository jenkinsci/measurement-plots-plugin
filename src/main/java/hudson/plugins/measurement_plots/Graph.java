/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Tom Huybrechts, Yahoo!, Inc., Stellar Science Ltd Co, K. R. Walker
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
 * Measurement graph.
 * @author krwalker
 */
public abstract class Graph extends hudson.util.Graph {

    final String title;

    protected Graph(String title, java.util.Calendar timestamp) {
        super(timestamp, 600, 300);
        this.title = title;
    }

    public String getUrlName() {
        return "graph";
    }

    public String getImageUrlName() {
        return "png";
    }

    public String getMapUrlName() {
        return "map";
    }
    
    // GraphLabel is non-public, but exported through public API.
    protected abstract hudson.util.DataSetBuilder<String, GraphLabel> getDataSetBuilder();

    protected org.jfree.chart.JFreeChart createGraph() {
        final org.jfree.data.category.CategoryDataset dataset = getDataSetBuilder().build();

        final org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createStackedAreaChart(
                title, // chart title
                null, // unused
                null, // range axis label
                dataset, // data
                org.jfree.chart.plot.PlotOrientation.VERTICAL, // orientation
                false, // include legend
                true, // tooltips
                false // urls
                );

        chart.setBackgroundPaint(java.awt.Color.white);

        final org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        // plot.setDomainGridlinesVisible(true);
        // plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(java.awt.Color.black);

        org.jfree.chart.axis.CategoryAxis domainAxis = new hudson.util.ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        hudson.util.ChartUtil.adjustChebyshev(dataset, rangeAxis);
        rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRange(true);

        org.jfree.chart.renderer.category.StackedAreaRenderer areaRenderer = new hudson.util.StackedAreaRenderer2() {

            @Override
            public java.awt.Paint getItemPaint(int row, int column) {
                GraphLabel key = (GraphLabel) dataset.getColumnKey(column);
                if (key.getColor() != null) {
                    return key.getColor();
                }
                return super.getItemPaint(row, column);
            }

            @Override
            public String generateURL(
                    org.jfree.data.category.CategoryDataset dataset,
                    int row, int column) {
                GraphLabel label = (GraphLabel) dataset.getColumnKey(column);
                return label.getUrl();
            }

            @Override
            public String generateToolTip(
                    org.jfree.data.category.CategoryDataset dataset,
                    int row, int column) {
                GraphLabel label = (GraphLabel) dataset.getColumnKey(column);
                return label.getToolTip();
            }
        };
        plot.setRenderer(areaRenderer);
        areaRenderer.setSeriesPaint(2, hudson.util.ColorPalette.BLUE);

        // crop extra space around the graph
        plot.setInsets(new org.jfree.ui.RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }
}

class GraphLabel implements Comparable<GraphLabel> {

    private Measurement measurement;
    String url;

    public GraphLabel(Measurement measurement) {
        this.measurement = measurement;
        this.url = null;
    }

    private Measurement getMeasurement() {
        return measurement;
    }

    public String getUrl() {
        if (this.url == null) {
            this.url = getMeasurement().getTestAction().getAbsoluteTestObjectUrl().toString();
        }
        return url;
    }

    public String getToolTip() {
        return  getMeasurement().getBuildName() + " : " +
                getMeasurement().getValue();
    }

    public int compareTo(GraphLabel that) {
        return  this.getMeasurement().getBuildNumber() -
                that.getMeasurement().getBuildNumber();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GraphLabel)) {
            return false;
        }
        GraphLabel that = (GraphLabel) object;
        return this.getMeasurement() == that.getMeasurement();
    }

    public java.awt.Color getColor() {
        return hudson.util.ColorPalette.BLUE;
    }

    @Override
    public int hashCode() {
        return getMeasurement().hashCode();
    }

    @Override
    public String toString() {
        String buildName = getMeasurement().getBuildName();
        String nodeName = getMeasurement().getNodeName();
        if (nodeName != null) {
            buildName += ' ' + nodeName;
        }
        return buildName;
    }
}
