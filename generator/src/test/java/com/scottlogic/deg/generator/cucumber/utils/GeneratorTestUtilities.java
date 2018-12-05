package com.scottlogic.deg.generator.cucumber.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.Rule;
import com.scottlogic.deg.generator.constraints.LogicalConstraint;
import com.scottlogic.deg.generator.cucumber.steps.DateValueStep;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeCollection;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeGenerator;
import com.scottlogic.deg.generator.decisiontree.NoopDecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.tree_partitioning.RelatedFieldTreePartitioner;
import com.scottlogic.deg.generator.generation.DataGenerator;
import com.scottlogic.deg.generator.generation.GenerationConfig;
import com.scottlogic.deg.generator.generation.IDataGenerator;
import com.scottlogic.deg.generator.generation.NoopDataGeneratorMonitor;
import com.scottlogic.deg.generator.outputs.GeneratedObject;
import com.scottlogic.deg.generator.reducer.ConstraintReducer;
import com.scottlogic.deg.generator.restrictions.FieldSpecFactory;
import com.scottlogic.deg.generator.restrictions.FieldSpecMerger;
import com.scottlogic.deg.generator.restrictions.RowSpecMerger;
import com.scottlogic.deg.generator.walker.CartesianProductDecisionTreeWalker;
import org.junit.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratorTestUtilities {
    private static final ObjectMapper mapper = createMapper();

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        return mapper;
    }

    /**
     * Runs the data generator and returns list of generated result data.
     *
     * @return Generated data
     */
    static List<List<Object>> getDEGGeneratedData(
        List<Field> profileFields,
        List<LogicalConstraint> constraints,
        GenerationConfig.DataGenerationType generationStrategy,
        GenerationConfig.TreeWalkerType walkerType,
        GenerationConfig.CombinationStrategyType combinationStrategy) {
        return getGeneratedDataAsList(profileFields, constraints, generationStrategy, walkerType, combinationStrategy)
            .stream()
            .map(genObj ->
                genObj.values
                    .stream()
                    .map(obj -> {
                        if (obj.value != null && obj.format != null) {
                            return String.format(obj.format, obj.value);
                        }
                        return obj.value;
                    })
                    .collect(Collectors.toList())
            ).collect(Collectors.toList());
    }

    private static List<GeneratedObject> getGeneratedDataAsList(
        List<Field> profileFields,
        List<LogicalConstraint> constraints,
        GenerationConfig.DataGenerationType generationStrategy,
        GenerationConfig.TreeWalkerType walkerType,
        GenerationConfig.CombinationStrategyType combinationStrategy) {
        Profile profile = new Profile(
            new ProfileFields(profileFields),
            Collections.singleton(new Rule("TEST_RULE", constraints)));

        final DecisionTreeCollection analysedProfile = new DecisionTreeGenerator().analyse(profile);

        final IDataGenerator dataGenerator = new DataGenerator(
            new CartesianProductDecisionTreeWalker(
                new ConstraintReducer(
                    new FieldSpecFactory(),
                    new FieldSpecMerger()),
                new RowSpecMerger(
                    new FieldSpecMerger())),
            new RelatedFieldTreePartitioner(),
            new NoopDecisionTreeOptimiser(),
            new NoopDataGeneratorMonitor());

        final GenerationConfig config = new GenerationConfig(generationStrategy, walkerType, combinationStrategy);
        final Stream<GeneratedObject> dataSet = dataGenerator.generateData(profile, analysedProfile.getMergedTree(), config);
        List<GeneratedObject> allActualRows = new ArrayList<>();
        dataSet.forEach(allActualRows::add);
        return allActualRows;
    }

    public static Object parseInput(String input) throws JsonParseException {
        if (input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1);
        } else if (input.matches(DateValueStep.DATE_REGEX)) {
            return DateValueStep.dateObject(input);
        } else if (input.equals("null")) {
            return null;
        } else if (input.matches("-?(\\d+(\\.\\d+)?)")) {
            return parseNumber(input);
        }

        return input;
    }

    public static Object parseNumber(String input) throws JsonParseException {
        try {
            return mapper.readerFor(Number.class).readValue(input);
        }
        catch (JsonParseException e){
            throw e;
        }
        catch (IOException e) {
            Assert.fail("Unexpected IO exception " + e.toString());
            return "<unexpected IO exception>";
        }
    }

    public static Object parseExpected(String input) throws JsonParseException {
        if (input.matches(DateValueStep.DATE_REGEX)) {
            return LocalDateTime.parse(input);
        }
        return parseInput(input);
    }
}
