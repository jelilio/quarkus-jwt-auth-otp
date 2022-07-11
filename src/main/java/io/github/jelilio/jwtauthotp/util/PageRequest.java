package io.github.jelilio.jwtauthotp.util;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

public class PageRequest {
    @QueryParam("page")
    @DefaultValue("0")
    public int page;
    @QueryParam("size")
    @DefaultValue("20")
    public int size;
}