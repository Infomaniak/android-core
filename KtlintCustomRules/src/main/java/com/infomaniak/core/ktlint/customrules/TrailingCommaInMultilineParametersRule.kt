/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.core.ktlint.customrules

import com.infomaniak.core.ktlint.customrules.Utils.checkTrailingComma
import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

class TrailingCommaInMultilineParametersRule :
    Rule(
        ruleId = RuleId("${CUSTOM_RULE_SET_ID}:trailing-comma-multiline-parameters"),
        about = About(),
    ) {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        if (isValueParameterList(node)) {
            checkTrailingComma(
                node,
                autoCorrect,
                emit,
                ElementType.VALUE_PARAMETER,
                "Multiline parameter list should have a trailing comma",
            )
        }
    }

    private fun isValueParameterList(node: ASTNode): Boolean {
        val isValueParameterList = node.elementType == ElementType.VALUE_PARAMETER_LIST
        return isValueParameterList && (node.psi.parent is KtFunction || node.psi.parent.parent is KtFunction)
    }
}
