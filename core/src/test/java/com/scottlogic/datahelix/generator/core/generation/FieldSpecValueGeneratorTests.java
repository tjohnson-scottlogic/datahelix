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

package com.scottlogic.datahelix.generator.core.generation;

import com.scottlogic.datahelix.generator.common.profile.Field;
import com.scottlogic.datahelix.generator.common.profile.StandardSpecificFieldType;
import com.scottlogic.datahelix.generator.common.whitelist.DistributedList;
import com.scottlogic.datahelix.generator.core.fieldspecs.FieldSpec;
import com.scottlogic.datahelix.generator.core.fieldspecs.FieldSpecFactory;
import com.scottlogic.datahelix.generator.core.fieldspecs.WhitelistFieldSpec;
import com.scottlogic.datahelix.generator.core.generation.databags.DataBagValue;
import com.scottlogic.datahelix.generator.core.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.datahelix.generator.core.restrictions.linear.Limit;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictionsFactory;
import com.scottlogic.datahelix.generator.core.utils.JavaUtilRandomNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.datahelix.generator.common.profile.FieldBuilder.createField;
import static com.scottlogic.datahelix.generator.core.config.detail.DataGenerationType.FULL_SEQUENTIAL;
import static com.scottlogic.datahelix.generator.core.config.detail.DataGenerationType.RANDOM;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.*;

class FieldSpecValueGeneratorTests {
    @Test
    void generate_fieldSpecMustContainRestrictionNullAndSetRestrictionsHasValues_returnsDataBagsWithValuesInSetRestrictions() {
        WhitelistFieldSpec fieldSpec = FieldSpecFactory.fromList(DistributedList.uniform(Arrays.asList(10, 20, 30)))
            .withNotNull();
        FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
            RANDOM,
            new DeterministicRandomForSet(Arrays.asList(0, 1, 2), false));

        final Set<DataBagValue> result = fieldSpecFulfiller.generate(createField(null), fieldSpec).limit(3).collect(Collectors.toSet());

        Set<DataBagValue> expectedDataBags = fieldSpec.getWhitelist().list()
            .stream()
            .map(DataBagValue::new)
            .collect(Collectors.toSet());

        assertThat(result, sameBeanAs(expectedDataBags));
    }

    @Test
    void generate_fieldSpecMustContainRestrictionNullAndNumericRestrictionApplied_returnsExpectedDataBagsForNumericRestriction() {
        FieldSpec fieldSpec = FieldSpecFactory.fromRestriction(
                LinearRestrictionsFactory.createNumericRestrictions(
                    new Limit<>(new BigDecimal(10), false),
                    new Limit<>(new BigDecimal(30), false)));
        FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
            RANDOM,
            new DeterministicRandomForSet(Stream.of(0, 1).collect(Collectors.toList()), true));

        final Set<DataBagValue> result =
            fieldSpecFulfiller
                .generate(
                    createField(null, StandardSpecificFieldType.DECIMAL.toSpecificFieldType()),
                    fieldSpec)
                .limit(3)
                .collect(Collectors.toSet());

        Set<DataBagValue> expectedDataBags = new HashSet<>(
            Arrays.asList(
                new DataBagValue(
                    new BigDecimal("10.00000000000000000001")
                ),
                new DataBagValue(
                    new BigDecimal("29.99999999999999999999")
                ),
                new DataBagValue(null)
            )
        );

        assertThat(result, sameBeanAs(expectedDataBags));
    }

    @Nested
    class GetDataValuesTests {
        FieldSpec fieldSpec;
        FieldValueSource fieldValueSource;
        JavaUtilRandomNumberGenerator randomNumberGenerator;

        @BeforeEach
        void beforeEach() {
            fieldValueSource = mock(FieldValueSource.class);
            fieldSpec = mock(FieldSpec.class);

            randomNumberGenerator = mock(JavaUtilRandomNumberGenerator.class);
            when(fieldSpec.getFieldValueSource()).thenReturn(fieldValueSource);
            when(fieldValueSource.generateAllValues()).thenReturn(Stream.empty());
            when(fieldValueSource.generateRandomValues(randomNumberGenerator)).thenReturn(Stream.empty());
        }

        @Test
        void generateRandom_uniqueFieldSpec_returnsAllValues() {
            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                RANDOM,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(new Field(null, StandardSpecificFieldType.STRING.toSpecificFieldType(), true, null, false, false, null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateRandom_notUniqueFieldSpec_returnsRandomValues() {
            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                RANDOM,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(0)).generateAllValues();
            verify(fieldValueSource, times(1)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateSequential_uniqueFieldSpec_returnsAllValues() {
            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                FULL_SEQUENTIAL,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }

        @Test
        void generateSequential_notUniqueFieldSpec_returnsAllValues() {
            FieldSpecValueGenerator fieldSpecFulfiller = new FieldSpecValueGenerator(
                FULL_SEQUENTIAL,
                randomNumberGenerator
            );

            fieldSpecFulfiller.generate(createField(null), fieldSpec).collect(Collectors.toSet());

            verify(fieldValueSource, times(1)).generateAllValues();
            verify(fieldValueSource, times(0)).generateRandomValues(randomNumberGenerator);
        }
    }
}

