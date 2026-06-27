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

package io.crate.planner.optimizer.symbol.rule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import io.crate.analyze.expressions.ExpressionAnalyzer;
import io.crate.planner.optimizer.matcher.Captures;
import io.crate.expression.operator.AndOperator;
import io.crate.expression.operator.EqOperator;
import io.crate.expression.symbol.Function;
import io.crate.expression.symbol.Literal;
import io.crate.expression.symbol.Symbol;
import io.crate.planner.optimizer.matcher.Match;
import io.crate.exceptions.ConversionException;
import io.crate.metadata.CoordinatorTxnCtx;
import io.crate.metadata.NodeContext;
import io.crate.metadata.Reference;
import io.crate.planner.optimizer.symbol.FunctionLookup;
import io.crate.testing.TestingHelpers;
import io.crate.types.DataTypes;

public class DeduplicateAndPredicatesTest {
    private static final NodeContext NODE_CONTEXT = TestingHelpers.createNodeContext();
    private static final FunctionLookup FUNCTION_LOOKUP = (f, args) -> {
        try {
            return ExpressionAnalyzer.allocateFunction(
                    f,
                    args,
                    null,
                    null,
                    CoordinatorTxnCtx.systemTransactionContext(),
                    NODE_CONTEXT);
        } catch (ConversionException e) {
            return null;
        }
    };
    private static final DeduplicateAndPredicates RULE = new DeduplicateAndPredicates();

    @Test
    public void test_deduplicates_identical_predicates() {
        Reference ref = TestingHelpers.createReference("x", DataTypes.INTEGER);

        Function eq2 = EqOperator.of(ref, Literal.of(2));
        Function eq3 = EqOperator.of(ref, Literal.of(3));

        Symbol functionToOptimize = AndOperator.join(List.of(eq2, eq2, eq3));
        Match<Function> match = RULE.pattern().accept(functionToOptimize, Captures.empty());
        assertThat(match.isPresent()).isTrue();

        Symbol optimizedFunction = RULE.apply(match.value(), match.captures(), NODE_CONTEXT, FUNCTION_LOOKUP,
                functionToOptimize);
        assertThat(optimizedFunction).isEqualTo(AndOperator.of(eq2, eq3));
    }

    @Test
    public void test_does_not_do_anything_if_no_identical_predicates() {
        Reference ref = TestingHelpers.createReference("x", DataTypes.INTEGER);

        Function eq2 = EqOperator.of(ref, Literal.of(2));
        Function eq3 = EqOperator.of(ref, Literal.of(3));

        Function functionToOptimize = AndOperator.of(eq2, eq3);

        Match<Function> match = RULE.pattern().accept(functionToOptimize, Captures.empty());
        assertThat(match.isPresent()).isTrue();

        Symbol optimizedFunction = RULE.apply(match.value(), match.captures(), NODE_CONTEXT, FUNCTION_LOOKUP,
                functionToOptimize);
        assertThat(optimizedFunction).isNull();
    }
}
