package models;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by User on 20/08/2017.
 */

public enum TrailEnvironment {
    WATERFALL(1), MOUNTAIN(2), CAVE(3);

    @JsonProperty
    private int value;

    TrailEnvironment(int val){
        this.value = val;
    }

    public int getValue(){
        return this.value;
    }

    public String getName(){
        switch (value){
            case 0:
                return "Cachoeira";
            case 1:
                return "Montanha";
            case 2:
                return "Caverna";
        }

        return null;
    }

    public static TrailEnvironment fromValue(int val){
        switch (val){
            case 0:
                return WATERFALL;
            case 1:
                return MOUNTAIN;
            case 2:
                return CAVE;
        }

        return null;
    }
}
