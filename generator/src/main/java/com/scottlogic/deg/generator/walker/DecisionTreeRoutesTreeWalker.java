package com.scottlogic.deg.generator.walker;

import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.reducer.ConstraintReducer;
import com.scottlogic.deg.generator.restrictions.RowSpec;
import com.scottlogic.deg.generator.restrictions.RowSpecMerger;
import com.scottlogic.deg.generator.walker.routes.RowSpecRoute;
import com.scottlogic.deg.generator.walker.routes.RowSpecRouteProducer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class DecisionTreeRoutesTreeWalker implements DecisionTreeWalker {
    private final ConstraintReducer constraintReducer;
    private final RowSpecMerger rowSpecMerger;
    private final RowSpecRouteProducer producer;

    public DecisionTreeRoutesTreeWalker(
        ConstraintReducer constraintReducer,
        RowSpecMerger rowSpecMerger,
        RowSpecRouteProducer producer) {
        this.constraintReducer = constraintReducer;
        this.rowSpecMerger = rowSpecMerger;
        this.producer = producer;
    }

    @Override
    public Stream<RowSpec> walk(DecisionTree tree){
        ConstraintNode rootNode = tree.getRootNode();
        ProfileFields fields = tree.getFields();
        Stream<RowSpecRoute> routes = this.producer.produceRoutes(tree);

        return routes
            .map(route -> {
                RowSpec accumulatedSpec = getRootRowSpec(fields, rootNode);
                return getRowSpec(fields, accumulatedSpec, rootNode, route.subRoutes);
            })
            .filter(rowSpec -> rowSpec != null);
    }

    private RowSpec getRowSpec(ProfileFields fields, RowSpec accumulatedSpec, ConstraintNode rootNode, RowSpecRoute[] routes) {
        Collection<DecisionNode> decisions = rootNode.getDecisions();

        if (decisions.size() != routes.length) {
            throw new UnsupportedOperationException("Invalid collection of routes; should be same size as number of decisions");
        }

        int index = 0;
        for (DecisionNode decision : decisions) {
            RowSpecRoute route = routes[index++]; //decision index
            ConstraintNode decisionOption = getOption(decision, route.decisionIndex);

            accumulatedSpec = getMergedRowSpec(fields, accumulatedSpec, decisionOption, route);

            if (accumulatedSpec == null) {
                return null; /* in case of contradicting decisions? */
            }
        }

        return accumulatedSpec;
    }

    private ConstraintNode getOption(DecisionNode decision, int index){
        Collection<ConstraintNode> options = decision.getOptions();
        return (ConstraintNode)options.toArray()[index];
    }

    private RowSpec getMergedRowSpec(ProfileFields fields, RowSpec accumulatedSpec, ConstraintNode decisionOption, RowSpecRoute route) {
        Optional<RowSpec> nominalRowSpec = constraintReducer.reduceConstraintsToRowSpec(
            fields,
            decisionOption.getAtomicConstraints());

        if (!nominalRowSpec.isPresent()) {
            return null;
        }

        final Optional<RowSpec> mergedRowSpecOpt = rowSpecMerger.merge(
            Arrays.asList(
                nominalRowSpec.get(),
                accumulatedSpec
            )
        );

        if (!mergedRowSpecOpt.isPresent()) {
            return null;
        }

        RowSpec rowSpec = mergedRowSpecOpt.get();

        boolean hasSubRoutes = route.subRoutes != null && route.subRoutes.length > 0;
        if (!hasSubRoutes && decisionOption.getDecisions().isEmpty()) {
            return rowSpec; //at a leaf node; return
        }

        return getRowSpec(fields, rowSpec, decisionOption, route.subRoutes);
    }

    private RowSpec getRootRowSpec(ProfileFields fields, ConstraintNode rootNode) {
        return constraintReducer.reduceConstraintsToRowSpec(
            fields,
            rootNode.getAtomicConstraints()).get();
    }
}