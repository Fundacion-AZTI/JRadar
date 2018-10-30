/**
 *  @author Jose Luis Asensio (jlasensio@azti.es) 
 *  18 de abril de 2017
 */
package es.azti.codar.beans;

import java.io.Serializable;

/**
 * @author Jose Luis Asensio (jlasensio@azti.es)
 *  18 de abril de 2017
 *
 *	Bean to manage the quality tests for Total files.
 */
public class TotalQCQATestBean implements Serializable {

	private static final long serialVersionUID = 1555239844751347768L;


	//Test labeling velocity vectors beyond a maximum velocity threshold.
	//The maximun will be proposed if possible from the data of radial file header
	//if not, it must be typed by user
	private float veloThreshold=Float.NaN;
	
	//Test labeling velocity vectors beyond a maximum variance threshold
	//The maximun will be proposed if possible from the data of radial file header
	//if not, it must be typed by user
	private float varianceThreshold=Float.NaN;
	
	//Test checking if the minimum number of radial velocity is present for the combination into the total velocity vector.
	private float dataDensityThreshold=Float.NaN;

	//Test labeling velocity vectors beyond a maximum GDOP threshold
	private float GDOPThreshold=Float.NaN;
	
	//Test labeling temporal derivative analysis, the vectors are compared with the values of the data from previous our and next hour.
	private float tempThreshold=Float.NaN;

	/**
	 * @param veloThreshold
	 * @param varianceThreshold
	 * @param dataDensityTreshold
	 * @param GDOPThreshold
	 */
	public TotalQCQATestBean(float veloThreshold, float varianceThreshold, float dataDensityThreshold, float GDOPThreshold, float tempThreshold) {
		super();
		this.veloThreshold = veloThreshold;
		this.varianceThreshold = varianceThreshold;
		this.dataDensityThreshold = dataDensityThreshold;
		this.GDOPThreshold = GDOPThreshold;
		this.tempThreshold = tempThreshold;
	}

	/**
	 * 
	 */
	public TotalQCQATestBean() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the tempThreshold
	 */
	public float getTempThreshold() {
		return tempThreshold;
	}
	
	/**
	 * @param tempThreshold the tempThreshold to set
	 */
	public void setTempThreshold(float tempThreshold) {
		this.tempThreshold = tempThreshold;
	}
	
	/**
	 * @return the veloThreshold
	 */
	public float getVeloThreshold() {
		return veloThreshold;
	}

	/**
	 * @param veloThreshold the veloThreshold to set
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
	 * @param varianceThreshold the varianceThreshold to set
	 */
	public void setVarianceThreshold(float varianceThreshold) {
		this.varianceThreshold = varianceThreshold;
	}

	
	/**
	 * Check if all the parameters needed to run the test are collected.
	 * Take in to account that the variance is not used for codar files
	 * but temporal derivative instead.
	 * @return true if all the parameters are collected, or false if something is missing.
	 */
	public boolean readyToRunTests() {
		boolean todoOk = false;
		
		if (!Float.isNaN(this.veloThreshold) &&
//			!Float.isNaN(this.varianceThreshold) &&
			!Float.isNaN(this.dataDensityThreshold) &&
			!Float.isNaN(this.GDOPThreshold) &&
			!Float.isNaN(this.tempThreshold)) {
				todoOk = true;
		} 
		
		return todoOk;
	}

	/**
	 * @return the dataDensityThreshold
	 */
	public float getDataDensityThreshold() {
		return dataDensityThreshold;
	}

	/**
	 * @param dataDensityThreshold the dataDensityThreshold to set
	 */
	public void setDataDensityThreshold(float dataDensityThreshold) {
		this.dataDensityThreshold = dataDensityThreshold;
	}

	/**
	 * @return the gDOPThreshold
	 */
	public float getGDOPThreshold() {
		return GDOPThreshold;
	}

	/**
	 * @param gDOPThreshold the gDOPThreshold to set
	 */
	public void setGDOPThreshold(float gDOPThreshold) {
		GDOPThreshold = gDOPThreshold;
	}

	
}
