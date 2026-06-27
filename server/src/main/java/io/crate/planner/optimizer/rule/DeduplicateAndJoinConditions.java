/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.planner.optimizer.rule;

import static io.crate.planner.optimizer.matcher.Pattern.typeOf;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import io.crate.expression.operator.AndOperator;
import io.crate.planner.operators.JoinPlan;
import io.crate.expression.symbol.Symbol;
import io.crate.planner.optimizer.Rule;
import io.crate.planner.operators.LogicalPlan;
import io.crate.planner.optimizer.matcher.Captures;
import io.crate.planner.optimizer.matcher.Pattern;

public class DeduplicateAndJoinConditions implements Rule<JoinPlan> {

    private final Pattern<JoinPlan> pattern = typeOf(JoinPlan.class);

    @Override
    public Pattern<JoinPlan> pattern() {
        return pattern;
    }

    @Override
    public LogicalPlan apply(JoinPlan join,
            Captures captures,
            Rule.Context context) {

        List<Symbol> conditions = AndOperator.split(join.joinCondition());
        Set<Symbol> uniqueConditions = new LinkedHashSet<>(conditions);

        if (uniqueConditions.size() < conditions.size()) {
            return new JoinPlan(
                    join.lhs(),
                    join.rhs(),
                    join.joinType(),
                    AndOperator.join(uniqueConditions),
                    join.isFiltered(),
                    join.isRewriteFilterOnOuterJoinToInnerJoinDone(),
                    join.isLookUpJoinRuleApplied(),
                    join.moveConstantJoinConditionRuleApplied(),
                    join.eliminateCrossJoinRuleIsApplied(),
                    join.lookUpJoin()

            );
        }
        return join;

    }

}
