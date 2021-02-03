package com.geosparc.gte;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.GlobalId;
import org.geotools.data.collection.CollectionFeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.List;
import java.util.Map;

public class TestData {

    private static final GeometryFactory geomFac = new GeometryFactory();

    private static CoordinateReferenceSystem CRS_4326;

    {
        try {
            CRS_4326 = CRS.decode("EPSG:4326");
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    protected static SimpleFeatureType buildLogicalEdgeType() throws Exception {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.add("id", String.class);
        typeBuilder.add("geom", Geometry.class, CRS.decode("EPSG:4326"));
        typeBuilder.add("from", String.class);
        typeBuilder.add("to", String.class);
        typeBuilder.add("code", String.class);
        typeBuilder.setName("logicalEdge");
        return typeBuilder.buildFeatureType();
    }

    protected static SimpleFeatureType buildLogicalNodeType() throws Exception {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.add("id", String.class);
        typeBuilder.add("geom", Geometry.class, CRS.decode("EPSG:4326"));
        typeBuilder.add("ref", String.class);
        typeBuilder.add("code", String.class);
        typeBuilder.setName("logicalNode");
        return typeBuilder.buildFeatureType();
    }

    protected static SimpleFeatureType buildGeoEdgeType() throws Exception {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.add("id", String.class);
        typeBuilder.add("geom", Geometry.class, CRS.decode("EPSG:4326"));
        typeBuilder.add("code", String.class);
        typeBuilder.setName("geoEdge");
        return typeBuilder.buildFeatureType();
    }

    /**
     * Build a feature simple type with given attributes.
     *
     * @param attributes
     * @return
     */
    public static SimpleFeatureType simpleFeatureType(String name, Map<String, Class> attributes) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.add("geom", Geometry.class, CRS_4326);
        typeBuilder.setName(name);
        for (Map.Entry<String, Class> attribute : attributes.entrySet()) {
            typeBuilder.add(attribute.getKey(), attribute.getValue());
        }
        return typeBuilder.buildFeatureType();
    }

    public static SimpleFeatureSource getLogicalNodes() throws Exception {
        SimpleFeatureType type = buildLogicalNodeType();
        MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        //first node
        builder.add("1");
        builder.add(geomFac.createPoint(new Coordinate(0, 0)));
        collection.add(builder.buildFeature("1"));
        //second node
        builder.add("2");
        builder.add(geomFac.createPoint(new Coordinate(3, 3)));
        builder.add(null);
        builder.add("x");
        collection.add(builder.buildFeature("2"));
        //third node
        builder.add("3");
        builder.add(geomFac.createPoint(new Coordinate(2, 6)));
        builder.add("c");
        builder.add("y");
        collection.add(builder.buildFeature("3"));
        return new CollectionFeatureSource(collection);
    }

    public static SimpleFeatureSource getLogicalNodes2() throws Exception {
        SimpleFeatureType type = buildLogicalNodeType();
        MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        //first node
        builder.add("a");
        builder.add(geomFac.createPoint(new Coordinate(0, 0)));
        collection.add(builder.buildFeature("1"));
        //second node
        builder.add("b");
        builder.add(geomFac.createPoint(new Coordinate(2, 2)));
        collection.add(builder.buildFeature("2"));
        //third node
        builder.add("c");
        builder.add(geomFac.createPoint(new Coordinate(3, 3)));
        collection.add(builder.buildFeature("2"));
        return new CollectionFeatureSource(collection);
    }

    public static SimpleFeatureSource getLogicalEdges() throws Exception {
        SimpleFeatureType type = buildLogicalEdgeType();
        MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        //first edge
        builder.add("12");
        builder.add(geomFac.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(3, 3)}));
        builder.add("1");
        builder.add("2");
        collection.add(builder.buildFeature("12"));
        //second edge
        builder.add("23");
        builder.add(geomFac.createLineString(new Coordinate[]{
                new Coordinate(3, 3), new Coordinate(5, 5)}));
        builder.add("2");
        builder.add("3");
        builder.add("x");
        collection.add(builder.buildFeature("23"));
        //third edge
        builder.add("13");
        builder.add(geomFac.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(5, 5)}));
        builder.add("1");
        builder.add("3");
        collection.add(builder.buildFeature("13"));

        return new CollectionFeatureSource(collection);
    }

    public static SimpleFeatureSource getGeoEdges() throws Exception {
        SimpleFeatureType type = buildLogicalEdgeType();
        MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        //first edge
        builder.add("a");
        builder.add(geomFac.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(3, 3)}));
        collection.add(builder.buildFeature("a"));
        //second edge
        builder.add("b");
        builder.add(geomFac.createLineString(new Coordinate[]{
                new Coordinate(3.0001, 2.9999), new Coordinate(4, 4)}));
        collection.add(builder.buildFeature("b"));
        //third edge
        builder.add("c");
        builder.add(geomFac.createLineString(new Coordinate[]{
                new Coordinate(3.001, 2.999), new Coordinate(5, 5)}));
        builder.add(null);
        builder.add(null);
        builder.add("x");
        collection.add(builder.buildFeature("c"));

        return new CollectionFeatureSource(collection);
    }

    public static DGraph<GlobalId, SimpleFeature, SimpleFeature> buildGraph(SimpleFeatureType featureType, List<TestGraphEdge> edges) {
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = new DGraph<GlobalId, SimpleFeature, SimpleFeature>(false);

        for (TestGraphEdge edge : edges) {
            Idp<GlobalId, SimpleFeature> fromVertex = new Idp<GlobalId, SimpleFeature>(
                    new GlobalId("vertices", (String) edge.getFrom()), builder.buildFeature(edge.getFrom()));
            Idp<GlobalId, SimpleFeature> toVertex = new Idp<GlobalId, SimpleFeature>(
                    new GlobalId("vertices", (String) edge.getTo()), builder.buildFeature(edge.getTo()));

            graph.addVertex(fromVertex);
            graph.addVertex(toVertex);

            graph.addEdge(fromVertex, toVertex,
                    new Idp<GlobalId, SimpleFeature>(
                            new GlobalId("edges", (String) edge.getId()),
                            builder.buildFeature(edge.getId(), edge.getAttributes()))); // Edge
        }
        return graph;
    }

    public static class TestGraphEdge {
        String from;
        String to;
        Object[] attributes;

        public TestGraphEdge(String from, String to, Object[] attributes) {
            this.from = from;
            this.to = to;
            this.attributes = attributes;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public Object[] getAttributes() {
            return attributes;
        }

        public void setAttributes(Object[] attributes) {
            this.attributes = attributes;
        }

        public String getId() {
            return getFrom() + "->" + getTo();
        }
    }

	/**
	 *                 /-2->-3- -- -3->-4- \
	 *  0->-1 -- -1->-2                     4->-5-
	 *                 \      -2-<-4-      /
	 */
	public static SimpleFeatureSource getGeoEdgesWithCycle() throws Exception {
		SimpleFeatureType type = buildLogicalEdgeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		//first edge
		builder.add("a");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(0,1), new Coordinate(1,1)}));
		collection.add(builder.buildFeature("a"));

		//second edge
		builder.add("b");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(1,1), new Coordinate(2,1)}));
		collection.add(builder.buildFeature("b"));

		//third edge
		builder.add("c");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(2,1), new Coordinate(3,1)}));
		collection.add(builder.buildFeature("c"));

		//fourth edge
		builder.add("d");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(3,1), new Coordinate(4,1)}));
		collection.add(builder.buildFeature("d"));

		//fifth edge
		builder.add("e");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(4,1), new Coordinate(2,1)}));
		collection.add(builder.buildFeature("e"));

		//sixth edge
		builder.add("f");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(4,1), new Coordinate(5,1)}));
		collection.add(builder.buildFeature("f"));

		return new CollectionFeatureSource(collection);
	}


	/**
	 *
	 *                 /-2->-3- -- -3->-4- \
	 *  0->-1 -- -1->-2                     4->-5-
	 *         |       \-2-> 3' -- 3'->-4- /
	 *          < -----1 < 3'----/
	 */
	public static SimpleFeatureSource getGeoEdgesWithComplexCycle() throws Exception {
		SimpleFeatureType type = buildLogicalEdgeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		//first edge
		builder.add("a");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(0,1), new Coordinate(1,1)}));
		collection.add(builder.buildFeature("a"));

		//second edge
		builder.add("b");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(1,1), new Coordinate(2,1)}));
		collection.add(builder.buildFeature("b"));

		//third edge
		builder.add("c");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(2,1), new Coordinate(3,1)}));
		collection.add(builder.buildFeature("c"));

		//fourth edge
		builder.add("d");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(3,1), new Coordinate(4,1)}));
		collection.add(builder.buildFeature("d"));

		//fifth edge
		builder.add("e");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(2,1), new Coordinate(3,2)}));
		collection.add(builder.buildFeature("e"));

		//sixth edge (end)
		builder.add("f");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(4,1), new Coordinate(5,1)}));
		collection.add(builder.buildFeature("f"));

		builder.add("g");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(3,2), new Coordinate(4,1)}));
		collection.add(builder.buildFeature("g"));

		builder.add("h");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(3,2), new Coordinate(3,1)}));
		collection.add(builder.buildFeature("h"));

		return new CollectionFeatureSource(collection);
	}

    /**
     *
     * Generate a graph that contains 20 split/merges, with a start end end edge.
     *
     * -<><>...<>-
     *
     */
	public static SimpleFeatureSource getGeoEdgesSplitMerges() throws Exception {
		SimpleFeatureType type = buildLogicalEdgeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

		int splits = 20;

		// first edge
		builder.add("a");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(0,1), new Coordinate(2,1)}));
		collection.add(builder.buildFeature("a"));


		for (int i = 1; i <= splits; i++) {

			int row = 2*i;

			builder.add("top-split-" + i);
			builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(row, 1), new Coordinate(row+1, 1)}));
			collection.add(builder.buildFeature("top-split-" + i));

			builder.add("top-merge-" + i);
			builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(row+1, 1), new Coordinate(row+2, 1)}));
			collection.add(builder.buildFeature("top-merge-" + i));

			builder.add("bottom-split-" + i);
			builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(row, 1), new Coordinate(row+1, 2)}));
			collection.add(builder.buildFeature("bottom-split-" + i));

			builder.add("bottom-merge-" + i);
			builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(row+1, 2), new Coordinate(row+2, 1)}));
			collection.add(builder.buildFeature("bottom-merge-" + i));

		}
		// last edge
		builder.add("end");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(2*(splits+1), 1), new Coordinate((2*(splits+1)) + 1,1)}));
		collection.add(builder.buildFeature("end"));

		return new CollectionFeatureSource(collection);
	}

	/**
	 *  a -- -- -- -- --
	 *      |  |  |  |  |
	 *       -- -- -- --
	 *      |  |  |  |  |
	 *       -- -- -- --
	 *      |  |  |  |  |
	 *       -- -- -- -- -- z
	 *
	 */
	public static SimpleFeatureSource getGeoEdgesLargeRaster() throws Exception {
		SimpleFeatureType type = buildLogicalEdgeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

		int gridsize = 10;

		// first edge
		builder.add("a");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(0,1), new Coordinate(1,1)}));
		collection.add(builder.buildFeature("a"));

		// intermediate edge
		builder.add("b");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(gridsize + 1, gridsize + 1), new Coordinate(gridsize+2,gridsize+1)}));
		collection.add(builder.buildFeature("b"));

		// last edge
		builder.add("z");
		builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(gridsize*2 + 1, gridsize*2 + 1), new Coordinate(gridsize*2+2,gridsize*2+1)}));
		collection.add(builder.buildFeature("z"));


		// -- first square
		for (int i = 1; i <= gridsize; i++) {
			for (int j = 1; j <= gridsize; j++) {
				// horizontal
				builder.add("h-" + i + "-" + j);
				builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(j, i), new Coordinate(j+1, i)}));
				collection.add(builder.buildFeature("h-" + i + "-" + j));
				// vertical
				if (i < gridsize) {
					builder.add("v-" + i + "-" + j);
					builder.add(geomFac.createLineString(new Coordinate[]{new Coordinate(j, i), new Coordinate(j, i+1)}));
					collection.add(builder.buildFeature("v-" + i + "-" + j));
				}
			}
		}

		// -- second square
		int start = gridsize+1;
		for (int i = gridsize+2; i <= gridsize*2; i++) {
			for (int j = gridsize+2; j <= gridsize*2; j++) {
				// horizontal
				builder.add("h-" + i + "-" + j);
				builder.add(geomFac.createLineString(new Coordinate[] {new Coordinate(j, i), new Coordinate(j+1, i)}));
				collection.add(builder.buildFeature("h-" + i + "-" + j));
				// vertical
				if (i < gridsize) {
					builder.add("v-" + i + "-" + j);
					builder.add(geomFac.createLineString(new Coordinate[]{new Coordinate(j, i), new Coordinate(j, i+1)}));
					collection.add(builder.buildFeature("v-" + i + "-" + j));
				}
			}
		}

		return new CollectionFeatureSource(collection);
	}

}
