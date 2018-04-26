package tradingmaster.exchange;

public class ExchangeResponse<T> {

    Boolean success;

    String message;

    T result;


    public ExchangeResponse() {
    }

    public ExchangeResponse(T result) {
        this.result = result;
        this.success = true;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
