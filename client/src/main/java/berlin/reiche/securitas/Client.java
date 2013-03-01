package berlin.reiche.securitas;

import android.content.Context;
import berlin.reiche.securitas.activies.MainActivity;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.tasks.DeviceRegistration;
import berlin.reiche.securitas.tasks.DeviceRegistration.DeviceCommand;
import berlin.reiche.securitas.util.Settings;

/**
 * The model of this application.
 * 
 * @author Konrad Reiche
 * 
 */
public class Client {

	public static String endpoint;

	public static Settings settings;

	public static boolean motionDetectionActive;

	public static MainActivity activity;

	private static ClientModel model;

	private static Controller<ClientModel.State> controller;

	static {
		model = new ClientModel();
		controller = new ClientController(model);
	}

	public static void registerDevice(String id, Context context) {
		String operation = "/device/register";
		String uri = endpoint + operation;
		new DeviceRegistration(id, DeviceCommand.REGISTER, context)
				.execute(uri);
	}

	public static void unregisterDevice(String id, Context context) {
		String operation = "/device/unregister";
		String uri = endpoint + operation;
		new DeviceRegistration(id, DeviceCommand.UNREGISTER, context)
				.execute(uri);
	}

	public static Settings getSettings() {
		return settings;
	}

	public static boolean isMotionDetectionActive() {
		return motionDetectionActive;
	}

	public static void restoreClientState(boolean motionDetectionActive) {
		if (Client.motionDetectionActive != motionDetectionActive) {
			Client.motionDetectionActive = motionDetectionActive;
		}
		activity.updateInterface();
	}

	public static void enableMotionDetection() {
		Client.motionDetectionActive = true;
		activity.updateInterface();
	}

	public static void disableMotionDetection() {
		Client.motionDetectionActive = false;
		activity.updateInterface();
	}

	public static ClientModel getModel() {
		return model;
	}

	public static Controller<ClientModel.State> getController() {
		return controller;
	}

}
