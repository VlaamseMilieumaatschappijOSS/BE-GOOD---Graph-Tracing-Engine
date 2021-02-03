package com.geosparc.graph.geo;

import com.geosparc.graph.base.DGraph;
import com.geosparc.gte.TestData;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;

import static com.geosparc.gte.TestData.buildGraph;

/**
 * @author Oliver May
 * created on 27/06/2019
 */
public class FeatureGraphAggregatorTest {

    private static final GeometryFactory geomFac = new GeometryFactory();
    private DGraph<GlobalId, SimpleFeature, SimpleFeature> graph;
    private DGraph<GlobalId, SimpleFeature, SimpleFeature> minimalGraph;
    private DGraph<GlobalId, SimpleFeature, SimpleFeature> multiGraph;
    private DGraph<GlobalId, SimpleFeature, SimpleFeature> loopGraph;
    private FeatureGraphTracer tracerUpstream;
    private FeatureGraphTracer tracerDownstream;
    private FeatureGraphTracer tracerDownstreamMinimal;
    private FeatureGraphTracer tracerDownstreamMulti;
    private FeatureGraphTracer tracerDownstreamLoop;
    private FeatureGraphTracer tracerUpstreamLoop;

    /**
     *
     *  Minimal graph:
     *
     *   1->-2
     *
     *
     * Example graph:
     *
     *         3->-4
     *        /     \
     *   1->-2      5->-6
     *       \     /
     *        7->-8
     *        \
     *         9
     *
     *
     * Multistart graph:
     *
     *         3->-4
     *        /     \
     *       2      5->-6
     *       \     /
     *        7->-8
     *        \
     *         9
     *
     *
     * Loop graph:
     *
     *         5
     *         |
     *     6-<-4
     *     |   |
     *     \   3
     *      \ /
     *   1->-2->-7->-8
     *
     */
    @Before
    public void prepareGraphs() {
        minimalGraph = buildGraph(
                TestData.simpleFeatureType("edge", new HashMap<String, Class>() {{
                    put("ie", Integer.class);
                }}),
                Arrays.asList(
                        new TestData.TestGraphEdge("1", "2", new Object[]{geomFac.createPoint(), 1})
                ));

        List<TestData.TestGraphEdge> base =  Arrays.asList(
                new TestData.TestGraphEdge("1", "2", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("2", "3", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("3", "4", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("4", "5", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("2", "7", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("7", "8", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("8", "5", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("5", "6", new Object[]{geomFac.createPoint(), 1}),
                new TestData.TestGraphEdge("7", "9", new Object[]{geomFac.createPoint(), 1}));

        graph = buildGraph(
                TestData.simpleFeatureType("edge", new HashMap<String, Class>() {{
                    put("ie", Integer.class);
                }}), base
        );

        List<TestData.TestGraphEdge> multi = new ArrayList<>(base);
        multi.remove(0);
        multiGraph = buildGraph(
                TestData.simpleFeatureType("edge", new HashMap<String, Class>() {{
                    put("ie", Integer.class);
                }}), multi
        );

        loopGraph = buildGraph(
                TestData.simpleFeatureType("edge", new HashMap<String, Class>() {{
                    put("ie", Integer.class);
                }}),
                Arrays.asList(
                        new TestData.TestGraphEdge("1", "2", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("2", "3", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("3", "4", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("4", "6", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("6", "2", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("2", "7", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("4", "5", new Object[]{geomFac.createPoint(), 1}),
                        new TestData.TestGraphEdge("7", "8", new Object[]{geomFac.createPoint(), 1})
                ));

        tracerUpstream = new FeatureGraphTracer(graph, new GlobalId("vertices", "1"), true);
        tracerDownstream = new FeatureGraphTracer(graph, new GlobalId("vertices", "1"), false);
        tracerDownstreamMinimal = new FeatureGraphTracer(minimalGraph, new GlobalId("vertices", "1"), false);
        tracerDownstreamMulti = new FeatureGraphTracer(multiGraph, new GlobalId("vertices", "2"), false);
        tracerDownstreamLoop = new FeatureGraphTracer(loopGraph, new GlobalId("vertices", "1"), false);
        tracerUpstreamLoop = new FeatureGraphTracer(loopGraph, new GlobalId("vertices", "1"), true);
    }

    @Test
    public void testFeatureGraphAggregatorAddUpstream() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                graph,
                tracerUpstream.orderEdges(graph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.ADD,
                true
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(9.0, 3.5, 4.5, 2.5, 2.5, 1.5, 1.5, 1.0, 1.0);
    }

    @Test
    public void testFeatureGraphAggregatorAddDownStream() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                graph,
                tracerDownstream.orderEdges(graph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.ADD,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 1.5, 2.5, 3.5, 1.5, 1.75, 1.75, 2.75, 7.25);
    }

    @Test
    public void testFeatureGraphAggregatorAddDownStreamMulti() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                multiGraph,
                tracerDownstreamMulti.orderEdges(multiGraph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.ADD,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 2.0, 3.0, 1.0, 1.5, 1.5, 2.5, 6.5);
    }


    /**
     *  TODO BREAKS!
     *  -- Current result (nonloop first)
     *
      *  edges:1->2=1.0
      *      edges:2->3=1.5, edges:3->4=2.5,
      *          edges:4->5=**2.25**
      *          edges:4->6=2.25, edges:6->2=3.25
      *      edges:2->7=3.125, edges:7->8=**4.125**
     *
     *  -- Current result (loop first)
      *  edges:1->2=1.0
      *      edges:2->3=2.75, edges:3->4=1.0
      *          edges:4->5=**1.5**
      *          edges:4->6=1.5, edges:6->2=2.5
      *      edges:2->7=2.75, edges:7->8=**3.75**
     */
    @Test
    @Ignore
    public void testFeatureGraphAggregatorAddDownStreamLoop() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                loopGraph,
                tracerDownstreamLoop.orderEdges(loopGraph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.ADD,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 1.5, 2.5, 3.5, 1.5, 1.75, 1.75, 2.75, 7.25);
    }

    /**
     *  TODO BREAKS!
     *  -- Current results
     *
     *  [edges:6->2=3.25, edges:2->7=3.125, edges:1->2=1.0, edges:3->4=2.5, edges:2->3=1.5, edges:4->5=2.25, edges:4->6=2.25, edges:7->8=4.125]
     *  [edges:6->2=2.5, edges:2->7=2.75, edges:1->2=1.0, edges:2->3=2.75, edges:3->4=1.0, edges:4->5=1.5, edges:4->6=1.5, edges:7->8=3.75]
     */
    @Test
    @Ignore
    public void testFeatureGraphAggregatorAddUpStreamLoop() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                loopGraph,
                tracerUpstreamLoop.orderEdges(loopGraph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.ADD,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 1.5, 2.5, 3.5, 1.5, 1.75, 1.75, 2.75, 7.25);
    }

    // ----------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testFeatureGraphAggregatorSplitpercentageUpstream() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                graph,
                tracerUpstream.orderEdges(graph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.SPLITFACTOR,
                true
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 1.5, 2.0);
    }

    @Test
    public void testFeatureGraphAggregatorSplitpercentageDownstream() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                graph,
                tracerDownstream.orderEdges(graph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.SPLITFACTOR,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 0.5, 0.5, 0.5, 0.5, 0.25, 0.25, 0.25, 0.75);
    }

    @Test
    public void testFeatureGraphAggregatorSplitpercentageDownstreamMinimal() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                minimalGraph,
                tracerDownstreamMinimal.orderEdges(minimalGraph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.SPLITFACTOR,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0);
    }

    @Test
    public void testFeatureGraphAggregatorSplitpercentageDownstreamMulti() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                multiGraph,
                tracerDownstreamMulti.orderEdges(multiGraph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.SPLITFACTOR,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(0.5, 0.5, 0.5, 0.5, 0.25, 0.25, 0.25, 0.75);
    }

    @Test
    public void testFeatureGraphAggregatorSplitpercentageDownstreamLoop() {
        FeatureGraphAggregator aggregator = new FeatureGraphAggregator(
                loopGraph,
                tracerDownstreamLoop.orderEdges(loopGraph),
                "edge",
                "ie",
                FeatureGraphAggregatorMethod.SPLITFACTOR,
                false
        );
        Map<GlobalId, Object> values = aggregator.aggregate();
        System.out.println(Arrays.toString(values.entrySet().toArray()));
        Assertions.assertThat(values.values()).containsExactlyInAnyOrder(1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.0, 0.0);
    }
}
