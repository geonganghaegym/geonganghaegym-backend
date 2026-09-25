package com.junghaebom.geonganghaegym.diet.presentation.dto.in;

import com.junghaebom.geonganghaegym.diet.domain.DietType;

public record DietAddCommandAtHome(DietType type, String file, boolean fast, String eatDate) {
}
