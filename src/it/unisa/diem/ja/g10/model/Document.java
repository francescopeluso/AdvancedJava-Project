package it.unisa.diem.ja.g10.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author david
 */
public class Document {
    private String nameDoc;
    private List<String> words;

    public Document(String nameDoc) {
        this.nameDoc = nameDoc;
        words= new ArrayList<>();
    }
    
    
    
}
