package berlin.reiche.securitas.controller;

import android.graphics.Bitmap;
import android.os.Message;
import berlin.reiche.securitas.activies.Action;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.Protocol;

/**
 * Specific controller which is responsible for the client, respectively this
 * Android application.
 * 
 * @author Konrad Reiche
 * 
 */
public class ClientController extends Controller<ClientModel.State> {

	public ClientController(ClientModel model) {
		this.model = model;
		this.setState(new IdleState(this));
	}

	@Override
	void handleMessage(Message msg) {
		state.handleMessage(msg);
	}

	/**
	 * @param motionFilename
	 *            filename of the snapshot that triggered the motion event or
	 *            <code>null</code> if there was no motion event.
	 */
	public void restoreClientState(String motionFilename) {
		int what = Protocol.RESTORE_CLIENT_STATE.code;
		Message message = Message.obtain(inboxHandler, what, motionFilename);
		inboxHandler.sendMessage(message);
	}

	public void downloadMotionSnapshot(String motionFilename) {
		int what = Protocol.DOWNLOAD_MOTION_SNAPSHOT.code;
		Message message = Message.obtain(inboxHandler, what, motionFilename);
		inboxHandler.sendMessage(message);
	}

	public void downloadLatestSnapshot() {
		int what = Protocol.DOWNLOAD_LATEST_SNAPSHOT.code;
		inboxHandler.sendEmptyMessage(what);
	}

	public void registerDevice(String id) {
		int what = Protocol.REGISTER_DEVICE.code;
		Message message = Message.obtain(inboxHandler, what, id);
		inboxHandler.sendMessage(message);
	}

	public void stopDetection() {
		inboxHandler.sendEmptyMessage(Protocol.STOP_DETECTION.code);
	}

	public void startDetection() {
		inboxHandler.sendEmptyMessage(Protocol.START_DETECTION.code);
	}

	public void alertProblem(String message) {
		notifyOutboxHandlers(Action.ALERT_PROBLEM.code, message);
	}

	public void setSnapshot(Bitmap snapshot) {
		notifyOutboxHandlers(Action.SET_SNAPSHOT.code, snapshot);
	}

	public void setRegisteredOnServer(boolean flag) {
		notifyOutboxHandlers(Action.SET_REGISTERED_ON_SERVER.code, flag);
	}

}
