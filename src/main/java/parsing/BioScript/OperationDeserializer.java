package parsing.BioScript;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

import executable.conditionals.Branch;
import executable.conditionals.Loop;
import executable.instructions.Combine;
import executable.instructions.Detect;
import executable.instructions.Dispense;
import executable.instructions.Heat;
import executable.instructions.Instruction;
import executable.instructions.Math;
import executable.instructions.Output;
import executable.instructions.React;
import executable.instructions.Return;
import executable.instructions.Split;
import executable.instructions.Store;
import substance.Property;
import variable.Instance;
import variable.Sensor;

/**
 * Created by jason on 2016/09/29.
 */
public class OperationDeserializer extends Deserializer<Instruction> {

    public static final Logger logger = LogManager.getLogger(OperationDeserializer.class.getSimpleName());

	public Instruction deserialize(JsonElement jsonElement, Type type,
	                               JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

		JsonObject obj = jsonElement.getAsJsonObject().get(OPERATION).getAsJsonObject();

		String classification = obj.get(CLASSIFICATION).getAsString();

		String name = obj.get(SubstanceDeserializer.NAME).getAsString();
		String IDString = (obj.get(SubstanceDeserializer.ID).getAsString());

		Long id = Long.parseLong(IDString);

		Instruction instruction;
		if(classification.toLowerCase().equals("mix")) {
			instruction = new Combine(id, name);
		} else if(classification.toLowerCase().equals("split")) {
			instruction = new Split(id, name);
		} else if(classification.toLowerCase().equals("detect")) {
			instruction = new Detect(id, name);
		} else if(classification.toLowerCase().equals("heat")) {
			instruction = new Heat(id, name);
		} else if(classification.toLowerCase().equals("output")) {
			instruction = new Output(id, name);
		} else if(classification.toLowerCase().equals("store")) {
			instruction = new Store(id, name);
		} else if(classification.toLowerCase().equals("dispense")) {
			instruction = new Dispense(id, name);
        } else if(classification.toLowerCase().equals("react")) {
            instruction = new React(id, name);
        } else if (classification.toLowerCase().equalsIgnoreCase("return")) {
            instruction = new Return(id, name);
        } else if (classification.toLowerCase().equalsIgnoreCase("math")) {
            instruction = new Math(id, name);
        } else if(classification.toLowerCase().equals("cfg_branch")) {
            String evaluation = obj.get(CONDITION).getAsString();
            instruction = new Branch(id, name, evaluation);

            // logger.error(obj.get(TRUE_BRANCH));

            if(obj.has(TRUE_BRANCH)) {
                for (JsonElement elem : obj.get(TRUE_BRANCH).getAsJsonArray()) {
                    ((Branch) instruction).addTrueBranch((Instruction) jsonDeserializationContext.deserialize(elem, Instruction.class));
                }
            }

            if(obj.has(FALSE_BRANCH)) {
                for(JsonElement elem : obj.get(FALSE_BRANCH).getAsJsonArray()) {
                    ((Branch)instruction).addElseBranch((Instruction) jsonDeserializationContext.deserialize(elem, Instruction.class));
                }
            }

            if(obj.has(ELSEIF_BRANCH)) {
                for(JsonElement elem : obj.get(ELSEIF_BRANCH).getAsJsonArray()) {
                    ((Branch)instruction).addElseIfBranch((Instruction) jsonDeserializationContext.deserialize(elem, Instruction.class));
                }
            }
            if(obj.has(NAME) && obj.get(NAME).getAsString().toLowerCase().equals("elseif")) {
                for(JsonElement elem : obj.getAsJsonArray(OPERATIONS))
                    ((Branch)instruction).addTrueBranch((Instruction) jsonDeserializationContext.deserialize(elem,Instruction.class));
            }
        } else if(classification.toLowerCase().equals("cfg_loop")) {
            String evaluation;
            if(obj.has(CONDITION)) {
                evaluation = obj.get(CONDITION).getAsString();
            } else if(obj.has(LOOP_NUM)) {
                StringBuilder number = new StringBuilder();
                number.append(LOOP_NUM + ": ");
                number.append(obj.get(LOOP_NUM).getAsString());
                evaluation = number.toString();
            } else evaluation = "No Expression";
            instruction = new Loop(id, name, evaluation);

            if(obj.has(OPERATIONS)) {
                for (JsonElement elem : obj.getAsJsonArray(OPERATIONS)) {
                    ((Loop) instruction).addTrueBranch((Instruction) jsonDeserializationContext.deserialize(elem, Instruction.class));
                }
            }
        } else if(classification.toLowerCase().equals("variable")) {
            instruction = new Dispense(id, name);
        } else {
            throw new UnsupportedOperationException(String.format("%s not supported: No other instructions have been created", classification.toLowerCase()));
        }

		if(obj.has(INPUTS)) {
			for(JsonElement elem : obj.get(INPUTS).getAsJsonArray()) {
				if (elem.getAsJsonObject().has(INPUT_TYPE)) {

					//if ((elem.getAsJsonObject().get(INPUT_TYPE).getAsString()).equals(VARIABLE)) {
					//	instruction.addInput((Reference) jsonDeserializationContext.deserialize(elem, Reference.class));
					//}
					//else if ((elem.getAsJsonObject().get(INPUT_TYPE).getAsString()).equals(CHEMICAL)) {
					if (elem.getAsJsonObject().has(SENSOR_DECLARATION))
						continue;

					if(!(elem.getAsJsonObject().get(INPUT_TYPE).getAsString()).equals(PROPERTY))
						instruction.addInput((Instance) jsonDeserializationContext.deserialize(elem, Instance.class));
					else {
						Property p = jsonDeserializationContext.deserialize(elem, Property.class);
						if (p != null)
							instruction.addProperty(p);
					}
				}
				else {
					logger.fatal("No Input Type specified by: " + elem.toString());
				}
				//instruction.addInput((Variable) jsonDeserializationContext.deserialize(elem, Variable.class));
			}
		}

		if(obj.has(OUTPUTS)) {
            for (JsonElement elem : obj.get(OUTPUTS).getAsJsonArray()) {
                if (elem.getAsJsonObject().has(SENSOR_DECLARATION)) {
                    instruction.addOutput((Sensor) jsonDeserializationContext.deserialize(elem, Sensor.class));
                }
				else if(!elem.getAsJsonObject().has(PROPERTY)) {
						instruction.addOutput((Instance) jsonDeserializationContext.deserialize(elem, Instance.class));
				}

			}
		}

		return instruction;
	}
}
