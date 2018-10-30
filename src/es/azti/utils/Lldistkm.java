/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  8 de oct. de 2018
 */
package es.azti.utils;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 8 de oct. de 2018
 * 
 *         Distances from Haversine formula:
 *         http://en.wikipedia.org/wiki/Haversine_formula and pythagorean
 *         theorem: http://en.wikipedia.org/wiki/Pythagorean_theorem translated
 *         from matlab script by Lorenzo Corgnati lldistkm
 *         https://github.com/LorenzoCorgnati/HFR_Node_tools
 *
 */
public class Lldistkm {

	/**
	 * @param args
	 *            * --Example 1, short distance: latlon1=[-43 172]; latlon2=[-44
	 *            171]; [d1km d2km]=distance(latlon1,latlon2) d1km =
	 *            137.365669065197 (km) d2km = 137.368179013869 (km) *d1km
	 *            approximately equal to d2km
	 *
	 *            --Example 2, longer distance: latlon1=[-43 172]; latlon2=[20
	 *            -108]; [d1km d2km]=distance(latlon1,latlon2) d1km =
	 *            10734.8931427602 (km) d2km = 31303.4535270825 (km) d1km is
	 *            significantly different from d2km (d2km is not able to work
	 *            for longer distances).
	 * 
	 */

	/**
	 * main entry point, just for developing purpouses.
	 * 
	 * @param args
	 *            empty
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// double[] result1 = Lldistkm.calculate(-43, 172, -44, 171);
		// System.out.println("distances: d1km: " + result1[0] + " d2km: " +
		// result1[1]);
		//
		//
		// double[] result2 = Lldistkm.calculate(-43, 172, 20, -108);
		// System.out.println("distances: d1km: " + result2[0] + " d2km: " +
		// result2[1]);
		//

		// Check file data
		double[] result1 = Lldistkm.calculate(43.4288750, -1.7568204, 43.4746747, -1.8532069);
		System.out.println("distances: d1km: " + result1[0] + "  d2km: " + result1[1]);
		// Data in file, X1=3.1522 y1=4.0346 X2=-4.6489 y2= 9.1239
		double x1 = 3.1522;
		double y1 = 4.0346;
		double x2 = -4.6489;
		double y2 = 9.1239;
		System.out.println("X1=3.1522  y1=4.0346 X2=-4.6489   y2= 9.1239::  distance: ="
				+ Math.sqrt(Math.pow(Math.abs(x2 - x1), 2) + Math.pow(Math.abs(y2 - y1), 2)));
	}

	/**
	 * d1km: distance in km based on Haversine formula (Haversine:
	 * http://en.wikipedia.org/wiki/Haversine_formula) d2km: distance in km
	 * based on Pythagoras theorem (see:
	 * http://en.wikipedia.org/wiki/Pythagorean_theorem) After:
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 *
	 * --Inputs: latlon1: latlon of origin point [lat lon] latlon2: latlon of
	 * destination point [lat lon]
	 *
	 * --Outputs: d1km: distance calculated by Haversine formula d2km: distance
	 * calculated based on Pythagoran theorem
	 *
	 *
	 * First version: 15 Jan 2012 Updated: 17 June 2012
	 * 
	 * @param lat1
	 *            latitude for the origin point
	 * @param lon1
	 *            longitude for the origin point
	 * @param lat2
	 *            latitude for the second point
	 * @param lon2
	 *            longitude for the second poing
	 */

	public static double[] calculate(double lat1, double lon1, double lat2, double lon2) {

		int radius = 6371;
		lat1 = (lat1 * Math.PI) / 180;
		lat2 = (lat2 * Math.PI) / 180;
		lon1 = (lon1 * Math.PI) / 180;
		lon2 = (lon2 * Math.PI) / 180;

		double deltaLat = lat2 - lat1;
		double deltaLon = lon2 - lon1;
		double a = Math.pow(Math.sin(deltaLat / 2), 2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d1km = radius * c;

		double x = deltaLon * Math.cos((lat1 + lat2) / 2);
		double y = deltaLat;
		double d2km = radius * Math.sqrt(x * x + y * y);

		return new double[] { d1km, d2km };

	}

}
