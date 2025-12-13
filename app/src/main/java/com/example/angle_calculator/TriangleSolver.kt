package com.example.angle_calculator

import kotlin.math.*

object TriangleSolver {

    data class Triangle(
        val sideA: Double? = null,
        val sideB: Double? = null,
        val sideC: Double? = null,
        val angleA: Double? = null,
        val angleB: Double? = null,
        val angleC: Double? = null
    ) {
        val isValid: Boolean
            get() {
                val sides = listOfNotNull(sideA, sideB, sideC)
                val angles = listOfNotNull(angleA, angleB, angleC)
                return sides.size + angles.size >= 3 && sides.isNotEmpty()
            }
    }

    class SolverException(message: String) : Exception(message)

    fun solve(input: Triangle): Triangle {
        if (!input.isValid) {
            throw SolverException("情報が不足しています。辺を1つ以上含む、3つ以上の値を入力してください。")
        }

        var a = input.sideA
        var b = input.sideB
        var c = input.sideC
        
        // Convert input angles to radians for calculation, if present
        var angA = input.angleA?.let { Math.toRadians(it) }
        var angB = input.angleB?.let { Math.toRadians(it) }
        var angC = input.angleC?.let { Math.toRadians(it) }

        // Iterative approach to solve until all values are found (simplified for standard cases)
        // In a real general solver, we'd check specific cases (SSS, SAS, etc.)
        
        val maxIterations = 5
        for (i in 0 until maxIterations) {
            val startState = listOf(a, b, c, angA, angB, angC)

            // 1. Angles sum to 180 (PI radians)
            if (angA != null && angB != null && angC == null) angC = PI - angA - angB
            if (angA != null && angC != null && angB == null) angB = PI - angA - angC
            if (angB != null && angC != null && angA == null) angA = PI - angB - angC
            
            // 2. Law of Sines: a/sinA = b/sinB = c/sinC = D
            // Find ratio if possible
            val ratios = mutableListOf<Double>()
            if (a != null && angA != null) ratios.add(a / sin(angA))
            if (b != null && angB != null) ratios.add(b / sin(angB))
            if (c != null && angC != null) ratios.add(c / sin(angC))
            
            val ratio = if (ratios.isNotEmpty()) ratios.average() else null // Use average if multiple available to reduce error

            if (ratio != null) {
                if (a == null && angA != null) a = ratio * sin(angA)
                if (b == null && angB != null) b = ratio * sin(angB)
                if (c == null && angC != null) c = ratio * sin(angC)
                
                if (angA == null && a != null) angA = asin(a / ratio)
                if (angB == null && b != null) angB = asin(b / ratio)
                if (angC == null && c != null) angC = asin(c / ratio)
            }

            // 3. Law of Cosines
            // a^2 = b^2 + c^2 - 2bc cosA
            if (a == null && b != null && c != null && angA != null) a = sqrt(b.pow(2) + c.pow(2) - 2*b*c*cos(angA))
            if (b == null && a != null && c != null && angB != null) b = sqrt(a.pow(2) + c.pow(2) - 2*a*c*cos(angB))
            if (c == null && a != null && b != null && angC != null) c = sqrt(a.pow(2) + b.pow(2) - 2*a*b*cos(angC))

            // Solve angles using Law of Cosines if all sides known
            if (a != null && b != null && c != null) {
                if (angA == null) angA = acos((b.pow(2) + c.pow(2) - a.pow(2)) / (2 * b * c))
                if (angB == null) angB = acos((a.pow(2) + c.pow(2) - b.pow(2)) / (2 * a * c))
                if (angC == null) angC = acos((a.pow(2) + b.pow(2) - c.pow(2)) / (2 * a * b))
            }
            
            val endState = listOf(a, b, c, angA, angB, angC)
            if (startState == endState && a != null && b != null && c != null && angA != null && angB != null && angC != null) {
                break
            }
        }
        
        // Final Validity Check
        if (a == null || b == null || c == null || angA == null || angB == null || angC == null) {
             throw SolverException("計算できませんでした。入力値が有効な三角形を形成するか確認してください。")
        }

        // Convert radians back to degrees and round
        fun toDeg(rad: Double) = Math.toDegrees(rad)
        
        return Triangle(a, b, c, toDeg(angA), toDeg(angB), toDeg(angC))
    }
}
