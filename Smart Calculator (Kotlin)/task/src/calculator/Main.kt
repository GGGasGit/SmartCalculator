package calculator

import java.math.BigInteger

enum class Operators(val stringFormat: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLICATION("*"),
    DIVISION("/")
}

fun operatorPrecedence(input: String): Int {
    return when (input) {
        Operators.PLUS.stringFormat, Operators.MINUS.stringFormat -> 0
        Operators.MULTIPLICATION.stringFormat, Operators.DIVISION.stringFormat -> 1
        else -> 2
    }
}

object Calculator {
    private val variables = mutableMapOf<String, BigInteger>()

    fun handleAssignment(input: String) {
        if (input.count {it == '='} != 1) {
            println("Invalid assignment")
        } else {
            val (identifier, variable) = input.replace(Regex("\\s"), "").split("=")
            if (Regex("[a-zA-Z]+").matches(identifier)) {
                if (Regex("[+\\-]?\\d+").matches(variable)) {
                    variables[identifier] = variable.toBigInteger()
                } else if (Regex("[a-zA-Z]+").matches(variable)) {
                    if (variables.containsKey(variable)) {
                        variables[identifier] = variables[variable]!!
                    } else {
                        println("Unknown variable")
                    }
                } else {
                    println("Invalid assignment")
                }
            } else {
                println("Invalid identifier")
            }
        }
    }

    fun printVariable(input: String) {
        if (variables.containsKey(input)) {
            println(variables[input])
        } else {
            println("Unknown variable")
        }
    }

    private fun convertToPostfix(input: String): String {
        val result = mutableListOf<String>()
        val stack = mutableListOf<String>()
        val inputList = input.replace(Regex("(-{2})+"), "+")
            .replace(Regex("\\+ *-"), "-")
            .replace(Regex("\\+{2,}"), "+")
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("*", " * ")
            .replace("/", " / ")
            .replace("(", " ( ")
            .replace(")", " ) ")
            .trim()
            .split(Regex("\\s+"))
        for (item in inputList) {
            when {
                Regex("[a-zA-Z0-9]+").matches(item) -> result.add(item)
                else -> {
                    when {
                        stack.isEmpty() || stack.last() == "(" -> stack.add(item)
                        item == "(" -> stack.add(item)
                        item == ")" -> {
                            while (stack.last() != "(") {
                                result.add(stack.last())
                                stack.removeLast()
                            }
                            stack.removeLast()
                        }
                        operatorPrecedence(item) > operatorPrecedence(stack.last()) -> stack.add(item)
                        operatorPrecedence(item) <= operatorPrecedence(stack.last()) -> {
                            do {
                                result.add(stack.last())
                                stack.removeLast()
                            } while (stack.isNotEmpty() && operatorPrecedence(stack.last()) >= operatorPrecedence(item) && stack.last() != "(")
                            stack.add(item)
                        }
                    }
                }
            }
        }
        do {
            result.add(stack.last())
            stack.removeLast()
        } while (stack.isNotEmpty())

        return result.joinToString(" ")
    }

    private fun performOperation(firstNumber: BigInteger, secondNumber: BigInteger, operation: String): BigInteger {
        return when (operation) {
            Operators.PLUS.stringFormat -> firstNumber + secondNumber
            Operators.MINUS.stringFormat -> firstNumber - secondNumber
            Operators.MULTIPLICATION.stringFormat -> firstNumber * secondNumber
            Operators.DIVISION.stringFormat -> firstNumber / secondNumber
            else -> BigInteger.ZERO
        }
    }

    fun calculateResult(input: String) {
        val postFixInput = convertToPostfix(input)
        val result = mutableListOf<BigInteger>()
        val inputList = postFixInput.split(Regex(" "))
        for (item in inputList) {
            when {
                Regex("\\d+").matches(item) -> result.add(item.toBigInteger())
                Regex("[a-zA-Z]").matches(item) -> {
                    if (variables.containsKey(item)) {
                        result.add(variables[item]!!)
                    } else {
                        println("Unknown variable")
                        return
                    }
                }
                else -> {
                    val secondNumber = result.removeLast()
                    val firstNumber = if (result.isNotEmpty()) result.removeLast() else BigInteger.ZERO
                    val operationResult = performOperation(firstNumber, secondNumber, item)
                    result.add(operationResult)
                }
            }
        }
        println(result.last())
    }

}



fun main() {
    val calculator = Calculator

    while (true) {
        val input = readln()

        when {
            input == "" -> continue
            input[0] == '/' -> {
                when (input) {
                    "/exit" -> {
                        println("Bye!")
                        break
                    }
                    "/help" -> println("The smart calculator calculates addition, subtraction, multiplication " +
                            "and integer division of numbers given in one line.\n" +
                            "Only numbers (positive or negative) as well as the +, -, *, / signs and parentheses can be used.\n" +
                            "E.g. -2 + 4 * (12 / 6).\n" +
                            "Use the /exit command to quit the program.")
                    else -> println("Unknown command")
                }
            }
            else -> {
                when {
                    input.contains(Regex("[^a-zA-Z0-9+\\-*/()= ]")) -> println("Invalid expression")
                    input.count {it == '('} != input.count {it == ')'} -> println("Invalid expression")
                    input.contains(Regex("\\*{2,}")) -> println("Invalid expression")
                    input.contains(Regex("/{2,}")) -> println("Invalid expression")
                    input.contains(Regex("\\+-")) -> println("Invalid expression")
                    input.contains(Regex("-\\+")) -> println("Invalid expression")
                    input.contains("=") -> calculator.handleAssignment(input)
                    Regex("^ *[+\\-]*[0-9]+ *\$").matches(input) -> println(input.trim().toInt())
                    Regex("^ *[a-zA-Z]+ *\$").matches(input) -> calculator.printVariable(input.trim())
                    else -> calculator.calculateResult(input)
                }
            }
        }
    }
}
