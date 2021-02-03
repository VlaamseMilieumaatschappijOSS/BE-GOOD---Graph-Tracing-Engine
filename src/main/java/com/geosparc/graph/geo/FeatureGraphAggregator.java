package com.geosparc.graph.geo;

import com.geosparc.graph.base.Idp;
import org.geotools.util.logging.Logging;
import org.jgrapht.Graph;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Helper class for calculating aggregates in feature graphs on edges.
 *
 * @author Niels Charlier
 */
public class FeatureGraphAggregator {

    private static final Logger LOGGER = Logging.getLogger(FeatureGraphAggregator.class);

    private Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph;

    private String typeName;

    private String sourceProperty;

    private EdgeCalculator edgeCalculator;

    private List<Idp<GlobalId, SimpleFeature>> orderedEdges;

    private Double ZERO = 0.0D;

    public FeatureGraphAggregator(Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph,
                                  List<Idp<GlobalId, SimpleFeature>> orderedEdges,
                                  String typeName,
                                  String sourceProperty,
                                  FeatureGraphAggregatorMethod method,
                                  boolean upstream) {
        this.graph = graph;
        this.sourceProperty = sourceProperty;
        this.typeName = typeName;
        this.edgeCalculator = getEdgeCalculatorFor(method, upstream);
        this.orderedEdges = orderedEdges;
    }

    public Map<GlobalId, Object> aggregate() {
        Map<GlobalId, Object> result = new HashMap<>();
        for (Idp<GlobalId, SimpleFeature> e : orderedEdges) {
            if (e.getData().getType().getName().getLocalPart().equals(typeName)) {
                edgeCalculator.calculate(e, result);
            }
        }
        return result;
    }

    // ----------------------------------------------------

    private EdgeCalculator getEdgeCalculatorFor(FeatureGraphAggregatorMethod method, boolean upstream) {
        if (upstream) {
            return new UpstreamEdgeCalculator(method);
        } else {
            return new DownstreamEdgeCalculator(method);
        }
    }

    protected interface EdgeCalculator {
        Object calculate(Idp<GlobalId, SimpleFeature> edge, Map<GlobalId, Object> result);
    }

    protected class UpstreamEdgeCalculator implements EdgeCalculator {
        private final FeatureGraphAggregatorMethod method;

        public UpstreamEdgeCalculator(@NonNull FeatureGraphAggregatorMethod method) {
            if (!method.supportsUpstream()) {
                throw new IllegalArgumentException("Method: " + method.name() + " does not support upstream calculation");
            }
            this.method = method;
        }

        @Override
        public Object calculate(Idp<GlobalId, SimpleFeature> edge, Map<GlobalId, Object> result) {
            Object value = result.get(edge.getId());
            if (value == null) {
                // this avoids infinite recursion if there is a loop
                // therefore, it is important to follow the right order
                // starting with the source
                result.put(edge.getId(), 0);

                Set<Idp<GlobalId, SimpleFeature>> outgoingEdges = graph.outgoingEdgesOf(graph.getEdgeTarget(edge));
                Set<Idp<GlobalId, SimpleFeature>> incomingEdges = graph.incomingEdgesOf(graph.getEdgeTarget(edge));

                value = method.computeEdgeValueOutgoing(edge.getData().getAttribute(sourceProperty), outgoingEdges.size(), incomingEdges.size());
                for (Idp<GlobalId, SimpleFeature> e : graph.outgoingEdgesOf(graph.getEdgeTarget(edge))) {
                    if (e.getData().getType().getName().getLocalPart().equals(typeName)) {
                        value = method.compute(value, method.computeEdgeValueIncoming(calculate(e, result), incomingEdges.size()));
                    }
                }
                result.put(edge.getId(), value);
            }
            return value;
        }
    }

    protected class DownstreamEdgeCalculator implements EdgeCalculator {
        private final FeatureGraphAggregatorMethod method;

        public DownstreamEdgeCalculator(FeatureGraphAggregatorMethod method) {
            if (!method.supportsDownstream()) {
                throw new IllegalArgumentException("Method: " + method.name() + " does not support downstream calculation");
            }
            this.method = method;
        }

        @Override
        public Object calculate(Idp<GlobalId, SimpleFeature> edge, Map<GlobalId, Object> result) {
            Object value = result.get(edge.getId());
            LOGGER.finest("calculate edge: " + edge.getId());

            if (value == null) {

                // To avoid infinite recursion if there is a loop, we add the edge we are currently working on, so we can detect it
                // therefore, it is important to follow the right order
                // starting with the source
                result.put(edge.getId(), edge); // adding self instead of value: implies not yet calculated (not pretty, I know)

                Set<Idp<GlobalId, SimpleFeature>> siblingEdges = graph.outgoingEdgesOf(graph.getEdgeSource(edge))
                        .stream().filter(e -> e.getData().getType().getName().getLocalPart().equals(typeName)).collect(Collectors.toSet());
                Set<Idp<GlobalId, SimpleFeature>> parentEdges = graph.incomingEdgesOf(graph.getEdgeSource(edge))
                        .stream().filter(e -> e.getData().getType().getName().getLocalPart().equals(typeName)).collect(Collectors.toSet());

                value = method.computeEdgeValueOutgoing(edge.getData().getAttribute(sourceProperty), parentEdges.size(), siblingEdges.size());
                for (Idp<GlobalId, SimpleFeature> e : parentEdges) {
                    Object singleValue = calculate(e, result);
                    if (isLoop(singleValue)) {
                        LOGGER.finest(" -- loop detected - edge: " + e.getId());
                        if (singleValue.equals(edge)) { // back to beginning of loop
                            LOGGER.finest("    -- root of loop: " + edge.getId());
                            fixLoop(e, result);
                            singleValue = ZERO;
                        } else { // do some backtracking
                            LOGGER.finest("    -- backtracking: " + edge.getId());
                            result.remove(edge.getId());
                            return singleValue; // return the 'offending' edge, this is the start of the loop.
                        }
                    }

                    value = method.compute(value, method.computeEdgeValueIncoming(singleValue, countValidSiblings(siblingEdges, result)));
                }

                LOGGER.finest(" -- adding edge: " + edge.getId() + ", value: " + value);
                result.put(edge.getId(), value);
            }
            return value;
        }

        private boolean isLoop(Object value) {
            return (value != null && value instanceof Idp);
        }

        /**
         * Siblings that have been explicitly set to zero should not be counted (in case of loop).
         *
         * @return
         */
        private long countValidSiblings(Set<Idp<GlobalId, SimpleFeature>> siblingEdges, Map<GlobalId, Object> result) {
            return siblingEdges.stream().filter(s -> !result.containsKey(s.getId()) || !ZERO.equals(result.get(s.getId()))).count();
        }

        /**
         * Mark the Loopy part as all zero's, so they aren't used twice.
         * Presumes there are not unused parents (ie. edges that are not part of this tracing.)
         *
         * Backtracking -> Reverse order!
         */
        private void fixLoop(Idp<GlobalId, SimpleFeature> edge, Map<GlobalId, Object> result) {
            if (!result.containsKey(edge.getId())) {
                LOGGER.finest(" -- fixing loopy edge: " + edge.getId());
                result.put(edge.getId(), ZERO);

                Set<Idp<GlobalId, SimpleFeature>> siblingEdges = graph.outgoingEdgesOf(graph.getEdgeSource(edge))
                        .stream().filter(e -> e.getData().getType().getName().getLocalPart().equals(typeName)).collect(Collectors.toSet());
                Set<Idp<GlobalId, SimpleFeature>> parentEdges = graph.incomingEdgesOf(graph.getEdgeSource(edge))
                        .stream().filter(e -> e.getData().getType().getName().getLocalPart().equals(typeName)).collect(Collectors.toSet());

                if (siblingEdges.size() == 1 && !parentEdges.isEmpty()) { // 1 sibling == self
                    for (Idp<GlobalId, SimpleFeature> parentEdge : parentEdges) {
                        fixLoop(parentEdge, result);
                    }
                }
            }
        }
    }

}
