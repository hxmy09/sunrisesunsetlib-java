package com.reedell.sunrisesunset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

/**
 * 
 */
public class SunsetCalculator extends SolarEventCalculator {

    public SunsetCalculator(Location location, Integer zenith) {
        super(location, zenith);
    }

    public SunsetCalculator(Location location, Integer zenith, Calendar sunsetDate) {
        super(location, zenith, sunsetDate);
    }

    protected BigDecimal getLongitudeHour() {
        return this.getLongitudeHour(18);
    }

    protected BigDecimal getMeanAnomaly() {
        BigDecimal multiplier = new BigDecimal("0.9856");
        return (multiplier.multiply(getLongitudeHour())).subtract(new BigDecimal("3.289"));
    }

    protected BigDecimal getSunTrueLongitude() {
        BigDecimal meanAnomalyInDegrees = convertDegreesToRadians(getMeanAnomaly());
        BigDecimal sinMeanAnomaly = new BigDecimal(Math.sin(meanAnomalyInDegrees.doubleValue()));
        BigDecimal sinDoubleMeanAnomaly = new BigDecimal(Math.sin(meanAnomalyInDegrees.multiply(BigDecimal.valueOf(2))
                .doubleValue()));

        BigDecimal firstPart = getMeanAnomaly().add(sinMeanAnomaly.multiply(BigDecimal.valueOf(1.916)));
        BigDecimal secondPart = sinDoubleMeanAnomaly.multiply(new BigDecimal("0.020")).add(new BigDecimal("282.634"));
        BigDecimal trueLongitude = firstPart.add(secondPart);

        if (trueLongitude.doubleValue() > 360) {
            trueLongitude = trueLongitude.subtract(BigDecimal.valueOf(360));
        }
        return trueLongitude;
    }

    protected BigDecimal getSunRightAscension() {
        BigDecimal trueLongInRads = this.convertDegreesToRadians(getSunTrueLongitude());
        BigDecimal tanL = new BigDecimal(Math.tan(trueLongInRads.doubleValue()));

        BigDecimal innerParens = this.convertRadiansToDegrees(tanL).multiply(new BigDecimal("0.91764"));
        BigDecimal rightAscension = new BigDecimal(Math.atan(this.convertDegreesToRadians(innerParens).doubleValue()));
        return this.convertRadiansToDegrees(rightAscension).setScale(4, RoundingMode.HALF_EVEN);
    }

    protected BigDecimal setQuadrantOfRightAscension() {
        BigDecimal ninety = BigDecimal.valueOf(90);
        BigDecimal longitudeQuadrant = getSunTrueLongitude().divide(ninety, 4, RoundingMode.FLOOR);
        longitudeQuadrant = longitudeQuadrant.multiply(ninety);

        BigDecimal rightAscensionQuadrant = getSunRightAscension().divide(ninety, 4, RoundingMode.FLOOR);
        rightAscensionQuadrant = rightAscensionQuadrant.multiply(ninety);

        BigDecimal augend = longitudeQuadrant.subtract(rightAscensionQuadrant);
        return (getSunRightAscension().add(augend)).divide(BigDecimal.valueOf(15), 4, RoundingMode.HALF_EVEN);
    }

    protected BigDecimal getRightAscensionInHours() {
        return this.setQuadrantOfRightAscension().divide(BigDecimal.valueOf(15), 4, RoundingMode.HALF_EVEN);
    }

    protected BigDecimal getSinOfSunDeclination() {
        BigDecimal sunTrueLongInRads = convertDegreesToRadians(getSunTrueLongitude());
        BigDecimal sinTrueLongitude = BigDecimal.valueOf(Math.sin(sunTrueLongInRads.doubleValue()));
        BigDecimal sinOfDeclination = sinTrueLongitude.multiply(new BigDecimal("0.39782"));

        return sinOfDeclination.setScale(4, RoundingMode.HALF_EVEN);
    }

    protected BigDecimal getCosineOfSunDeclination() {
        BigDecimal arcSinOfSinDeclination = BigDecimal.valueOf(Math.asin(this.getSinOfSunDeclination().doubleValue()));
        BigDecimal cosDeclination = BigDecimal.valueOf(Math.cos(arcSinOfSinDeclination.doubleValue()));
        return cosDeclination.setScale(4, RoundingMode.HALF_EVEN);
    }

    protected BigDecimal getCosineSunLocalHour() {
        BigDecimal cosineZenith = BigDecimal.valueOf(Math.cos(this.zenith));
        BigDecimal sinLatitude = BigDecimal.valueOf(Math.sin(this.location.getLatitude().doubleValue()));
        BigDecimal cosineLatitude = BigDecimal.valueOf(Math.cos(this.location.getLatitude().doubleValue()));

        BigDecimal sinDeclinationTimesSinLat = this.getSinOfSunDeclination().multiply(sinLatitude);
        BigDecimal top = cosineZenith.subtract(sinDeclinationTimesSinLat);
        BigDecimal bottom = this.getCosineOfSunDeclination().multiply(cosineLatitude);

        BigDecimal cosineLocalHour = top.divide(bottom, 4, RoundingMode.HALF_EVEN);
        return cosineLocalHour;
    }

    protected BigDecimal getSunLocalHour() {
        BigDecimal arcCosineOfCosineHourAngle = getArcCosineFor(this.getCosineSunLocalHour());
        return arcCosineOfCosineHourAngle.divide(BigDecimal.valueOf(15), 4, RoundingMode.HALF_EVEN);
    }
}
