
package android.olayinka.file.transfer;

public abstract class AsyncTask<Params, Progress, Result> extends android.os.AsyncTask<Params, Progress, Result> {

    public android.os.AsyncTask<Params, Progress, Result> executeNow(Params... params) {
        if (Utils.hasHoneycomb()) {
            return executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        } else {
            return execute(params);
        }
    }
}