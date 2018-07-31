package com.scottlogic.deg.restriction;

import java.util.Map;

public class FieldRestrictionClassifier {
    private final Map<Class<? extends FieldSpec>, FieldRestrictionType> typeMapping = Map.of(
            NumericFieldRestriction.class, FieldRestrictionType.Numeric,
            StringFieldRestriction.class, FieldRestrictionType.String
    );

    public FieldRestrictionType classify(FieldSpec constraint) {
        if (!typeMapping.containsKey(constraint.getClass())) {
            throw new IllegalStateException();
        }
        return typeMapping.get(constraint.getClass());
    }
}
