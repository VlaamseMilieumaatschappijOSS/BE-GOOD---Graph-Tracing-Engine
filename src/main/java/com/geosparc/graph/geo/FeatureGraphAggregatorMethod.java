package com.geosparc.graph.geo;

public enum FeatureGraphAggregatorMethod {

    /**
     * Aggregates by addition, when the result splits, the total is divided between the split paths.
     * TODO DOES NOT HANDLE LOOPS CORRECTLY ATM.
     */
    ADD {
        @Override
        public Object compute(Object one, Object two) {
            return (one == null ? 0.0d : ((Number) one).doubleValue()) +
                   (two == null ? 0.0d : ((Number) two).doubleValue());
        }

        /**
         * @param calculateEdge
         * @param incomingEdgeCount (will be outgoing when tracing upstream)
         * @param outgoingEdgeCount (will be incoming when tracing upstream)
         * @return
         */
        @Override
        public Object computeEdgeValueOutgoing(Object calculateEdge, int incomingEdgeCount, int outgoingEdgeCount) {
            return (calculateEdge == null ? 0.0d : ((Number) calculateEdge).doubleValue());
        }

        /**
         * @param calculateEdge
         * @param siblingEdgeCount Outgoing when downstream / incoming when upstream
         * @return
         */
        @Override
        public Object computeEdgeValueIncoming(Object calculateEdge, long siblingEdgeCount) {
            if (siblingEdgeCount < 1) { throw new IllegalStateException("There should be at least one edge"); }
            return calculateEdge == null ? 0.0d : ((Number) calculateEdge).doubleValue() / siblingEdgeCount;
        }
    },

    /**
     * Calculates the split factor for the trace result. Starting edges get a total weight of 1, which is then divided on splits or aggregated on merges.
     * DOES NOT WORK UPSTREAM ATM.
     */
    SPLITFACTOR {
        @Override
        public Object compute(Object one, Object two) {
            return (one == null ? 0.0d : ((Number) one).doubleValue()) +
                   (two == null ? 0.0d : ((Number) two).doubleValue());
        }

        /**
         * This is only used at startup (initial values), for all following iterations this is 0.
         * (so 1 goes in at start -> 1 comes out at the end)
         *
         * @param calculateEdge
         * @param incomingEdgeCount (will be outgoing when tracing upstream)
         * @param outgoingEdgeCount (will be incoming when tracing upstream)
         * @return
         */
        @Override
        public Object computeEdgeValueOutgoing(Object calculateEdge, int incomingEdgeCount, int outgoingEdgeCount) {
            // if there are no incoming edges (== start of path): return 1 / outgoing edges (if any), else 0 (the values of the incoming edges will be used)
            return incomingEdgeCount < 1 && outgoingEdgeCount > 0 ? 1.0d / outgoingEdgeCount : 0.0d;
        }

        /**
         * @param calculateEdge
         * @param siblingEdgeCount Outgoing when downstream / incoming when upstream
         * @return
         */
        @Override
        public Object computeEdgeValueIncoming(Object calculateEdge, long siblingEdgeCount) {
            // total incoming value must be split over the number of siblings
            if (siblingEdgeCount < 1) {
                throw new IllegalStateException("There should be at least one edge");
            }
            return calculateEdge == null ? 0.0d : ((Number) calculateEdge).doubleValue() / siblingEdgeCount;
        }

        @Override
        public boolean supportsUpstream() {
            return false;
        }
    };

    // ----------------------------------------------------

    public abstract Object compute(Object one, Object two);
    public abstract Object computeEdgeValueOutgoing(Object calculateEdge, int incomingEdgeCount, int outgoingEdgeCount);
    public abstract Object computeEdgeValueIncoming(Object calculateEdge, long siblingEdgeCount);
    public boolean supportsUpstream() { return true; }
    public boolean supportsDownstream() { return true; }

}
