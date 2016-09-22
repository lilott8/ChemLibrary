package executable.instructions;

import variables.Variable;

import java.util.Map;

/**
 * Created by jason on 2016/09/22.
 */
public class Output extends Instruction {

	public Output(int id, String name) {
		super(id, name, Output.class);
	}

	public Output(int id) {
		super(id, Output.class);
	}

	public Output(String name) {
		super(name, Output.class);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		return sb.toString();
	}

	public void execute() {

	}

	public void execute(Variable... variables) {

	}
}