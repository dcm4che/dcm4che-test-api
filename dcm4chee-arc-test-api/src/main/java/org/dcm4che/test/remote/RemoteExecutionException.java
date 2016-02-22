package org.dcm4che.test.remote;

/**
 * Thrown on client side to indicate that the execution of the warp'd code produced an exception.
 * The exception stacktrace from the server is encapsulated inside the message.
 *
 * @author rawmahn
 */
public class RemoteExecutionException extends RuntimeException {

    public RemoteExecutionException(String message) {
        super(message);
    }
}
