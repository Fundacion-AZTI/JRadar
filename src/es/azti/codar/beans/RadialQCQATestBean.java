/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  21 de mar. de 2017
 */
package es.azti.codar.beans;

import java.io.Serializable;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es) 21 de mar. de 2017
 *
 *         Bean that manages the quality test of radial files.
 */
public class RadialQCQATestBean implements Serializable {

	private static final long serialVersionUID = 1555239844751347768L;

	// Test labeling velocity vectors beyond a maximum velocity threshold.
	// The maximun will be proposed if possible from the data of radial file
	// header
	// if not, it must be typed by user
	private float veloThreshold = Float.NaN;

	// Test labeling velocity vectors beyond a maximum variance threshold
	// The maximun will be proposed if possible from the data of radial file
	// header
	// if not, it must be typed by user
	private float varianceThreshold = Float.NaN;

	// for each source vector, the median of all velocities within a radius of
	// <RCLim> and whose vector bearing (angle of arrival at site) is also ithin
	// an angular distance fo <AngLim> degrees from the source vector's bearing
	// is evaluated. Of the difference between the vectors velocity and the
	// median velocity is greater than a threshold then the median velocity is
	// used.
	private float medianFilter = Float.NaN;
	private float rcLim = Float.NaN;
	private float angLim = Float.NaN;

	// Average radial bearing
	// Test determining that the average radial bearing lies within a specified
	// margin around the expected value for normal operation. The value fo
	// normal operation has to be defined within a time interval when the proper
	// functioning of the device is assessed. The margin has to be set according
	// site-specific properties.
	// Two values to determine the range, minimun and maximun, always in degrees
	private float avRadialBearingMin = Float.NaN;
	private float avRadialBearingMax = Float.NaN;

	// Test labeling temporal derivative analysis, the vectors are compared with
	// the values of the data from previous our and next hour.
	private float tempThreshold = Float.NaN;

	// Test radial count, a minimun of good data
	private float radialCount = Float.NaN;

	/**
	 * @param veloThreshold
	 * @param varianceThreshold
	 * @param medianFilter
	 * @param avRadialBearing
	 */
	public RadialQCQATestBean(float veloThreshold, float varianceThreshold, float medianFilter,
			float avRadialBearingMin, float avRadialBearingMax, float tempThreshold, float radialCount, float rcLim,
			float angLim) {
		super();
		this.veloThreshold = veloThreshold;
		this.varianceThreshold = varianceThreshold;
		this.medianFilter = medianFilter;
		this.avRadialBearingMin = avRadialBearingMin;
		this.avRadialBearingMax = avRadialBearingMax;
		this.tempThreshold = tempThreshold;
		this.radialCount = radialCount;
		this.rcLim = rcLim;
		this.angLim = angLim;
	}

	/**
	 * @return the tempThreshold
	 */
	public float getTempThreshold() {
		return tempThreshold;
	}

	/**
	 * @param tempThreshold
	 *            the tempThreshold to set
	 */
	public void setTempThreshold(float tempThreshold) {
		this.tempThreshold = tempThreshold;
	}

	/**
	 * @return the radialCount
	 */
	public float getRadialCount() {
		return radialCount;
	}

	/**
	 * @param radialCount
	 *            the radialCount to set
	 */
	public void setRadialCount(float radialCount) {
		this.radialCount = radialCount;
	}

	/**
	 * 
	 */
	public RadialQCQATestBean() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the veloThreshold
	 */
	public float getVeloThreshold() {
		return veloThreshold;
	}

	/**
	 * @param veloThreshold
	 *            the veloThreshold to set
	 */
	public void setVeloThreshold(float veloThreshold) {
		this.veloThreshold = veloThreshold;
	}

	/**
	 * @return the varianceThreshold
	 */
	public float getVarianceThreshold() {
		return varianceThreshold;
	}

	/**
	 * @param varianceThreshold
	 *            the varianceThreshold to set
	 */
	public void setVarianceThreshold(float varianceThreshold) {
		this.varianceThreshold = varianceThreshold;
	}

	/**
	 * @return the medianFilter
	 */
	public float getMedianFilter() {
		return medianFilter;
	}

	/**
	 * @param medianFilter
	 *            the medianFilter to set
	 */
	public void setMedianFilter(float medianFilter) {
		this.medianFilter = medianFilter;
	}

	/**
	 * @return the rcLim radius to check the median filter
	 */
	public float getRcLim() {
		return rcLim;
	}

	/**
	 * @param rcLim
	 *            the rcLim to set radius to check the median filter
	 */
	public void setRcLim(float rcLim) {
		this.rcLim = rcLim;
	}

	/**
	 * @return the angLim bearing angular distance to check median filter
	 */
	public float getAngLim() {
		return angLim;
	}

	/**
	 * @param angLim
	 *            the angLim to set bearing angular distance to check median
	 *            filter
	 */
	public void setAngLim(float angLim) {
		this.angLim = angLim;
	}

	/**
	 * @return the avRadialBearingMin
	 */
	public float getAvRadialBearingMin() {
		return avRadialBearingMin;
	}

	/**
	 * @param avRadialBearingMin
	 *            the avRadialBearingMin to set
	 */
	public void setAvRadialBearingMin(float avRadialBearingMin) {
		this.avRadialBearingMin = avRadialBearingMin;
	}

	/**
	 * @return the avRadialBearingMax
	 */
	public float getAvRadialBearingMax() {
		return avRadialBearingMax;
	}

	/**
	 * @param avRadialBearingMax
	 *            the avRadialBearingMax to set
	 */
	public void setAvRadialBearingMax(float avRadialBearingMax) {
		this.avRadialBearingMax = avRadialBearingMax;
	}

	/**
	 * Check if all the parameters needed to run the test are collected. Take in
	 * to accout that for codar files, the variance is not used but temporal
	 * derivative instead.
	 * 
	 * @return true if all the parameters are collected, or false if something
	 *         is missing.
	 */
	public boolean readyToRunTests() {
		boolean todoOk = false;

		if (!Float.isNaN(this.veloThreshold) &&
		// !Float.isNaN(this.varianceThreshold) &&
				!Float.isNaN(this.medianFilter) && !Float.isNaN(this.rcLim) && !Float.isNaN(this.angLim)
				&& !Float.isNaN(this.avRadialBearingMin) && !Float.isNaN(this.tempThreshold)
				&& !Float.isNaN(this.radialCount) && !Float.isNaN(this.avRadialBearingMax)) {
			todoOk = true;
		}

		return todoOk;
	}

}
