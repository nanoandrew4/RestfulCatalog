package api;

import java.time.Instant;

public class JSONBuilder {
	/**
	 * Builds a JSON string in the same format Spring builds its return codes.
	 *
	 * @param errorCode    Error code value
	 * @param errorCodeStr Error code name
	 * @param errorMessage Message to be displayed in the body of the error message
	 * @param path         Path where the error happened
	 * @return JSON string containing the specified error, in the same format as Spring uses to create error responses
	 */
	public static String apiErrorBuilder(int errorCode, String errorCodeStr, String errorMessage, String path) {
		return "{\"timestamp\":\"" + Instant.now().toString() + "\",\"status\":" + errorCode +
			   ",\"error\":\"" + errorCodeStr + "\",\"message\":\"" + errorMessage + "\",\"path\":\"" + path + "\"}";
	}
}
