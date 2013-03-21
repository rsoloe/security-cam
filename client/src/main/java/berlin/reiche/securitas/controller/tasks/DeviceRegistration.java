package berlin.reiche.securitas.controller.tasks;

import static berlin.reiche.securitas.controller.tasks.DeviceRegistration.Command.REGISTER;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.activies.Action;
import berlin.reiche.securitas.activies.MainActivity;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.util.HttpUtilities;

/**
 * This asynchronous task manages the registration ID which is required to
 * receive GCM messages. The registration ID is send to the server application.
 * 
 * @author Konrad Reiche
 * 
 */
public class DeviceRegistration extends AsyncTask<String, Void, HttpResponse> {

	public enum Command {
		REGISTER, UNREGISTER
	};

	String id;

	Command command;

	ClientModel model;

	Controller<State> controller;

	private static String TAG = MainActivity.class.getSimpleName();

	public DeviceRegistration(Model<State> model, Controller<State> controller,
			String id, Command command) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
		this.id = id;
		this.command = command;
	}

	@Override
	protected HttpResponse doInBackground(String... uri) {

		Log.i(TAG, "Perform " + command + " POST request on endpoint.");
		HttpClient client = HttpUtilities.newHttpClient();
		String[][] data = { { "id", id } };
		HttpPost post = new HttpPost(uri[0]);
		HttpUtilities.setAuthorization(post, Client.getSettings());
		HttpUtilities.setRequestBody(post, data);

		try {
			return client.execute(post);
		} catch (IOException e) {
			Log.i(TAG, command + " failed, due to " + e.getMessage());
			int what = Action.SET_REGISTERED_ON_SERVER.code;
			model.setRegisteredOnServer(false);
			controller.notifyOutboxHandlers(what, false);
		} finally {
			HttpUtilities.closeClient(client);
		}

		return null;
	}

	@Override
	protected void onPostExecute(HttpResponse response) {

		int what;
		if (response == null) {
			Log.e(TAG, "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else {
			switch (response.getStatusLine().getStatusCode()) {
			case SC_OK:
				boolean isRegistered = command == REGISTER;
				model.setRegisteredOnServer(isRegistered);
				if (isRegistered) {
					what = Action.SET_REGISTERED_ON_SERVER.code;
					controller.notifyOutboxHandlers(what, isRegistered);
				}
				break;
			default:
				Log.i(TAG, response.getStatusLine().getReasonPhrase());
			}
		}
	}

}
