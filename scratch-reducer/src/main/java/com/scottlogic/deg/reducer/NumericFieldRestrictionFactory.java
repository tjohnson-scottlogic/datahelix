package com.scottlogic.deg.reducer;

import com.scottlogic.deg.constraint.IConstraint;
import com.scottlogic.deg.input.Field;
import com.scottlogic.deg.restriction.FieldSpec;
import com.scottlogic.deg.restriction.NumericFieldRestriction;

public class NumericFieldRestrictionFactory {
    private final NumericConstraintTypeClassifier numericConstraintTypeClassifier = new NumericConstraintTypeClassifier();

    public FieldSpec getForConstraint(Field field, IConstraint constraint) {
        final var constraintType = numericConstraintTypeClassifier.classify(constraint);
        switch(constraintType) {
            case Integer:
                return new NumericFieldRestriction<Integer>(field, Integer.class);
            case Double:
                return new NumericFieldRestriction<Double>(field, Double.class);
            default:
                throw new IllegalStateException();
        }
    }
}
