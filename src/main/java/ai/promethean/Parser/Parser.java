package ai.promethean.Parser;


import ai.promethean.DataModel.*;
import com.google.gson.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Parser {
    private JsonParser parser = new JsonParser();
    private String json;
    //ArrayLists are mutable objects so we don't need a setter
    private ArrayList<Object> parsedObjects = new ArrayList<Object>();
    private TaskDictionary taskDictionary = new TaskDictionary();
    private StaticOptimizations optimizationList =  new StaticOptimizations();
    private ArrayList<Object> perturbationList =  new ArrayList<Object>();

    public Parser(){
        setJson("");
    }

    public Parser(String _json, Boolean isFile){
        if(!isFile) {
            setJson(_json);
        }
        else{
            try
            {
                String content = new String(Files.readAllBytes(Paths.get(_json)), StandardCharsets.ISO_8859_1);
               setJson(content);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    public void setJson(String _json){
        this.json=_json;
    }

    public ArrayList<Object> parse() {
        JsonElement jsonTree = parser.parse(json);
        JsonArray jsonArray = jsonTree.getAsJsonArray();
        if (jsonTree.isJsonArray()) {
            for(JsonElement j: jsonArray) {
                JsonObject jsonObject = j.getAsJsonObject();
                if (jsonObject.get("title").getAsJsonPrimitive().isString()) {
                    String title = jsonObject.get("title").getAsString();

                    if (title.equalsIgnoreCase("Optimization")) {
                        if (jsonObject.get("name").getAsJsonPrimitive().isString()) {
                            String name = jsonObject.get("name").getAsString();
                            if (jsonObject.get("type").getAsString().toLowerCase().contains("min")) {
                                Optimization o = new Optimization(name, true);
                                optimizationList.addOptimization(o);
                            }
                             else  if (jsonObject.get("type").getAsString().toLowerCase().contains("max")) {
                                  Optimization o = new Optimization(name, false);
                                    optimizationList.addOptimization(o);
                                 }
                            else{
                                throw new IllegalArgumentException("JSON Object Optimization type is invalid (must be a minimum or maximum)");
                            }
                        }
                        else{
                            throw new IllegalArgumentException("JSON Object name is invalid type");
                        }

                    } else if (title.equalsIgnoreCase("State")) {
                        if (jsonObject.get("UID").getAsJsonPrimitive().isNumber()) {
                            int UID = jsonObject.get("UID").getAsInt();
                            if (jsonObject.get("isGoal").getAsJsonPrimitive().isBoolean()) {
                                boolean isGoal = jsonObject.get("isGoal").getAsBoolean();
                                SystemState systemState;
                                systemState = new SystemState(UID, isGoal);

                                if (jsonObject.get("properties") != null) {
                                    JsonArray properties = jsonObject.get("properties").getAsJsonArray();
                                    for (JsonElement elem : properties) {
                                        JsonObject property = elem.getAsJsonObject();
                                        if (property.get("name").getAsJsonPrimitive().isString()){
                                            String name = property.get("name").getAsString();
                                            boolean isDelta;
                                            if (property.get("type") ==null || property.get("type").getAsString().toLowerCase().contains("assign")) {
                                                isDelta=false;
                                            }
                                            else if (property.get("type").getAsString().toLowerCase().contains("delta")) {
                                                isDelta=true;
                                            }
                                            else {
                                                throw new IllegalArgumentException("Invalid property type (must be an assignment or delta)");
                                            }
                                            JsonPrimitive value = property.get("value").getAsJsonPrimitive();
                                            if (value.getAsJsonPrimitive().isBoolean()) {
                                                systemState.addProperty(name, value.getAsBoolean(), isDelta);
                                            } else if (value.getAsJsonPrimitive().isNumber()) {
                                                systemState.addProperty(name, value.getAsDouble(),isDelta);
                                            } else if (value.getAsJsonPrimitive().isString()) {
                                                systemState.addProperty(name, value.getAsString(),isDelta);
                                            } else {
                                                throw new IllegalArgumentException("Invalid property type");
                                            }
                                        }
                                        else {
                                            throw new IllegalArgumentException("Invalid State Property name type");
                                        }
                                    }
                                }
                                parsedObjects.add(systemState);
                            }
                            else{
                                throw new IllegalArgumentException("JSON Object State isGoal is invalid type");
                            }
                        }
                        else{
                            throw new IllegalArgumentException("JSON Object State UID is invalid type");
                        }

                    } else if (title.equalsIgnoreCase("Task")) {
                        if (jsonObject.get("UID").getAsJsonPrimitive().isNumber() &&jsonObject.get("duration").getAsJsonPrimitive().isNumber()){
                            int UID = jsonObject.get("UID").getAsInt();
                            int duration = jsonObject.get("duration").getAsInt();
                            Task task = new Task(UID, duration);

                            if (jsonObject.get("requirements") != null) {
                                JsonArray requirements = jsonObject.get("requirements").getAsJsonArray();
                                for (JsonElement elem : requirements) {
                                    JsonObject requirement = elem.getAsJsonObject();
                                    if (requirement.get("name").getAsJsonPrimitive().isString() && requirement.get("operator").getAsJsonPrimitive().isString() ) {
                                        String name = requirement.get("name").getAsString();
                                        String operator = requirement.get("operator").getAsString();
                                        JsonPrimitive value = requirement.get("value").getAsJsonPrimitive();
                                        if (value.getAsJsonPrimitive().isBoolean()) {
                                            task.addRequirement(name, value.getAsBoolean(), operator);
                                        } else if (value.getAsJsonPrimitive().isNumber()) {
                                            task.addRequirement(name, value.getAsDouble(), operator);
                                        } else if (value.getAsJsonPrimitive().isString()) {
                                            task.addRequirement(name, value.getAsString(), operator);
                                        } else {
                                            throw new IllegalArgumentException("Invalid Task requirement value type");
                                        }
                                    }
                                    else {
                                        throw new IllegalArgumentException("Invalid Task requirement name/operator type");
                                    }

                                }
                            }

                            if (jsonObject.get("property_impacts") != null) {
                                JsonArray properties = jsonObject.get("property_impacts").getAsJsonArray();
                                for (JsonElement elem : properties) {
                                    JsonObject property = elem.getAsJsonObject();
                                    if (property.get("name").getAsJsonPrimitive().isString()) {
                                        String name = property.get("name").getAsString();
                                        boolean isDelta;
                                        if (property.get("type") ==null || property.get("type").getAsString().toLowerCase().contains("assign")) {
                                            isDelta=false;
                                        }
                                        else if (property.get("type").getAsString().toLowerCase().contains("delta")) {
                                            isDelta=true;
                                        }
                                        else {
                                            throw new IllegalArgumentException("Invalid property type (must be an assignment or delta)");
                                        }
                                        JsonPrimitive value = property.get("value").getAsJsonPrimitive();
                                        if (value.getAsJsonPrimitive().isBoolean()) {
                                            task.addProperty(name, value.getAsBoolean(), isDelta);
                                        } else if (value.getAsJsonPrimitive().isNumber()) {
                                            task.addProperty(name, value.getAsDouble(),isDelta);
                                        } else if (value.getAsJsonPrimitive().isString()) {
                                            task.addProperty(name, value.getAsString(),isDelta);
                                        } else {
                                            throw new IllegalArgumentException("Invalid property type");
                                        }
                                    }
                                    else {
                                        throw new IllegalArgumentException("Invalid Task Property name type");
                                    }
                                }
                            }
                            taskDictionary.addTask(task);
                        }
                        else{
                            throw new IllegalArgumentException("JSON Object Task UID/Duration invalid type");
                        }

                    } else if (title.equalsIgnoreCase("Perturbation")) {
                        Perturbation perturbation;
                        if (jsonObject.get("time") == null) {
                            perturbation = new Perturbation();
                        } else if(jsonObject.get("time").getAsJsonPrimitive().isNumber()){
                            int time = jsonObject.get("time").getAsInt();
                            perturbation = new Perturbation(time);
                        }
                        else{
                            throw new IllegalArgumentException("JSON Object Perturbation Time invalid type");
                        }

                        if (jsonObject.get("properties") != null) {
                            JsonArray properties = jsonObject.get("properties").getAsJsonArray();
                            for (JsonElement elem : properties) {
                                JsonObject property = elem.getAsJsonObject();
                                if (property.get("name").getAsJsonPrimitive().isString()) {
                                    String name = property.get("name").getAsString();
                                    boolean isDelta;
                                    if (property.get("type") ==null || property.get("type").getAsString().toLowerCase().contains("assign")) {
                                        isDelta=false;
                                    }
                                    else if (property.get("type").getAsString().toLowerCase().contains("delta")) {
                                        isDelta=true;
                                    }
                                    else {
                                        throw new IllegalArgumentException("Invalid property type (must be an assignment or delta)");
                                    }
                                    JsonPrimitive value = property.get("value").getAsJsonPrimitive();
                                    if (value.getAsJsonPrimitive().isBoolean()) {
                                        perturbation.addProperty(name, value.getAsBoolean(), isDelta);
                                    } else if (value.getAsJsonPrimitive().isNumber()) {
                                        perturbation.addProperty(name, value.getAsDouble(),isDelta);
                                    } else if (value.getAsJsonPrimitive().isString()) {
                                        perturbation.addProperty(name, value.getAsString(),isDelta);
                                    } else {
                                        throw new IllegalArgumentException("Invalid property type");
                                    }
                                }
                                else {
                                    throw new IllegalArgumentException("Invalid Perturbation Property Name type");
                                }
                            }
                        }
                        perturbationList.add(perturbation);

                    } else {
                        throw new IllegalArgumentException("JSON Object title does not exist");
                    }
                }
                else{
                    throw new IllegalArgumentException("JSON Object title is invalid type");
                }

            }

        }
        parsedObjects.add(taskDictionary);
        parsedObjects.add(optimizationList);
        parsedObjects.add(perturbationList);
        return parsedObjects;
    }
}
