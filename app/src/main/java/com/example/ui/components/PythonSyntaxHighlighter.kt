package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

object PythonSyntaxHighlighter {

    // Cyberpunk JARVIS Color Palette for Syntax
    val ColorKeyword = Color(0xFF38BDF8)       // Sky Blue
    val ColorBuiltin = Color(0xFF22D3EE)       // Bright Cyan
    val ColorDefinition = Color(0xFFFDE047)    // Electric Yellow
    val ColorString = Color(0xFF4ADE80)        // Emerald Green
    val ColorComment = Color(0xFF64748B)       // Muted Slate Gray
    val ColorNumber = Color(0xFFFB923C)        // Amber Orange
    val ColorDecorator = Color(0xFFE879F9)     // Fuchsia / Magenta
    val ColorOperator = Color(0xFF93C5FD)      // Light Blue
    val ColorDefault = Color(0xFFF1F5F9)       // Crisp Off-White

    private val PYTHON_KEYWORDS = setOf(
        "def", "class", "import", "from", "return", "if", "elif", "else",
        "while", "for", "in", "try", "except", "finally", "with", "as",
        "lambda", "pass", "break", "continue", "yield", "raise", "assert",
        "global", "nonlocal", "async", "await", "and", "or", "not", "is", "del", "match", "case"
    )

    private val PYTHON_BUILTINS = setOf(
        "True", "False", "None", "self", "cls", "int", "str", "float",
        "bool", "list", "dict", "set", "tuple", "bytes", "bytearray",
        "Exception", "BaseException", "ValueError", "TypeError", "KeyError",
        "IndexError", "AttributeError", "FileNotFoundError", "RuntimeError",
        "print", "len", "range", "enumerate", "zip", "sum", "min", "max",
        "round", "type", "isinstance", "issubclass", "open", "id", "iter",
        "next", "map", "filter", "all", "any", "abs", "sorted", "reversed",
        "super", "property", "staticmethod", "classmethod", "format", "input",
        "callable", "hasattr", "getattr", "setattr", "delattr", "repr", "vars", "dir"
    )

    fun highlight(text: String, language: String = "Python"): AnnotatedString {
        if (text.isEmpty()) return buildAnnotatedString { }

        return buildAnnotatedString {
            append(text)

            // Default style
            addStyle(
                style = SpanStyle(
                    color = ColorDefault,
                    fontFamily = FontFamily.Monospace
                ),
                start = 0,
                end = text.length
            )

            // Highlight Numbers (integers, floats, hex, binary)
            val numberPattern = Pattern.compile("\\b(0x[0-9a-fA-F]+|0b[01]+|\\d+(\\.\\d+)?([eE][+-]?\\d+)?)\\b")
            val numberMatcher = numberPattern.matcher(text)
            while (numberMatcher.find()) {
                addStyle(
                    SpanStyle(color = ColorNumber),
                    numberMatcher.start(),
                    numberMatcher.end()
                )
            }

            // Highlight Decorators (@staticmethod, etc.)
            val decoratorPattern = Pattern.compile("@\\w+")
            val decoratorMatcher = decoratorPattern.matcher(text)
            while (decoratorMatcher.find()) {
                addStyle(
                    SpanStyle(color = ColorDecorator, fontWeight = FontWeight.Bold),
                    decoratorMatcher.start(),
                    decoratorMatcher.end()
                )
            }

            // Highlight Words (Keywords & Builtins)
            val wordPattern = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b")
            val wordMatcher = wordPattern.matcher(text)
            while (wordMatcher.find()) {
                val word = wordMatcher.group()
                when {
                    PYTHON_KEYWORDS.contains(word) -> {
                        addStyle(
                            SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold),
                            wordMatcher.start(),
                            wordMatcher.end()
                        )
                    }
                    PYTHON_BUILTINS.contains(word) -> {
                        addStyle(
                            SpanStyle(color = ColorBuiltin, fontWeight = FontWeight.SemiBold),
                            wordMatcher.start(),
                            wordMatcher.end()
                        )
                    }
                }
            }

            // Highlight Function / Class Names after 'def ' and 'class '
            val defPattern = Pattern.compile("\\b(def|class)\\s+([A-Za-z_][A-Za-z0-9_]*)")
            val defMatcher = defPattern.matcher(text)
            while (defMatcher.find()) {
                val nameStart = defMatcher.start(2)
                val nameEnd = defMatcher.end(2)
                if (nameStart in 0..text.length && nameEnd in 0..text.length) {
                    addStyle(
                        SpanStyle(color = ColorDefinition, fontWeight = FontWeight.Bold),
                        nameStart,
                        nameEnd
                    )
                }
            }

            // Highlight Operators and Punctuation
            val operatorPattern = Pattern.compile("(->|==|!=|<=|>=|\\+=|-=|\\*=|/=|\\*\\*|//|[+\\-*/%=<>!&|^~:])")
            val operatorMatcher = operatorPattern.matcher(text)
            while (operatorMatcher.find()) {
                addStyle(
                    SpanStyle(color = ColorOperator),
                    operatorMatcher.start(),
                    operatorMatcher.end()
                )
            }

            // Highlight Strings ("""...""", '''...''', "...", '...')
            // Note: We apply strings AFTER keywords so strings with keywords inside remain colored as strings
            val stringPattern = Pattern.compile("(?s)(\"\"\"[^\"]*\"\"\"|'''[^']*'''|\"[^\"\\n\\\\]*(?:\\\\.[^\"\\n\\\\]*)*\"|'[^'\\n\\\\]*(?:\\\\.[^'\\n\\\\]*)*')")
            val stringMatcher = stringPattern.matcher(text)
            while (stringMatcher.find()) {
                addStyle(
                    SpanStyle(color = ColorString),
                    stringMatcher.start(),
                    stringMatcher.end()
                )
            }

            // Highlight Comments (# to end of line) - Applied last to override everything else on comment lines
            val commentPattern = Pattern.compile("#.*")
            val commentMatcher = commentPattern.matcher(text)
            while (commentMatcher.find()) {
                addStyle(
                    SpanStyle(
                        color = ColorComment,
                        fontStyle = FontStyle.Italic
                    ),
                    commentMatcher.start(),
                    commentMatcher.end()
                )
            }
        }
    }
}

class PythonSyntaxVisualTransformation(private val language: String = "Python") : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = PythonSyntaxHighlighter.highlight(text.text, language)
        return TransformedText(transformed, OffsetMapping.Identity)
    }
}
