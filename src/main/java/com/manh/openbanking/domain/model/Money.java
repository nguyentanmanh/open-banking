package com.manh.openbanking.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal value, String currency) {
}
