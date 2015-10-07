package com.github.aenevala.karaf.restds;

import org.omg.CORBA.StringHolder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by nevalaa on 5.10.2015.
 */
@XmlRootElement
public class Person {

    @XmlElement
    private String name;

    private Person() {

    }

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
