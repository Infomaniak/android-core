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
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.psiUtil.children

object Utils {

    fun checkTrailingComma(
        listNode: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
        elementType: IElementType,
        errorMessage: String,
    ) {
        val elements = listNode.children().filter { it.elementType == elementType }.toList()
        val leftParenthesis = listNode.findChildByType(ElementType.LPAR) ?: return
        val rightParenthesis = listNode.findChildByType(ElementType.RPAR) ?: return

        if (elements.size == 1) {
            val element = elements.first()
            val textBetweenLparAndElement = getTextBetween(leftParenthesis.treeNext, element.treePrev)
            val textBetweenArgAndRElement = getTextBetween(element.treeNext, rightParenthesis.treePrev)

            if (!(textBetweenLparAndElement.contains('\n') || textBetweenArgAndRElement.contains('\n'))) {
                return
            }
        } else if (elements.isEmpty()) {
            return
        }

        var isMultiline = false
        var previousElementEndNode: ASTNode = leftParenthesis

        for (argIndex in elements.indices) {
            val currentElement = elements[argIndex]
            var nodeToInspect: ASTNode? = previousElementEndNode.treeNext
            var foundNewlineBeforeCurrentElement = false

            while (nodeToInspect != null && nodeToInspect != currentElement) {
                if (nodeToInspect.isWhiteSpaceWithNewline()) {
                    foundNewlineBeforeCurrentElement = true
                    break
                }
                nodeToInspect = nodeToInspect.treeNext
            }

            if (foundNewlineBeforeCurrentElement) {
                isMultiline = true
                break
            }
            previousElementEndNode = currentElement
        }

        if (!isMultiline && elements.isNotEmpty()) {
            val lastElement = elements.last()
            var nodeToInspect: ASTNode? = lastElement.treeNext

            while (nodeToInspect != null && nodeToInspect != rightParenthesis) {
                if (nodeToInspect.isWhiteSpaceWithNewline()) {
                    isMultiline = true
                    break
                }
                nodeToInspect = nodeToInspect.treeNext
            }
        }

        if (isMultiline) {
            val lastElement = elements.last()
            val nextSignificantTokenAfterLastElement = lastElement.treeNext?.let { node ->
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

            if (nextSignificantTokenAfterLastElement?.elementType != ElementType.COMMA) {
                val insertOffset = lastElement.startOffset + lastElement.textLength
                emit(
                    insertOffset,
                    errorMessage,
                    autoCorrect,
                )

                if (autoCorrect) {
                    (rightParenthesis as TreeElement).rawInsertBeforeMe(LeafPsiElement(COMMA, ","))
                }
            }
        }
    }

    fun getTextBetween(startNode: ASTNode?, endNode: ASTNode?): String {
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
