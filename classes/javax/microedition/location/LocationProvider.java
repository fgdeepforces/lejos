package javax.microedition.location;

import java.util.Enumeration;
import java.util.Vector;

/**
 * This is the starting point for applications using this API and represents a source of
 * the location information. A LocationProvider represents a location-providing module,
 * generating Locations.
 * <p>
 * Applications obtain LocationProvider instances (classes implementing the actual
 * functionality by extending this abstract class) by calling the factory method getInstance(). It is
 * the responsibility of the implementation to return the correct LocationProvider-derived
 * object.
 * <p>
 * Rather than passing an actual Criteria object to getInstance(), just use null because leJOS NXJ has no need for criteria arguments.
 *
 * @author BB
 */
public abstract class LocationProvider {
	
	public static final int AVAILABLE = 1;
	public static final int TEMPORARILY_UNAVAILABLE = 2;
	public static final int OUT_OF_SERVICE = 3;
	
	/**
	 * This is where we store the proximity listeners. The elements are 3 element Object [] arrays:-
	 * <ul>
	 * <li>[0] are the ProximityListeners</li>
	 * <li>[1] are the Coordinates</li>
	 * <li>[2] is the proximityRadius as a Float</li>
	 * </ul>
	 */
	private static final Vector listeners = new Vector();
	
	/**
	 * Empty constructor to help implementations and extensions. This is not intended to be used by applications. Applications should not make subclasses of this class and invoke this constructor from the subclass.
	 */
	protected LocationProvider() {}
	
	/**
	 * Returns the current state of this LocationProvider. The return value shall be one of the availability status code constants defined in this class.
	 * @return the availability state of this LocationProvider
	 */
	public abstract int getState();
	
	/**
	 * This is a factory method to retrieve an instance of LocationProvider. With leJOS NXJ 
	 * the LocationProvider is the class ??? i.e. a Bluetooth GPS unit. It looks through all the paired
	 * Bluetooth devices on your NXT brick and tries connecting to any that are identified as
	 * GPS units.
	 * @param criteria
	 * @return
	 * @throws LocationException
	 */
	public static LocationProvider getInstance(Criteria criteria) throws LocationException {
		return null;
	}
	
	/**
	 * Retrieves a Location with the constraints given by the Criteria associated with this class. If no result could be retrieved, a LocationException is thrown. If the location can't be determined within the timeout period specified in the parameter, the method shall throw a LocationException.
	 * <p>
	 * If the provider is temporarily unavailable, the implementation shall wait and try to obtain the location until the timeout expires. If the provider is out of service, then the LocationException is thrown immediately.
	 * <p>
	 * Note that the individual Location returned might not fulfill exactly the criteria used for selecting this LocationProvider. The Criteria is used to select a location provider that typically is able to meet the defined criteria, but not necessarily for every individual location measurement.
   	 * @param timeout a timeout value in seconds. -1 is used to indicate that the implementation shall use its default timeout value for this provider.
	 * @return a Location object
	 * @throws LocationException if the location couldn't be retrieved or if the timeout period expired
	 * @throws java.lang.InterruptedException if the operation is interrupted by calling reset() from another thread
	 */
	public abstract Location getLocation(int timeout) throws LocationException, java.lang.InterruptedException;

	/**
	 * Adds a LocationListener for updates at the defined interval. The listener will be called with updated location at the defined interval. The listener also gets updates when the availablilty state of the LocationProvider changes.
	 * <p>
	 * Passing in -1 as the interval selects the default interval which is dependent on the used location method. Passing in 0 as the interval registers the listener to only receive provider status updates and not location updates at all.
	 * <p>
	 * Only one listener can be registered with each LocationProvider instance. Setting the listener replaces any possibly previously set listener. Setting the listener to null cancels the registration of any previously set listener. 
	 * @param listener 
	 * 		the listener to be registered. If set to null the registration of any previously set listener is cancelled.
	 * @param interval
	 * 		the interval in seconds. -1 is used for the default interval of this provider. 0 is used to indicate that the application wants to receive only provider status updates and not location updates at all.
	 * @param timeout
	 * 		timeout value in seconds, must be greater than 0. if the value is -1, the default timeout for this provider is used. Also, if the interval is -1 to indicate the default, the value of this parameter has no effect and the default timeout for this provider is used. If timeout == -1 and interval > 0 and the default timeout of the provider is greater than the specified interval, then the timeout parameter is handled as if the application had passed the same value as timeout as the interval (i.e. timeout is considered to be equal to the interval).  If the interval is 0, this parameter has no effect.
	 * @param maxAge
	 * 		maximum age of the returned location in seconds, must be greater than 0 or equal to -1 to indicate that the default maximum age for this provider is used. Also, if the interval is -1 to indicate the default, the value of this parameter has no effect and the default maximum age for this provider is used. 
	 */
	public abstract void setLocationListener(LocationListener listener, int interval, int timeout, int maxAge);
	
	/**
	 * Resets the LocationProvider.
	 * <p>
	 * All pending synchronous location requests will be aborted and any blocked getLocation method calls will terminate with InterruptedException.
	 * <p>
	 * Applications can use this method e.g. when exiting to have its threads freed from blocking synchronous operations. 
	 */
	public abstract void reset();
	
	/**
	 * Returns the last known location that the implementation has. This is the best estimate that the implementation has for the previously known location.
	 * <p>
	 * Applications can use this method to obtain the last known location and check the timestamp and other fields to determine if this is recent enough and good enough for the application to use without needing to make a new request for the current location.
	 * @return a location object. null is returned if the implementation doesn't have any previous location information.
	 */
	public static Location getLastKnownLocation() {
		return null;
	}
	
	/**
	 *     Adds a ProximityListener for updates when proximity to the specified coordinates is detected.
	 *     <p>
	 *     If this method is called with a ProximityListener that is already registered, the registration to the specified coordinates is added in addition to the set of coordinates it has been previously registered for. A single listener can handle events for multiple sets of coordinates.
	 *     <p>
	 *     If the current location is known to be within the proximity radius of the specified coordinates, the listener shall be called immediately.
	 *     <p>
	 *     Detecting the proximity to the defined coordinates is done on a best effort basis by the implementation. Due to the limitations of the methods used to implement this, there are no guarantees that the proximity is always detected; especially in situations where the terminal briefly enters the proximity area and exits it shortly afterwards, it is possible that the implementation misses this. It is optional to provide this feature as it may not be reasonably implementable with all methods used to implement this API.
	 *     <p>
	 *     If the implementation is capable of supporting the proximity monitoring and has resources to add the new listener and coordinates to be monitored but the monitoring can't be currently done due to the current state of the method used to implement it, this method shall succeeed and the monitoringStateChanged method of the listener shall be immediately called to notify that the monitoring is not active currently.
	 * @param listener
	 * 		the listener to be registered
	 * @param coordinates
	 * 		the coordinates to be registered
	 * @param proximityRadius
	 * 		the radius in meters that is considered to be the threshold for being in the proximity of the specified coordinates
	 * @throws LocationException
	 * 		if the platform does not have resources to add a new listener and coordinates to be monitored or does not support proximity monitoring at all
	 */
	public static void addProximityListener(ProximityListener listener, Coordinates coordinates, float proximityRadius) throws LocationException {
		
		if ((listener == null) || (coordinates == null))
			throw new NullPointerException();

		if ((proximityRadius <= 0.0))
			throw new IllegalArgumentException();
		
		Float radius = new Float(proximityRadius);
		Object[] listenerArray = new Object[3];
		listenerArray[0] = listener;
		listenerArray[1] = coordinates;
		listenerArray[2] = radius;

		listeners.addElement(listenerArray);

	}
	
	/**
	 * Removes a ProximityListener from the list of recipients for updates. If the specified listener is not registered or if the parameter is null, this method silently returns with no action.
	 * @param listener
	 * 		the listener to remove
	 */
	public static void removeProximityListener(ProximityListener listener) {
		if (listener == null)
			throw new NullPointerException();

		// synchronise because this is temporarily breaking the Enumeration
		synchronized (listeners) {
			// can't use Enumeration because we are removing elements
			Object[] list = new Object[listeners.size()];
			Enumeration en = listeners.elements();
			for (int i = 0; en.hasMoreElements(); i++) {
				list[i] = en.nextElement();
			}
			for (int i = 0; i < list.length; i++) {
				Object[] listenerArray = (Object[]) list[i];
				// remove every registration of this listener
				if (listenerArray[0].equals(listener)) {
					listeners.removeElement(listenerArray);
				}
			}
		}
	}
}
