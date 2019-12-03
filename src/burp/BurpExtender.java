package burp;

import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BurpExtender implements burp.IBurpExtender, burp.IHttpListener
{
    private burp.IExtensionHelpers helpers;
    private PrintWriter stdout;
    private PrintWriter stderr;

    private Integer counter = null;
    private Integer incrementInitialNum = null;
    private burp.IResponseInfo responseInfo;

    //
    // implement IBurpExtender
    //
    @Override
    public void registerExtenderCallbacks(burp.IBurpExtenderCallbacks callbacks)
    {
        // obtain an extension helpers object
        helpers = callbacks.getHelpers();
        stdout = new PrintWriter(callbacks.getStdout(), true);
        stderr = new PrintWriter(callbacks.getStderr(),true);

        callbacks.setExtensionName("IncrementItPlease");
        // set our extension name

        // register ourselves as an HTTP listener
        callbacks.registerHttpListener(this);

        // Register Success
        stdout.println("Register: IncrementItPlease");
    }

    //
    // implement IHttpListener
    //
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, burp.IHttpRequestResponse messageInfo)
    {
        boolean updated = false;

        // only process requests
        if (messageIsRequest) {
            // get the HTTP service for the request
            burp.IHttpService httpService = messageInfo.getHttpService();
            burp.IRequestInfo iRequest = helpers.analyzeRequest(messageInfo);

            String request = new String(messageInfo.getRequest());
            List<String> headers = iRequest.getHeaders();

            // get the request body
            String reqBody = request.substring(iRequest.getBodyOffset());

            if(counter == null) {
                incrementInitialNum = getIncrementedInitialNumber(reqBody);
                counter = incrementInitialNum;
            }
            if (counter != null) {
                String replaceTargetString = "IncrementItPlease:" + incrementInitialNum;
                reqBody = reqBody.replaceAll(replaceTargetString, String.valueOf(counter));
                updated = true;
            }

            if (updated) {
                byte[] message = helpers.buildHttpMessage(headers, reqBody.getBytes());
                messageInfo.setRequest(message);
                stdout.println("-----Request After Plugin Update-------");
                stdout.println("");
                stdout.println(helpers.bytesToString(messageInfo.getRequest()));
                stdout.println("");
                stdout.println("-----end output-------");
            }
        } else { // only process response
            responseInfo = helpers.analyzeResponse(messageInfo.getResponse());
            Short status = responseInfo.getStatusCode();
            stdout.println("StatusCode is: " + status.toString());

            if(isResponse2xxOr3xx(status)) counter++;
        }
    }

    private Integer getIncrementedInitialNumber(String requestBody) {
        Pattern p = Pattern.compile("(?<=IncrementItPlease:)[0-9]+");
        Matcher m = p.matcher(requestBody);
        if(m.find()) {
            String matchStr = m.group();
            try {
                stdout.println("exist replaceTarget: " + matchStr);
                return Integer.parseInt(matchStr);
            } catch (NumberFormatException e) {
                stderr.println("Error: " + e);
            }
        }
        stdout.println("not exist replaceTarget");
        return null;
    }

    private Boolean isResponse2xxOr3xx(Short status) {
        return ((200 <= status && status <=299)
                || (300 <= status && status <= 399));
    }

}
