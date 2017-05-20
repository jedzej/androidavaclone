package com.avaclone.utils.forms.validators;

import com.avaclone.utils.forms.ValidableField;
import com.avaclone.utils.forms.ValidationFailedException;

/**
 * Created by jedzej on 11.05.2017.
 */

public class EmailValidator implements ValidableField.Validable<String> {

    @Override
    public void validate(String data) throws ValidationFailedException {
        if (data.isEmpty())
            throw new ValidationFailedException("Field required");
        else if (!data.contains("@"))
            throw new ValidationFailedException("Incorrect email format");
    }
}
