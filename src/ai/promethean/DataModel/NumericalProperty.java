package ai.promethean.DataModel;

public class NumericalProperty extends Property {
    private Double value;

    public NumericalProperty(String _name, Double _value){
        super(_name);
        setValue(_value);
    }

    public void setValue(Double _value){
        this.value=_value;
    }

    public Double getValue(){
        return this.value;
    }

    @Override
    public String toString(){
        return "Property Name:"+ super.name + ", Property Value: " + this.value;
    }
}
