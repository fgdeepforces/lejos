package lejos.robotics.objectdetection;

import lejos.robotics.RangeReading;

/**
 * Any class implementing this interface and registering with a FeatureDetector will receieve
 *  notifications when a feature is detected. 
 *  @see lejos.robotics.objectdetection.FeatureDetector#addListener(FeatureListener)
 *
 */
public interface FeatureListener {
	
	/**
	 * The angle and range (in a RangeReading) of a feature is reported when a feature is detected.
	 * @param rr The RangeReading, which contains angle and range.
	 */
	public void featureDetected(RangeReading rr);
	
}