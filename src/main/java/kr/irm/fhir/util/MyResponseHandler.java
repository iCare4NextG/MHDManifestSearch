package kr.irm.fhir.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class MyResponseHandler implements ResponseHandler<String> {

    public String handleResponse(final HttpResponse response) throws IOException {

        //Get the status of the response
        int status = response.getStatusLine().getStatusCode();
        if (status == 200) {//Response.Status.OK 처럼 코드화
            HttpEntity entity = response.getEntity();
            if(entity == null) {
                return "";
            } else {
                return EntityUtils.toString(entity, Charset.forName("UTF-8"));
            }

        } else {
            return ""+status;
        }
    }
}
