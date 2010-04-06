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
 * Resolves TestActions for the given TestObject.
 * This object itself is persisted as a part of AbstractBuild, so it needs to be XStream-serializable.
 *
 * @author krwalker
 */
class TestActionResolver extends hudson.tasks.junit.TestResultAction.Data {

    private static final java.util.logging.Logger LOGGER =
        java.util.logging.Logger.getLogger(TestActionResolver.class.getName());

    private static final com.thoughtworks.xstream.XStream XSTREAM = new hudson.util.XStream2();

    // Convert BuildMeasurements to XML in a more friendly way.
    private static class BuildMeasurementsConverter extends
            com.thoughtworks.xstream.converters.collections.MapConverter {
        BuildMeasurementsConverter(com.thoughtworks.xstream.mapper.Mapper mapper) {
            super(mapper);
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(BuildMeasurements.class);
        }
    }
    // Convert TestObjectMeasurements to XML in a more friendly way.
    private static class TestObjectMeasurementsConverter extends
            com.thoughtworks.xstream.converters.collections.CollectionConverter {
        TestObjectMeasurementsConverter(com.thoughtworks.xstream.mapper.Mapper mapper) {
            super(mapper);
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(TestObjectMeasurements.class);
        }
    }

    // Convert TestObjectIds to XML in a more friendly way.
    private static class TestObjectIdConverter extends
            com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter {

        @Override
        public boolean canConvert(Class type) {
            return type.equals(TestObjectId.class);
        }

        @Override
        public Object fromString(String string) {
            return TestObjectId.fromString(string);
        }

    }
    static {
        XSTREAM.registerConverter(new BuildMeasurementsConverter(XSTREAM.getMapper()));
        XSTREAM.alias("build-measurements", BuildMeasurements.class);
        XSTREAM.alias("test-object", java.util.Map.Entry.class);
        XSTREAM.registerConverter(new TestObjectIdConverter());
        XSTREAM.alias("id", TestObjectId.class);
        XSTREAM.registerConverter(new TestObjectMeasurementsConverter(XSTREAM.getMapper()));
        XSTREAM.alias("measurements", TestObjectMeasurements.class);
        XSTREAM.alias("measurement", Measurement.class);
        // Is this needed?
        XSTREAM.registerConverter(new hudson.util.HeapSpaceStringConverter(),100);
    }

    // Store a weak reference to the build measurements. They are stored in
    // their own file (measurement-plots.xml) and loaded on demand.
    private transient java.lang.ref.WeakReference<BuildMeasurements> weakBuildMeasurements;

    // The build used during both the load and save, and may be set by
    // the constructor or getTestAction().
    private transient hudson.model.AbstractBuild<?, ?> build;

    // The listener is used only during the save. It is set during the constructor.
    private transient final hudson.model.BuildListener listener;

    TestActionResolver(final BuildMeasurements buildMeasurements, 
        hudson.model.AbstractBuild<?, ?> build, hudson.model.BuildListener listener) {
        this.build = build;
        this.listener = listener;
        setBuildMeasurements(buildMeasurements, listener);
    }

    @Override
    public java.util.List<hudson.tasks.junit.TestAction> getTestAction(
            @SuppressWarnings({"deprecation"}) hudson.tasks.junit.TestObject junitTestObject) {
        if (build == null) {
            build = junitTestObject.getOwner();
        }
        TestObjectMeasurements testObjectMeasurements =
                getBuildMeasurements().get(TestObjectId.fromString(junitTestObject.getId()));
        if (testObjectMeasurements != null && !testObjectMeasurements.isEmpty()) {
            // This cast should always succeed.
            hudson.tasks.test.TestObject testObject =
                    (hudson.tasks.test.TestObject)junitTestObject;
            return java.util.Collections.<hudson.tasks.junit.TestAction> singletonList(
                    new TestAction(testObject, testObjectMeasurements));
        }
        return java.util.Collections.emptyList();
    }

    public synchronized BuildMeasurements getBuildMeasurements() {
        BuildMeasurements buildMeasurements;
        if(weakBuildMeasurements == null) {
            buildMeasurements = load();
            weakBuildMeasurements = new java.lang.ref.WeakReference<BuildMeasurements>(buildMeasurements);
        } else {
            buildMeasurements = weakBuildMeasurements.get();
        }

        if(buildMeasurements == null) {
            buildMeasurements = load();
            weakBuildMeasurements = new java.lang.ref.WeakReference<BuildMeasurements>(buildMeasurements);
        }
        return buildMeasurements;
    }

    /**
     * Overwrites the {@link BuildMeasurements} by a new data set.
     */
    public synchronized void setBuildMeasurements(BuildMeasurements buildMeasurements, hudson.model.BuildListener listener) {

        // persist the data
        try {
            getDataFile().write(buildMeasurements);
        } catch (java.io.IOException exception) {
            exception.printStackTrace(listener.fatalError("Failed to save build measurements"));
        }

        this.weakBuildMeasurements = new java.lang.ref.WeakReference<BuildMeasurements>(buildMeasurements);
    }

    private hudson.XmlFile getDataFile() {
        return new hudson.XmlFile(XSTREAM, new java.io.File(build.getRootDir(), "measurement-plots.xml"));
    }

    /**
     * Loads a {@link BuildMeasurements} from disk.
     */
    private BuildMeasurements load() {
        BuildMeasurements loadedMeasurements;
        try {
            loadedMeasurements = (BuildMeasurements)getDataFile().read();
        } catch (java.io.IOException exception) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to load " + getDataFile(), exception);
            loadedMeasurements = new BuildMeasurements();   // return a dummy
        }
        return loadedMeasurements;
    }
}
