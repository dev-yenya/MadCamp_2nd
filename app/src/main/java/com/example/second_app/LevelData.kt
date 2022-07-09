package com.example.second_app

import java.io.Serializable

data class LevelData(
    val id: Int,
    val tiles: ArrayList<Tile>,
    val startpoint: Point,
    val endpoint: Point,
    val inittemp: Double,
): Serializable

data class Tile(
    val type: String,
    val point: Point,
): Serializable

data class Point(
    val x: Int,
    val y: Int,
): Serializable

data class LevelObject(
    val type: String,
): Serializable