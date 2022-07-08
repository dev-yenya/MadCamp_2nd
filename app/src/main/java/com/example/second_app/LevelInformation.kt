package com.example.second_app

import java.io.Serializable

// 레벨의 정보를 저장할 클래스.
// 이 클래스는 레벨 스토어에도 사용할 수 있어야 함.
// DB와 연동할 것을 생각하고 구성해보자.

data class LevelInformation(
    val id: Int,
    val levelname: String,
    val boardsize: Int,
    // 더 추가할 것.
): Serializable