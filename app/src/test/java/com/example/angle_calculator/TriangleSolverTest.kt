package com.example.angle_calculator

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.abs

class TriangleSolverTest {

    private fun assertClose(expected: Double, actual: Double, delta: Double = 0.1) {
        if (abs(expected - actual) > delta) {
            fail("Expected $expected but got $actual (diff ${abs(expected - actual)})")
        }
    }

    @Test
    fun solve_SSS_3_4_5() {
        val input = TriangleSolver.Triangle(sideA = 3.0, sideB = 4.0, sideC = 5.0)
        val result = TriangleSolver.solve(input)
        
        assertNotNull(result.angleA)
        assertNotNull(result.angleB)
        assertNotNull(result.angleC)
        
        // 3-4-5 triangle has angles approx 36.87, 53.13, 90
        assertClose(36.87, result.angleA!!, 0.1)
        assertClose(53.13, result.angleB!!, 0.1)
        assertClose(90.0, result.angleC!!, 0.1)
    }

    @Test
    fun solve_Equilateral() {
        val input = TriangleSolver.Triangle(sideA = 10.0, sideB = 10.0, sideC = 10.0)
        val result = TriangleSolver.solve(input)
        
        assertClose(60.0, result.angleA!!, 0.1)
        assertClose(60.0, result.angleB!!, 0.1)
        assertClose(60.0, result.angleC!!, 0.1)
    }

    @Test
    fun solve_SAS() {
        // Side A=3, Side B=4, Angle C=90 -> Hypotenuse C should be 5
        val input = TriangleSolver.Triangle(sideA = 3.0, sideB = 4.0, angleC = 90.0)
        val result = TriangleSolver.solve(input)
        
        assertClose(5.0, result.sideC!!, 0.1)
        assertClose(36.87, result.angleA!!, 0.1)
    }

    @Test
    fun solve_ASA() {
        // Angle A=30, Side C=10, Angle B=60 -> Angle C=90. Side B=5rt3 (8.66), Side A=5
        val input = TriangleSolver.Triangle(angleA = 60.0, sideC = 10.0, angleB = 30.0) 
        // Note: Assigned A=60, B=30 so C=90. Side C=10 (hypotenuse).
        // Side A (opposite 60) = 10 * sin(60) = 8.66
        // Side B (opposite 30) = 10 * sin(30) = 5
        
        val result = TriangleSolver.solve(input)
        
        assertClose(90.0, result.angleC!!, 0.1)
        assertClose(8.66, result.sideA!!, 0.1)
        assertClose(5.0, result.sideB!!, 0.1)
    }
    
    @Test(expected = TriangleSolver.SolverException::class)
    fun solve_InsufficientInput() {
        val input = TriangleSolver.Triangle(sideA = 3.0)
        TriangleSolver.solve(input)
    }
}
