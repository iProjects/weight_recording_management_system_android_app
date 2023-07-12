package com.nyax.tech.weight_recording;

public class responsedto {

    public responsedto() {
    } // you MUST have an empty constructor

    private String responsesuccessmessage = "";
    private String responseerrormessage = "";
    private String responsemethod;
    private String responseclass;
    private boolean isresponseresultsuccessful;
    private Object responseresultobject;

    public String getresponsesuccessmessage() {
        return responsesuccessmessage;
    }

    public void setresponsesuccessmessage(String _responsesuccessmessage) {
        responsesuccessmessage = _responsesuccessmessage;
    }

    public String getresponseerrormessage() {
        return responseerrormessage;
    }

    public void setresponseerrormessage(String _responseerrormessage) {
        responseerrormessage = _responseerrormessage;
    }

    public String getresponsemethod() {
        return responsemethod;
    }

    public void setresponsemethod(String _responsemethod) {
        responsemethod = _responsemethod;
    }

    public String getresponseclass() {
        return responseclass;
    }

    public void setresponseclass(String _responseclass) {
        responseclass = _responseclass;
    }

    public boolean getisresponseresultsuccessful() {
        return isresponseresultsuccessful;
    }

    public void setisresponseresultsuccessful(boolean _isresponseresultsuccessful) {
        isresponseresultsuccessful = _isresponseresultsuccessful;
    }

    public Object getresponseresultobject() {
        return responseresultobject;
    }

    public void setresponseresultobject(Object _responseresultobject) {
        responseresultobject = _responseresultobject;
    }

    @Override
    public String toString() {
        return getresponsesuccessmessage() + Utils.get_new_line() + getresponseerrormessage();
    }
}
