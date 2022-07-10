package com.example.second_app

import java.io.Serializable

data class LevelData(
    val id: Int,                    // 레벨 ID
    val tiles: ArrayList<Tile>,     // 타일 분포
    val startpoint: Point,          // 시작점
    val inittemp: Double,           // 시작 온도.
    val items: ArrayList<Item>,     // 아이템 분포
): Serializable

data class Tile(
    val type: String,
    val point: Point,
): Serializable

data class Point(
    val x: Int,
    val y: Int,
): Serializable

/*
data class Item

아이템 클래스:
아이템은 다음과 같은 것들이 있다.
"goal": 먹으면 레벨을 완료한다.
"life": 먹으면 온도를 1도 내린다.
"fire": 먹으면 온도를 2도 올린다.
(추가 예정)

 */
data class Item(
    val type: String,
    val point: Point,
): Serializable {
    fun getImageId(): Int {
        return when (type) {
            "goal" -> R.drawable.board_goal
            "life" -> R.drawable.item_life
            "fire" -> R.drawable.item_fire
            else -> throw Error("Item type $type is not allowed")
        }
    }
}