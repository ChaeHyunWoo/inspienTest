package com.eai.inspiendev.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "ORDERS")
public class OrdersXml {

    @JacksonXmlProperty(localName = "HEADER")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HeaderXml> headers;

    @JacksonXmlProperty(localName = "ITEM")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ItemXml> items;
}