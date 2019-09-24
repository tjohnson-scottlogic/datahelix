/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.generator.fieldspecs;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.restrictions.*;
import com.scottlogic.deg.generator.restrictions.linear.*;
import com.scottlogic.deg.generator.restrictions.linear.NumericRestrictions;

import java.math.BigDecimal;

import static com.scottlogic.deg.common.profile.Types.NUMERIC;

public class NumericRestrictionsMergeOperation implements RestrictionMergeOperation {
    private final LinearRestrictionsMerger merger;

    @Inject
    public NumericRestrictionsMergeOperation(LinearRestrictionsMerger merger) {
        this.merger = merger;
    }

    @Override
    public FieldSpec applyMergeOperation(FieldSpec left, FieldSpec right) {
        MergeResult<LinearRestrictions<BigDecimal>> mergeResult = merger.merge(
            left.getNumericRestrictions(), right.getNumericRestrictions());

        if (!mergeResult.successful) {
            return FieldSpec.nullOnlyFromType(NUMERIC);
        }

        return FieldSpec.fromType(NUMERIC).withNumericRestrictions((NumericRestrictions) mergeResult.restrictions);
    }
}

