package com.eai.inspiendev.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeaderXml {

    @JacksonXmlProperty(localName = "USER_ID")
    private String userId;

    @JacksonXmlProperty(localName = "NAME")
    private String name;

    @JacksonXmlProperty(localName = "ADDRESS")
    private String address;

    @JacksonXmlProperty(localName = "STATUS")
    private String status;
}
