/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.planner.optimizer.symbol.rule;

import static io.crate.planner.optimizer.matcher.Pattern.typeOf;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import io.crate.planner.optimizer.symbol.Rule;
import io.crate.planner.optimizer.symbol.FunctionLookup;
import io.crate.metadata.NodeContext;
import io.crate.expression.symbol.Function;
import io.crate.expression.symbol.Symbol;
import io.crate.planner.optimizer.matcher.Captures;
import io.crate.planner.optimizer.matcher.Pattern;
import io.crate.expression.operator.AndOperator;

public class DeduplicateAndPredicates implements Rule<Function> {

    private final Pattern<Function> pattern;

    public DeduplicateAndPredicates() {
        this.pattern = typeOf(Function.class)
                .with(f -> AndOperator.NAME.equals(f.name()));
    }

    @Override
    public Pattern<Function> pattern() {
        return pattern;
    }

    @Override
    public Symbol apply(Function operator, Captures captures, NodeContext nodeCtx, FunctionLookup functionLookup,
            Symbol parentNode) {
        List<Symbol> predicates = AndOperator.split(operator);
        Set<Symbol> uniquePredicates = new LinkedHashSet<>(predicates);

        if (uniquePredicates.size() < predicates.size()) {
            return AndOperator.join(uniquePredicates);
        }

        return null;

    }

}
