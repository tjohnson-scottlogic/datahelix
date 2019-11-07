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

package com.scottlogic.deg.profile.dtos.constraints.grammatical;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.scottlogic.deg.profile.common.ConstraintType;
import com.scottlogic.deg.profile.common.ConstraintTypeJsonProperty;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintDTO;

import java.util.List;

@JsonDeserialize(as = AllOfConstraintDTO.class)
public class AllOfConstraintDTO extends GrammaticalConstraintDTO
{
    @JsonProperty(ConstraintTypeJsonProperty.ALL_OF)
    public List<ConstraintDTO> constraints;

    public AllOfConstraintDTO() {
        super(ConstraintType.ALL_OF);
    }
}