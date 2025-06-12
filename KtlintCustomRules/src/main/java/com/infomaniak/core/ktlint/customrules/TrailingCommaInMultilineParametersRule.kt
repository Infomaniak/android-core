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

import com.pinterest.ktlint.rule.engine.core.api.ElementType
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.children

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
        val isValueParameterList = node.elementType == ElementType.VALUE_PARAMETER_LIST
        if (isValueParameterList && (node.psi.parent is KtFunction || node.psi.parent.parent is KtFunction)) {
            checkTrailingComma(node, emit)
        }
    }

    private fun checkTrailingComma(
        parameterListNode: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ) {
        val parameters = parameterListNode.children().filter { it.elementType == ElementType.VALUE_PARAMETER }.toList()
        val leftParenthesis = parameterListNode.findChildByType(ElementType.LPAR) ?: return
        val rightParenthesis = parameterListNode.findChildByType(ElementType.RPAR) ?: return

        if (parameters.size == 1) {
            val paramNode = parameters.first()
            val textBetweenLparAndParam = getTextBetween(leftParenthesis.treeNext, paramNode.treePrev)
            val textBetweenParamAndRpar = getTextBetween(paramNode.treeNext, rightParenthesis.treePrev)

            if (!(textBetweenLparAndParam.contains('\n') || textBetweenParamAndRpar.contains('\n'))) {
                return
            }
        } else if (parameters.isEmpty()) {
            return
        }

        var isMultiline = false
        var previousParamEndNode: ASTNode = leftParenthesis

        for (paramIndex in parameters.indices) {
            val currentParam = parameters[paramIndex]
            var nodeToInspect: ASTNode? = previousParamEndNode.treeNext

            var foundNewlineBeforeCurrentParam = false
            while (nodeToInspect != null && nodeToInspect != currentParam) {
                if (nodeToInspect.isWhiteSpaceWithNewline()) {
                    foundNewlineBeforeCurrentParam = true
                    break
                }
                nodeToInspect = nodeToInspect.treeNext
            }

            if (foundNewlineBeforeCurrentParam) {
                isMultiline = true
                break
            }
            previousParamEndNode = currentParam
        }

        if (!isMultiline && parameters.isNotEmpty()) {
            val lastParam = parameters.last()
            var nodeToInspect: ASTNode? = lastParam.treeNext
            while (nodeToInspect != null && nodeToInspect != rightParenthesis) {
                if (nodeToInspect.isWhiteSpaceWithNewline()) {
                    isMultiline = true
                    break
                }
                nodeToInspect = nodeToInspect.treeNext
            }
        }

        if (isMultiline) {
            val lastParameter = parameters.last()
            val nextSignificantTokenAfterLastParam = lastParameter.treeNext?.let { node ->
                var current: ASTNode? = node
                while (current != null &&
                    (
                        current.elementType == ElementType.WHITE_SPACE ||
                            current.elementType == ElementType.EOL_COMMENT ||
                            current.elementType == ElementType.BLOCK_COMMENT
                        )
                ) {
                    current = current.treeNext
                }
                current
            }

            if (nextSignificantTokenAfterLastParam?.elementType != ElementType.COMMA) {
                val insertOffset = lastParameter.startOffset + lastParameter.textLength
                emit(
                    insertOffset,
                    "Multiline parameter list should have a trailing comma",
                    true,
                )
            }
        }
    }

    private fun getTextBetween(
        startNode: ASTNode?,
        endNode: ASTNode?
    ): String {
        if (startNode == null || endNode == null || startNode == endNode) return ""

        return StringBuilder().apply {
            var current: ASTNode? = startNode
            while (current != null && current != endNode) {
                append(current.text)
                current = current.treeNext
                if (current == endNode) break
            }
        }.toString()
    }
}
