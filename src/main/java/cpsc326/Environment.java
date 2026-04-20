package cpsc326;
import java.util.Map;

public class Environment {
    Environment enclosing;
    Map<String, Object> values;

    // default constructor
    Environment(){
        this.enclosing = null;
        this.values = new java.util.HashMap<>();
    }

    // parameterized constructor
    Environment(Environment enclosing){
        this.enclosing = enclosing;
        this.values = new java.util.HashMap<>();
    }

    public void define(String name, Object value){
        // case 1; var name;
        // case 2; var name = value;
        // check if it exists, if so throw error for redefinition, if not create key value pair.
        // Note: allow shadowing (creating a variable of the same name as a var in an exterior environment that does not change the value of the outside one)
        if(values.containsKey(name)){
            throw new RuntimeError(name, "Cannot redefine the same variable within the same environment."); // why does this not exist already am i being silly making a new runtime error?🥺
        } else {
            values.put(name, value);
        }
        return;
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name); // recursion moment :chillz:
        }
        throw new RuntimeError(name, "Undefined var: '" + name.lexeme);
    }

    public void assign(Token name, Object value){
        // case: name = value;
        // check if key value pair exists, if so reassign value of key, if not throw error for trying to assign a non defined var 
        // NOTE: check if it exists in parent environments too
        if (values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null){
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined var: " + name.lexeme);
    }


}
