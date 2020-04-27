/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

package io.crate.expression.operator;

import io.crate.common.collections.MapComparator;
import io.crate.data.Input;
import io.crate.metadata.FunctionIdent;
import io.crate.metadata.FunctionInfo;
import io.crate.metadata.TransactionContext;
import io.crate.metadata.functions.Signature;

import javax.annotation.Nullable;
import java.util.Map;

import static io.crate.metadata.functions.TypeVariableConstraint.typeVariable;
import static io.crate.types.TypeSignature.parseTypeSignature;

public final class EqOperator extends Operator<Object> {

    public static final String NAME = "op_=";

    public static void register(OperatorModule module) {
        module.register(
            Signature.scalar(
                NAME,
                parseTypeSignature("E"),
                parseTypeSignature("E"),
                Operator.RETURN_TYPE.getTypeSignature()
            ).withTypeVariableConstraints(typeVariable("E")),
            (signature, dataTypes) ->
                new EqOperator(
                    new FunctionInfo(
                        new FunctionIdent(NAME, dataTypes),
                        Operator.RETURN_TYPE
                    ),
                    signature
                )
        );

        module.register(
            Signature.scalar(
                NAME,
                parseTypeSignature("object(K, V)"),
                parseTypeSignature("object(K, V)"),
                Operator.RETURN_TYPE.getTypeSignature()
            )
                .withTypeVariableConstraints(typeVariable("K"), typeVariable("V")),
            (signature, dataTypes) ->
                new ObjectEqOperator(
                    new FunctionInfo(
                        new FunctionIdent(NAME, dataTypes),
                        Operator.RETURN_TYPE
                    ),
                    signature
                )
        );
    }

    private final FunctionInfo info;
    private final Signature signature;

    public EqOperator(FunctionInfo info, Signature signature) {
        this.info = info;
        this.signature = signature;
    }

    @Override
    public Boolean evaluate(TransactionContext txnCtx, Input<Object>[] args) {
        assert args.length == 2 : "number of args must be 2";
        Object left = args[0].value();
        if (left == null) {
            return null;
        }
        Object right = args[1].value();
        if (right == null) {
            return null;
        }
        return left.equals(right);
    }

    @Override
    public FunctionInfo info() {
        return info;
    }

    @Nullable
    @Override
    public Signature signature() {
        return signature;
    }

    private static class ObjectEqOperator extends Operator<Object> {

        private final FunctionInfo info;
        private final Signature signature;

        ObjectEqOperator(FunctionInfo info, Signature signature) {
            this.info = info;
            this.signature = signature;
        }

        @Override
        @SafeVarargs
        public final Boolean evaluate(TransactionContext txnCtx, Input<Object>... args) {
            Object left = args[0].value();
            Object right = args[1].value();
            if (left == null || right == null) {
                return null;
            }
            return MapComparator.compareMaps(((Map) left), ((Map) right)) == 0;
        }

        @Override
        public FunctionInfo info() {
            return info;
        }

        @Nullable
        @Override
        public Signature signature() {
            return signature;
        }
    }
}
