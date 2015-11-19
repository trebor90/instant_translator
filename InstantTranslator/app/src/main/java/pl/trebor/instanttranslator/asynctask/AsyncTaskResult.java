package pl.trebor.instanttranslator.asynctask;

/**
 * Created by trebor on 2/21/2015.
 */
public class AsyncTaskResult<T> {
    private T result = null;
    private Exception exception = null;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
