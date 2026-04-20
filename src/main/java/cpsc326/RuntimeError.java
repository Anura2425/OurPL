package cpsc326;

class RuntimeError extends RuntimeException {
    final Token token;
    final String key;
    // wtf
    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
        this.key = null;
    }
    
    RuntimeError(String key, String message){
        super(message);
        this.token = null;
        this.key = key;
    }

}