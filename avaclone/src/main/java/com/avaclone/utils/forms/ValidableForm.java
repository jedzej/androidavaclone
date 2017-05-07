package com.avaclone.utils.forms;

import java.util.Collection;
import java.util.HashMap;


public class ValidableForm extends HashMap<Object,ValidableField> {

    private Collection<ValidableField> fields(){
        return values();
    }

    public void addField(Object key, ValidableField vf){
        put(key, vf);
    }

    public Object getValue(Object key){
        return get(key).getValue();
    }

    public ValidableField getField(Object key){
        return get(key);
    }

    public boolean isValid(){
        for(ValidableField vf : fields()){
            if(!vf.isValid())
                return false;
        }
        return true;
    }
}