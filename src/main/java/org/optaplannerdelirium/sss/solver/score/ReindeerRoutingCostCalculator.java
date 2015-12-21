package org.optaplannerdelirium.sss.solver.score;

import java.math.BigDecimal;

public class ReindeerRoutingCostCalculator {

    public static final long WEIGHT_LONG_FACTOR = 100000000000L;
    public static final double WEIGHT_DOUBLE_FACTOR = 100000000000.0;
    public static final BigDecimal WEIGHT_BIG_DECIMAL_FACTOR = new BigDecimal(WEIGHT_LONG_FACTOR);

    public static final double SOFT_COST_DOUBLE_FACTOR = 100000000.0; // Lower to avoid overflow

    public static long parseWeight(String weightString) {
        BigDecimal weightBigDecimal = new BigDecimal(weightString);
        if (weightBigDecimal.scale() > 11) {
            throw new IllegalArgumentException("The weightString (" + weightString + ") has a scale ("
                    + weightBigDecimal.scale() + ") higher than 11.");
        }
        weightBigDecimal = weightBigDecimal.setScale(11, BigDecimal.ROUND_HALF_UP);
        return weightBigDecimal.multiply(WEIGHT_BIG_DECIMAL_FACTOR).longValueExact();
    }

    public static long multiplyWeightAndDistance(long weightLong, double distanceDouble) {
        // Long arithmetic overflows because maxPowerConsumption (675.4800000000) * maxPowerCost (0.0228608333)
        double weightDouble = ((double) (weightLong)) / WEIGHT_DOUBLE_FACTOR;
        double result = weightDouble * distanceDouble;
        return Math.round(result * SOFT_COST_DOUBLE_FACTOR);
    }

}
