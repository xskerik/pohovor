package com.pohovor.demo.model;

import lombok.Value;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Value
public class Stat implements Serializable {

    ZonedDateTime at;
    String msg;
}
